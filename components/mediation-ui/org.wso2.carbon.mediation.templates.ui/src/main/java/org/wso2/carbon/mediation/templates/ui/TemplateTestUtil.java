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

import org.apache.synapse.endpoints.Template;
import org.wso2.carbon.mediation.templates.common.EndpointTemplateInfo;

public class TemplateTestUtil {
    private static int STATIC_ENDPOINT_TEMPLATES = 5;
    private static int DYNAMIC_ENDPOITNT_TEMPLATES = 1;

    public static EndpointTemplateInfo[] getEndpointTemplates(int pageNumber, int endpointTemplatesPerPage) {
        EndpointTemplateInfo[] templates = new EndpointTemplateInfo[STATIC_ENDPOINT_TEMPLATES];

        EndpointTemplateInfo templ1 = new EndpointTemplateInfo();
        templ1.setTemplateName("endp_template_address");
        templ1.setEndpointType("address");

        EndpointTemplateInfo templ2 = new EndpointTemplateInfo();
        templ2.setTemplateName("endp_template_wsdl");
        templ2.setEndpointType("wsdl");

        EndpointTemplateInfo templ3 = new EndpointTemplateInfo();
        templ3.setTemplateName("endp_template_defaut");
        templ3.setEndpointType("default");

        EndpointTemplateInfo templ4 = new EndpointTemplateInfo();
        templ4.setTemplateName("endp_template_loadbalance");
        templ4.setEndpointType("loadbalance");

        EndpointTemplateInfo templ5 = new EndpointTemplateInfo();
        templ5.setTemplateName("endp_template_failover");
        templ5.setEndpointType("failover");


        templates[0] = templ1;
        templates[1] = templ2;
        templates[2] = templ3;
        templates[3] = templ4;
        templates[4] = templ5;
//        return new TemplateInfo[0];
        return templates;
    }

    public static EndpointTemplateInfo[] getDynamicEndpointTemplates(int pageNumber, int endpointTemplatesPerPage) {
        EndpointTemplateInfo[] templates = new EndpointTemplateInfo[DYNAMIC_ENDPOITNT_TEMPLATES];
        EndpointTemplateInfo templ1 = new EndpointTemplateInfo();
        templ1.setTemplateName("dynamic_template1");
        templates[0] = templ1;
//        return new TemplateInfo[0];
        return templates;
    }

    public static int getEndpointTemplatesCount() {
        return STATIC_ENDPOINT_TEMPLATES;
    }

    public static int getDynamicEndpointTemplatesCount() {
        return DYNAMIC_ENDPOITNT_TEMPLATES;
    }

    public static Template getTempalate(String templateName) {
        if (templateName != null) {
            Template test = new Template();
            test.addParameter("ep_param1");
            test.addParameter("ep_param2");
            test.setName(templateName);

            return test;
        }
        return null;
    }


}
