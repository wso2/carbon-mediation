/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mediator.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@Component(
        name = "carbon.mediator.service",
        immediate = true)
public class MediatorListener {

    private static final Log log = LogFactory.getLog(MediatorListener.class);

    @Reference(
            name = "mediator.service",
            service = org.wso2.carbon.mediator.service.MediatorService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeMediatorService")
    protected void addMediatorService(MediatorService mediatorService) {

        MediatorStore mediatorStore = MediatorStore.getInstance();
        mediatorStore.registerMediator(mediatorService.getTagLocalName(), mediatorService);
        if (log.isDebugEnabled()) {
            log.debug("Registered the mediator for the tag name : " + mediatorService.getTagLocalName());
        }
    }

    protected void removeMediatorService(MediatorService mediatorService) {

        MediatorStore mediatorStore = MediatorStore.getInstance();
        mediatorStore.unRegisterMediator(mediatorService.getTagLocalName());
        if (log.isDebugEnabled()) {
            log.debug("Un-Registered the mediator for the tag name : " + mediatorService.getTagLocalName());
        }
    }
}
