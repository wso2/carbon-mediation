/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.jms;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.format.DataSourceMessageBuilder;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.vfs.VFSConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * 
 * JMSInjectHandler use to mediate the received JMS message
 * 
 */
public class JMSInjectHandler {

    private static final Log log = LogFactory.getLog(JMSInjectHandler.class);

    private String injectingSeq;
    private String onErrorSeq;
    private boolean sequential;
    private SynapseEnvironment synapseEnvironment;
    private Properties jmsProperties;

    public JMSInjectHandler(String injectingSeq, String onErrorSeq, boolean sequential,
            SynapseEnvironment synapseEnvironment, Properties jmsProperties) {
        this.injectingSeq = injectingSeq;
        this.onErrorSeq = onErrorSeq;
        this.sequential = sequential;
        this.synapseEnvironment = synapseEnvironment;
        this.jmsProperties = jmsProperties;
    }

    public boolean invoke(Object object) throws SynapseException{

        Message msg = (Message) object;
        try {
            org.apache.synapse.MessageContext msgCtx = createMessageContext();
            String contentType = msg.getJMSType();
            if(contentType == null || contentType.trim().equals("")){
                contentType = jmsProperties.getProperty(JMSConstants.CONTENT_TYPE);
            }
            if (log.isDebugEnabled()) {
                log.debug("Processed JMS Message of Content-type : " + contentType);
            }
            MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx)
                    .getAxis2MessageContext();
            // Determine the message builder to use
            Builder builder;
            if (contentType == null) {
                log.debug("No content type specified. Using SOAP builder.");
                builder = new SOAPBuilder();
            } else {
                int index = contentType.indexOf(';');
                String type = index > 0 ? contentType.substring(0, index) : contentType;
                builder = BuilderUtil.getBuilderFromSelector(type, axis2MsgCtx);
                if (builder == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("No message builder found for type '" + type
                                + "'. Falling back to SOAP.");
                    }
                    builder = new SOAPBuilder();
                }
            }
            OMElement documentElement = null;
            // set the message payload to the message context
            if (msg instanceof TextMessage) {
                String message = ((TextMessage) msg).getText();
                InputStream in = new AutoCloseInputStream(new ByteArrayInputStream(
                        message.getBytes()));
                documentElement = builder.processDocument(in, contentType, axis2MsgCtx);
            } else if (msg instanceof BytesMessage) {
                if (builder instanceof DataSourceMessageBuilder) {
                    documentElement = ((DataSourceMessageBuilder) builder).processDocument(
                            new BytesMessageDataSource((BytesMessage) msg), contentType,
                            axis2MsgCtx);
                } else {
                    documentElement = builder.processDocument(new BytesMessageInputStream(
                            (BytesMessage) msg), contentType, axis2MsgCtx);
                }
            } else if (msg instanceof MapMessage) {
                documentElement = convertJMSMapToXML((MapMessage) msg);
            }

            // Setting JMSXDeliveryCount header on the message context
            try {
                int deliveryCount = msg.getIntProperty("JMSXDeliveryCount");
                axis2MsgCtx.setProperty(JMSConstants.DELIVERY_COUNT, deliveryCount);
            } catch (NumberFormatException nfe) {
                if (log.isDebugEnabled()) {
                    log.debug("JMSXDeliveryCount is not set in the received message");
                }
            }

            // Inject the message to the sequence.
            msgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
            if (injectingSeq == null || injectingSeq.equals("")) {
                log.error("Sequence name not specified. Sequence : " + injectingSeq);
                return false;
            }
            SequenceMediator seq = (SequenceMediator) synapseEnvironment.getSynapseConfiguration()
                    .getSequence(injectingSeq);           
            if (seq != null) {
                if (log.isDebugEnabled()) {
                    log.debug("injecting message to sequence : " + injectingSeq);
                }
                seq.setErrorHandler(onErrorSeq);
                if (!synapseEnvironment.injectInbound(msgCtx, seq, sequential)) {
                    return false;
                }
            } else {
                log.error("Sequence: " + injectingSeq + " not found");
            }

            Object o = msgCtx.getProperty(JMSConstants.SET_ROLLBACK_ONLY);
            if (o != null) {
                if ((o instanceof Boolean && ((Boolean) o))
                        || (o instanceof String && Boolean.valueOf((String) o))) {
                    return false;
                }
            }
        } catch (SynapseException se) {
            throw se;            
        } catch (Exception e) {
            log.error("Error while processing the JMS Message");
        }
        return true;
    }

    /**
     * 
     * @param message
     *            JMSMap message
     * @return XML representation of JMS Map message
     */
    public static OMElement convertJMSMapToXML(MapMessage message) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace jmsMapNS = OMAbstractFactory.getOMFactory().createOMNamespace(
                JMSConstants.JMS_MAP_NS, "");
        OMElement jmsMap = fac.createOMElement(JMSConstants.JMS_MAP_ELEMENT_NAME, jmsMapNS);
        try {
            Enumeration names = message.getMapNames();
            while (names.hasMoreElements()) {
                String nextName = names.nextElement().toString();
                String nextVal = message.getString(nextName);
                OMElement next = fac.createOMElement(nextName.replace(" ", ""), jmsMapNS);
                next.setText(nextVal);
                jmsMap.addChild(next);
            }
        } catch (JMSException e) {
            log.error("Error while processing the JMS Map Message. " + e.getMessage());
        }
        return jmsMap;
    }

    /**
     * Create the initial message context for the file
     * */
    private org.apache.synapse.MessageContext createMessageContext() {
        org.apache.synapse.MessageContext msgCtx = synapseEnvironment.createMessageContext();
        MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx)
                .getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, carbonContext.getTenantDomain());
        // There is a discrepency in what I thought, Axis2 spawns a nes threads
        // to
        // send a message is this is TRUE - and I want it to be the other way
        msgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, true);
        return msgCtx;
    }

}
