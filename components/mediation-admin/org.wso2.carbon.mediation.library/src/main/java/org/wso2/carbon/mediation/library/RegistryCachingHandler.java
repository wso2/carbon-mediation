/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.library;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.wso2.carbon.mediation.library.util.ConfigHolder;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.List;

/**
 *  Caching handler to remove resources in synapse cache when resources get update.
 */
public class RegistryCachingHandler extends Handler {

    public static final Log log = LogFactory.getLog(org.wso2.carbon.mediation.library.RegistryCachingHandler.class);

    public static final String GOV_REGISTRY_PATH = "/_system/governance";
    public static final String GOV_REGISTRY_PREFIX = "gov:";
    public static final String CONFIG_REGISTRY_PATH = "/_system/config";
    public static final String CONFIG_REGISTRY_PREFIX = "conf:";

    /**
     * Clear cache when adding new resource into the registry.
     *
     * @param requestContext    The context of the request
     * @throws RegistryException    Registry caching exception
     */
    @Override
    public void put(RequestContext requestContext) throws RegistryException {
        resolveRegistryPathAndClear(requestContext);
    }

    /**
     * Clear cache when rename existing resource in registry.
     *
     * @param requestContext    The context of the request
     * @return  Target path         Target path for rename
     * @throws RegistryException    Registry caching exception
     */
    @Override
    public String rename(RequestContext requestContext) throws RegistryException {
        resolveRegistryPathAndClear(requestContext);
        return requestContext.getTargetPath();
    }

    /**
     * Clear cache when resource move in registry.
     *
     * @param requestContext    The context of the request
     * @return  Target path         Target path to move file
     * @throws RegistryException    Registry caching exception
     */
    @Override
    public String move(RequestContext requestContext) throws RegistryException {
        resolveRegistryPathAndClear(requestContext);
        return requestContext.getTargetPath();
    }

    /**
     * Clear cache when resource deleted in registry.
     *
     * @param requestContext        The context of the request
     * @throws RegistryException    Registry caching exception
     */
    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        resolveRegistryPathAndClear(requestContext);
    }

    /**
     * Resolve the path for registry and Clear cache for the given registry path
     *
     * @param requestContext      path to the resource
     */
    private void resolveRegistryPathAndClear(RequestContext requestContext) {
        String resourcePath = requestContext.getResourcePath().getPath();
        ConfigHolder configHolder = ConfigHolder.getInstance();
        if (configHolder.isSynapseConfigurationInitialized()) {
            try {
                SynapseConfiguration synapseConfiguration = configHolder.getSynapseConfiguration();
                if (!resourcePath.isEmpty()) {
                    List<Mount> mounts = requestContext.getRegistry().getRegistryContext().getMounts();
                    for (Mount mount : mounts) {
                        if (resourcePath.startsWith(mount.getTargetPath())) {
                            String key;
                            if (mount.getPath().contains(CONFIG_REGISTRY_PATH)) {
                                key = CONFIG_REGISTRY_PREFIX + resourcePath.substring(mount.getTargetPath().length());
                                Object entry = synapseConfiguration.getEntry(key);
                                if (entry != null) {
                                    removeEntry(synapseConfiguration, key);
                                }
                                break;
                            } else if (mount.getPath().contains(GOV_REGISTRY_PATH)) {
                                key = GOV_REGISTRY_PREFIX + resourcePath.substring(mount.getTargetPath().length());
                                Object entry = synapseConfiguration.getEntry(key);
                                if (entry != null) {
                                    removeEntry(synapseConfiguration, key);
                                }
                                break;
                            } else {
                                log.warn("Given path is invalid to clear Synapse cache " + resourcePath);
                            }
                        }
                    }
                    if (mounts.size() == 0) {
                        String key = "";
                        if (resourcePath.startsWith(CONFIG_REGISTRY_PATH)) {
                            key = CONFIG_REGISTRY_PREFIX + resourcePath.substring(CONFIG_REGISTRY_PATH.length());
                            Object entry = synapseConfiguration.getEntry(key);
                            if (entry != null) {
                                removeEntry(synapseConfiguration, key);
                            }
                        } else if (resourcePath.startsWith(GOV_REGISTRY_PATH)) {
                            key = GOV_REGISTRY_PREFIX + resourcePath.substring(GOV_REGISTRY_PATH.length());
                            Object entry = synapseConfiguration.getEntry(key);
                            if (entry != null) {
                                removeEntry(synapseConfiguration, key);
                            }
                        } else {
                            log.warn("Given path is invalid to clear Synapse cache " + resourcePath);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error while loading synapse configurations for clear cache in " + resourcePath, e);
            }
        }
    }

    /**
     * Remove entry from synapse cache by given key
     *
     * @param synapseConfiguration      Synapse configuration object
     * @param key                       Key to delete
     */
    private void removeEntry(SynapseConfiguration synapseConfiguration, String key) {
        Entry entry = synapseConfiguration.getEntryDefinition(key);
        if (entry != null && entry.isCached()) {
            synapseConfiguration.removeEntry(key);
        }

    }
}
