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
package org.wso2.carbon.sequences.ui.factory.impl;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.mediator.service.builtin.SequenceMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;
import org.wso2.carbon.sequences.ui.client.EditorUIClient;
import org.wso2.carbon.sequences.ui.client.SequenceAdminClient;
import org.wso2.carbon.sequences.ui.factory.EditorUIClientFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class SequenceEditorClientFactory extends EditorUIClientFactory {
    private static Map<String, String> meta = new HashMap<String, String>();

    static{
        meta.put("editorMode","sequence");
        meta.put("sequence.edit.text","sequence.edit.text");
        meta.put("sequence.design.text","sequence.design.text");
        meta.put("sequence.design.view.text","sequence.design.view.text");
        meta.put("sequence.name","sequence.name");
        meta.put("sequence.root.text","sequence.root.text");
        meta.put("sequence.button.save.text","sequence.button.save.text");
        meta.put("sequence.button.saveas.text","sequence.button.saveas.text");
    }
    @Override
    public EditorUIClient createClient(ServletConfig config, HttpSession session) {
        try {
            return new SequenceAdminClient(config ,session);
        } catch (AxisFault axisFault) {
            return null;
        }
    }

    @Override
    public Mediator createEditingMediator() {
        return new SequenceMediator();
    }

    @Override
    public Map<String, String> getUIMetaInfo() {
        return meta;
    }
}
