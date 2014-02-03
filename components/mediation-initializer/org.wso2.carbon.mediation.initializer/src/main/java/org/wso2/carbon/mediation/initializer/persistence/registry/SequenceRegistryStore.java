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
public class SequenceRegistryStore extends AbstractRegistryStore {

    public SequenceRegistryStore(UserRegistry registry, String configName) {
        super(registry, configName);
        // if sequences collection does not exists, create a one
        createCollection(getConfigurationPath());
    }

    public Collection<OMElement> getElements() {
        Collection<OMElement> sequenceElements = new ArrayList<OMElement>();
        try {
            sequenceElements = getChildElementsInPath(
                    getConfigurationPath());
        } catch (RegistryException e) {
            handleException("Couldn't get the list of sequences from the registry in path : "
                    + getConfigurationPath(), e);
        }
        return sequenceElements;
    }

    public OMElement getElement(String name) {
        // todo
        return null;
    }

    public void persistElement(String name, OMElement element, String fileName) {
        if (element.getLocalName().equals(
                XMLConfigConstants.SEQUENCE_ELT.getLocalPart())) {
            if (log.isDebugEnabled()) {
                log.debug("Persisting sequence : " + name + " to the registry");
            }

            String sequencePath = getConfigurationPath()
                    + RegistryConstants.PATH_SEPARATOR + name;

            AppDeployerUtils.attachArtifactToOwnerApp(fileName, ServiceBusConstants
                    .SEQUENCE_TYPE, name, registry.getTenantId());

            try {
                persistElement(element, sequencePath, fileName);
            } catch (RegistryException e) {
                handleException("Unable to persist the sequence in the path : " + sequencePath, e);
            }
        } else {
            handleException("The element provided to persist is not a sequence");
        }
    }

    public void deleteElement(String name) {
        String sequencePath = getConfigurationPath()
                + RegistryConstants.PATH_SEPARATOR + name;
        try {
            if (registry.resourceExists(sequencePath)) {
                registry.delete(sequencePath);
            }
        } catch (RegistryException e) {
            handleException("Error in deleting the sequence at path : " + sequencePath, e);
        }
    }

    protected String getConfigurationPath() {
         return getConfigurationRoot() + RegistryConstants.PATH_SEPARATOR +
                ServiceBusConstants.RegistryStore.SEQUENCE_REGISTRY;
    }
}
