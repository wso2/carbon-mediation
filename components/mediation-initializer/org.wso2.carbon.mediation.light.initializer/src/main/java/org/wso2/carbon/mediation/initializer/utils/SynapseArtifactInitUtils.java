/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediation.initializer.utils;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.SynapseImportSerializer;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.xml.namespace.QName;

/**
 * Utility class containing util functions to initialize synapse artifacts
 */
public class SynapseArtifactInitUtils {

    private static final Log log = LogFactory.getLog(SynapseArtifactInitUtils.class);

    /**
     * Function to create synapse imports to enable installed connectors
     *
     * @param axisConfiguration axis configuration
     */
    public static void initializeConnectors (AxisConfiguration axisConfiguration) {
        String synapseLibPath = axisConfiguration.getRepository().getPath() +
                                        File.separator + ServiceBusConstants.SYNAPSE_LIB_CONFIGS;
        File synapseLibDir = new File(synapseLibPath);
        if (synapseLibDir.exists() && synapseLibDir.isDirectory()) {
            File[] connectorList = synapseLibDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".zip");
                }
            });

            if (connectorList == null) {
                // No connectors found
                return;
            }

            //Check import related to the connector is available
            String importConfigDirPath = axisConfiguration.getRepository().getPath() +
                    ServiceBusConstants.SYNAPSE_IMPORTS_CONFIG_PATH;
            File importsDir = new File(importConfigDirPath);

            if (!importsDir.exists() && !importsDir.mkdirs()) {
                log.error("Import synapse config directory does not exists and unable to create: " +
                        importsDir.getAbsolutePath());
                // Retrying the same for other connectors is waste
                return;
            }

            for (File connectorZip : connectorList) {
                if (log.isDebugEnabled()) {
                    log.debug("Generating import for connector deployed with package: " + connectorZip.getName());
                }
                // Retrieve connector name
                String connectorName = connectorZip.getName().substring(0, connectorZip.getName().indexOf('-'));
                QName qualifiedName = new QName(ServiceBusConstants.SYNAPSE_CONNECTOR_PACKAGE, connectorName);
                File importFile = new File(importsDir, qualifiedName.toString() + ".xml");

                if (!importFile.exists()) {
                    // Import file enabling file connector not available in synapse imports directory
                    if (log.isDebugEnabled()) {
                        log.debug("Generating import config to enable connector: " + qualifiedName);
                    }
                    generateImportConfig(qualifiedName, importFile);
                }
            }
        }
    }

    /**
     * Function to create import configuration enabling connector
     *
     * @param qualifiedName
     * @param targetImportFile
     */
    private static void generateImportConfig (QName qualifiedName, File targetImportFile) {
        SynapseImport synImport = new SynapseImport();
        synImport.setLibName(qualifiedName.getLocalPart());
        synImport.setLibPackage(qualifiedName.getNamespaceURI());
        synImport.setStatus(true);
        OMElement impEl = SynapseImportSerializer.serializeImport(synImport);

        if (impEl != null) {
            try (FileWriter fileWriter = new FileWriter(targetImportFile)) {
                fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + impEl.toString());
            } catch (IOException e) {
                log.error("Error occurred while writing import file: " + qualifiedName);
            }
        } else {
            log.error("Could not add Synapse Import. Invalid import params for libName : " +
                    qualifiedName.getLocalPart() + " packageName : " + qualifiedName.getNamespaceURI());
        }
    }
}
