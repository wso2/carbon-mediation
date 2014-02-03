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

package org.wso2.carbon.mediation.initializer.persistence;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.RegistrySerializer;
import org.apache.synapse.registry.Registry;
import org.wso2.carbon.mediation.initializer.persistence.registry.SynapseRegistriesRegistryStore;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.File;

public class SynapseRegistryStore extends AbstractStore<Registry> {

    public SynapseRegistryStore(String configPath, UserRegistry registry, String configName) {
        super(configPath);
        if (registry != null) {
            this.registryStore = new SynapseRegistriesRegistryStore(registry, configName);
        }
    }

    // This is slightly different case.
    // So we have to override a bunch of methods in the super class to implement the
    // case specific behavior.

    @Override
    public void save(String name, SynapseConfiguration config) {
        if (name == null) {
            log.warn("Name of the configuration item is not given");
            return;
        }

        Registry registry = getObjectToPersist(name, config);
        if (registry == null) {
            log.warn("Unable to find the Synapse registry for persistence");
            return;
        }

        OMElement element = null;
        String fileName = null;
        try {
            if (!Boolean.valueOf(config.getProperty(
                    MultiXMLConfigurationBuilder.SEPARATE_REGISTRY_DEFINITION))) {

                serializer.serializeSynapseXML(config);
            } else {
                element = serializer.serializeSynapseRegistry(registry, config, null);
                fileName = MultiXMLConfigurationBuilder.REGISTRY_FILE;
            }

        } catch (Exception e) {
            handleException("Error while saving the mediation registry to the file system", e);
        }

        if (registryStore != null) {
            if (element == null) {
                element = serialize(registry);
            }
            saveToRegistry(name, element, fileName);
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void deleteFile(String fileName, SynapseConfiguration synapseConfiguration) {
        File rootDir = new File(configPath);
        if (!rootDir.exists()) {
            return;
        }

        File registryFile = new File(configPath, fileName);
        registryFile.delete();
    }

    protected String getFileName(Registry registry) {
        return null;
    }

    protected Registry getObjectToPersist(String name, SynapseConfiguration config) {
        return config.getRegistry();
    }

    protected OMElement saveToFile(Registry registry, SynapseConfiguration synapseConfiguration) {
        return null;
    }

    protected OMElement serialize(Registry registry) {
        return RegistrySerializer.serializeRegistry(null, registry);
    }

    @Override
    protected void saveToRegistry(String name, OMElement element, String fileName) {
        SynapseRegistriesRegistryStore synapseRegStore =
                (SynapseRegistriesRegistryStore) registryStore;
        synapseRegStore.persistElement(element, fileName);
    }
}
