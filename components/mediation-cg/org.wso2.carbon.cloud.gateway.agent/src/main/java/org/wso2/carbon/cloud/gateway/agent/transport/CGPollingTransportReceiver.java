/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.gateway.agent.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.AbstractTransportListenerEx;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.wso2.carbon.cloud.gateway.agent.observer.CGAgentSubject;
import org.wso2.carbon.cloud.gateway.agent.observer.CGAgentSubjectImpl;
import org.wso2.carbon.cloud.gateway.common.CGConstant;
import org.wso2.carbon.cloud.gateway.common.CGUtils;

/**
 * CSG Polling Transport receiver implementation
 */
public class CGPollingTransportReceiver
        extends AbstractTransportListenerEx<CGPollingTransportEndpoint> {

    /**
     * The worker pool for polling tasks
     */
    private WorkerPool csgWorkerPool;

    /**
     * Keep track of any changes in remote server for notifying the observers
     */
    private CGAgentSubject subject;


    @Override
    protected void doInit() throws AxisFault {

        subject = new CGAgentSubjectImpl();

        csgWorkerPool = WorkerPoolFactory.getWorkerPool(
                CGUtils.getIntProperty(
                        CGConstant.CG_THRIFT_T_CORE, CGConstant.WORKERS_CORE_THREADS),
                CGUtils.getIntProperty(
                        CGConstant.CG_THRIFT_T_MAX, CGConstant.CG_WORKERS_MAX_THREADS),
                CGUtils.getIntProperty(
                        CGConstant.CG_THRIFT_T_ALIVE, CGConstant.WORKER_KEEP_ALIVE),
                CGUtils.getIntProperty(
                        CGConstant.CG_THRIFT_T_QLEN, CGConstant.WORKER_BLOCKING_QUEUE_LENGTH),
                "CGPollingTransportReceiver-worker-thread-group",
                "CGPollingTransportReceiver-worker");
        log.info("CGThrift transport receiver started");
    }

    @Override
    protected CGPollingTransportEndpoint createEndpoint() {
        return new CGPollingTransportEndpoint(csgWorkerPool, this);
    }

    @Override
    protected void startEndpoint(CGPollingTransportEndpoint csgThriftEndpoint) throws AxisFault {
        CGPollingTransportTaskManager tm = csgThriftEndpoint.getTaskManager();
        tm.start();

        log.info("CGThrift polling task started for service '" + tm.getServiceName() + "'");
    }

    @Override
    protected void stopEndpoint(CGPollingTransportEndpoint csgThriftEndpoint) {
        CGPollingTransportTaskManager tm = csgThriftEndpoint.getTaskManager();
        tm.stop();
        log.info("CGThrift polling task stopped listen for service '" +
                csgThriftEndpoint.getService() + "'");
    }

    public CGAgentSubject getSubject() {
        return subject;
    }

}
