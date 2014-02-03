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
package org.wso2.carbon.business.messaging.salesforce.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.business.messaging.salesforce.core.SalesforceProxy;
import org.wso2.carbon.business.messaging.salesforce.core.impl.SalesforceProxyImpl;

public class Activator implements BundleActivator {

    private static final Log log = LogFactory.getLog(Activator.class);

    private ServiceTracker serviceTracker;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */

    public void start(BundleContext context) throws Exception {
        context.registerService(SalesforceProxy.class.getName(), new SalesforceProxyImpl(null), null);
        serviceTracker = new ServiceTracker(context, SalesforceProxy.class.getName(), null);
        serviceTracker.open();
        log.info("SalesforceProxy started...");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        serviceTracker.close();
        log.info("SalesforceProxy closed...");
    }

}
