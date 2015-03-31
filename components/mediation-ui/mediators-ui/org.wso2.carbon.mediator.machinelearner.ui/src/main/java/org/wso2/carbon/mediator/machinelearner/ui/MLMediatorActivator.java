/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediator.machinelearner.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.mediator.service.MediatorService;

public class MLMediatorActivator implements BundleActivator {

    private static final Log log = LogFactory.getLog(MLMediatorActivator.class);

    public void start(BundleContext bundleContext) {

        if (log.isDebugEnabled()) {
            log.debug("Starting the machineLearner mediator component ...");
        }

        bundleContext.registerService(
                MediatorService.class.getName(), new MLMediatorService(), null);

        if (log.isDebugEnabled()) {
            log.debug("Successfully registered the machineLearner mediator service");
        }
    }

    public void stop(BundleContext bundleContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopped the machineLearner mediator component ...");
        }
    }
}