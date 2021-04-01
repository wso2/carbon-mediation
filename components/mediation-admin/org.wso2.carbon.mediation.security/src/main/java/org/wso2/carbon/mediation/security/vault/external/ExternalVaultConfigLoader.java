/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediation.security.vault.external;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * {@code ExternalVaultLoader} contains utilities to load configuration file content required for External vault loader
 * implementation.
 */
public class ExternalVaultConfigLoader {

    private static Log log = LogFactory.getLog(ExternalVaultConfigLoader.class);

    private static final QName ROOT_Q = new QName("secureVaults");
    private static final QName VAULT_NAME_Q = new QName("name");

    private static final String EXTERNAL_VAULTS = "external-vaults.xml";

    private static SecretResolver secretResolver;

    private static Map<String, Map<String, String>> externalVaultMap = new HashMap<>();

    private ExternalVaultConfigLoader() {}

    /**
     * Reads the external-vaults.xml file located in conf/security dir and loads to the memory.
     *
     * @param secretCallbackHandlerService secret resolver to resolve the cipher-tool encrypted secrets
     */
    public static void loadExternalVaultConfigs(SecretCallbackHandlerService secretCallbackHandlerService)
            throws ExternalVaultException {
        String vaultConfigFilePath = CarbonUtils.getCarbonSecurityConfigDirPath() + File.separator + EXTERNAL_VAULTS;
        OMElement vaultConfig = null;

        try {
            File externalVaultFile = new File(vaultConfigFilePath);
            if (externalVaultFile.exists()) {
                String configsFileAsString = FileUtils.readFileToString(externalVaultFile);
                vaultConfig = AXIOMUtil.stringToOM(configsFileAsString);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No such file: " + EXTERNAL_VAULTS + " in location " + vaultConfigFilePath);
                }
            }
        } catch (IOException| XMLStreamException e) {
            log.error("Error while reading the " + EXTERNAL_VAULTS + " file in location + "
                    + vaultConfigFilePath, e);
        }

        if (vaultConfig != null) {
            if (!ROOT_Q.equals(vaultConfig.getQName())) {
                throw new ExternalVaultException("Invalid external secure vault configuration file");
            }
            setSecretResolver(vaultConfig);

            Iterator vaultIterator =  vaultConfig.getChildElements();
            while (vaultIterator.hasNext()) {
                OMElement vaultNode = (OMElement) vaultIterator.next();
                if (vaultNode != null) {
                    Iterator vaultNodeIterator =  vaultNode.getChildElements();
                    Map<String, String> childParameters = new HashMap<>();
                    while (vaultNodeIterator.hasNext()) {
                        OMElement vaultChildNode = (OMElement) vaultNodeIterator.next();
                        if (vaultChildNode != null) {
                            String resolvedValue = MiscellaneousUtil.resolve(vaultChildNode, secretResolver);
                            childParameters.put(vaultChildNode.getAttributeValue(VAULT_NAME_Q), resolvedValue);
                        }
                    }
                    externalVaultMap.put(vaultNode.getAttributeValue(VAULT_NAME_Q), childParameters);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug(EXTERNAL_VAULTS + " file external secure vault configurations loaded to the map");
            }
        }
    }

    /**
     * Get the external vault configuration map based on the given vault name.
     *
     * @param name vault name
     */
    public static Map<String, String> getVaultParameters(String name) {
        return externalVaultMap.get(name);
    }

    /**
     * Sets the SecretResolver the document OMElement.
     *
     * @param rootElement Document OMElement
     */
    private static void setSecretResolver(OMElement rootElement) {
        secretResolver = SecretResolverFactory.create(rootElement, true);
    }
}
