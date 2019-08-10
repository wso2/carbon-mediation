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
package org.wso2.carbon.task.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.task.services.TaskDescriptionRepositoryService;
import org.wso2.carbon.task.services.TaskDescriptionRepositoryServiceImpl;
import org.wso2.carbon.task.services.TaskSchedulerService;
import org.wso2.carbon.task.services.TaskSchedulerServiceImpl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "mediation.task.services.component",
        immediate = true)
public class TaskServiceRegistrarComponent {

    private static final Log log = LogFactory.getLog(TaskServiceRegistrarComponent.class);

    private ServiceRegistration tssRegistration;

    private ServiceRegistration tdrsRegistration;

    @Activate
    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Starting 'TaskServiceRegistrarComponent'");
        }
        tssRegistration = ctxt.getBundleContext().registerService(TaskSchedulerService.class.getName(), new
                TaskSchedulerServiceImpl(), null);
        tdrsRegistration = ctxt.getBundleContext().registerService(TaskDescriptionRepositoryService.class.getName(),
                new TaskDescriptionRepositoryServiceImpl(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Stopping 'TaskServiceRegistrarComponent'");
        }
        ctxt.getBundleContext().ungetService(tssRegistration.getReference());
        ctxt.getBundleContext().ungetService(tdrsRegistration.getReference());
    }
}
