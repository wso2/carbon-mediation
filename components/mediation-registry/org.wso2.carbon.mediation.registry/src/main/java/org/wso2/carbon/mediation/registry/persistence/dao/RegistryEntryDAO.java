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

package org.wso2.carbon.mediation.registry.persistence.dao;

import org.wso2.carbon.mediation.registry.persistence.dataobject.BaseDO;
import org.wso2.carbon.mediation.registry.persistence.dataobject.RegistryEntryDO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;


/**
 *
 */
public class RegistryEntryDAO extends BaseDAO {

    public RegistryEntryDAO(Registry registry) {
        super(registry);
    }

    public void populateResource(Resource resource, BaseDO baseDO) {
        if (baseDO instanceof RegistryEntryDO) {
            RegistryEntryDO registryEntryDO = (RegistryEntryDO) baseDO;
            resource.setProperty(RegistryEntryDO.REGISTRY_KEY, registryEntryDO.getRegistryKey());
            resource.setProperty(RegistryEntryDO.EXPIRY_TIME, String.valueOf(registryEntryDO.getExpiryTime()));
        }
    }

    public BaseDO populateDataObject(Resource resource) {
        RegistryEntryDO registryEntryDO = new RegistryEntryDO();
        registryEntryDO.setExpiryTime(Long.parseLong(resource.getProperty(RegistryEntryDO.EXPIRY_TIME)));
        registryEntryDO.setRegistryKey(resource.getProperty(RegistryEntryDO.REGISTRY_KEY));
        return registryEntryDO;
    }

    public void addRegistryEntry(RegistryEntryDO registryEntryDO) {
        if (registryEntryDO != null && registryEntryDO.getRegistryKey() != null) {
            super.create(registryEntryDO.getRegistryKey(), registryEntryDO);
        } else {
            handleException("Invalid registry entry for add " + registryEntryDO);
        }
    }

    public void updateRegistryEntry(RegistryEntryDO registryEntryDO) {
        if (registryEntryDO != null && registryEntryDO.getRegistryKey() != null) {
            RegistryEntryDO storedEntry = getRegistryEntry(registryEntryDO.getRegistryKey());
            if (storedEntry != null) {
                storedEntry.setExpiryTime(registryEntryDO.getExpiryTime());
                super.update(registryEntryDO.getRegistryKey(), storedEntry);
            }
        } else {
            handleException("Invalid registry entry for update " + registryEntryDO);
        }
    }

    public void saveOrUpdateRegistryEntry(RegistryEntryDO registryEntryDO) {
        if (registryEntryDO != null && registryEntryDO.getRegistryKey() != null) {
            RegistryEntryDO storedEntry = getRegistryEntry(registryEntryDO.getRegistryKey());
            if (storedEntry != null) {
                storedEntry.setExpiryTime(registryEntryDO.getExpiryTime());
                super.update(registryEntryDO.getRegistryKey(), storedEntry);
            } else {
                super.create(registryEntryDO.getRegistryKey(), registryEntryDO);
            }
        } else {
            handleException("Invalid registry entry for save/update " + registryEntryDO);
        }
    }

    public RegistryEntryDO getRegistryEntry(String key) {
        return (RegistryEntryDO) super.get(key);
    }

    public void deleteRegistryEntry(String key) {
        super.delete(key);
    }
}
