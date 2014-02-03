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

package org.wso2.carbon.mediation.initializer.persistence.registry;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.Collection;

public class APIRegistryStore extends AbstractRegistryStore {

    public APIRegistryStore(UserRegistry registry, String configName) {
        super(registry, configName);
        createCollection(getConfigurationPath());
    }

    public Collection<OMElement> getElements() {
        Collection<OMElement> apiElements = new ArrayList<OMElement>();
        try {
            apiElements = getChildElementsInPath(getConfigurationPath());
        } catch (RegistryException e) {
            handleException("Couldn't get the list of APIs from the registry in path : "
                    + getConfigurationPath(), e);
        }
        return apiElements;
    }

    public OMElement getElement(String name) {
        // todo
        return null;
    }

    public void persistElement(String name, OMElement element, String fileName) {
        if (element.getLocalName().equals(
                XMLConfigConstants.API_ELT.getLocalPart())) {
            if (log.isDebugEnabled()) {
                log.debug("Persisting API: " + name + " to the registry");
            }

            String apiPath = getConfigurationPath() + RegistryConstants.PATH_SEPARATOR + name;

            try {
                persistElement(element, apiPath, fileName);
            } catch (RegistryException e) {
                handleException("Unable to persist the API in the path : " + apiPath, e);
            }
        } else {
            handleException("The element provided to persist is not an API");
        }
    }

    public void deleteElement(String name) {
        String apiPath = getConfigurationPath() + RegistryConstants.PATH_SEPARATOR + name;
        try {
            if (registry.resourceExists(apiPath)) {
                registry.delete(apiPath);
            }
        } catch (RegistryException e) {
            handleException("Error in deleting the API at path : " + apiPath, e);
        }
    }

    protected String getConfigurationPath() {
        return getConfigurationRoot() + RegistryConstants.PATH_SEPARATOR +
                ServiceBusConstants.RegistryStore.REST_API_REGISTRY;
    }
}
