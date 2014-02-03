/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediation.dependency.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.SynapseObserver;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.dependency.mgt.DependencyTracker;

import java.util.List;

public class ConfigurationTrackingServiceImpl implements ConfigurationTrackingService {

    private final Log log = LogFactory.getLog(this.getClass());

    private BundleContext bndCtx;
    private DependencyManagementServiceImpl mgtService = new DependencyManagementServiceImpl();
    private boolean serviceRegistered = false;

    public ConfigurationTrackingServiceImpl(BundleContext bndCtx) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing ConfigurationTrackingService");
        }
        this.bndCtx = bndCtx;
    }

    public void setSynapseConfiguration(SynapseConfiguration synCfg) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        List<SynapseObserver> observers = synCfg.getObservers();
        for (SynapseObserver o : observers) {
            if (o instanceof DependencyTracker) {
                DependencyTracker tracker = (DependencyTracker) o;
                mgtService.setDependencyTracker(tenantId, tracker);
                if (log.isDebugEnabled()) {
                    log.debug("Dependency tracker found for the tenant: " + tenantId);
                }

                if (!serviceRegistered) {
                    bndCtx.registerService(DependencyManagementService.class.getName(),
                            mgtService, null);
                    if (log.isDebugEnabled()) {
                        log.debug("DependencyManagementService registered and ready to be used");
                    }
                    serviceRegistered = true;
                }
                break;
            }
        }
    }
}
