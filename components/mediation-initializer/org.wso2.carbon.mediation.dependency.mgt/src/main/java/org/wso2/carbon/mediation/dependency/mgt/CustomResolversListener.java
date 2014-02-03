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

import org.osgi.framework.BundleListener;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;

import java.util.Map;
import java.util.HashMap;
import java.util.Dictionary;

public class CustomResolversListener implements BundleListener {

    private DependencyManagementServiceComponent svcComponent;
    private BundleContext bndCtx;
    private boolean listenerRegistered;
    private Map<String,Bundle> resolverBundles = new HashMap<String,Bundle>();    

    CustomResolversListener(DependencyManagementServiceComponent svcComponent,
                                   BundleContext bndCtx) {
        this.svcComponent = svcComponent;
        this.bndCtx = bndCtx;
    }

    boolean registerBundleListener() {
        //what if there are no pending things, then don't register the listener
        if(resolverBundles.isEmpty()){
            listenerRegistered = false;
        } else {
            bndCtx.addBundleListener(this);
            listenerRegistered = true;
        }
        return listenerRegistered;
    }

    void unregisterBundleListener() {
        if (listenerRegistered) {
            bndCtx.removeBundleListener(this);
        }
    }

    void addResolverBundle(String name, Bundle bundle) {
        resolverBundles.put(name, bundle);
    }

    synchronized void start() {
        //Searching Non ACTIVE Bundles and add them to the pending list.
        for (String resolverName : resolverBundles.keySet()) {
            Bundle bundle = resolverBundles.get(resolverName);
            if (bundle.getState() != Bundle.ACTIVE) {
                svcComponent.addPendingResolver(resolverName);
            }
        }
    }

    public void bundleChanged(BundleEvent event) {

        //checking the event type
        if(event.getType() != BundleEvent.STARTED) {
            return;
        }

        Dictionary headers = event.getBundle().getHeaders();

        //Searching for a Deployer
        String value = (String) headers.get("MediatorDependencyResolver");
        if (value != null) {
            svcComponent.removePendingResolver(value.trim());
        }
    }
}
