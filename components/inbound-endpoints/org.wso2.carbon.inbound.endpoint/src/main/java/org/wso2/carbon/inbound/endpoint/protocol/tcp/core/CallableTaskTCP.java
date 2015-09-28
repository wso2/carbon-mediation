/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.inbound.endpoint.protocol.tcp.core;

import org.apache.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;

import java.util.concurrent.Callable;

/**
 * Callable task which handle the message injection to synapse
 */
public class CallableTaskTCP implements Callable<Boolean> {
    private static final Logger log = Logger.getLogger(CallableTaskTCP.class);

    private MessageContext requestMessageContext;
    private SequenceMediator injectingSequence;
    private SynapseEnvironment synapseEnvironment;

    public CallableTaskTCP(MessageContext synCtx, SequenceMediator injectingSequence) {
        this.requestMessageContext = synCtx;
        this.injectingSequence = injectingSequence;
        this.synapseEnvironment = synCtx.getEnvironment();
    }

    @Override public Boolean call() throws Exception {
        // inject incoming message to synapse here
        return synapseEnvironment.injectInbound(requestMessageContext, injectingSequence, true);
    }
}
