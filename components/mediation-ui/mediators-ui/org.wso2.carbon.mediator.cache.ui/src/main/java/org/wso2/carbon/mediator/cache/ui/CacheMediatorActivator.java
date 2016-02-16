/**
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

package org.wso2.carbon.mediator.cache.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.mediator.service.MediatorService;

/**
 * Activator class for Cache Mediator
 */
public class CacheMediatorActivator implements BundleActivator {

    private static final Log log = LogFactory.getLog(CacheMediatorActivator.class);

	/**
	 * Starts the cache mediator service
	 *
	 * @param bundleContext the execution context of the bundle being started.
	 * @throws Exception If this method throws an exception, this bundle is marked as stopped and the Framework will
	 * remove this bundle's listeners, unregister all services registered by this bundle, and release all services used
	 * by this bundle.
	 */
    public void start(BundleContext bundleContext) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Starting the cache mediator component ...");
        }

        bundleContext.registerService(MediatorService.class.getName(), new CacheMediatorService(), null);

        if (log.isDebugEnabled()) {
            log.debug("Successfully registered the cache mediator service");
        }
    }

	/**
	 * Stops the cache mediator service
	 *
	 * @param bundleContext the execution context of the bundle being stopped.
	 * @throws Exception If this method throws an exception, the bundle is still marked as stopped, and the Framework
	 * will remove the bundle's listeners, unregister all services registered by the bundle, and release all services
	 * used by the bundle.
	 */
    public void stop(BundleContext bundleContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Stopped the cache mediator component ...");
        }
    }
}
