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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.gateway.common.CGConstant;
import org.wso2.carbon.cloud.gateway.common.CGUtils;
import org.wso2.carbon.cloud.gateway.stub.types.common.CGThriftServerBean;
import org.wso2.carbon.core.util.CryptoException;

/**
 * The factory for {@link CGPollingTransportTaskManager}
 */
public class CGPollingTransportTaskManagerFactory {

    private static final Log log = LogFactory.getLog(CGPollingTransportTaskManagerFactory.class);

    private CGPollingTransportTaskManagerFactory() {

    }

    public static CGPollingTransportTaskManager createTaskManagerForService(AxisService service,
                                                                            WorkerPool workerPool,
                                                                            CGPollingTransportEndpoint endpoint,
                                                                            CGPollingTransportReceiver
                                                                                    receiver)
            throws AxisFault {
        String serviceName = service.getName();

        String encryptedToken = (String) service.getParameterValue(CGConstant.TOKEN);
        String token;

        try {
            token = CGUtils.getPlainToken(encryptedToken);
        } catch (CryptoException e) {
            throw new AxisFault(e.getMessage(), e);
        }

        if ("".equals(token)) {
            throw new AxisFault("The secure token is not set for service '" + serviceName + "'");
        }
        CGPollingTransportTaskManager stm = new CGPollingTransportTaskManager();
        stm.setToken(token);
        stm.setServiceName(serviceName);
        stm.setWorkerPool(workerPool);
        stm.setConcurrentClients(CGUtils.getIntProperty(CGConstant.NO_OF_CONCURRENT_CONSUMERS, 1));
        stm.setSubject(receiver.getSubject());

        // for maximum performance keep the dispatching task busy, i.e. allocate a thread per each
        // CPU( if there are two physical CPUs let the NO_OF_DISPATCH_TASK to be 2)
        stm.setNoOfDispatchingTask(CGUtils.getIntProperty(CGConstant.NO_OF_DISPATCH_TASK, 2));
        stm.setEndpoint(endpoint);

        int requestBlockSize = CGUtils.getIntProperty(
                CGConstant.MESSAGE_BLOCK_SIZE, 5);
        int responseBlockSize = CGUtils.getIntProperty(
                CGConstant.RESPONSE_MESSAGE_BLOCK_SIZE, 1000);
        int messageProcessingBlockSize = CGUtils.getIntProperty(
                CGConstant.MESSAGE_PROCESSING_BLOCK_SIZE, CGConstant.DEFAULT_MESSAGE_PROCESSING_BLOCK_SIZE);
        if (messageProcessingBlockSize > CGConstant.CG_WORKERS_MAX_THREADS) {
            // guard against system running out of resources
            log.warn("The message processing block size '" + messageProcessingBlockSize + "' " +
                    "is large than the worker pool size '" + CGConstant.CG_WORKERS_MAX_THREADS +
                    "'. All polling tasks and the message processing tasks share the worker pool," +
                    "so the default value for the message processing block '"
                    + CGConstant.DEFAULT_MESSAGE_PROCESSING_BLOCK_SIZE + "' will be used");
            messageProcessingBlockSize = CGConstant.DEFAULT_MESSAGE_PROCESSING_BLOCK_SIZE;
        }

        CGThriftServerBean bean = (CGThriftServerBean) service.getParameterValue(CGConstant.CG_SERVER_BEAN);
        if (bean == null) {
            throw new AxisFault("Remote CSG server information is missing");
        }
        String hostName = bean.getHostName();
        int port = bean.getPort();
        int timeout = bean.getTimeOut();

        int initialReconnectionDuration = CGUtils.getIntProperty(
                CGConstant.INITIAL_RECONNECT_DURATION, 10000);
        double progressionFactor = CGUtils.getDoubleProperty(
                CGConstant.PROGRESSION_FACTOR, 2.0);

        stm.setRequestBlockSize(requestBlockSize);
        stm.setMessageProcessingBlockSize(messageProcessingBlockSize);
        stm.setResponseBlockSize(responseBlockSize);
        stm.setHostName(hostName);
        stm.setPort(port);
        stm.setTimeout(timeout);
        stm.setReconnectionProgressionFactor(progressionFactor);
        stm.setInitialReconnectDuration(initialReconnectionDuration);
        stm.setTrustStoreLocation(CGUtils.getTrustStoreFilePath());
        stm.setTrustStorePassWord(CGUtils.getTrustStorePassWord());
        stm.setPollingTaskSuspendDuration(CGUtils.getLongProperty(CGConstant.CG_POLLING_TASK_SUSPEND_DURATION,
                15));

        // set the request/response buffers for this
        stm.setTaskBuffers(new CGPollingTransportBuffers());

        return stm;
    }
}
