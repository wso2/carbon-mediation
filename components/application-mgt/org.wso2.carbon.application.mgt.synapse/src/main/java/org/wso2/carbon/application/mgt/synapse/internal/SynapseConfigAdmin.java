/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.application.mgt.synapse.internal;

import org.apache.synapse.Startup;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.endpoints.*;
import org.apache.synapse.startup.quartz.StartUpController;
import org.apache.synapse.task.TaskDescription;
import org.wso2.carbon.application.mgt.synapse.EndpointMetadata;
import org.wso2.carbon.application.mgt.synapse.TaskMetadata;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


public class SynapseConfigAdmin extends AbstractServiceBusAdmin {

    private SynapseConfiguration synapseConfiguration;
    private Map<String, Endpoint> namedEndpointMap;
    private Collection<Startup> namedTaskMap;

    // Constants to identify
    private final String ADDRESS_EP = "address";
    private final String FAILOVER_EP = "failOver";
    private final String WSDL_EP = "WSDL";
    private final String LOADBALANCE_EP = "loadBalance";
    private final String DEFAULT_EP = "default";

    public SynapseConfigAdmin() {
        synapseConfiguration = getSynapseConfiguration();
        namedEndpointMap = synapseConfiguration.getDefinedEndpoints();
        namedTaskMap = synapseConfiguration.getStartups();
    }

    /**
     * Get the EndpointMetadata from name
     *
     * @param endpointName Name of the endpoint
     * @return EndpointMetadata
     */
    public EndpointMetadata getEndpointMetadata(String endpointName) {
        Endpoint endpoint = namedEndpointMap.get(endpointName);

        if (endpoint != null) {
            EndpointMetadata epData = new EndpointMetadata();
            epData.setName(endpointName);
            epData.setType(getEndpointType(endpoint));

            return epData;
        } else {
            return null;
        }

    }

    /**
     * Get the TaskMetadata from task name
     *
     * @param instanceName
     * @return
     */
    public TaskMetadata getTaskMetaData(String instanceName) {
        Iterator iterator = namedTaskMap.iterator();
        while (iterator.hasNext()) {
            Startup startup = (Startup) iterator.next();
            if (startup.getName().equals(instanceName)) {
                TaskDescription taskDescription = ((StartUpController) startup).getTaskDescription();
                TaskMetadata taskMetadata = new TaskMetadata();
                taskMetadata.setName(taskDescription.getName());
                taskMetadata.setGroupName(taskDescription.getTaskGroup());
                return taskMetadata;
            }
        }
        return null;
    }


    /**
     * Decides the endpoint type by checking the type of the endpoint instance
     *
     * @param ep - Endpoint instance
     * @return type
     */
    private String getEndpointType(Endpoint ep) {
        String epType = null;
        if (ep instanceof AddressEndpoint) {
            epType = ADDRESS_EP;
        } else if (ep instanceof DefaultEndpoint) {
            epType = DEFAULT_EP;
        } else if (ep instanceof WSDLEndpoint) {
            epType = WSDL_EP;
        } else if (ep instanceof FailoverEndpoint) {
            epType = FAILOVER_EP;
        } else if (ep instanceof LoadbalanceEndpoint) {
            epType = LOADBALANCE_EP;
        }
        return epType;
    }

}
