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
package org.wso2.carbon.mediator.calltemplate.client;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.mediation.templates.stub.types.TemplateAdminServiceStub;
import org.wso2.carbon.mediation.templates.stub.types.common.TemplateInfo;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;

public class TemplateAdminClient {
    public static final QName PARAMETER_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "parameter");
    public static final QName NAME_Q = new QName("name");
    private TemplateAdminServiceStub templateAdminStub;

    public TemplateAdminClient(ServletConfig config, HttpSession session) throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext)
                config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serviceURL = backendServerURL + "TemplateAdminService";
        templateAdminStub = new TemplateAdminServiceStub(configContext, serviceURL);
        ServiceClient client = templateAdminStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String[] getAllTempalateNames() throws CallTemplateUIException {
        String[] staticTemplateNames = new String[0];
        String[] dynamicTemplateNames = new String[0];
        try {
            staticTemplateNames = getStaticTemplateNames();
        } catch (Exception e) {
            handleException("CallTemplateUI Couldn't retrieve info on static Template Names ", e);
        }
        try {
            dynamicTemplateNames = getDynamicTemplateNames();
        } catch (Exception e) {
            handleException("CallTemplateUI Couldn't retrieve info on dynamic Tempalate Names ", e);
        }
        return merge(staticTemplateNames, dynamicTemplateNames);

    }

    /**
     * This method will try to derive the parameter names for a given template name
     *
     * @param tempalteName
     * @return parameter name string, string will be on the format og p1;p2;p3 ...
     * @throws CallTemplateUIException
     */
    public String getParameterStringForTemplate(String tempalteName) throws CallTemplateUIException {
        OMElement mediatorElement = null;
        try {
            mediatorElement = templateAdminStub.getTemplate(tempalteName).getFirstElement();
        } catch (Exception e) {
            handleException("CallTemplateUI Couldn't retrieve the template element with name '"
                            + tempalteName + "'", e);
        }
        if (mediatorElement != null) {
            return getTempalateParamsString(mediatorElement);
        } else {
            try {
                mediatorElement = templateAdminStub.getDynamicTemplate(tempalteName).getFirstElement();
            } catch (Exception e) {
                handleException("CallTemplateUI Couldn't retrieve the template element with name '"
                                + tempalteName + "'", e);
            }
            return getTempalateParamsString(mediatorElement);
        }
    }


    private static String getTempalateParamsString(OMElement templateElem) {
        Iterator subElements = templateElem.getChildElements();
        String templateParamsStr = "";
        while (subElements.hasNext()) {
            OMElement child = (OMElement) subElements.next();
            if (child.getQName().equals(PARAMETER_Q)) {
                OMAttribute paramNameAttr = child.getAttribute(NAME_Q);
                if (paramNameAttr != null) {
                    templateParamsStr = templateParamsStr + paramNameAttr.getAttributeValue() + ";";
                }
            }
        }
        return templateParamsStr;
    }


    private static String[] merge(String[] first, String[] second) {
        String[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    private String[] getStaticTemplateNames() throws RemoteException {
        int staticTemplateCount = templateAdminStub.getTemplatesCount();
        TemplateInfo[] temp1 =
                templateAdminStub.getTemplates(0, staticTemplateCount);
        String[] staticTemplateNames;
        if (temp1 == null || temp1.length == 0 || temp1[0] == null) {
            staticTemplateNames = new String[0];
        } else {
            staticTemplateNames = new String[temp1.length];
            int i = 0;
            for (TemplateInfo templateInfo : temp1) {
                staticTemplateNames[i] = templateInfo.getName();
                i++;
            }
        }
        return staticTemplateNames;
    }

    private String[] getDynamicTemplateNames() throws RemoteException {
        int dynamicTemplateCount = templateAdminStub.getDynamicTemplateCount();
        TemplateInfo[] temp1 =
                templateAdminStub.getDynamicTemplates(0, dynamicTemplateCount);
        String[] dynamicTemplateNames;
        if (temp1 == null || temp1.length == 0 || temp1[0] == null) {
            dynamicTemplateNames = new String[0];
        } else {
            dynamicTemplateNames = new String[temp1.length];
            int i = 0;
            for (TemplateInfo templateInfo : temp1) {
                dynamicTemplateNames[i] = templateInfo.getName();
                i++;
            }
        }
        return dynamicTemplateNames;
    }

    private void handleException(String message, Throwable e) throws CallTemplateUIException {
//        log.error(message, e);
        throw new CallTemplateUIException(message, e);
    }

}
