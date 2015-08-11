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
import java.rmi.RemoteException;
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
import org.wso2.carbon.inbound.stub.InboundAdminInboundManagementException;
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
        if (!strType.equals(InboundClientConstants.TYPE_HTTP)
                && !strType.equals(InboundClientConstants.TYPE_HTTPS)
                && !strType.equals(InboundClientConstants.TYPE_HL7)) {
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
                                      String protocol, String classImpl, String suspended, List<ParamDTO> lParameters) throws Exception {
        try {
            lParameters = validateParameterList(lParameters);
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
            if (canAdd(name, protocol, parameterDTOs, true)) {
                stub.addInboundEndpoint(name, sequence, onError, protocol, classImpl, suspended, parameterDTOs);
                return true;
            }else {
                log.warn("Cannot add Inbound endpoint " + name + " may be duplicate inbound already exists");
                return false;
            }
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
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
                        if ((strProtocol.equals(InboundClientConstants.TYPE_KAFKA) &&
                                ((mandatory && !strVal.contains("highlevel.") &&
                                        !strVal.contains("simple.")) || !mandatory)) ||
                                !strProtocol.equals(InboundClientConstants.TYPE_KAFKA)) {
                            rtnList.add(strVal);
                        }
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

    /**
     * We can add an endpoint on following criteria:
     * - Protocol can be polling or listener:
     *      - If two endpoints have the same name do not allow.
     * - If protocol is listener:
     *      - If two endpoints have same protocol do no allow.
     * these.
     * @param name
     * @param protocol
     * @param parameterDTOs
     * @param addMode Use when new endpoint is being added (not update).
     * @return boolean on whether endpoint can be added.
     */
    private boolean canAdd(String name, String protocol, ParameterDTO[] parameterDTOs, boolean addMode) {
        try {
            String port = null;
            if (protocol != null && (isListener(protocol))) {
                for(ParameterDTO paramDTO: parameterDTOs) {
                    if(isListenerPortParam(paramDTO.getName())) {
                        Integer.parseInt(paramDTO.getValue());
                    }
                }
            }

            InboundEndpointDTO[] inboundEndpointDTOs = stub.getAllInboundEndpointNames();
            if(inboundEndpointDTOs != null) {
                for (InboundEndpointDTO inboundEndpointDTO : inboundEndpointDTOs) {
                    if (addMode && inboundEndpointDTO.getName().equals(name)) {  // if two names are same, we can't add.
                        return false;
                    }

                    if (!addMode && inboundEndpointDTO.getName().equals(name)
                            && inboundEndpointDTO.getProtocol() != null 
                                    && inboundEndpointDTO.getProtocol().equals(protocol)) { // an update on existing
                        return true;
                    }

                    if (protocol != null && isListener(protocol)) {   // if listener, only allow if no other endpoint has port in use
                        ParameterDTO[] existingParameterDTOs = inboundEndpointDTO.getParameters();
                        for (ParameterDTO parameterDTO : existingParameterDTOs) {
                            if (isListenerPortParam(parameterDTO.getName())) {
                                port = parameterDTO.getValue();
                                if (isListenerPortInUse(port, parameterDTOs)) {
                                    log.warn("Port " + port + " already in use by another endpoint. Inbound endpoint "
                                            + name + " deployment failed");
                                    return false;
                                }
                            }
                        }

                        return true;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Error occured while validating the inbound endpoint.", e);
            return false;
        }
    }

    private boolean isListenerPortInUse(String port, ParameterDTO[] parameterDTOs) {
        for (ParameterDTO parameterDTO : parameterDTOs) {
            if (isListenerPortParam(parameterDTO.getName()) && parameterDTO.getValue().equals(port)) {
                return true;
            }
        }

        return false;
    }

    private boolean isListener(String protocolName) {
        for (String listener : InboundClientConstants.LISTENER_TYPES) {
            if (protocolName.equals(listener)) {
                return true;
            }
        }

        return false;
    }

    private boolean isListenerPortParam(String portParam) {
        for (String listenerPortParam : InboundClientConstants.LISTENER_PORT_PARAMS) {
            if (portParam.equals(listenerPortParam)) {
                return true;
            }
        }

        return false;
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
            String protocol, String classImpl, String suspended, List<ParamDTO> lParameters) throws Exception {
        try {
            lParameters = validateParameterList(lParameters);
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

            if (canAdd(name,protocol,parameterDTOs, false)) {
                stub.updateInboundEndpoint(name, sequence, onError, protocol, classImpl, suspended, parameterDTOs);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    private List<ParamDTO> validateParameterList(List<ParamDTO> paramDTOList) {
        List<ParamDTO> paramDTOs = new ArrayList<ParamDTO>();
        for (ParamDTO paramDTO : paramDTOList) {
            if (paramDTO.getValue() != null && paramDTO.getValue().trim().length() > 0) {
                paramDTOs.add(paramDTO);
            }
        }
        return paramDTOs;
    }

    public String[] getAllInboundNames() {
        String[] inboundNameList = null;
        try {
            InboundEndpointDTO[] inboundEndpointDTOs = stub.getAllInboundEndpointNames();
            if (inboundEndpointDTOs != null) {
                inboundNameList = new String[inboundEndpointDTOs.length];
                if (inboundEndpointDTOs != null) {
                    for (int i = 0; i < inboundEndpointDTOs.length; i++) {
                        inboundNameList[i] = inboundEndpointDTOs[i].getName();
                    }
                }
            }
        } catch (RemoteException e) {
            log.error(e);
        } catch (InboundAdminInboundManagementException e) {
            log.error(e);
        }
        return inboundNameList;
    }

    public String getKAFKASpecialParameters() {
        String specialParamsList = "";
        loadProperties();
        if (prop != null) {
            String strKey = "kafka.mandatory";
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
                    if (strVal.contains("highlevel.") || strVal.contains("simple.")) {
                        if(specialParamsList.equals("")){
                            if (strVal.contains("highlevel.")){
                                specialParamsList = strVal.replace("highlevel.", "");
                            } else {
                                specialParamsList = strVal;
                            }
                        } else {
                            if (strVal.contains("highlevel.")){
                                specialParamsList = strVal.replace("highlevel.", "");
                            } else {
                                specialParamsList = specialParamsList + "," + strVal;
                            }
                        }
                    }
                }
            }
        }
        return specialParamsList;
    }
}