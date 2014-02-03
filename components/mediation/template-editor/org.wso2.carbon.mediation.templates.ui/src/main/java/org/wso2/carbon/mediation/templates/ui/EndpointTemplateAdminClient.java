/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mediation.templates.ui;


import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.endpoints.TemplateFactory;
import org.apache.synapse.endpoints.Template;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.mediation.templates.common.EndpointTemplateInfo;
import org.wso2.carbon.mediation.templates.common.TemplateEditorException;
import org.wso2.carbon.mediation.templates.endpoint.stub.types.EndpointTemplateAdminServiceStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EndpointTemplateAdminClient {
    private static final Log log = LogFactory.getLog(EndpointTemplateAdminClient.class);
    private EndpointTemplateAdminServiceStub endpointTemplateAdminStub;


    public EndpointTemplateAdminClient(ServletConfig config, HttpSession session) throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext)
                config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serviceURL = backendServerURL + "EndpointTemplateAdminService";
        endpointTemplateAdminStub = new EndpointTemplateAdminServiceStub(configContext, serviceURL);
        ServiceClient client = endpointTemplateAdminStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    public String[] getAllTempalateNames() throws TemplateEditorException {
        List<String> templateNames = new ArrayList<String>();

        EndpointTemplateInfo[] definedTempalates = getEndpointTemplates(0, getEndpointTemplatesCount());
        if (definedTempalates != null) {
            for (EndpointTemplateInfo definedTempalate : definedTempalates) {
                if (definedTempalate.getTemplateName() != null) {
                    templateNames.add(definedTempalate.getTemplateName());
                }
            }
        }

        EndpointTemplateInfo[] dynamicTempalates = getDynamicEndpointTemplates(0, getDynamicEndpointTemplatesCount());
        if (dynamicTempalates != null) {
            for (EndpointTemplateInfo dynTempalate : dynamicTempalates) {
                if (dynTempalate.getTemplateName() != null) {
                    templateNames.add(dynTempalate.getTemplateName());
                }
            }
        }
        return templateNames.toArray(new String[0]);
    }

    public int getEndpointTemplatesCount() throws TemplateEditorException {
        try {
            return endpointTemplateAdminStub.getEndpointTemplatesCount();
        } catch (Exception e) {
            handleException("Couldn't retrieve the endpoint template count", e);
        }
        return 0;
//        return TemplateTestUtil.getEndpointTemplatesCount();
    }

    public int getDynamicEndpointTemplatesCount() throws TemplateEditorException {
        try {
            return endpointTemplateAdminStub.getDynamicEndpointTemplatesCount();
        } catch (Exception e) {
            handleException("Couldn't retrieve the dynamic endpoint template count", e);
        }
        return 0;

//        return TemplateTestUtil.getDynamicEndpointTemplatesCount();
    }

    public EndpointTemplateInfo[] getEndpointTemplates(int pageNumber, int endpointTemplatesPerPage) throws TemplateEditorException {
        List<EndpointTemplateInfo> templates = new ArrayList<EndpointTemplateInfo>();
        try {
            org.wso2.carbon.mediation.templates.endpoint.stub.types.common.EndpointTemplateInfo[] temp =
                    endpointTemplateAdminStub.getEndpointTemplates(pageNumber, endpointTemplatesPerPage);
            if (temp == null || temp.length == 0 || temp[0] == null) {
                return null;
            }

            for (org.wso2.carbon.mediation.templates.endpoint.stub.types.common.EndpointTemplateInfo info : temp) {
                EndpointTemplateInfo templInfo = new EndpointTemplateInfo();
                templInfo.setTemplateName(info.getTemplateName());
                templInfo.setDescription(info.getDescription());
                templInfo.setEndpointType(info.getEndpointType());
                templates.add(templInfo);
            }
        } catch (Exception e) {
            handleException("Couldn't retrieve the information of the endpoint templates", e);
        }

        if (templates.size() > 0) {
            return templates.toArray(new EndpointTemplateInfo[templates.size()]);
        }
        return null;

//        return TemplateTestUtil.getEndpointTemplates(pageNumber, endpointTemplatesPerPage);
    }

    public EndpointTemplateInfo[] getDynamicEndpointTemplates(int pageNumber, int endpointTemplatesPerPage) throws TemplateEditorException {
        List<EndpointTemplateInfo> templates = new ArrayList<EndpointTemplateInfo>();
        try {
            org.wso2.carbon.mediation.templates.endpoint.stub.types.common.EndpointTemplateInfo[] temp =
                    endpointTemplateAdminStub.getDynamicEndpointTemplates(pageNumber, endpointTemplatesPerPage);
            if (temp == null || temp.length == 0 || temp[0] == null) {
                return null;
            }

            for (org.wso2.carbon.mediation.templates.endpoint.stub.types.common.EndpointTemplateInfo info : temp) {
                EndpointTemplateInfo templInfo = new EndpointTemplateInfo();
                templInfo.setTemplateName(info.getTemplateName());
                templInfo.setDescription(info.getDescription());
                templInfo.setEndpointType(info.getEndpointType());
                templates.add(templInfo);
            }
        } catch (Exception e) {
            handleException("Couldn't retrieve the information of the endpoint templates", e);
        }

        if (templates.size() > 0) {
            return templates.toArray(new EndpointTemplateInfo[templates.size()]);
        }
        return null;


//        return TemplateTestUtil.getDynamicEndpointTemplates(pageNumber, endpointTemplatesPerPage);
    }

    public Template getTempalate(String templateName) throws RemoteException, TemplateEditorException {
        OMElement element = null;
        Template template = null;
        try {
            element = endpointTemplateAdminStub.getTemplate(templateName).getFirstElement();
            template = new TemplateFactory().createEndpointTemplate(element, new Properties());
        } catch (Exception e) {
            handleException("Couldn't retrieve the endpoint template element with name '"
                            + templateName + "'", e);
        }
        return template;

//        return TemplateTestUtil.getTempalate(templateName);
    }

    public void saveTemplate(String templateConfig) throws TemplateEditorException {
        try {
            endpointTemplateAdminStub.saveEndpointTemplate(templateConfig);
        } catch (Exception e) {
            handleException("Error in saving the template with the configuration "
                            ,e);
        }
    }

    public void saveDynamicTemplate(String key,String templateConfig) throws TemplateEditorException {
        try {
            endpointTemplateAdminStub.saveDynamicEndpointTemplate(key, templateConfig);
        } catch (Exception e) {
            handleException("Error in saving the template with the configuration "
                            ,e);
        }
    }

    public void addTemplate(String templateConfig) throws TemplateEditorException {
        try {
            endpointTemplateAdminStub.addEndpointTemplate(templateConfig);
        } catch (Exception e) {
            handleException("Error in adding the template with the configuration "
                            ,e);
        }
    }

    public void addDynamicTemplate(String key,String templateConfig) throws TemplateEditorException {
        try {
            endpointTemplateAdminStub.addDynamicEndpointTemplate(key, templateConfig);
        } catch (Exception e) {
            handleException("Error in adding the template with the configuration "
                            ,e);
        }
    }

    private void handleException(String message, Throwable e) throws TemplateEditorException {
        log.error(message, e);
        throw new TemplateEditorException(message, e);
    }

    public void deleteTemplate(String templateName) throws TemplateEditorException {
        try {
            endpointTemplateAdminStub.deleteEndpointTemplate(templateName);
        } catch (Exception e) {
            handleException("Couldn't delete the endpoint template '" + templateName + "'", e);
        }
    }

    public void deleteDynamicTemplate(String templateName) throws TemplateEditorException {
        try {
            endpointTemplateAdminStub.deleteDynamicEndpointTemplate(templateName);
        } catch (Exception e) {
            handleException("Couldn't delete the endpoint template '" + templateName + "'", e);
        }
    }

   public boolean hasDuplicateTemplateEndpoint(String  templateElementConfig)throws TemplateEditorException {
       try {
           return endpointTemplateAdminStub.hasDuplicateTempleteEndpoint(templateElementConfig);
       } catch (Exception e) {
           handleException("Couldn't Check the endpoint template : ", e);
       }
       return false;
   }



}
