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
package org.wso2.carbon.mediation.templates.ui.factory;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.mediation.service.templates.TemplateMediator;
import org.wso2.carbon.mediation.templates.ui.TemplateAdminClientAdapter;
import org.wso2.carbon.mediator.service.ui.Mediator;
import org.wso2.carbon.sequences.ui.client.EditorUIClient;
import org.wso2.carbon.sequences.ui.factory.EditorUIClientFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class TemplateEditorClientFactory extends EditorUIClientFactory{
    private static Map<String, String> meta = new HashMap<String, String>();

    static{
        meta.put("editorMode","template");
        meta.put("forwardPage","../templates/list_templates.jsp");
        meta.put("sequence.edit.text","template.edit.text");
        meta.put("sequence.design.text","template.design.text");
        meta.put("sequence.design.view.text","template.design.view.text");
        meta.put("sequence.name","template.name");
        meta.put("sequence.root.text","template.root.text");
        meta.put("sequence.button.save.text","template.button.save.text");
        meta.put("sequence.button.saveas.text","template.button.saveas.text");
        meta.put("sequence.design.header","template.design.header");
        meta.put("sequence.edit.header","template.edit.header");
    }
    @Override
    public EditorUIClient createClient(ServletConfig servletConfig, HttpSession httpSession) {
        try {
            return new TemplateAdminClientAdapter(servletConfig, httpSession);
        } catch (AxisFault axisFault) {
            return null;
        }
    }

    @Override
    public Mediator createEditingMediator() {
        return new TemplateMediator();
    }

    @Override
    public Map<String, String> getUIMetaInfo() {
        return meta;
    }
}
