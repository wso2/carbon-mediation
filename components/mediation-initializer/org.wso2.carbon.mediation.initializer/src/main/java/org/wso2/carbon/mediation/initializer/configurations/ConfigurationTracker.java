/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.mediation.initializer.configurations;

import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * This class is to hold the information about multiple configurations.
 */
public class ConfigurationTracker {

    public static final Log log = LogFactory.getLog(ConfigurationTracker.class);
    
    public static final String CONFIGURATION_TRACKER = "configurationTracker";

    /** Registry */
    private UserRegistry registry = null;

    /** The current configuration which is active */
    private ConfigurationInformation currentConfig = null;

    public ConfigurationTracker(UserRegistry configRegistry) {
        this.registry = configRegistry;
    }

    public String getCurrentConfigurationName() {
        return currentConfig.getName();
    }

    public synchronized void create(String configurationName, String description)
            throws RegistryException {
        if (!registry.resourceExists(ServiceBusConstants.ESB_CONFIGURATIONS)) {
            init();
        }

        try {
            registry.beginTransaction();
            // add the default configuration
            Resource r = registry.newResource();
            r.setProperty(ServiceBusConstants.DESCRIPTION, description);
            r.setProperty(ServiceBusConstants.ACTIVE, "false");
            r.setProperty(ServiceBusConstants.CREATED, "false");
            registry.put(ServiceBusConstants.ESB_CONFIGURATIONS + RegistryConstants.PATH_SEPARATOR
                    + configurationName, r);
            registry.commitTransaction();
        } catch (Exception e) {
            rollbackTransaction(e);
        }
    }

    private void rollbackTransaction(Exception e) throws RegistryException {
        log.error("Error occurred accessing registry", e);
        registry.rollbackTransaction();
    }

    public synchronized void activate(String configurationName)
            throws RegistryException {
        currentConfig.setActive(false);

        updateConfiguration(currentConfig);

        ConfigurationInformation newConfigurationInfo =
                loadConfigurationInfo(configurationName);
        newConfigurationInfo.setActive(true);

        updateConfiguration(newConfigurationInfo);

        currentConfig = newConfigurationInfo;
        currentConfig.setCreated(true);
    }

    public void delete(String configurationName)
            throws RegistryException {
        try {
            registry.beginTransaction();
            String resourceName = ServiceBusConstants.ESB_CONFIGURATIONS +
                    RegistryConstants.PATH_SEPARATOR + configurationName;
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
            registry.commitTransaction();
        } catch (Exception e) {
            rollbackTransaction(e);
        }
    }

    public List<ConfigurationInformation> getConfigurationList() throws RegistryException {
        if (!registry.resourceExists(ServiceBusConstants.ESB_CONFIGURATIONS)) {
            init();
        }
        List<ConfigurationInformation> configList = new ArrayList<ConfigurationInformation>();

        Resource resource = registry.get(ServiceBusConstants.ESB_CONFIGURATIONS);
        if (resource instanceof org.wso2.carbon.registry.core.Collection) {
            org.wso2.carbon.registry.core.Collection c =
                    (org.wso2.carbon.registry.core.Collection) resource;

            for (String rName : c.getChildren()) {
                Resource r = registry.get(rName);

                String description = r.getProperty(ServiceBusConstants.DESCRIPTION);
                String name = rName.substring(ServiceBusConstants.ESB_CONFIGURATIONS.length() + 2);
                String active = r.getProperty(ServiceBusConstants.ACTIVE);
                String created = r.getProperty(ServiceBusConstants.CREATED);

                ConfigurationInformation config = new ConfigurationInformation(name);

                config.setDescription(description);
                if (active != null &&
                        (active.equalsIgnoreCase("TRUE") || active.equals("FALSE"))) {
                    config.setActive(Boolean.parseBoolean(active));
                }

                config.setCreated(Boolean.parseBoolean(created));
                configList.add(config);
            }
        }

        return configList;
    }

    public boolean isConfigurationCreated(String name) throws RegistryException {
        Resource resource = registry.get(ServiceBusConstants.ESB_CONFIGURATIONS +
                RegistryConstants.PATH_SEPARATOR + name);
        String created = resource.getProperty(ServiceBusConstants.CREATED);

        if (created != null) {
            if (created.equalsIgnoreCase("FALSE")) {
                return false;
            } else if (created.equalsIgnoreCase("TRUE")) {
                return true;
            }
        }

        return false;
    }

    public boolean isConfigurationExists(String name) throws RegistryException {
        return registry.resourceExists(ServiceBusConstants.ESB_CONFIGURATIONS +
                RegistryConstants.PATH_SEPARATOR + name);
    }

    public void init() throws RegistryException {
        // if the configutation information is not in the registry create one
        if (!registry.resourceExists(ServiceBusConstants.ESB_CONFIGURATIONS)) {
            createConfigurationsCollection();

            persistConfiguration("default");
        }

        List<ConfigurationInformation> configurationInformations = getConfigurationList();

        for (ConfigurationInformation info : configurationInformations) {
            if (info.isActive()) {
                currentConfig = info;
            }
        }

        if (currentConfig == null) {
            currentConfig = configurationInformations.get(0);
            log.warn("No Current configurations found. Activating the :" + currentConfig.getName());
        }
    }

    public void update(String name, boolean created) throws RegistryException {
        ConfigurationInformation configurationInformation = loadConfigurationInfo(name);

        configurationInformation.setCreated(created);

        updateConfiguration(configurationInformation);
    }

    public ConfigurationInformation getConfigurationInformation(String name)
            throws RegistryException {
        return loadConfigurationInfo(name);    
    }

    private void createConfigurationsCollection() throws RegistryException {
        try {
            registry.beginTransaction();
            org.wso2.carbon.registry.core.Collection c = registry.newCollection();
            // add the collection
            registry.put(ServiceBusConstants.ESB_CONFIGURATIONS, c);
            registry.commitTransaction();
        } catch (Exception e) {
            rollbackTransaction(e);
        }
    }

    private void updateConfiguration(ConfigurationInformation configurationInformation)
            throws RegistryException {

        try {
            registry.beginTransaction();
            Resource r = registry.get(ServiceBusConstants.ESB_CONFIGURATIONS +
                    RegistryConstants.PATH_SEPARATOR + configurationInformation.getName());

            r.setProperty(ServiceBusConstants.DESCRIPTION,
                    configurationInformation.getDescription());
            r.setProperty(ServiceBusConstants.ACTIVE,
                    Boolean.toString(configurationInformation.isActive()));
            r.setProperty(ServiceBusConstants.CREATED,
                    Boolean.toString(configurationInformation.isCreated()));

            registry.delete(ServiceBusConstants.ESB_CONFIGURATIONS +
                    RegistryConstants.PATH_SEPARATOR + configurationInformation.getName());

            registry.put(ServiceBusConstants.ESB_CONFIGURATIONS +
                    RegistryConstants.PATH_SEPARATOR + configurationInformation.getName(), r);
            registry.commitTransaction();
        } catch (Exception e) {
            rollbackTransaction(e);
        }
    }

    private void persistConfiguration(String configName) throws RegistryException {
        registry.beginTransaction();

        try {
            // add the default configuration
            Resource r = registry.newResource();
            r.setProperty(ServiceBusConstants.DESCRIPTION, "Default Configuration");
            r.setProperty(ServiceBusConstants.ACTIVE, "true");
            r.setProperty(ServiceBusConstants.CREATED, "true");
            registry.put(ServiceBusConstants.ESB_CONFIGURATIONS +
                    RegistryConstants.PATH_SEPARATOR + configName, r);
            registry.commitTransaction();
        } catch (Exception e) {
            rollbackTransaction(e);
        }
    }

    private ConfigurationInformation loadConfigurationInfo(String configName)
            throws RegistryException {
        ConfigurationInformation configurationInformation = null;
        try {
            registry.beginTransaction();
            Resource r = registry.get(ServiceBusConstants.ESB_CONFIGURATIONS +
                    RegistryConstants.PATH_SEPARATOR + configName);

            String description = r.getProperty(ServiceBusConstants.DESCRIPTION);
            String active = r.getProperty(ServiceBusConstants.ACTIVE);
            String created = r.getProperty(ServiceBusConstants.CREATED);

            configurationInformation =
                    new ConfigurationInformation(configName);

            configurationInformation.setActive(Boolean.parseBoolean(active));
            configurationInformation.setCreated(Boolean.parseBoolean(created));
            configurationInformation.setDescription(description);
            registry.commitTransaction();
        } catch (Exception e) {
            rollbackTransaction(e);
        }
        return configurationInformation;
    }
}
