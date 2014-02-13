/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.message.store.util;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.wso2.carbon.mediation.dependency.mgt.services.DependencyManagementService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.HashMap;
import java.util.Map;


/**
 * Class <code>ConfigHolder</code> acts as a Singleton holder for the Message Store admin Service
 * backend component which store configurations.
 */
public class ConfigHolder {

    private static ConfigHolder instance =  new ConfigHolder();;
    private static final Log log = LogFactory.getLog(ConfigHolder.class);

    private SynapseConfiguration synapseConfiguration;
    private AxisConfiguration axisConfiguration;
    private UserRegistry registry;
    private DependencyManagementService dependencyManager;

    private Map<Integer, SynapseEnvironmentService> synapseEnvironmentServices =
            new HashMap<Integer, SynapseEnvironmentService>();

    private ConfigHolder() {
    }

    /**
     * Returns the Config holder instance that holds the configurations
     * @return  Config holder singleton instance
     */
    public static ConfigHolder getInstance() {
        return instance;
    }

    /**
     * Returns the Synapse configuration that is stored in the ConfigHolder instance
     * @return synapseConfiguration
     *
     */
    public SynapseConfiguration getSynapseConfiguration() throws Exception {
        assertNull("SynapseConfiguration", synapseConfiguration);
        return synapseConfiguration;
    }


    /**
     * Set the Synapse Configuration instance in the ConfigHolder
     * @param synapseConfiguration
     */
    public void setSynapseConfiguration(SynapseConfiguration synapseConfiguration) {
        this.synapseConfiguration = synapseConfiguration;
    }


    public AxisConfiguration getAxisConfiguration() throws Exception {
        assertNull("AxisConfiguration", axisConfiguration);
        return axisConfiguration;
    }

    public void setAxisConfiguration(AxisConfiguration axisConfiguration) {
        this.axisConfiguration = axisConfiguration;
    }

    public UserRegistry getRegistry() throws Exception {
        assertNull("Registry", registry);
        return registry;
    }

    public DependencyManagementService getDependencyManager() {
        return dependencyManager;
    }

    public void setDependencyManager(DependencyManagementService dependencyManager) {
        this.dependencyManager = dependencyManager;
    }

    public void setRegistry(UserRegistry registry) {
        this.registry = registry;
    }

    private void assertNull(String name, Object object) throws Exception {
        if (object == null) {
            String message = name + " reference in the Message Store admin config holder is null";
            log.error(message);
            throw new Exception(message);
        }
    }

    public SynapseEnvironmentService getSynapseEnvironmentService(int id) {
        return synapseEnvironmentServices.get(id);
    }

    public void addSynapseEnvironmentService(int id,
                                             SynapseEnvironmentService synapseEnvironmentService) {
        synapseEnvironmentServices.put(id, synapseEnvironmentService);
    }

    public void removeSynapseEnvironmentService(int id) {
        synapseEnvironmentServices.remove(id);
    }

    public Map<Integer, SynapseEnvironmentService> getSynapseEnvironmentServices() {
        return synapseEnvironmentServices;
    }
}
