package org.wso2.carbon.inbound.endpoint.protocol.nats;

import io.nats.client.Message;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundEndpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.transport.customlogsetter.CustomLogSetter;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class NatsInjectHandler {
    private static final Log log = LogFactory.getLog(NatsInjectHandler.class);

    private String injectingSeq;
    private String onErrorSeq;
    private boolean sequential;
    private SynapseEnvironment synapseEnvironment;
    private String contentType;

    NatsInjectHandler(String injectingSeq, String onErrorSeq, boolean sequential,
            SynapseEnvironment synapseEnvironment, String contentType) {
        this.injectingSeq = injectingSeq;
        this.onErrorSeq = onErrorSeq;
        this.sequential = sequential;
        this.synapseEnvironment = synapseEnvironment;
        this.contentType = contentType;
    }

    /**
     * Determine the message builder to use, set the message payload to the message context and
     * inject the message to the sequence
     */
    public boolean invoke(Object object, String name) throws SynapseException {
        try {
            org.apache.synapse.MessageContext msgCtx = createMessageContext();
            msgCtx.setProperty(SynapseConstants.INBOUND_ENDPOINT_NAME, name);
            msgCtx.setProperty(SynapseConstants.IS_INBOUND, true);
            InboundEndpoint inboundEndpoint = msgCtx.getConfiguration().getInboundEndpoint(name);
            CustomLogSetter.getInstance().setLogAppender(inboundEndpoint.getArtifactContainerName());
            printDebugLog("Processed NATS Message.");
            MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx)
                    .getAxis2MessageContext();
            // Determine the message builder to use
            Builder builder;
            if (contentType == null) {
                printDebugLog("No content type specified. Using SOAP builder.");
                builder = new SOAPBuilder();
            } else {
                int index = contentType.indexOf(';');
                String type = index > 0 ? contentType.substring(0, index) : contentType;
                builder = BuilderUtil.getBuilderFromSelector(type, axis2MsgCtx);
                if (builder == null) {
                    printDebugLog("No message builder found for type '" + type + "'. Falling back to SOAP.");
                    builder = new SOAPBuilder();
                }
            }
            // set the message payload to the message context
//            Message message = (Message) object;
//            if (object == null) {
//                System.out.println("object is null hehe.");
//            } else {
//                System.out.println(object.toString() + "===" + new String(message.getData(), StandardCharsets.UTF_8));
//            }
            InputStream in = new AutoCloseInputStream(new ByteArrayInputStream((byte[]) object));
            OMElement documentElement = builder.processDocument(in, contentType, axis2MsgCtx);
            msgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
            // Inject the message to the sequence.

            if (StringUtils.isEmpty(injectingSeq)) {
                log.error("Sequence name not specified. Sequence : " + injectingSeq);
                return false;
            }
            SequenceMediator seq = (SequenceMediator) synapseEnvironment.getSynapseConfiguration().getSequence(injectingSeq);
            if (seq != null) {
                printDebugLog("Injecting message to sequence : " + injectingSeq);
                if (!seq.isInitialized()) {
                    seq.init(synapseEnvironment);
                }
                seq.setErrorHandler(onErrorSeq);
                return synapseEnvironment.injectInbound(msgCtx, seq, sequential);
            } else {
                log.error("Sequence: " + injectingSeq + " not found");
            }
        } catch (SynapseException se) {
            throw se;
        } catch (Exception e) {
            throw new SynapseException("Error while processing the NATS Message", e);
        }
        return true;
    }

    /**
     * Create the initial message context for kafka
     */
    private org.apache.synapse.MessageContext createMessageContext() {
        org.apache.synapse.MessageContext msgCtx = synapseEnvironment.createMessageContext();
        MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx)
                .getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UIDGenerator.generateUID());
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, carbonContext.getTenantDomain());
        msgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, true);
        return msgCtx;
    }

    /**
     * Check if debug is enabled for logging.
     *
     * @param text log text
     */
    private void printDebugLog(String text) {
        if (log.isDebugEnabled()) {
            log.debug(text);
        }
    }
}
