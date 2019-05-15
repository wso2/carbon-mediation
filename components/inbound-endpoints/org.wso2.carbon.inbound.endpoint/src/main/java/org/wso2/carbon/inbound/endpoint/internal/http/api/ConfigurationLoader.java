/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.endpoint.internal.http.api;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.util.MiscellaneousUtil;
import org.apache.synapse.transport.passthru.core.ssl.SSLConfiguration;
import org.wso2.carbon.inbound.endpoint.persistence.PersistenceUtils;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * {@code ConfigurationLoader} contains utilities to load configuration file content required for Internal APIs
 * implementation.
 */
public class ConfigurationLoader {

    private static Log log = LogFactory.getLog(ConfigurationLoader.class);

    private static final QName ROOT_Q = new QName("internalApis");
    private static final QName API_Q = new QName("api");
    private static final QName CLASS_Q = new QName("class");
    private static final QName NAME_ATT = new QName("name");
    private static final QName PROTOCOL_Q = new QName("protocol");

    private static final String APIS = "apis";
    private static final String SSL_CONFIG = "sslConfig";
    private static final String KEYSTORE_ATT = "keystore";
    private static final String TRUSTSTORE_ATT = "truststore";
    private static final String SSL_VERIFY_CLIENT_ATT = "sslVerifyClient";
    private static final String SSL_PROTOCOL_ATT = "sslProtocol";
    private static final String HTTPS_PROTOCOLS_ATT = "httpsProtocols";
    private static final String CERTIFICATE_REVOCATION_VERIFIER_ATT = "certificateRevocationVerifier";
    private static final String PREFERRED_CIPHERS_ATT = "preferredCiphers";

    private static SSLConfiguration sslConfiguration;
    private static boolean sslConfiguredSuccessfully;

    private static List<InternalAPI> internalHttpApiList = new ArrayList<>();
    private static List<InternalAPI> internalHttpsApiList = new ArrayList<>();

    private static String internalInboundHttpPortProperty;
    private static String internalInboundHttpsPortProperty;

    private static final int PORT_OFFSET = PersistenceUtils.getPortOffset();

    public static void loadInternalApis(String apiFilePath) {

        OMElement apiConfig = MiscellaneousUtil.loadXMLConfig(apiFilePath);

        if (apiConfig != null) {

            if (!ROOT_Q.equals(apiConfig.getQName())) {
                handleException("Invalid internal api configuration file");
            }

            Iterator apiIterator = apiConfig.getChildrenWithLocalName(APIS);

            if (apiIterator.hasNext()) {

                OMElement apis = (OMElement) apiIterator.next();
                Iterator apiList = apis.getChildrenWithName(API_Q);
                if (apiList != null) {

                    Iterator sslConfigIterator = apiConfig.getChildrenWithLocalName(SSL_CONFIG);
                    if (sslConfigIterator.hasNext()) {
                        sslConfiguration = setSslConfig((OMElement) sslConfigIterator.next());
                    }

                    while (apiList.hasNext()) {

                        OMElement apiElement = (OMElement) apiList.next();
                        String name = null;

                        if (apiElement.getAttribute(NAME_ATT) != null) {
                            name = apiElement.getAttributeValue(NAME_ATT);
                            if (name == null || name.isEmpty()) {
                                handleException("Name not specified in one or more handlers");
                            }
                            String property = Constants.PREFIX_TO_ENABLE_INTERNAL_APIS + name;
                            if (!Boolean.parseBoolean(System.getProperty(property)) && !Boolean
                                    .parseBoolean(System.getenv(property))) {
                                continue;
                            }
                        } else {
                            handleException("Name not defined in one or more handlers");
                        }

                        if (apiElement.getAttribute(CLASS_Q) != null) {

                            String className = apiElement.getAttributeValue(CLASS_Q);
                            if (!className.isEmpty()) {

                                InternalAPI internalApi = createApi(className);
                                internalApi.setName(name);

                                if (apiElement.getAttribute(PROTOCOL_Q) != null) {

                                    String protocols = apiElement.getAttributeValue(PROTOCOL_Q);
                                    if (!protocols.isEmpty()) {

                                        String[] protocolList = protocols.split(" ");
                                        for (String protocol : protocolList) {
                                            switch (protocol) {
                                            case "http":
                                                internalHttpApiList.add(internalApi);
                                                break;
                                            case "https":
                                                internalHttpsApiList.add(internalApi);
                                                break;
                                            default:
                                                handleException("Unsupported Protocol found for Internal API");
                                            }
                                        }

                                    } else {
                                        log.warn("No protocol specified for InternalAPI : " + name
                                                + ". Hence it will not be enabled.");
                                    }
                                } else {
                                    log.warn("Protocol not defined for InternalAPI : " + name
                                            + ". Hence it will not be enabled.");
                                }
                            } else {
                                handleException("Class name is null for Internal InternalAPI name : " + name);
                            }
                        } else {
                            handleException("Class name not defined for Internal InternalAPI named : " + name);
                        }
                    }
                }
            }
        }
    }

    private static InternalAPI createApi(String classFQName) {

        Object obj = null;
        try {
            obj = Class.forName(classFQName).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            handleException("Error creating Internal InternalAPI for class name : " + classFQName, e);
        }

        if (obj instanceof InternalAPI) {
            return (InternalAPI) obj;
        } else {
            handleException("Error creating Internal InternalAPI. The InternalAPI should be of type InternalAPI");
        }
        return null;
    }

    public static int getInternalInboundHttpPort() {

        return getPort(Constants.INTERNAL_HTTP_API_PORT, internalInboundHttpPortProperty,
                Constants.DEFAULT_INTERNAL_HTTP_API_PORT);
    }

    public static int getInternalInboundHttpsPort() {

        return getPort(Constants.INTERNAL_HTTPS_API_PORT, internalInboundHttpsPortProperty,
                Constants.DEFAULT_INTERNAL_HTTPS_API_PORT);
    }

    private static int getPort(String propertyName, String portProperty, int defaultPort) {

        int port = defaultPort;
        if (portProperty != null) {
            try {
                port = Integer.parseInt(portProperty);
            } catch (NumberFormatException ex) {
                handleException(propertyName + " is not in proper format", ex);
            }
        }
        return port + PORT_OFFSET;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static void handleException(String msg, Exception ex) {
        log.error(msg, ex);
        throw new SynapseException(msg, ex);
    }

    public static SSLConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    public static List<InternalAPI> getHttpInternalApis() {
        return internalHttpApiList;
    }

    public static List<InternalAPI> getHttpsInternalApis() {
        return internalHttpsApiList;
    }

    public static boolean isSslConfiguredSuccessfully() {
        return sslConfiguredSuccessfully;
    }

    /**
     * Reads and check from the synapse properties file whether the Internal api is enabled.
     *
     * @return - whether internal api is enabled in synapse properties file.
     */
    public static boolean isInternalApiEnabled() {

        File synapseProperties = Paths.get(CarbonUtils.getCarbonConfigDirPath(), "synapse.properties").toFile();
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(synapseProperties)) {
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            handleException("synapse.properties file not found", e);
        } catch (IOException e) {
            handleException("Error while reading synapse.properties file", e);
        }
        String internalInboundEnabledProperty = properties.getProperty(Constants.INTERNAL_HTTP_API_ENABLED);
        if (internalInboundEnabledProperty == null) {
            return false;
        }
        boolean isEnabled = Boolean.parseBoolean(internalInboundEnabledProperty);
        if (isEnabled) {
            internalInboundHttpPortProperty = properties.getProperty(Constants.INTERNAL_HTTP_API_PORT);
            internalInboundHttpsPortProperty = properties.getProperty(Constants.INTERNAL_HTTPS_API_PORT);
        }
        return isEnabled;
    }

    private static SSLConfiguration setSslConfig(OMElement sslConfig) {

        Iterator iterator = sslConfig.getChildElements();

        String trustStore = null;
        String keyStore = null;
        String clientAuth = null;
        String httpsProtocols = null;
        String revocationVerifier = null;
        String sslProtocol = null;
        String prefferedCiphers = null;

        while (iterator.hasNext()) {

            OMElement parameter = (OMElement) iterator.next();
            String attributeName = parameter.getAttributeValue(NAME_ATT);

            if (parameter.getFirstElement() != null) {

                String value = parameter.getFirstElement().toString();

                switch (attributeName) {
                case KEYSTORE_ATT:
                    keyStore = value;
                    break;
                case TRUSTSTORE_ATT:
                    trustStore = value;
                    break;
                case CERTIFICATE_REVOCATION_VERIFIER_ATT:
                    revocationVerifier = value;
                    break;
                default:
                    handleException("Invalid parameter found for internal API ssl configuration");
                }

            } else {

                String value = parameter.getText();

                switch (attributeName) {
                case SSL_PROTOCOL_ATT:
                    sslProtocol = value;
                    break;
                case PREFERRED_CIPHERS_ATT:
                    prefferedCiphers = value;
                    break;
                case HTTPS_PROTOCOLS_ATT:
                    httpsProtocols = value;
                    break;
                case SSL_VERIFY_CLIENT_ATT:
                    clientAuth = value;
                    break;
                default:
                    handleException("Invalid parameter found for internal API ssl configuration");
                }
            }
        }

        if (keyStore == null) {
            log.error("Keystore must be specified to configure internal Https Api.");
        } else {
            sslConfiguredSuccessfully = true;
        }

        return new SSLConfiguration(keyStore, trustStore, clientAuth, httpsProtocols, revocationVerifier, sslProtocol,
                prefferedCiphers);
    }

    public static void destroy() {
        internalHttpApiList = new ArrayList<>();
        internalHttpsApiList = new ArrayList<>();
    }

}
