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
public class ProxyServiceRegistryStore extends AbstractRegistryStore {

    public ProxyServiceRegistryStore(UserRegistry registry, String configName) {
        super(registry, configName);
        // if proxyservices collection does not exists, create a one
        createCollection(getConfigurationPath());
    }

    public Collection<OMElement> getElements() {
        Collection<OMElement> proxyServiceElements = new ArrayList<OMElement>();
        try {
            proxyServiceElements = getChildElementsInPath(
                    getConfigurationPath());
        } catch (RegistryException e) {
            handleException("Couldn't get the list of proxy services from the registry in path : "
                    + getConfigurationPath(), e);
        }
        return proxyServiceElements;
    }

    public OMElement getElement(String name) {
        // todo
        return null;
    }

    public void persistElement(String name, OMElement element, String fileName) {
        if (element.getLocalName().equals(
                XMLConfigConstants.PROXY_ELT.getLocalPart())) {
            if (log.isDebugEnabled()) {
                log.debug("Persisting proxy service : " + name + " to the registry");
            }

            String proxyServicePath = getConfigurationPath()
                    + RegistryConstants.PATH_SEPARATOR + name;

            AppDeployerUtils.attachArtifactToOwnerApp(fileName, ServiceBusConstants
                    .PROXY_SERVICE_TYPE, name, registry.getTenantId());

            try {
                persistElement(element, proxyServicePath, fileName);
            } catch (RegistryException e) {
                handleException("Unable to persist the proxy service" +
                        " in the path : " + proxyServicePath, e);
            }
        } else {
            handleException("The element provided to persist is not a proxy service");
        }
    }

    public void deleteElement(String name) {
        String proxyServicePath = getConfigurationPath()
                + RegistryConstants.PATH_SEPARATOR + name;
        try {
            if (registry.resourceExists(proxyServicePath)) {
                registry.delete(proxyServicePath);
            }
        } catch (RegistryException e) {
            handleException("Error in deleting the proxy service at path : " + proxyServicePath, e);
        }
    }

    protected String getConfigurationPath() {
         return getConfigurationRoot() + RegistryConstants.PATH_SEPARATOR +
                ServiceBusConstants.RegistryStore.PROXY_SERVICE_REGISTRY;
    }
}
