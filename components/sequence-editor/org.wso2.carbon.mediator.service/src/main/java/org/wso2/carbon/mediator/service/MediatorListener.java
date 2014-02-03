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

package org.wso2.carbon.mediator.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @scr.component name="carbon.mediator.service"" immediate="true"
 * @scr.reference name="mediator.service"
 * interface="org.wso2.carbon.mediator.service.MediatorService"
 * cardinality="1..n" policy="dynamic"
 * bind="addMediatorService" unbind="removeMediatorService"
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class MediatorListener {

    private static final Log log = LogFactory.getLog(MediatorListener.class);

    protected void addMediatorService(MediatorService mediatorService) {
        MediatorStore mediatorStore = MediatorStore.getInstance();
        mediatorStore.registerMediator(mediatorService.getTagLocalName(), mediatorService);
        if (log.isDebugEnabled()) {
            log.debug("Registered the mediator for the tag name : "
                    + mediatorService.getTagLocalName());
        }
    }

    protected void removeMediatorService(MediatorService mediatorService) {
        MediatorStore mediatorStore = MediatorStore.getInstance();
        mediatorStore.unRegisterMediator(mediatorService.getTagLocalName());
        if (log.isDebugEnabled()) {
            log.debug("Un-Registered the mediator for the tag name : "
                    + mediatorService.getTagLocalName());
        }
    }  
}
