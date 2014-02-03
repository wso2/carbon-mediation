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
public class LocalEntryRegistryStore extends AbstractRegistryStore {

    public LocalEntryRegistryStore(UserRegistry registry, String configName) {
        super(registry, configName);
        // if localentries collection does not exists, create a one
        createCollection(getConfigurationPath());
    }

    public Collection<OMElement> getElements() {
        Collection<OMElement> localEntryElements = new ArrayList<OMElement>();
        try {
            localEntryElements = getChildElementsInPath(
                    getConfigurationPath());
        } catch (RegistryException e) {
            handleException("Couldn't get the list of local entries from the registry in path : "
                    + getConfigurationPath(), e);
        }
        return localEntryElements;
    }

    public OMElement getElement(String name) {
        // todo
        return null;
    }

    public void persistElement(String name, OMElement element, String fileName) {
        if (element.getLocalName().equals(
                XMLConfigConstants.ENTRY_ELT.getLocalPart())) {
            if (log.isDebugEnabled()) {
                log.debug("Persisting local entry : " + name + " to the registry");
            }

            name = name.replaceAll("/", ".");
            String localEntryPath = getConfigurationPath()
                    + RegistryConstants.PATH_SEPARATOR + name;

            AppDeployerUtils.attachArtifactToOwnerApp(fileName, ServiceBusConstants
                    .LOCAL_ENTRY_TYPE, name, registry.getTenantId());

            try {
                persistElement(element, localEntryPath, fileName);
            } catch (RegistryException e) {
                handleException("Unable to persist the local entry" +
                        " in the path : " + localEntryPath, e);
            }
        } else {
            handleException("The element provided to persist is not a local entry");
        }
    }

    public void deleteElement(String name) {
        String localEntryPath = getConfigurationPath()
                + RegistryConstants.PATH_SEPARATOR + name;
        try {
            if (registry.resourceExists(localEntryPath)) {
                registry.delete(localEntryPath);
            }
        } catch (RegistryException e) {
            handleException("Error in deleting the local entry at path : " + localEntryPath, e);
        }
    }

    protected String getConfigurationPath() {
         return getConfigurationRoot() + RegistryConstants.PATH_SEPARATOR +
                ServiceBusConstants.RegistryStore.LOCAL_ENTRY_REGISTRY;
    }
}
