package org.wso2.carbon.inbound.endpoint.protocol.hl7;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;

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
class CallbackRunnableTask implements CallbackRunnable {
    private static final Log log = LogFactory.getLog(CallbackRunnableTask.class);

    private volatile boolean complete = false;

    private MessageContext requestMessageContext;

    private SequenceMediator injectingSequence;
    private SynapseEnvironment synapseEnvironment;

    public CallbackRunnableTask(MessageContext synCtx, SequenceMediator injectingSequence,
                                SynapseEnvironment synapseEnvironment) {
        this.requestMessageContext = synCtx;
        this.injectingSequence = injectingSequence;
        this.synapseEnvironment = synapseEnvironment;
    }

    @Override
    public void run() {
        // inject to synapse here
        synapseEnvironment.injectMessage(requestMessageContext, injectingSequence);
        complete = true;
        return;
    }

    @Override
    public void onComplete() {
        // handler to call when thread execution is finished.
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

}