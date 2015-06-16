/*
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

package org.wso2.carbon.mediation.configadmin;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.*;
import org.apache.synapse.config.xml.*;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.configurations.*;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * This is an admin service for managing the SynapseConfiguration of a Carbon server. It can be
 * used to get the current active configuration and make modifications to it on the fly.
 */

@SuppressWarnings({"UnusedDeclaration"})
public class ConfigAdmin extends AbstractServiceBusAdmin {

    final static String PROP_REPORT_CDATA = "http://java.sun.com/xml/stream/properties/report-cdata-event";
    private static final Log log = LogFactory.getLog(ConfigAdmin.class);

    /**
     * Get the current Synapse configuration serialized as an string
     *
     * @return return XML configuration serialized in to a string
     * @throws org.apache.axis2.AxisFault if an error occurs
     */
    public String getConfiguration() throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();

            // ConfigurationFactoryAndSerializerFinder might not have been initialized
            // and hence we need to call the getInstance to load the factories and serializers
            ConfigurationFactoryAndSerializerFinder.getInstance();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            XMLConfigurationSerializer.serializeConfiguration(getSynapseConfiguration(), stream);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            if (factory.isPropertySupported(PROP_REPORT_CDATA)) {
                factory.setProperty(PROP_REPORT_CDATA, Boolean.TRUE);
            }
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    stream.toByteArray());
            org.wso2.carbon.mediation.configadmin.util.XMLPrettyPrinter xmlPrettyPrinter =
                    new org.wso2.carbon.mediation.configadmin.util.XMLPrettyPrinter(byteArrayInputStream);
            return xmlPrettyPrinter.xmlFormatWithComments();

        } catch (XMLStreamException e) {
            handleException("Error serializing the Synapse configuration : Error " + e.getMessage(),
                            e);
        } catch (Exception e) {
            handleException("Error serializing the Synapse configuration : Error " + e.getMessage(),
                            e);
        } finally {
            lock.unlock();
        }
        return "";
    }

    /**
     * Get the list of configurations available
     *
     * @return the list is retrieved from the registry
     * @throws org.apache.axis2.AxisFault if an error occurs
     */
    public ConfigurationInformation[] getConfigurationList() throws AxisFault {
        try {
            List<org.wso2.carbon.mediation.initializer.configurations.ConfigurationInformation>
                    list = getConfigurationManager().getConfigurationsList();

            List<ConfigurationInformation> configList = new ArrayList<ConfigurationInformation>();
            for (org.wso2.carbon.mediation.initializer.configurations.ConfigurationInformation info:
                    list) {
                ConfigurationInformation configInfo = new ConfigurationInformation();
                configInfo.setActive(info.isActive());
                configInfo.setName(info.getName());
                configInfo.setDescription(info.getDescription());

                configList.add(configInfo);
            }
            return configList.toArray(new ConfigurationInformation[configList.size()]);
        } catch (ConfigurationInitilizerException e) {
            handleException("Failed to get the configurations list", e);
        }
        return null;
    }

    /**
     * Delete a specific configuration identified by the name
     *
     * @param name name of the configuration
     * @return true if the configuration is deleted successfully
     * @throws org.apache.axis2.AxisFault if an error occurs
     */
    public boolean deleteConfiguration(String name) throws AxisFault {
        UserRegistry registry = (UserRegistry) getConfigSystemRegistry();
        try {
            getConfigurationManager().delete(name);
        } catch (ConfigurationInitilizerException e) {
            handleException("Error deleting the configuration: " + name, e);
        }
        return false;
    }

    /**
     * Update the active configuration with the new configuration
     *
     * @param configElement a SOAPElement for the configuration
     * @return true if the update is successful
     * @throws org.apache.axis2.AxisFault if an error occurs
     */
    public boolean updateConfiguration(OMElement configElement) throws AxisFault {
        Exception error = null;
        final Lock lock = getLock();
        try {
            lock.lock();
            ConfigurationUpdater updater = new ConfigurationUpdater(getServerContextInformation(),
                    getConfigContext(), getMediationPersistenceManager(),
                    (UserRegistry) getConfigSystemRegistry());
            updater.update(configElement);

            MediationPersistenceManager pm = getMediationPersistenceManager();
            if (pm != null) {
                pm.saveItem(null, ServiceBusConstants.ITEM_TYPE_FULL_CONFIG);
            }

        } catch (Exception e) {
            handleException("Error while updating the Synapse configuration", e);
        } finally {
            lock.unlock();
        }

        return true;
    }

    public ValidationError[] validateConfiguration(OMElement configElement) {
        MediationPersistenceManager pm = getMediationPersistenceManager();
        if (pm != null) {
            String path = getSynapseConfiguration().getPathToConfigFile();
            MultiXMLConfigurationSerializer serializer = new MultiXMLConfigurationSerializer(path);
            if (!serializer.isWritable()) {
                return new ValidationError[] {
                        new ValidationError("Configuration Directory", "Locked by another process")
                };
            }
        }

        ConfigurationValidator validator = new ConfigurationValidator();
        ValidationError[] errors = validator.validate(configElement);
        if (errors != null && errors.length > 0) {
            return errors;
        }
        return null;
    }

    /**
     * Create a new synapse configuration in the specified path. Save the current
     * configuration and destroy it.
     *
     * @param name name of the configuration to be removed
     * @param description description for the configuration
     * @return true if the new configuration creation is successful
     * @throws org.apache.axis2.AxisFault if an error occurs
     */
    public boolean create(String name, String description) throws AxisFault {
        try {
            getConfigurationManager().create(name, description);
            return true;
        } catch (ConfigurationInitilizerException e) {
            handleException("Couldn't initialize the new configuration", e);
        }
        return false;
    }

    /**
     * Add an existing configuration to the ESB configuration management system
     * @param name name of the configuration
     *
     * @return true if the configuration is added successfully
     * @throws org.apache.axis2.AxisFault if an error occurs
     */
    public boolean addExistingConfiguration(String name) throws AxisFault {
        try {
            getConfigurationManager().addExistingConfiguration(name);
            return true;
        } catch (ConfigurationInitilizerException e) {
            handleException("Failed to add the existing configuration: " + name, e);
        }
        return false;
    }

    /**
     * Load the configuration from the given file
     *
     * @param name name of configuration
     * @throws org.apache.axis2.AxisFault if an error occurs
     * @return true if the new configuration is successful created
     */
    public boolean activate(String name) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            getConfigurationManager().activate(name);
        } catch (Exception e) {
            handleException("Error creating a new Synapse configuration", e);
        } finally {
            lock.unlock();
        }

        return false;
    }

    public boolean saveConfigurationToDisk() throws AxisFault {
        if (log.isTraceEnabled()) {
            log.trace("Saving configuration..");
        }

        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration config = getSynapseConfiguration();
            FileOutputStream fos = new FileOutputStream(config.getPathToConfigFile());
            XMLConfigurationSerializer.serializeConfiguration(config, fos);
            try {
                fos.close();
                XMLPrettyPrinter.prettify(new File(config.getPathToConfigFile()));
            } catch (IOException e) {
                // ignore prettify errors
            }
            if (log.isTraceEnabled()) {
                log.trace("Configuration saved to disk");
            }
            return true;
        } catch (XMLStreamException e) {
            handleException("Could not save changes to disk." +
                    e.getMessage() + " Check log for more details", e);
        } catch (FileNotFoundException e) {
            handleException("Could not locate the Synapse configuration file to save changes", e);
        } catch (SynapseException se) {
            handleException("Unable to update the Synapse configuration." +
                    se.getMessage() + " Check log for more details", se);
        } catch (Exception e) {
            handleException("Unable to update the Synapse configuration." +
                    e.getMessage() + " Check log for more details", e);
        } finally {
            lock.unlock();
        }
        return false;
    }

    private ConfigurationManager getConfigurationManager() {
        return (ConfigurationManager)
                getConfigContext().getProperty(ConfigurationManager.CONFIGURATION_MANAGER);
    }

    private String getParameter(String name) {
        String value = System.getProperty(name);
        if (value != null) {
            return value;
        }

        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        return serverConfig.getFirstProperty(name);
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
