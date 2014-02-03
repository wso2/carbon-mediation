/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.initializer.persistence.registry;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;

import java.util.Collection;
import java.util.ArrayList;

public class ExecutorRegistryStore extends AbstractRegistryStore {
    public ExecutorRegistryStore(UserRegistry registry, String configName) {
        super(registry, configName);
        // if executor collection does not exists, create a one
        createCollection(getConfigurationPath());
    }

    public Collection<OMElement> getElements() {
        Collection<OMElement> executorElements = new ArrayList<OMElement>();
        try {
            executorElements = getChildElementsInPath(getConfigurationPath());
        } catch (RegistryException e) {
            handleException("Couldn't get the list of executors from the registry in path : "
                    + getConfigurationPath(), e);
        }
        return executorElements;
    }

    public OMElement getElement(String name) {
        return null;
    }

    public void persistElement(String name, OMElement element, String fileName) {
        if (element.getLocalName().equals(
                XMLConfigConstants.EXECUTOR_ELT.getLocalPart())) {
            if (log.isDebugEnabled()) {
                log.debug("Persisting executor : " + name + " to the registry");
            }

            String executorPath = getConfigurationPath()
                    + RegistryConstants.PATH_SEPARATOR + name;
            try {
                // TODO: articat deployer names
                persistElement(element, executorPath, fileName);
            } catch (RegistryException e) {
                handleException("Unable to persist the executor in the path : " + executorPath, e);
            }
        } else {
            handleException("The element provided to persist is not a sequence");
        }
    }

    public void deleteElement(String name) {
        String executorPath = getConfigurationPath() + RegistryConstants.PATH_SEPARATOR + name;
        try {
            if (registry.resourceExists(executorPath)) {
                registry.delete(executorPath);
            }
        } catch (RegistryException e) {
            handleException("Error in deleting the executor at path : " + executorPath, e);
        }
    }

    protected String getConfigurationPath() {
         return getConfigurationRoot() + RegistryConstants.PATH_SEPARATOR +
                ServiceBusConstants.RegistryStore.EXECUTOR_REGISTRY;
    }
}
