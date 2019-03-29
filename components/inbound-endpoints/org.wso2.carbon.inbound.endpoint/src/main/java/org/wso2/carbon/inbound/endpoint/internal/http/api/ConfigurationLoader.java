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

    private static final QName ROOT_Q = new QName("apis");
    private static final QName HANDLER_Q = new QName("api");
    private static final QName CLASS_Q = new QName("class");
    private static final QName NAME_ATT = new QName("name");

    public static List<InternalAPI> loadInternalAPIs() {

        List<InternalAPI> internalApis = new ArrayList<>();
        OMElement apiConfig = MiscellaneousUtil.loadXMLConfig(Constants.INTERNAL_APIS_FILE);
        if (apiConfig != null) {

            if (!ROOT_Q.equals(apiConfig.getQName())) {
                handleException("Invalid internal api configuration file");
            }

            Iterator iterator = apiConfig.getChildrenWithName(HANDLER_Q);
            while (iterator.hasNext()) {
                OMElement handlerElem = (OMElement) iterator.next();

                String name = null;
                if (handlerElem.getAttribute(NAME_ATT) != null) {
                    name = handlerElem.getAttributeValue(NAME_ATT);
                } else {
                    handleException("Name not defined in one or more handlers");
                }

                if (handlerElem.getAttribute(CLASS_Q) != null) {
                    String className = handlerElem.getAttributeValue(CLASS_Q);
                    if (!"".equals(className)) {
                        InternalAPI internalApi = createAPI(className);
                        if (internalApi != null) {
                            internalApis.add(internalApi);
                            internalApi.setName(name);
                        }
                    } else {
                        handleException("Class name is null for Internal InternalAPI name : " + name);
                    }
                } else {
                    handleException("Class name not defined for Internal InternalAPI named : " + name);
                }
            }
        }
        return internalApis;
    }

    private static InternalAPI createAPI(String classFQName) {

        Object obj = null;
        try {
            obj = Class.forName(classFQName).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            handleException("Error creating Internal InternalAPI for class name : " + classFQName, e);
        }

        if (obj instanceof InternalAPI) {
            return (InternalAPI) obj;
        } else {
            handleException("Error creating Internal InternalAPI. The InternalAPI should be of type " +
                    "InternalAPI");
        }
        return null;
    }


    public static int getInternalInboundPort() {

        File synapseProperties = Paths.get(CarbonUtils.getCarbonConfigDirPath(), "synapse.properties").toFile();
        Properties properties = new Properties();
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(synapseProperties);
            properties.load(inputStream);
            inputStream.close();
        } catch (FileNotFoundException e) {
            handleException("synapse.properties file not found", e);
        } catch (IOException e) {
            handleException("Error while reading synapse.properties file", e);
        }
        int internalInboundPort = Constants.DEFAULT_INTERNAL_HTTP_API_PORT;

        String internalInboundEnabledProperty = properties.getProperty(Constants.INTERNAL_HTTP_API_ENABLED);
        if (internalInboundEnabledProperty == null) {
            return -1;
        }
        boolean internalInboundEnabled = Boolean.parseBoolean(internalInboundEnabledProperty);
        if (!internalInboundEnabled) {
            return -1;
        }

        String internalInboundPortProperty =
                properties.getProperty(Constants.INTERNAL_HTTP_API_PORT);
        if (internalInboundPortProperty != null) {
            try {
                internalInboundPort = Integer.parseInt(internalInboundPortProperty);
            } catch (Exception ex) {
                handleException(Constants.INTERNAL_HTTP_API_PORT + " is not in proper format", ex);
            }
        }
        return internalInboundPort;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static void handleException(String msg, Exception ex) {
        log.error(msg, ex);
        throw new SynapseException(msg, ex);
    }
}
