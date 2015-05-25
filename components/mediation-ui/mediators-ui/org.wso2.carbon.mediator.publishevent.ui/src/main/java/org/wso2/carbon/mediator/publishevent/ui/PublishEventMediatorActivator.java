/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.publishevent.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.mediator.service.MediatorService;
//TODO: put proper comment
/**
 *
 *
 */
public class PublishEventMediatorActivator implements BundleActivator {

	private static final Log log = LogFactory.getLog(PublishEventMediatorActivator.class);

	public void start(BundleContext bundleContext) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Starting the publishEvent mediator component ...");
		}

		//Properties props = new Properties();
		bundleContext.registerService(
				MediatorService.class.getName(), new PublishEventMediatorService(), null);

		if (log.isDebugEnabled()) {
			log.debug("Successfully registered the publishEvent mediator service");
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Stopped the publishEvent mediator component ...");
		}
	}
}
