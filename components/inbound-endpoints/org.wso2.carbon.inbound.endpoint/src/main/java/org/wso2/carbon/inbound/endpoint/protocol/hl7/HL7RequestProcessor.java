package org.wso2.carbon.inbound.endpoint.protocol.hl7;

import ca.uhn.hl7v2.HL7Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.mediators.base.SequenceMediator;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class HL7RequestProcessor implements InboundResponseSender {
    private static final Log log = LogFactory.getLog(InboundHL7IOReactor.class);

    private static ExecutorService executorService;
    private volatile Map<String, HL7CallableTask> tasks;

    private InboundProcessorParams params;
    private String inSequence;
    private String onErrorSequence;
    private SynapseEnvironment synEnv;

    public HL7RequestProcessor(InboundProcessorParams params) {
        executorService  = Executors.newFixedThreadPool(2);
        this.params = params;
        this.inSequence = params.getInjectingSeq();
        this.onErrorSequence = params.getOnErrorSeq();
        this.synEnv = params.getSynapseEnvironment();
    }

    public void processRequest(MLLPContext mllpContext) {
        mllpContext.setAckReady(false);
        // Set AUTO ACK - no need to do else clause as
        if (params.getProperties().getProperty(MLLPConstants.PARAM_HL7_AUTO_ACK).equals("false")) {
            mllpContext.setAutoAck(false);
        }

        // Prepare Synapse Context for message injection
        MessageContext synCtx = HL7MessageUtils.createSynapseMessageContext(mllpContext.getHl7Message(), params, synEnv);

        // If AUTO ACK lets ACK immediately.
        if (mllpContext.isAutoAck()) {
            sendBackImmediateAck(mllpContext);
        } else {
            // If not AUTO ACK, we need response invocation through this processor
            synCtx.setProperty(SynapseConstants.IS_INBOUND, true);
            synCtx.setProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER, this);
        }

        synCtx.setProperty(MLLPConstants.MLLP_CONTEXT, mllpContext);

        SequenceMediator injectSeq = (SequenceMediator) synEnv.getSynapseConfiguration().getSequence(inSequence);
        injectSeq.setErrorHandler(onErrorSequence);

        CallbackRunnable task = new CallbackRunnableTask(synCtx, injectSeq, synEnv);
        executorService.execute(task);
    }

    @Override
    public void sendBack(MessageContext messageContext) {
        MLLPContext mllpContext = (MLLPContext) messageContext.getProperty(MLLPConstants.MLLP_CONTEXT);
        sendBack(messageContext, mllpContext);
    }

    private void sendBack(MessageContext messageContext, MLLPContext mllpContext) {
        try {
            mllpContext.setHl7Message(HL7MessageUtils.payloadToHL7Message(messageContext, params));
            mllpContext.setAckReady(true);
        } catch (HL7Exception e) {
            log.error("Error while generating HL7 response from payload.");
        }
    }

    private void sendBackImmediateAck(MLLPContext mllpContext) {
        mllpContext.setAckReady(true);
    }
}
