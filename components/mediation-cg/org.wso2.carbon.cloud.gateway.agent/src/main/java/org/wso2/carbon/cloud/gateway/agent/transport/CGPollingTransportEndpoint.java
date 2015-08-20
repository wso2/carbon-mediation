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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.ProtocolEndpoint;
import org.apache.axis2.transport.base.threads.WorkerPool;

import java.util.HashSet;
import java.util.Set;

/**
 * Represent a CSG Polling transport endpoint
 */
public class CGPollingTransportEndpoint extends ProtocolEndpoint {

    private Set<EndpointReference> endpointReferences = new HashSet<EndpointReference>();

    private final WorkerPool workerPool;

    private CGPollingTransportTaskManager taskManager;


    private CGPollingTransportReceiver receiver;


    public CGPollingTransportEndpoint(WorkerPool pool, CGPollingTransportReceiver receiver) {
        this.workerPool = pool;
        this.receiver = receiver;
    }

    public CGPollingTransportTaskManager getTaskManager() {
        return taskManager;
    }

    @Override
    public boolean loadConfiguration(ParameterInclude params) throws AxisFault {
        if (!(params instanceof AxisService)) {
            return false;
        }

        AxisService service = (AxisService)params;

        taskManager = CGPollingTransportTaskManagerFactory.createTaskManagerForService(
                service, workerPool, this, receiver);
        return true;
    }

    @Override
    public EndpointReference[] getEndpointReferences(AxisService axisService, String s)
            throws AxisFault {
        return endpointReferences.toArray(new EndpointReference[endpointReferences.size()]);
    }
}
