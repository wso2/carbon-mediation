/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.cloud.gateway.agent.service;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.transports.TransportService;

import java.util.*;

/**
 * Whenever a transport specific bundle drops in, TransportListener will load corresponding
 * attributes in to the TransportStore. We only maintain a single instance of the TransportStore.
 * 
 */
public class TransportStore {

    private Map<String, TransportService> transportStore = new HashMap<String, TransportService>();

    private static final Log log = LogFactory.getLog(TransportStore.class);

    private static final TransportStore INSTANCE = new TransportStore();

    /**
	 * We only maintain a single instance of the TransportStore.
	 */
	private TransportStore() {
        if (log.isDebugEnabled()) {
            log.debug("Initialized the singleton transport store INSTANCE");
        }
    }

    public static TransportStore getInstance() {
		return INSTANCE;
	}

	/**
	 * Store corresponding transport information to facilitate management through the management
	 * console.
	 * 
	 * @param name Name of the transport to be added
	 * @param transportService TransportService
     * @param axisConfig AxisConfiguration of the super tenant
	 */
	public void addTransport(String name, TransportService transportService, AxisConfiguration axisConfig) {

		if (!transportService.isEnabled(true, axisConfig)) {
			// This is not in the axis configuration.
			// Need to check whether it is available for enabling.
			if (!transportService.isAvailable(true, axisConfig)) {
				return;
			}
		}

        if (!transportService.isEnabled(false, axisConfig)) {
            if (!transportService.isAvailable(false, axisConfig)) {
                return;
            }
        }
                
        if (!transportStore.containsKey(name)) {
			transportStore.put(name, transportService);
            if (log.isDebugEnabled()) {
                log.debug(name + " transport added to the transport store and will be available " +
                        "via the management console");
            }

        } else {
			if (log.isDebugEnabled()) {
				log.debug("Transport already exists " + name);
			}
		}
	}

    public void removeTransport(String name) {
        TransportService trpService = transportStore.remove(name);
        if (trpService != null && log.isDebugEnabled()) {
            log.debug(name + " transport removed from the transport store and will not be " +
                    "available via the management console");
        }
    }

    /**
	 * Get the set of available transports in a map
     *
	 * @return a map of transports
	 */
	public Map<String, TransportService> getAvailableTransports() {
		return Collections.unmodifiableMap(transportStore);
	}

	/**
	 *
	 * @param protocol name of the transport
	 * @return a TransportInfo object for the specified transoport
	 */
	public TransportService getTransport(String protocol) {
		return transportStore.get(protocol);
	}
}
