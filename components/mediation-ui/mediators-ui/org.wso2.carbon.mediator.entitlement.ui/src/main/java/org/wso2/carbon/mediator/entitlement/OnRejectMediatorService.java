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
package org.wso2.carbon.mediator.entitlement;

import org.wso2.carbon.mediator.service.AbstractMediatorService;
import org.wso2.carbon.mediator.service.ui.Mediator;


public class OnRejectMediatorService extends AbstractMediatorService {
    public String getTagLocalName() {
        return "onReject";
    }

    public String getDisplayName() {
        return "OnReject";
    }

    public String getLogicalName() {
        return "OnRejectMediator";
    }

    public boolean isMovingAllowed() {
        return false;
    }

    public boolean isAddSiblingEnabled() {
        return false;
    }

    public Mediator getMediator() {
        return new OnRejectMediator();
    }

    public String getUIFolderName() {
        return "onreject";
    }
}
