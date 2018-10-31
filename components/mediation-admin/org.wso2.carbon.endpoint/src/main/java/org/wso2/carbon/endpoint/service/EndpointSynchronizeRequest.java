/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.endpoint.service;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.endpoints.Endpoint;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import java.util.UUID;

/**
 * This class is used to switch on and off endpoints
 */
public class EndpointSynchronizeRequest extends ClusteringMessage {

    public enum EndpointOperationType {
        DEACTIVATE, ACTIVATE
    }
    private static final transient Log log = LogFactory.getLog(EndpointSynchronizeRequest.class);

    private int tenantId;
    private String tenantDomain;
    private UUID messageId;
    private String endpointName;
    private EndpointOperationType endpointOperationType;

    EndpointSynchronizeRequest(int tenantId, String tenantDomain, UUID messageId,
                               String endpointName, EndpointOperationType endpointOperationType) {
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.messageId = messageId;
        this.endpointName = endpointName;
        this.endpointOperationType = endpointOperationType;
    }

    @Override
    public ClusteringCommand getResponse() {
        return null;
    }

    /**
     * Function to switch on/off endpoints
     * @param configurationContext
     * @throws ClusteringFault
     */
    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("Received [" + this + "] ");
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(tenantId);
            privilegedCarbonContext.setTenantDomain(tenantDomain);
            SynapseConfiguration synapseConfiguration =
                    (SynapseConfiguration) configurationContext.getAxisConfiguration().
                            getParameter("synapse.config").getValue();
            Endpoint endpoint = synapseConfiguration.getEndpoint(endpointName);
            if (endpoint == null) {
                throw new ClusteringFault(String.format("Endpoint could not be found: ", endpointName));
            }
            if (endpointOperationType == EndpointOperationType.ACTIVATE) {
                endpoint.getContext().switchOn();
            }
            else {
                endpoint.getContext().switchOff();
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Function used to print its class variables
     * @return class variables
     */
    @Override
    public String toString() {
        return "EndpointSynchronizeRequest{"
                + "tenantId=" + tenantId
                + ", tenantDomain='" + tenantDomain + '\''
                + ", messageId=" + messageId
                + ", endpointName='" + endpointName + '\''
                + ", endpointOperationType=" + endpointOperationType
                + '}';
    }
}
