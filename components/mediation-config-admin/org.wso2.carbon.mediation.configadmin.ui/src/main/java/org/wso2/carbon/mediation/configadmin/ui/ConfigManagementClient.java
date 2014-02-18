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

package org.wso2.carbon.mediation.configadmin.ui;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;
import org.wso2.carbon.mediation.configadmin.stub.types.carbon.ConfigurationInformation;
import org.wso2.carbon.mediation.configadmin.stub.types.carbon.ValidationError;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 */
public class ConfigManagementClient {

    private static final Log log = LogFactory.getLog(ConfigManagementClient.class);

    private ConfigServiceAdminStub stub;

    private ConfigManagementClient(String cookie,
                                   String backendServerURL,
                                   ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "ConfigServiceAdmin";
        stub = new ConfigServiceAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
		option.setTimeOutInMilliSeconds(15 * 60 * 1000);
        option.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        option.setProperty(HTTPConstants.CONNECTION_TIMEOUT,15 * 60 * 1000);
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    public static ConfigManagementClient getInstance(ServletConfig config, HttpSession session) throws AxisFault {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new ConfigManagementClient(cookie, backendServerURL, configContext);
    }

    public ResponseInformation getConfiguration(HttpSession session) {
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            String config = stub.getConfiguration();
            responseInformation.setResult(config);
            ConfigManagementClientUtils.setCachedConfiguration(config, session);
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;
    }

    public ResponseInformation updateConfiguration(String configElement, HttpSession session) {
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            stub.updateConfiguration(createOMElement(configElement));
            ConfigManagementClientUtils.setCachedConfiguration(configElement, session);
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;
    }

    public ResponseInformation validateConfiguration(String configElement) {
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            ValidationError[] errors = stub.validateConfiguration(createOMElement(configElement));
            if (errors == null || errors.length == 0 || errors[0] == null) {
                responseInformation.setResult(null);
            } else {
                responseInformation.setResult(errors);
            }
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;
    }

    public ResponseInformation saveConfigurationToDisk() {
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            stub.saveConfigurationToDisk();
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;
    }

    public ResponseInformation getConfigurations() {
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            ConfigurationInformation[]list = stub.getConfigurationList();

            List<ConfigurationInformation> configList = new ArrayList<ConfigurationInformation>();
            if (list != null) {
                if (list.length > 1 || (list.length == 1 && list[0] != null)) {
                    Collections.addAll(configList, list);
                }
            }
            responseInformation.setResult(configList);
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }         
        return responseInformation;
    }

    public ResponseInformation deleteConfiguration(String name) {
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            stub.deleteConfiguration(name);
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;
    }

    public ResponseInformation loadConfiguration(String name) {
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            stub.activate(name);
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;
    }

    public ResponseInformation newConfiguration(String name, String description) {
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            stub.create(name, description);
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;
    }

    public ResponseInformation addConfiguration(String name) {
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            stub.addExistingConfiguration(name);
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;
    }

    private static OMElement createOMElement(String xml) throws ServletException {
        try {

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();

        } catch (XMLStreamException e) {
            handleException("Invalid XML " + xml);
        }
        return null;
    }

    private static void handleException(String msg) throws ServletException {
        log.error(msg);
        throw new ServletException(msg);
    }

}
