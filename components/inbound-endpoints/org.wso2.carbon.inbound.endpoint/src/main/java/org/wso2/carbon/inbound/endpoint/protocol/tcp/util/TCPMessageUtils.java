/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.inbound.endpoint.protocol.tcp.util;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.inbound.endpoint.osgi.service.ServiceReferenceHolder;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.context.TCPContext;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.InboundTCPConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Message Transformation between TCPMessageContext & SynapseMessageContext
 */
public class TCPMessageUtils {
    private static final Logger log = Logger.getLogger(TCPMessageUtils.class);

    /**
     * Create synapse message context from TCPMessageContext
     *
     * @param tcpContext TCP Message related parameters per client basis
     * @param params     TCP message processing parameters
     * @return Synapse Message context
     * @throws AxisFault
     */
    public static MessageContext createSynapseMessageContext(TCPContext tcpContext, InboundProcessorParams params)
            throws AxisFault {
        MessageContext synCtx = createSynapseMessageContext(
                params.getProperties().getProperty(InboundTCPConstants.TCP_INBOUND_TENANT_DOMAIN));
        synCtx.setEnvelope(createEnvelope(synCtx, tcpContext.getBaos(), params));
        return synCtx;
    }

    // Create Synapse Message Context
    private static org.apache.synapse.MessageContext createSynapseMessageContext(String tenantDomain) throws AxisFault {
        // Create super tenant message context
        org.apache.axis2.context.MessageContext axis2MsgCtx = createAxis2MessageContext();
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MsgCtx.setServiceContext(svcCtx);
        axis2MsgCtx.setOperationContext(opCtx);

        // If not super tenant, assign tenant configuration context
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            ConfigurationContext tenantConfigCtx =
                    TenantAxisUtils.getTenantConfigurationContext(tenantDomain, axis2MsgCtx.getConfigurationContext());
            axis2MsgCtx.setConfigurationContext(tenantConfigCtx);
            axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, tenantDomain);
        }
        return MessageContextCreatorForAxis2.getSynapseMessageContext(axis2MsgCtx);
    }

    // Create Axis2 Message Context
    private static org.apache.axis2.context.MessageContext createAxis2MessageContext() {

        org.apache.axis2.context.MessageContext axis2MsgCtx = new org.apache.axis2.context.MessageContext();
        axis2MsgCtx.setMessageID(UIDGenerator.generateURNString());
        axis2MsgCtx.setConfigurationContext(
                ServiceReferenceHolder.getInstance().getConfigurationContextService().getServerConfigContext());
        // Axis2 spawns a new threads to send a message if this is TRUE
        axis2MsgCtx.setProperty(org.apache.axis2.context.MessageContext.CLIENT_API_NON_BLOCKING, Boolean.FALSE);
        axis2MsgCtx.setServerSide(true);
        return axis2MsgCtx;
    }

    //creating soap envelop and set message body.
    private static SOAPEnvelope createEnvelope(MessageContext synCtx, ByteArrayOutputStream baos,
                                               InboundProcessorParams params) throws AxisFault {

        String contentType = params.getProperties().getProperty(InboundTCPConstants.TCP_MSG_CONTENT_TYPE);
        if (log.isDebugEnabled()) {
            log.debug("Starting TCP Message Building, message content type : " + contentType);
        }
        org.apache.axis2.context.MessageContext axis2MsgCtx =
                ((org.apache.synapse.core.axis2.Axis2MessageContext) synCtx).getAxis2MessageContext();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SOAPEnvelope envelope = null;
        try {
            envelope = TransportUtils.createSOAPMessage(axis2MsgCtx, bais, contentType);
        } catch (XMLStreamException xmlStreamException) {
            log.error(xmlStreamException);
        }
        return envelope;
    }

    public static int getInt(String key, InboundProcessorParams params) throws NumberFormatException {
        return Integer.valueOf(params.getProperties().getProperty(key));
    }

    /**
     * Convert synapse response message to byte array
     *
     * @param messageContext synapse msg context
     * @param params         Inbound End point parameters
     * @return response message in bytes
     */
    public static byte[] payloadToTCPMessage(MessageContext messageContext, InboundProcessorParams params) {
        String contentType = params.getProperties().getProperty(InboundTCPConstants.TCP_MSG_CONTENT_TYPE);
        org.apache.axis2.context.MessageContext axis2MsgCtx =
                ((org.apache.synapse.core.axis2.Axis2MessageContext) messageContext).getAxis2MessageContext();
        MessageFormatter formatter = BaseUtils.getMessageFormatter(axis2MsgCtx);
        OMOutputFormat format = BaseUtils.getOMOutputFormat(axis2MsgCtx);
        format.setContentType(contentType);
        format.setDoOptimize(false);
        format.setDoingSWA(false);

        byte[] message_bytes;
        try {
            message_bytes = formatter.getBytes(axis2MsgCtx, format);
        } catch (AxisFault axisFault) {
            message_bytes = new byte[0];
            log.error("Could not get message bytes using the formatter ", axisFault);
        }
        return message_bytes;
    }
}
