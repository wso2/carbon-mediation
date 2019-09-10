/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint.protocol.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.inbound.endpoint.protocol.grpc.util.Event;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.wso2.carbon.inbound.endpoint.protocol.grpc.InboundGrpcConstants.HEADER_MAP_SEQUENCE_PARAMETER_NAME;

public class GrpcInjectHandler {
    private static final Log log = LogFactory.getLog(GrpcInjectHandler.class);

    private String injectingSeq;
    private String onErrorSeq;
    private boolean sequential;
    private SynapseEnvironment synapseEnvironment;

    GrpcInjectHandler(String injectingSeq, String onErrorSeq, boolean sequential,
                      SynapseEnvironment synapseEnvironment) {
        this.injectingSeq = injectingSeq;
        this.onErrorSeq = onErrorSeq;
        this.sequential = sequential;
        this.synapseEnvironment = synapseEnvironment;
    }

    public void invokeProcess(Event receivedEvent, StreamObserver<Event> responseObserver) {
        try {
            org.apache.synapse.MessageContext msgCtx = createMessageContext();
            if (msgCtx != null) {
                msgCtx.setProperty(InboundGrpcConstants.GRPC_RESPONSE_OBSERVER, responseObserver);
            }
            initiateSequenceAndInjectPayload(responseObserver, receivedEvent, msgCtx);
        } catch (SynapseException se) {
            throw se;
        } catch (Exception e) {
            log.error("Error while processing the Grpc Message", e);
            throw new SynapseException("Error while processing the JMS Message", e);
        }
    }

    public void invokeConsume(Event receivedEvent, StreamObserver<Empty> responseObserver) {
        try {
            initiateSequenceAndInjectPayload(responseObserver, receivedEvent, createMessageContext());
        } catch (SynapseException se) {
            throw se;
        } catch (Exception e) {
            log.error("Error while consuming the Grpc Message", e);
            throw new SynapseException("Error while consuming the JMS Message", e);
        }
    }

    private org.apache.synapse.MessageContext initiateSequenceAndInjectPayload(StreamObserver responseObserver,
                                                                               Event receivedEvent,
                                                                               org.apache.synapse.MessageContext msgCtx)
            throws AxisFault {
        String msgPayload = receivedEvent.getPayload();
        String sequenceName = receivedEvent.getHeadersMap().get(HEADER_MAP_SEQUENCE_PARAMETER_NAME);
        SequenceMediator seq;
        if (sequenceName != null) {
            seq = (SequenceMediator) synapseEnvironment.getSynapseConfiguration().getSequence(sequenceName);
        } else {
            if (injectingSeq == null || injectingSeq.isEmpty()) {
                log.error("Sequence name not specified. Sequence : " + injectingSeq);
                return null;
            }
            seq = (SequenceMediator) synapseEnvironment.getSynapseConfiguration().getSequence(injectingSeq);
        }
        msgCtx.setProperty(SynapseConstants.IS_INBOUND, true);
        //validating the sequence
        if (seq != null) {
            if (!seq.isInitialized()) {
                seq.init(synapseEnvironment);
            }
            seq.setErrorHandler(onErrorSeq);
            if (log.isDebugEnabled()) {
                log.debug("injecting received Grpc message to sequence : " + injectingSeq);
            }
            if (!synapseEnvironment.injectInbound(msgCtx, seq, this.sequential)) {
                return null;
            }
        } else {
            log.error("Sequence: " + injectingSeq + " not found");
        }
        String contentType = receivedEvent.getHeadersMap().get(InboundGrpcConstants.HEADER_MAP_CONTENT_TYPE_PARAMETER_NAME);
        MessageContext axis2MsgCtx =
                ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx).getAxis2MessageContext();
        //setting transport headers
        axis2MsgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS
                , receivedEvent.getHeadersMap());
        // Determine the message builder to use
        // TODO: 8/28/19 get as a standarized MIME Type currently only support json and xml
        if (InboundGrpcConstants.CONTENT_TYPE_JSON.equalsIgnoreCase(contentType)) {
            contentType = InboundGrpcConstants.CONTENT_TYPE_JSON_MIME_TYPE;
        } else if (InboundGrpcConstants.CONTENT_TYPE_XML.equalsIgnoreCase(contentType)) {
            contentType = InboundGrpcConstants.CONTENT_TYPE_XML_MIME_TYPE;
        } else {
            log.error("Error ocurred when processing response. " + contentType + " type not supported");
            // TODO: 9/4/19 log and throw
            responseObserver.onError(new Throwable("Content type not supported. Error when processing GRPC Event"));
            // TODO: 8/30/19 log and drop? how to handle that in Grpc level
        }
        Builder builder = BuilderUtil.getBuilderFromSelector(contentType, axis2MsgCtx);
        OMElement documentElement;
        // set the message payload to the message context
        try {
            InputStream in = new AutoCloseInputStream(new ByteArrayInputStream(msgPayload.getBytes()));
            documentElement = builder.processDocument(in, contentType, axis2MsgCtx);
        } catch (Exception ex) {
            // Handle message building error
            log.error("Error while building the message", ex);
            return null;
        }
        // Inject the message to the sequence.
        msgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
        return msgCtx;
    }

    /**
     * Create the initial message context for grpc
     */
    private org.apache.synapse.MessageContext createMessageContext() {
        org.apache.synapse.MessageContext msgCtx = synapseEnvironment
                .createMessageContext();
        MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx)
                .getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());
        msgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, true);
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, carbonContext.getTenantDomain());
        return msgCtx;
    }
}
