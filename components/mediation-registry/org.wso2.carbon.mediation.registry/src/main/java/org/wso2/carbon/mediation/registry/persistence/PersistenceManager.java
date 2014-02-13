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

package org.wso2.carbon.mediation.registry.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.registry.ESBRegistryConstants;
import org.wso2.carbon.mediation.registry.persistence.dao.RegistryEntryDAO;
import org.wso2.carbon.mediation.registry.persistence.dataobject.RegistryEntryDO;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 *
 */
public class PersistenceManager {

    private static final PersistenceManager PERSISTENCE_MANAGER = new PersistenceManager();
    private final static Log log = LogFactory.getLog(PersistenceManager.class);
    private Registry registry;

    private PersistenceManager() {
    }

    public static PersistenceManager getInstance() {
        return PERSISTENCE_MANAGER;
    }

    /* Data access methods for ESB registry */
    public void addRegistryEntry(RegistryEntryDO registryEntryDO) {
        new RegistryEntryDAO(registry).addRegistryEntry(registryEntryDO);
    }

    public void updateRegistryEntry(RegistryEntryDO registryEntryDO) {
        new RegistryEntryDAO(registry).updateRegistryEntry(registryEntryDO);
    }

    public void saveOrUpdateRegistryEntry(RegistryEntryDO registryEntryDO) {
        new RegistryEntryDAO(registry).saveOrUpdateRegistryEntry(registryEntryDO);
    }

    public RegistryEntryDO getRegistryEntry(String key) {
        return new RegistryEntryDAO(registry).getRegistryEntry(key);
    }

    public void deleteRegistryEntry(String key) {
        new RegistryEntryDAO(registry).deleteRegistryEntry(key);
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
        try {
            if (!registry.resourceExists(ESBRegistryConstants.ROOT_PATH)) {
                CollectionImpl collection = new CollectionImpl();
                collection.setPath(ESBRegistryConstants.ROOT_PATH);
                registry.put(ESBRegistryConstants.ROOT_PATH, collection);
            }
        } catch (RegistryException e) {
            handleException("Error during initiating 'PersistenceManager'", e);
        }
    }

    private static void handleException(String msg, Throwable throwable) {
        log.error(msg, throwable);
        throw new RuntimeException(msg, throwable);
    }
}
