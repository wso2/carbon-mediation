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
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.Collection;

public class MessageStoreRegistryStore extends AbstractRegistryStore{

    public MessageStoreRegistryStore(UserRegistry registry, String configName) {
        super(registry, configName);
        // if Template collection does not exists, create a one
        createCollection(getConfigurationPath());
    }

    @Override
    public Collection<OMElement> getElements() {
        Collection<OMElement> messageStoreElements = new ArrayList<OMElement>();
        try {
            messageStoreElements = getChildElementsInPath(
                    getConfigurationPath());
        } catch (RegistryException e) {
            handleException("Couldn't get the list of Message Stores from the registry in path : "
                    + getConfigurationPath(), e);
        }
        return messageStoreElements;
    }

    @Override
    public OMElement getElement(String name) {
        //TODO
        return null;
    }

    @Override
    public void persistElement(String name, OMElement element, String fileName) {
        if (element.getLocalName().equals(
                XMLConfigConstants.MESSAGE_STORE_ELT.getLocalPart())) {
            if (log.isDebugEnabled()) {
                log.debug("Persisting Message Store : " + name + " to the registry");
            }

            String messageStorePath = getConfigurationPath()
                    + RegistryConstants.PATH_SEPARATOR + name;

            AppDeployerUtils.attachArtifactToOwnerApp(fileName, ServiceBusConstants
                    .MESSAGE_STORE_TYPE, name, registry.getTenantId());

            try {
                persistElement(element, messageStorePath, fileName);
            } catch (RegistryException e) {
                handleException("Unable to persist the Message Store " +
                        " in the path : " + messageStorePath, e);
            }
        } else {
            handleException("The element provided to persist is not a Message Store");
        }
    }

    @Override
    public void deleteElement(String name) {
        String messageStorePath = getConfigurationPath()
                + RegistryConstants.PATH_SEPARATOR + name;
        try {
            if (registry.resourceExists(messageStorePath)) {
                registry.delete(messageStorePath);
            }
        } catch (RegistryException e) {
            handleException("Error in deleting the Message Store at path : " + messageStorePath, e);
        }
    }

    @Override
    protected String getConfigurationPath() {
       return getConfigurationRoot() + RegistryConstants.PATH_SEPARATOR +
                ServiceBusConstants.RegistryStore.MESSAGE_STORE_REGISTRY;
    }
}
