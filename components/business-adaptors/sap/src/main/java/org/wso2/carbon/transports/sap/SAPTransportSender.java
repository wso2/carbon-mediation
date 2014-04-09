/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transports.sap;

import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.IDocFactory;
import com.sap.conn.idoc.IDocRepository;
import com.sap.conn.idoc.jco.JCoIDoc;
import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.Environment;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.util.MessageContextBuilder;
import org.wso2.carbon.transports.sap.bapi.util.RFCConstants;
import org.wso2.carbon.transports.sap.bapi.util.RFCMetaDataParser;
import org.wso2.carbon.transports.sap.idoc.DefaultIDocXMLMapper;
import org.wso2.carbon.transports.sap.idoc.IDocXMLMapper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <code>SAPTransportSender </code> provides the ransport Sender implementation for SAP endpoints
 *
 */
public class SAPTransportSender extends AbstractTransportSender {

    private Map<String, IDocXMLMapper> xmlMappers = new HashMap<String, IDocXMLMapper>();

    private IDocXMLMapper defaultMapper = new DefaultIDocXMLMapper();

    /**
     * The SAP endpoint error code
     */
    public static final String ERROR_CODE = "ERROR_CODE";

    /**
     * Error while sending the Message through wire
     */
    public static final int SAP_TRANSPORT_ERROR = 8000;

    /**
     * SAP destination error. Possibly something wrong with the remote R/* system
     */
    public static final int SAP_DESTINATION_ERROR = 8001;

    @Override
    public void init(ConfigurationContext cfgCtx, TransportOutDescription trpOut) throws AxisFault {
        super.init(cfgCtx, trpOut);

        CarbonDestinationDataProvider provider = new CarbonDestinationDataProvider();
        if (!Environment.isServerDataProviderRegistered()) {
            Environment.registerServerDataProvider(provider);
        }
        if (!Environment.isDestinationDataProviderRegistered()) {
            Environment.registerDestinationDataProvider(provider);
        }
        //check and initalize if XML mappers are declared
        Parameter xmlMappersParam = trpOut.getParameter(SAPConstants.CUSTOM_IDOC_XML_MAPPERS);
        if (xmlMappersParam != null) {
            OMElement mappersElt = xmlMappersParam.getParameterElement().getFirstElement();
            Iterator mappers = mappersElt.getChildrenWithName(new QName(SAPConstants.XML_MAPPER_ELT));
            try {
                while (mappers.hasNext()) {
                    OMElement m = (OMElement) mappers.next();
                    String key = m.getAttributeValue(new QName(SAPConstants.XML_MAPPER_KEY_ATTR));
                    String value = m.getText().trim();
                    Class clazz = this.getClass().getClassLoader().loadClass(value);
                    IDocXMLMapper mapper = (IDocXMLMapper) clazz.newInstance();

                    xmlMappers.put(key, mapper);
                }
            } catch (Exception e) {
                throw new AxisFault("Error while initializing the SAP transport sender", e);
            }
        }
    }

    /**
     * Send the SAP message to the SAP R/* system, Accepted URL format: idoc:/MyServer[?version=2]
     * @param messageContext axis2 message context
     * @param targetEPR SAP EPR
     * @param outTransportInfo out transport info
     * @throws AxisFault throws in case of an error
     */
    public void sendMessage(MessageContext messageContext, String targetEPR,
                            OutTransportInfo outTransportInfo) throws AxisFault {

        if (targetEPR == null) {
            throw new AxisFault("Cannot send an IDoc without a target SAP EPR");
        }

        try {
            URI uri = new URI(targetEPR);
            String destName = uri.getPath().substring(1);
            JCoDestination destination = JCoDestinationManager.getDestination(destName);

            if (uri.getScheme().equals(SAPConstants.SAP_IDOC_PROTOCOL_NAME)) {
                IDocRepository iDocRepository = JCoIDoc.getIDocRepository(destination);
                String tid = destination.createTID();
                IDocDocumentList iDocList = getIDocs(messageContext, iDocRepository);
                JCoIDoc.send(iDocList, getIDocVersion(uri), destination, tid);
                destination.confirmTID(tid);
            } else if (uri.getScheme().equals(SAPConstants.SAP_BAPI_PROTOCOL_NAME)) {
                try {
                    OMElement payLoad,body;
                    body = messageContext.getEnvelope().getBody();
                    payLoad = body.getFirstChildWithName(new QName(RFCConstants.BAPIRFC));
                    log.info("Received RFC/Meta DATA: " + payLoad);
                    String rfcFunctionName = RFCMetaDataParser.getBAPIRFCFucntionName(payLoad);
                    log.info("Looking up the BAPI/RFC function: " + rfcFunctionName + ". In the " +
                             "meta data repository");

                    JCoFunction function = getRFCfunction(destination, rfcFunctionName);
                    RFCMetaDataParser.processMetaDataDocument(payLoad, function);
                    String responseXML = evaluateRFCfunction(function, destination);
                    processResponse(messageContext, responseXML);
                } catch (Exception e) {
                    sendFault(messageContext, e , SAP_TRANSPORT_ERROR);
                }

            } else {
                handleException("Invalid protocol name : " + uri.getScheme() + " in SAP URL");
            }
        } catch (Exception e) {
            sendFault(messageContext,e, SAP_DESTINATION_ERROR);
            handleException("Error while sending an IDoc to the EPR : " + targetEPR, e);
        }
    }

    private char getIDocVersion(URI uri) {
        String query = uri.getQuery();
        if (query != null && query.startsWith(SAPConstants.SAP_IDOC_VERSION)) {
            String version = query.substring(query.indexOf('=') + 1);
            if (SAPConstants.SAP_IDOC_VERSION_2.equals(version)) {
                return IDocFactory.IDOC_VERSION_2;
            } else if (SAPConstants.SAP_IDOC_VERSION_3.equals(version)) {
                return IDocFactory.IDOC_VERSION_3;
            }
        }
        return IDocFactory.IDOC_VERSION_DEFAULT;
    }

    /**
     * retrive IDOCs from message context
     *
     * @param msgContext Synapse Message Context
     * @param repo       the repository to be used for querying the needed IDoc meta data information in
     *                   order to create the corresponding IDocDocumentList instance
     * @return A list of IDOcs
     * @throws Exception in case of an error
     */
    private IDocDocumentList getIDocs(MessageContext msgContext,
                                      IDocRepository repo) throws Exception {

        Object mapper = msgContext.getOptions().getProperty(SAPConstants.CLIENT_XML_MAPPER_KEY);
        //check for any user defined xml mappers
        if (mapper != null && xmlMappers.containsKey(mapper.toString())) {
            return xmlMappers.get(mapper.toString()).getDocumentList(repo, msgContext);
        } else {
            return defaultMapper.getDocumentList(repo, msgContext);
        }
    }

    /**
     * Evaluate the BAPI/RFC function in a remote R/* system
     * @param function the BAPI/RFC function
     * @param destination jco destination
     * @return the result of the function execution
     * @throws AxisFault throws in case of an error
     */
    private String evaluateRFCfunction(JCoFunction function, JCoDestination destination)
            throws AxisFault {
        log.info("Invoking the RFC function :" + function.getName());
        try {
            function.execute(destination);
        } catch (JCoException e) {
            throw new AxisFault("Cloud not execute the RFC function: " + function, e);
        }

        JCoStructure returnStructure = null;
        try {
            returnStructure = function.getExportParameterList().getStructure("RETURN");
        } catch (Exception ignore) {

        }
        // there seems to be some error that we need to report: TODO ?
        if (returnStructure != null && (!(returnStructure.getString("TYPE").equals("")
                                          || returnStructure.getString("TYPE").equals("S")))) {
            throw new AxisFault(returnStructure.getString("MESSAGE"));
        }

        return function.toXML();
    }

    /**
     * Returns the BAPI/RFC function from the SAP repository
     * @param destination SAP JCO destination
     * @param rfcName the rfc name
     * @return the BAPI/RFC function
     * @throws AxisFault throws in case of an error
     */
    private JCoFunction getRFCfunction(JCoDestination destination, String rfcName)
            throws AxisFault {
        log.info("Retriving the BAPI/RFC function : " + rfcName + " from the destination : " +
                 destination);
        JCoFunction function = null;
        try {
            function = destination.getRepository().getFunction(rfcName);
        } catch (JCoException e) {
            throw new AxisFault("RFC function " + rfcName + " could not found in SAP system", e);
        }
        return function;
    }


    /**
     * Process and send the response of the RFC execution through axis engine
     * @param msgContext axis2 message context
     * @param payLoad RFC execution payload
     * @throws AxisFault throws in case of an error
     */
    private void processResponse(MessageContext msgContext, String payLoad)
            throws AxisFault {
        if (!(msgContext.getAxisOperation() instanceof OutInAxisOperation)) {
            return;
        }
        try {
            MessageContext responseMessageContext = createResponseMessageContext(msgContext);
            ByteArrayInputStream bais = new ByteArrayInputStream(payLoad.getBytes());
            SOAPEnvelope envelope = TransportUtils.createSOAPMessage(msgContext, bais,
                                                                     SAPConstants.SAP_CONTENT_TYPE);
            responseMessageContext.setEnvelope(envelope);
            AxisEngine.receive(responseMessageContext);
            log.info("Sending response out..");
        } catch (XMLStreamException e) {
            throw new AxisFault("Error while processing response", e);
        }
    }

    /**
     * Send an axis fault if an error happened
     * @param msgContext axis2 message context
     * @param e the exception
     * @param errorCode error code of the error
     */
    private void sendFault(MessageContext msgContext, Exception e , int errorCode) {
        //TODO Fix this properly
        try {
                MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(
                        msgContext, e);
                faultContext.setProperty(ERROR_CODE,errorCode);
                faultContext.setProperty("ERROR_MESSAGE",e.getMessage());
                faultContext.setProperty("SENDING_FAULT", Boolean.TRUE);
                msgContext.getAxisOperation().getMessageReceiver().receive(faultContext);
            } catch (AxisFault axisFault) {
                log.fatal("Cloud not create the fault message.", axisFault);
            }
    }
}
