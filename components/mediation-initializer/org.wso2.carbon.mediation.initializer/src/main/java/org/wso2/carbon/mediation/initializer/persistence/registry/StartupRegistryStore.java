/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.initializer.persistence.registry;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 */
public class StartupRegistryStore extends AbstractRegistryStore {

    public StartupRegistryStore(UserRegistry registry, String ConfigName) {
        super(registry, ConfigName);
        // if startup collection does not exists, create a one
        createCollection(getConfigurationPath());
    }

    public Collection<OMElement> getElements() {
        Collection<OMElement> startupElements = new ArrayList<OMElement>();
        try {
            startupElements = getChildElementsInPath(
                    getConfigurationPath());
        } catch (RegistryException e) {
            handleException("Couldn't get the list of startup tasks from the registry in path : "
                    + getConfigurationPath(), e);
        }
        return startupElements;
    }

    public OMElement getElement(String name) {
        // todo
        return null;
    }

    public void persistElement(String name, OMElement element, String fileName) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting startup task : " + name + " to the registry");
        }

        String startupPath = getConfigurationPath()
                + RegistryConstants.PATH_SEPARATOR + name;

        AppDeployerUtils.attachArtifactToOwnerApp(fileName, ServiceBusConstants.TASK_TYPE,
                name, registry.getTenantId());

        try {
            persistElement(element, startupPath, fileName);
        } catch (RegistryException e) {
            handleException("Unable to persist the startup task" +
                    " in the path : " + startupPath, e);
        }
    }

    public void deleteElement(String name) {
        String startupPath = getConfigurationPath()
                + RegistryConstants.PATH_SEPARATOR + name;
        try {
            if (registry.resourceExists(startupPath)) {
                registry.delete(startupPath);
            }
        } catch (RegistryException e) {
            handleException("Error in deleting the startup task at path : " + startupPath, e);
        }
    }

    protected String getConfigurationPath() {
         return getConfigurationRoot() + RegistryConstants.PATH_SEPARATOR +
                ServiceBusConstants.RegistryStore.SYNAPSE_STARTUP_REGISTRY;
    }
}
