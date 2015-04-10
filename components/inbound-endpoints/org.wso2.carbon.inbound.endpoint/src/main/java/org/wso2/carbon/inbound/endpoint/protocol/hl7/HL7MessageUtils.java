package org.wso2.carbon.inbound.endpoint.protocol.hl7;

/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundProcessorParams;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class HL7MessageUtils {
    private static final Log log = LogFactory.getLog(HL7MessageUtils.class);

    private static PipeParser parser = new PipeParser();

    public static Message parse(String msg) throws HL7Exception {
        return parser.parse(msg);
    }

    public static MessageContext createSynapseMessageContext(Message message, InboundProcessorParams params,
                                                             SynapseEnvironment synapseEnvironment) {

        MessageContext synCtx = synapseEnvironment.createMessageContext();

        if (params.getProperties().getProperty(HL7Constants.HL7_VALIDATION_PASSED) != null) {
            synCtx.setProperty(HL7Constants.HL7_VALIDATION_PASSED,
                    params.getProperties().getProperty(HL7Constants.HL7_VALIDATION_PASSED));
        }

        try {
            synCtx.setEnvelope(createEnvelope(synCtx, message, params));
        } catch (AxisFault e) {
            log.error("Error while building envelope from HL7 message. " + e.getMessage());
        } catch (HL7Exception e) {
            log.error("Error while building envelope from HL7 message. " + e.getMessage());
        } catch (XMLStreamException e) {
            log.error("Error while building envelope from HL7 message. " + e.getMessage());
        }

        return synCtx;
    }

    private static SOAPEnvelope createEnvelope(MessageContext synCtx, Message message,
                                               InboundProcessorParams params) throws HL7Exception, XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();

        Parser xmlParser = new DefaultXMLParser();
        String xmlDoc = "";
        try {
            xmlDoc = xmlParser.encode(message);
            synCtx.setProperty(HL7Constants.HL7_VALIDATION_PASSED, new Boolean(true));
        } catch (HL7Exception e) {
            synCtx.setProperty(HL7Constants.HL7_VALIDATION_PASSED, new Boolean(false));
            if (params.getProperties().getProperty(HL7Constants.HL7_BUILD_RAW_MESSAGE).equals("true")) {
                xmlDoc =  "<rawMessage>" + StringEscapeUtils.escapeXml(message.encode()) + "</rawMessage>";
            } else {
                log.error("Could not encode HL7 message into XML: " + e.getMessage() + " " +
                        "Set " + HL7Constants.HL7_BUILD_RAW_MESSAGE + " to build invalid HL7 messages containing raw HL7 message.");
            }
        }

        OMElement messageEl = generateHL7MessageElement(xmlDoc);
        envelope.getBody().addChild(messageEl);
        return envelope;
    }

    public static OMElement generateHL7MessageElement(String hl7XmlMessage)
            throws XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        OMElement hl7Element = AXIOMUtil.stringToOM(hl7XmlMessage);
        OMNamespace ns = fac.createOMNamespace(HL7Constants.HL7_NAMESPACE,
                HL7Constants.HL7_ELEMENT_NAME);
        OMElement messageEl = fac.createOMElement(HL7Constants.HL7_MESSAGE_ELEMENT_NAME, ns);
        messageEl.addChild(hl7Element);
        return messageEl;
    }

    /**
     * Get the hl7message from the MessageContext
     * @param ctx
     * @return
     * @throws HL7Exception
     */
    public static Message payloadToHL7Message(MessageContext ctx, InboundProcessorParams params) throws HL7Exception  {

        OMElement hl7MsgEl = (OMElement) ctx.getEnvelope().getBody().getChildrenWithName(new
                QName(HL7Constants.HL7_NAMESPACE, HL7Constants.HL7_MESSAGE_ELEMENT_NAME))
                .next();
        String hl7XMLPayload = hl7MsgEl.getFirstElement().toString();
        String pipeMsg;
        Message msg = null;
        Parser xmlParser = new DefaultXMLParser();
        Parser pipeParser = new PipeParser();
        try {
            msg = xmlParser.parse(hl7XMLPayload);
            pipeMsg = pipeParser.encode(msg);
            msg = pipeParser.parse(pipeMsg);
            return msg;
        } catch (EncodingNotSupportedException e) {
            log.error("Encoding error in the message",e);
            throw new HL7Exception("Encoding error in the message: " +
                    e.getMessage(), e);
        }
        catch (DataTypeException e) {
            // Make this as warning.Since some remote systems require enriched messages that violate some HL7
            //rules it would 	be nice to be able to still send the message.
            log.warn("Rule validation fails."+ e);
            if (!(params.getProperties().getProperty(HL7Constants.HL7_VALIDATE_MESSAGE).equals("false"))) {
                xmlParser.setValidationContext(new NoValidation());
                msg = xmlParser.parse(hl7XMLPayload);
                return msg;
            }

        }
        catch (HL7Exception e) {
            log.error("Error in the Message :" , e);
            throw new HL7Exception("Encoding error in the message: " +
                    e.getMessage(), e);
        }
        return msg;
    }

}
