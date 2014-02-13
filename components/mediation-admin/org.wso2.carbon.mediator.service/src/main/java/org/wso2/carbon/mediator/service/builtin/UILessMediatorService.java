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
 **/

package org.wso2.carbon.mediator.service.builtin;

import org.wso2.carbon.mediator.service.AbstractMediatorService;
import org.wso2.carbon.mediator.service.ui.Mediator;

public class UILessMediatorService extends AbstractMediatorService {

    private String localName = "";

    public String getTagLocalName() {
        return localName;
    }

    public String getDisplayName() {
        return localName;
    }

    public String getLogicalName() {
        return localName + "Mediator";
    }

    public Mediator getMediator() {
        return new UILessMediator();
    }

    @Override
    public String getUIFolderName() {
        return "uiless";
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }    
}
