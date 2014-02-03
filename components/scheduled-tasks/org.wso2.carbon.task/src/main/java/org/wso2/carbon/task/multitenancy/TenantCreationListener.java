/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.task.multitenancy;

import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.task.JobMetaDataProviderServiceHandler;
import org.wso2.carbon.task.TaskManager;
import org.wso2.carbon.task.TaskManagementServiceHandler;
import org.wso2.carbon.task.util.ConfigHolder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.task.*;

public class TenantCreationListener extends AbstractAxis2ConfigurationContextObserver {
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        ConfigurationContext superTenentCtx =
                ConfigHolder.getInstance().getConfigCtxSvc().getServerConfigContext();

        JobMetaDataProviderServiceHandler jobMetaDataProviderServiceHandler =
                (JobMetaDataProviderServiceHandler) superTenentCtx.getProperty(
                        TaskManager.CARBON_TASK_JOB_METADATA_SERVICE);

        TaskManagementServiceHandler taskManagementServiceHandler =
                (TaskManagementServiceHandler) superTenentCtx.getProperty(
                        TaskManager.CARBON_TASK_MANAGEMENT_SERVICE);

        // if this configuration listener called first we create the task scheduler and set it to
        // the configuration context
        // TODO we need to improve the tenant listeners calling order in carbon core
        if (configurationContext.getProperty(TaskManager.CARBON_TASK_SCHEDULER) == null) {
            TaskScheduler scheduler = new TaskScheduler(TaskConstants.TASK_SCHEDULER);
            configurationContext.setProperty(TaskManager.CARBON_TASK_SCHEDULER, scheduler);
        }

        // if this configuration listener called first we create the task repository and set it to
        // the configuration context
        // TODO we need to improve the tenant listeners calling order in carbon core
        TaskDescriptionRepository repository;
        if (configurationContext.getProperty(TaskManager.CARBON_TASK_REPOSITORY) == null) {
            repository = new TaskDescriptionRepository();
            configurationContext.setProperty(TaskManager.CARBON_TASK_REPOSITORY, repository);
        } else {
            repository = (TaskDescriptionRepository)
                    configurationContext.getProperty(TaskManager.CARBON_TASK_REPOSITORY);
        }


        if (taskManagementServiceHandler != null && jobMetaDataProviderServiceHandler != null) {
            TaskManager taskManager = new TaskManager();

            taskManager.setTaskDescriptionRepository(repository);
            taskManager.init(jobMetaDataProviderServiceHandler, taskManagementServiceHandler);

            configurationContext.setProperty(TaskManager.CARBON_TASK_MANAGER, taskManager);            
        }
    }

}
