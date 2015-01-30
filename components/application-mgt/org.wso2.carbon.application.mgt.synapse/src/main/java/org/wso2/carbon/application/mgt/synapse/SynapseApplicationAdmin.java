/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.application.mgt.synapse;

import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.synapse.SynapseAppDeployerConstants;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.mgt.synapse.internal.SynapseAppServiceComponent;
import org.wso2.carbon.core.AbstractAdmin;
import org.apache.synapse.Startup;
import org.apache.synapse.endpoints.*;
import org.apache.synapse.startup.quartz.StartUpController;
import org.apache.synapse.task.TaskDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class SynapseApplicationAdmin extends AbstractAdmin {

    public static final String ADDRESS_EP = "address";
    public static final String FAILOVER_EP = "failOver";
    public static final String WSDL_EP = "WSDL";
    public static final String LOADBALANCE_EP = "loadBalance";
    public static final String DEFAULT_EP= "default";

    private static final Log log = LogFactory.getLog(SynapseApplicationAdmin.class);

    /**
     * Gives a SynapseApplicationMetadata object with all synapse artifacts deployed through the
     * given app. This can consist of proxy services, endpoints etc..
     *
     * @param appName - input app name
     * @return - SynapseApplicationMetadata object with found artifact info
     * @throws Exception - error on retrieving metadata
     */
    public SynapseApplicationMetadata getSynapseAppData(String appName) throws Exception {
        SynapseApplicationMetadata data = new SynapseApplicationMetadata();
        String tenantId = AppDeployerUtils.getTenantIdString(getAxisConfig());

        // Check whether there is an application in the system from the given name
        ArrayList<CarbonApplication> appList
                = SynapseAppServiceComponent.getAppManager().getCarbonApps(tenantId);
        CarbonApplication currentApplication = null;
        for (CarbonApplication application : appList) {
            if (appName.equals(application.getAppNameWithVersion())) {
                data.setAppName(application.getAppName());
                currentApplication = application;
                break;
            }
        }

        // If the app not found, throw an exception
        if (currentApplication == null) {
            String msg = "No Carbon Application found of the name : " + appName;
            log.error(msg);
            throw new Exception(msg);
        }

        List<String> sequenceList = new ArrayList<String>();
        List<EndpointMetadata> endpointList = new ArrayList<EndpointMetadata>();
        List<String> proxyList = new ArrayList<String>();
        List<String> leList = new ArrayList<String>();
        List<String> eventList = new ArrayList<String>();
        List<String> mediatorList = new ArrayList<String>();
        List<TaskMetadata> taskList = new ArrayList<TaskMetadata>();

        List<Artifact.Dependency> dependencies = currentApplication.getAppConfig().
                getApplicationArtifact().getDependencies();

        // iterate the dependent artifacts and create metadata elements
        for (Artifact.Dependency dependency : dependencies) {
            Artifact artifact = dependency.getArtifact();

            String type = artifact.getType();
            String instanceName = artifact.getName();

            // if the instance name is null, artifact deployment has failed..
            if (instanceName == null) {
                continue;
            }

            if (SynapseAppDeployerConstants.SEQUENCE_TYPE.equals(type)) {
                sequenceList.add(instanceName);
            } else if (SynapseAppDeployerConstants.ENDPOINT_TYPE.equals(type)) {
                Endpoint endpoint = SynapseAppServiceComponent.getScService().
                        getSynapseConfiguration().getEndpoint(instanceName);
                if (endpoint != null) {
                    EndpointMetadata epData = new EndpointMetadata();
                    epData.setName(instanceName);
                    epData.setType(getEndpointType(endpoint));
                    endpointList.add(epData);
                }
            } else if (SynapseAppDeployerConstants.PROXY_SERVICE_TYPE.equals(type)) {
                proxyList.add(instanceName);
            } else if (SynapseAppDeployerConstants.LOCAL_ENTRY_TYPE.equals(type)) {
                leList.add(instanceName);
            } else if (SynapseAppDeployerConstants.EVENT_SOURCE_TYPE.equals(type)) {
                eventList.add(instanceName);
            } else if (SynapseAppDeployerConstants.MEDIATOR_TYPE.equals(type)) {
                mediatorList.add(instanceName);
            } else if (SynapseAppDeployerConstants.TASK_TYPE.equals(type)) {
                Startup task = SynapseAppServiceComponent.getScService().
                        getSynapseConfiguration().getStartup(instanceName);
                TaskDescription taskDescription = ((StartUpController) task).getTaskDescription();
                TaskMetadata taskMetadata = new TaskMetadata();
                taskMetadata.setName(taskDescription.getName());
                taskMetadata.setGroupName(taskDescription.getTaskGroup());
                taskList.add(taskMetadata);
            }
        }

        // Set found artifacts in the data object
        data.setSequences(sequenceList.toArray(new String[sequenceList.size()]));
        data.setEndpoints(endpointList.toArray(new EndpointMetadata[endpointList.size()]));
        data.setProxyServices(proxyList.toArray(new String[proxyList.size()]));
        data.setLocalEntries(leList.toArray(new String[leList.size()]));
        data.setEvents(eventList.toArray(new String[eventList.size()]));
        data.setMediators(mediatorList.toArray(new String[mediatorList.size()]));
        data.setTasks(taskList.toArray(new TaskMetadata[taskList.size()]));

        return data;
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
        } else if(ep instanceof DefaultEndpoint) {
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
