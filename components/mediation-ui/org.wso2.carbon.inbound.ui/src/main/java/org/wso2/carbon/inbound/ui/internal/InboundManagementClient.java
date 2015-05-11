/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.ui.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.inbound.stub.InboundAdminStub;
import org.wso2.carbon.inbound.stub.types.carbon.InboundEndpointDTO;
import org.wso2.carbon.inbound.stub.types.carbon.ParameterDTO;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

public class InboundManagementClient {

    private static final Log log = LogFactory.getLog(InboundManagementClient.class);

    private InboundAdminStub stub;

    private Properties prop = null;

    private InboundManagementClient(String cookie, String backendServerURL,
            ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "InboundAdmin";
        stub = new InboundAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    private void loadProperties() {
        if (prop == null) {
            prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream(
                    "/config/inbound.properties");
            if (is != null) {
                try {
                    prop.load(is);
                } catch (IOException e) {
                    log.error("Unable to load properties.", e);
                }
            }
        }
    }

    public static InboundManagementClient getInstance(ServletConfig config, HttpSession session)
            throws AxisFault {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new InboundManagementClient(cookie, backendServerURL, configContext);
    }

    public List<InboundDescription> getAllInboundDescriptions() throws Exception {

        InboundEndpointDTO[] inboundEndpointDTOs = stub.getAllInboundEndpointNames();
        if (log.isDebugEnabled()) {
            log.debug("All Inbound configurations :" + inboundEndpointDTOs);
        }
        List<InboundDescription> descriptions = new ArrayList<InboundDescription>();
        if (inboundEndpointDTOs == null || inboundEndpointDTOs.length == 0) {
            return descriptions;
        }

        for (InboundEndpointDTO inboundEndpointDTO : inboundEndpointDTOs) {
            InboundDescription inboundDescription = new InboundDescription(
                    inboundEndpointDTO.getName());
            descriptions.add(inboundDescription);
        }
        if (log.isDebugEnabled()) {
            log.debug("All Inbound Descriptions :" + descriptions);
        }
        return descriptions;
    }

    public List<String> getDefaultParameters(String strType) {
        List<String> rtnList = new ArrayList<String>();
        if (!strType.equals(InboundClientConstants.TYPE_HTTP) && !strType.equals(InboundClientConstants.TYPE_HTTPS)) {
            rtnList.addAll(getList("common", true));
        }
        if (!strType.equals(InboundClientConstants.TYPE_CLASS)) {
            rtnList.addAll(getList(strType, true));
        }
        return rtnList;
    }

    public List<String> getAdvParameters(String strType) {
        List<String> rtnList = new ArrayList<String>();
        if (!strType.equals(InboundClientConstants.TYPE_CLASS)) {
            rtnList.addAll(getList(strType, false));
        }
        return rtnList;
    }

    public boolean addInboundEndpoint(String name, String sequence, String onError,
                                      String protocol, String classImpl, List<ParamDTO> lParameters) throws Exception {
        try {
            ParameterDTO[] parameterDTOs = new ParameterDTO[lParameters.size()];
            int i = 0;
            for (ParamDTO parameter : lParameters) {
                ParameterDTO parameterDTO = new ParameterDTO();
                parameterDTO.setName(parameter.getName());
                String strValue = parameter.getValue();
                if(strValue != null && strValue.startsWith(InboundDescription.REGISTRY_KEY_PREFIX)){
               	 parameterDTO.setKey(strValue.replaceFirst(InboundDescription.REGISTRY_KEY_PREFIX, ""));	
                }                 
                parameterDTO.setValue(strValue);
                parameterDTOs[i++] = parameterDTO;
            }
            if (canAdd(name, protocol, parameterDTOs)) {
                stub.addInboundEndpoint(name, sequence, onError, protocol, classImpl, parameterDTOs);
                return true;
            }else {
                log.warn("Cannot add Inbound endpoint " + name + " may be duplicate inbound already exists");
            }
        } catch (Exception e) {
            log.error(e);
            return false;
        }
        return false;
    }

    private List<String> getList(String strProtocol, boolean mandatory) {
        List<String> rtnList = new ArrayList<String>();
        loadProperties();
        if (prop != null) {
            String strKey = strProtocol;
            if (mandatory) {
                strKey += ".mandatory";
            } else {
                strKey += ".optional";
            }
            String strLength = prop.getProperty(strKey);
            Integer iLength = null;
            if (strLength != null) {
                try {
                    iLength = Integer.parseInt(strLength);
                } catch (Exception e) {
                    iLength = null;
                }
            }
            if (iLength != null) {
                for (int i = 1; i <= iLength; i++) {
                    String tmpString = strKey + "." + i;
                    String strVal = prop.getProperty(tmpString);
                    if (strVal != null) {
                        rtnList.add(strVal);
                    }
                }
            }
        }
        return rtnList;
    }

    public boolean removeInboundEndpoint(String name) throws Exception {
        try {
            stub.removeInboundEndpoint(name);
            return true;
        } catch (Exception e) {
            log.error(e);
            return false;
        }
    }

    private boolean canAdd(String name, String protocol, ParameterDTO[] parameterDTOs) {
        try {
            String port = null;
            InboundEndpointDTO[] inboundEndpointDTOs = stub.getAllInboundEndpointNames();
            if(inboundEndpointDTOs != null) {
                for (InboundEndpointDTO inboundEndpointDTO : inboundEndpointDTOs) {
                    if (inboundEndpointDTO.getName().equals(name)) {
                        return false;
                    }
                    if (protocol.equals("http") || protocol.equals("https")) {
                        ParameterDTO[] existparameterDTOs = inboundEndpointDTO.getParameters();
                        for (ParameterDTO parameterDTO : existparameterDTOs) {
                            if (parameterDTO.getName().equals("inbound.http.port")) {
                                port = parameterDTO.getValue();
                            }
                        }
                    }
                }
                if (protocol.equals("http") || protocol.equals("https")) {
                    for (ParameterDTO parameterDTO : parameterDTOs) {
                        if (parameterDTO.getName().equals("inbound.http.port") && parameterDTO.getValue().equals(port)) {
                            log.warn("Already used port " + port + "by another endpoint may be inbound endpoint " + name + " deployment failed");
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
            return false;
        }
        return true;
    }




    public InboundDescription getInboundDescription(String name) {
        try {
            InboundEndpointDTO inboundEndpointDTO = stub.getInboundEndpointbyName(name);
            return new InboundDescription(inboundEndpointDTO);
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public boolean updteInboundEndpoint(String name, String sequence, String onError,
            String protocol, String classImpl, List<ParamDTO> lParameters) throws Exception {
        try {
            ParameterDTO[] parameterDTOs = new ParameterDTO[lParameters.size()];
            int i = 0;
            for (ParamDTO parameter : lParameters) {
                ParameterDTO parameterDTO = new ParameterDTO();
                parameterDTO.setName(parameter.getName());
                String strValue = parameter.getValue();
                if(strValue != null && strValue.startsWith(InboundDescription.REGISTRY_KEY_PREFIX)){
               	 parameterDTO.setKey(strValue.replaceFirst(InboundDescription.REGISTRY_KEY_PREFIX, ""));	
                }  
                parameterDTO.setValue(strValue);	 
                parameterDTOs[i++] = parameterDTO;
            }

            InboundEndpointDTO inboundEndpointDTO = stub.getInboundEndpointbyName(name);
            if(inboundEndpointDTO != null){
                stub.removeInboundEndpoint(name);
            }
            if(canAdd(name,protocol,parameterDTOs)) {
                stub.addInboundEndpoint(name, sequence, onError, protocol, classImpl, parameterDTOs);
                return true;
            }else if(inboundEndpointDTO != null){
                stub.addInboundEndpoint(inboundEndpointDTO.getName(), inboundEndpointDTO.getInjectingSeq(),
                                        inboundEndpointDTO.getOnErrorSeq(), inboundEndpointDTO.getProtocol(),
                                        inboundEndpointDTO.getClassImpl(), inboundEndpointDTO.getParameters());
                return false;
            }
        } catch (Exception e) {
            log.error(e);
            return false;
        }
        return false;
    }

}
