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
package org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.invoker;

import org.apache.cxf.continuations.Continuation;
import org.apache.cxf.continuations.ContinuationProvider;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.AbstractInvoker;
import org.apache.log4j.Logger;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.InboundRMResponseSender;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.RMRequestCallable;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.utils.RMConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * After the RM protocol messages are processed, the request is processed here
 */
public class InboundRMHttpInvoker extends AbstractInvoker {

    private static Logger logger = Logger.getLogger(InboundRMHttpInvoker.class);
    private SynapseEnvironment synapseEnvironment;
    private String injectingSequence;
    private String onErrorSequence;
    private ExecutorService executorService;
    private InboundRMResponseSender inboundRMResponseSender;
    private Object bean;

    /**
     * Constructor for the invoker
     *
     * @param bean               An instance of the backend business logic implementing class
     * @param synapseEnvironment The SynapseEnvironment
     * @param injectingSequence  The injecting sequence name
     * @param onErrorSequence    The fault sequence name
     */
    public InboundRMHttpInvoker(Object bean, SynapseEnvironment synapseEnvironment,
                                String injectingSequence, String onErrorSequence) {
        this.synapseEnvironment = synapseEnvironment;
        this.injectingSequence = injectingSequence;
        this.onErrorSequence = onErrorSequence;
        this.bean = bean;
        inboundRMResponseSender = new InboundRMResponseSender();

        setExecutorService(Executors.newFixedThreadPool(RMConstants.THREAD_POOL_SIZE));
    }

    @Override
    public Object getServiceObject(Exchange exchange) {
        return bean;
    }

    /**
     * This is where the the incoming request from the CXF Bus is paused and handed to Synapse
     *
     * @param exchange exchange that contains the messages
     * @param o        the dummy back end object
     * @return null
     */
    @Override
    public Object invoke(Exchange exchange, Object o) {

        ContinuationProvider continuationProvider = (ContinuationProvider) exchange.getInMessage().get(ContinuationProvider.class.getName());
        final Continuation continuation = continuationProvider.getContinuation();

        synchronized (continuation) {
            if (continuation.isNew()) {
                /*
                 * This is a new request
                 * execute a task asynchronously
                 */
                FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new RMRequestCallable(exchange, continuation, synapseEnvironment,
                        injectingSequence,
                        onErrorSequence,
                        inboundRMResponseSender));

                continuation.setObject(futureTask);
                continuation.suspend(0);
                getExecutorService().execute(futureTask);
            } else {
                FutureTask futureTask = (FutureTask) continuation.getObject();
                if (futureTask.isDone()) {
                    try {
                        return futureTask.get();
                    } catch (Exception e) {
                        logger.error("Error occurred while waiting for the response through Synapse", e);
                    }
                } else {
                    continuation.suspend(0);
                }
            }
        }
        return null;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public final void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
