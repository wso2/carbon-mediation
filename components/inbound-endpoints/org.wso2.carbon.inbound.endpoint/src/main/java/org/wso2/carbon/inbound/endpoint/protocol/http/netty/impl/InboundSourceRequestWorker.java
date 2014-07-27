/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.http.netty.impl;


import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.transport.nhttp.util.NhttpUtil;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundHttpConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

/**
 * working class for queue.poll msgs from queue and send it to the medition engine by creating Syanpse msg context.
 */
public class InboundSourceRequestWorker implements Runnable {

    private static final Log log = LogFactory.getLog(InboundSourceRequestWorker.class);

    private BlockingQueue<InboundSourceRequest> eventQueue;

    public InboundSourceRequestWorker(BlockingQueue<InboundSourceRequest> eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void run() {
        InboundSourceRequest inboundSourceRequest = eventQueue.poll();
        if (inboundSourceRequest != null) {
            try {
                org.apache.synapse.MessageContext msgCtx = createMessageContext(inboundSourceRequest);

                byte[] bytes = inboundSourceRequest.getContentBytes();
                String contentType = inboundSourceRequest.getHttpheaders().get(HTTP.CONTENT_TYPE);
                int soapVersion;
                if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -InboundHttpConstants.SOAP_11) {
                    soapVersion = InboundHttpConstants.SOAP_12;
                } else if (contentType
                        .indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -InboundHttpConstants.SOAP_11) {
                    soapVersion = InboundHttpConstants.SOAP_11;
                } else {
                    soapVersion = InboundHttpConstants.SOAP_11;
                }
                SOAPEnvelope soapEnvelope = toSOAPENV(new ByteArrayInputStream(bytes), soapVersion);// building message need to check whether msg should build or not
                msgCtx.setEnvelope(soapEnvelope);
                msgCtx.setProperty(SynapseConstants.IS_INBOUND, "true");
                msgCtx.setProperty(SynapseConstants.CHANNEL_HANDLER_CONTEXT, inboundSourceRequest.getChannelHandlerContext());
                msgCtx.setProperty(SynapseConstants.OUT_SEQUENCE, inboundSourceRequest.getOutSeq());
                msgCtx.setWSAAction(inboundSourceRequest.getHttpheaders().get(InboundHttpConstants.SOAP_ACTION));
                if (inboundSourceRequest.getInjectSeq() == null || inboundSourceRequest.getInjectSeq().equals("")) {
                    log.error("Sequence name not specified. Sequence : " + inboundSourceRequest.getInjectSeq());
                }
                SequenceMediator seq = (SequenceMediator) inboundSourceRequest.getSynapseEnvironment().getSynapseConfiguration().getSequence(inboundSourceRequest.getInjectSeq());
                seq.setErrorHandler(inboundSourceRequest.getFaultSeq());
                if (seq != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("injecting message to sequence : " + inboundSourceRequest.getInjectSeq());
                    }
                    inboundSourceRequest.getSynapseEnvironment().injectAsync(msgCtx, seq);
                } else {
                    log.error("Sequence: " + inboundSourceRequest.getInjectSeq() + " not found");
                }
            } catch (XMLStreamException e) {
                log.error(e.getMessage());
            } catch (AxisFault axisFault) {
                log.error(axisFault.getMessage());
            }


        }
    }

    private org.apache.synapse.MessageContext createMessageContext(InboundSourceRequest inboundSourceRequest) {
        org.apache.synapse.MessageContext msgCtx = inboundSourceRequest.getSynapseEnvironment().createMessageContext();

        MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx).getAxis2MessageContext();

        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());

        String oriUri = inboundSourceRequest.getTo();
        String restUrlPostfix = NhttpUtil.getRestUrlPostfix(oriUri, axis2MsgCtx.getConfigurationContext().getServicePath());
        msgCtx.setTo(new EndpointReference(oriUri));

        //  ((Axis2MessageContext) msgCtx).setAxis2MessageContext(axis2MsgCtx);

        // There is a discrepency in what I thought, Axis2 spawns a nes threads to
        // send a message is this is TRUE - and I want it to be the other way
        msgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, false);
        return msgCtx;
    }

    private SOAPEnvelope toSOAPENV(InputStream inputStream, int version) throws XMLStreamException {


        try {
            XMLStreamReader reader =
                    XMLInputFactory.newInstance().createXMLStreamReader(inputStream);

            SOAPFactory f = null;
            if (version == InboundHttpConstants.SOAP_11) {
                f = new SOAP11Factory();
            } else if (version == InboundHttpConstants.SOAP_12) {
                f = new SOAP12Factory();
            }


            StAXSOAPModelBuilder builder =

                    OMXMLBuilderFactory.createStAXSOAPModelBuilder(f, reader);
            SOAPEnvelope soapEnvelope = builder.getSOAPEnvelope();


            return soapEnvelope;

        } catch (XMLStreamException e) {
            log.error("Error creating a OMElement from an input stream : ",
                    e);
            throw new XMLStreamException(e);
        }
    }

}
