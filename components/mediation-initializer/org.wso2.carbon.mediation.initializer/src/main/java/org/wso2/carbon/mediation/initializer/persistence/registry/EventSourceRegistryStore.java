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
public class EventSourceRegistryStore extends AbstractRegistryStore {

    public EventSourceRegistryStore(UserRegistry registry, String configName) {
        super(registry, configName);
        // if eventsource collection does not exists, create a one
        createCollection(getConfigurationPath());
    }

    public Collection<OMElement> getElements() {
        Collection<OMElement> eventSourceElements = new ArrayList<OMElement>();
        try {
            eventSourceElements = getChildElementsInPath(getConfigurationPath());
        } catch (RegistryException e) {
            handleException("Couldn't get the list of event sources from the registry in path : "
                    + getConfigurationPath(), e);
        }
        return eventSourceElements;
    }

    public OMElement getElement(String name) {
        // todo
        return null;
    }

    public void persistElement(String name, OMElement element, String fileName) {
        if (element.getLocalName().equals(
                XMLConfigConstants.EVENT_SOURCE_ELT.getLocalPart())) {
            if (log.isDebugEnabled()) {
                log.debug("Persisting event source : " + name + " to the registry");
            }

            String eventSourcePath = getConfigurationPath()
                    + RegistryConstants.PATH_SEPARATOR + name;

            AppDeployerUtils.attachArtifactToOwnerApp(fileName, ServiceBusConstants
                    .EVENT_SOURCE_TYPE, name, registry.getTenantId());

            try {
                persistElement(element, eventSourcePath, fileName);
            } catch (RegistryException e) {
                handleException("Unable to persist the event source" +
                        " in the path : " + eventSourcePath, e);
            }
        } else {
            handleException("The element provided to persist is not an event source");
        }
    }

    public void deleteElement(String name) {
        String eventSourcePath = getConfigurationPath() + RegistryConstants.PATH_SEPARATOR + name;
        try {
            if (registry.resourceExists(eventSourcePath)) {
                registry.delete(eventSourcePath);
            }
        } catch (RegistryException e) {
            handleException("Error in deleting the event source at path : " + eventSourcePath, e);
        }
    }

    protected String getConfigurationPath() {
         return getConfigurationRoot() + RegistryConstants.PATH_SEPARATOR +
                ServiceBusConstants.RegistryStore.EVENT_SOURCE_REGISTRY;
    }
}
