/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.business.messaging.salesforce.mediator.ui;

import org.wso2.carbon.mediator.service.AbstractMediatorService;
import org.wso2.carbon.mediator.service.ui.Mediator;

/**
 * service object used to track ui Menu properties + mediator object creation
 */
public class SalesforceMediatorService extends AbstractMediatorService {
    public static final String SALESFORCE_TAG_LOCAL = "salesforce";
    public static final String SALESFORCE_UI_DISPLAY_NAME = "Salesforce";
    public static final String SALESFORCE_UI_LOGICAL_NAME = "SalesforceMediator";
    public static final String SALESFORCE_UI_GROUP_NAME = "Advanced";

    /**
     * @return top level tag element corresponding to salesforce config
     *         any configuration starting with this tag name will be called upon by the framework
     */
    public String getTagLocalName() {
        return SALESFORCE_TAG_LOCAL;
    }

    /**
     * @return UI Display name of salesforce mediator
     */
    public String getDisplayName() {
        return SALESFORCE_UI_DISPLAY_NAME;
    }

    /**
     * @return symbolic name
     */
    public String getLogicalName() {
        return SALESFORCE_UI_LOGICAL_NAME;
    }

    /**
     * @return group type salesforce mediator belongs to in sequence UI Editor
     */
    public String getGroupName() {
        return SALESFORCE_UI_GROUP_NAME;
    }

    /**
     * @return
     */
    public Mediator getMediator() {
        return new SalesforceMediator();
    }
}
