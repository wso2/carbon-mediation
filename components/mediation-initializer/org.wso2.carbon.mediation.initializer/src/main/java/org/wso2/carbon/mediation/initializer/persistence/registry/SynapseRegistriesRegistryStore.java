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
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.Collection;

/**
 * 
 */
public class SynapseRegistriesRegistryStore extends AbstractRegistryStore {

    public SynapseRegistriesRegistryStore(UserRegistry registry, String configName) {
        super(registry, configName);
        // if registry collection does not exists, create a one
        createCollection(getConfigurationPath());
    }

    public Collection<OMElement> getElements() {
        throw new UnsupportedOperationException("There is only one synapse registry and " +
                "hence this method is unsupported, use the getElement('registryInfo') method " +
                "to get the declared registry element");
    }

    public OMElement getElement() {
        return getElement(ServiceBusConstants.SYNAPSE_REGISTRY_RESOURCE_NAME);
    }

    public OMElement getElement(String name) {
        OMElement synapseRegistryElem = null;
        String synapseRegistryPath
                = getConfigurationPath()
                + RegistryConstants.PATH_SEPARATOR + name;
        try {
            if (registry.resourceExists(synapseRegistryPath)) {
                synapseRegistryElem = getResourceAsElement(synapseRegistryPath);
            }
        } catch (RegistryException e) {
            handleException("Couldn't get the synapse registry info from the registry in path : "
                    + synapseRegistryPath, e);
        }
        return synapseRegistryElem;
    }

    public void persistElement(OMElement element, String fileName) {
        persistElement(ServiceBusConstants.SYNAPSE_REGISTRY_RESOURCE_NAME, element, fileName);
    }

    public void persistElement(String name, OMElement element, String fileName) {
        if (element.getLocalName().equals(
                XMLConfigConstants.REGISTRY_ELT.getLocalPart())) {
            if (log.isDebugEnabled()) {
                log.debug("Persisting synapse registry to the registry");
            }

            String synapseRegistryPath
                    = getConfigurationPath()
                    + RegistryConstants.PATH_SEPARATOR + name;
            try {
                persistElement(element, synapseRegistryPath, null);
            } catch (RegistryException e) {
                handleException("Unable to persist the synapse registry" +
                        " in the path : " + synapseRegistryPath, e);
            }
        } else {
            handleException("The element provided to persist is not a synapse registry");
        }
    }

    public void deleteElement() {
        deleteElement(getConfigurationPath());
    }

    public void deleteElement(String name) {
        try {
            if (registry.resourceExists(name)) {
                registry.delete(name);
            }
        } catch (RegistryException e) {
            handleException("Error in deleting the synapse registry" +
                    " at path : " + name, e);
        }
    }

    protected String getConfigurationPath() {
         return getConfigurationRoot() + RegistryConstants.PATH_SEPARATOR +
                ServiceBusConstants.RegistryStore.SYNAPSE_REGISTRY_REGISTRY;
    }
}
