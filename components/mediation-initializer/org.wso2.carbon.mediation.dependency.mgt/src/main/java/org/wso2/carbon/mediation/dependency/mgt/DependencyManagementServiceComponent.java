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

package org.wso2.carbon.mediation.dependency.mgt;

import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.wso2.carbon.mediation.dependency.mgt.services.ConfigurationTrackingService;
import org.wso2.carbon.mediation.dependency.mgt.services.ConfigurationTrackingServiceImpl;
import org.wso2.carbon.mediation.dependency.mgt.services.ResolverRegistrationService;
import org.wso2.carbon.mediation.dependency.mgt.services.ResolverRegistrationServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * @scr.component name="esb.config.dependency.mgt" immediate="true"
 */
public class DependencyManagementServiceComponent {

    private final Log log = LogFactory.getLog(this.getClass());

    private final List<String> pendingResolvers = new ArrayList<String>();
    private CustomResolversListener resolverListener;
    private BundleContext bndCtx;
    private Timer timer = new Timer();

    protected void activate(ComponentContext cmpCtx) {

        bndCtx = cmpCtx.getBundleContext();
        bndCtx.registerService(ResolverRegistrationService.class.getName(),
                new ResolverRegistrationServiceImpl(), null);

        resolverListener = new CustomResolversListener(this, bndCtx);

        // Find the bundles with the MediatorDependencyResolver header
        for (Bundle bundle : bndCtx.getBundles()) {
            Dictionary headers = bundle.getHeaders();
            String value = (String) headers.get("MediatorDependencyResolver");
            if (value != null) {
                resolverListener.addResolverBundle(value.trim(), bundle);
            }
        }

        if (resolverListener.registerBundleListener()) {
            resolverListener.start();

        } else if (log.isDebugEnabled()) {
            log.debug("No custom dependency resolvers were found... Skipping...");
        }

        //check whether pending list is empty, If so initialize Carbon
        if (pendingResolvers.isEmpty()) {
            finishInitialization();
        } else {
            //Scheduling timer to run if the required items are being delayed.
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    try {
                        if (!pendingResolvers.isEmpty()) {
                            log.warn("Carbon initialization is delayed due to the following " +
                                    "uninitialized mediator dependency resolvers...");
                            for (String configItem : pendingResolvers) {
                                log.warn("Waiting for required resolver " + configItem);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }, 5000, 10000);
        }
    }

    private void finishInitialization() {
        resolverListener.unregisterBundleListener();
        timer.cancel();
        bndCtx.registerService(ConfigurationTrackingService.class.getName(),
                new ConfigurationTrackingServiceImpl(bndCtx),
                null);

        if (log.isDebugEnabled()) {
            log.debug("Configuration tracking service initialized");
        }
    }

    void addPendingResolver(String name) {
        synchronized (pendingResolvers) {
            pendingResolvers.add(name);
        }
    }

    void removePendingResolver(String name) {
        synchronized (pendingResolvers) {
            if (pendingResolvers.contains(name)) {
                if (log.isDebugEnabled()) {
                    log.debug("Pending resolver removed : " + name);
                }
                pendingResolvers.remove(name);
                if (pendingResolvers.isEmpty()) {
                    finishInitialization();
                }
            }
        }
    }


}
