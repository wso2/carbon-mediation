/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.management;

import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.common.AbstractInboundEndpointManager;
import org.wso2.carbon.inbound.endpoint.inboundfactory.InboundRequestProcessorFactoryImpl;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointInfoDTO;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.InboundRMHttpListener;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.utils.RMConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.concurrent.ConcurrentHashMap;

public class CXFEndpointManager extends AbstractInboundEndpointManager {

	private static CXFEndpointManager instance;
	private static final Logger logger = Logger.getLogger(CXFEndpointManager.class);
	private ConcurrentHashMap<Integer, InboundRMHttpListener> cxfInboundEndpointMap;

	private CXFEndpointManager() {
		cxfInboundEndpointMap = new ConcurrentHashMap<>();
	}

	public static synchronized CXFEndpointManager getInstance() {
		if (instance == null) {
			instance = new CXFEndpointManager();
		}
		return instance;
	}

	/**
	 * Checks if it is possible to start an inbound endpoint on the given port
	 * @param port port
	 * @param name name of the inbound endpoint
	 * @param params inbound endpoint parameters
	 * @return true if it is possible
	 */
	public boolean authorizeCXFInboundEndpoint(int port, String name, InboundProcessorParams params) {

		PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		String tenantDomain = carbonContext.getTenantDomain();
		String epName = dataStore.getListeningEndpointName(port, tenantDomain);

		if (epName != null) {
			if (epName.equalsIgnoreCase(name)) {
				if (cxfInboundEndpointMap.containsKey(port)) {
					//If it a tenant we will not create a new server
					if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
						return false;
					}
					cxfInboundEndpointMap.get(port).destroy();
				}
				logger.info("Restarting endpoint " + epName + " on port : " + port);
				return true;
			} else {
				String msg = "Another endpoint named : " + epName + " is currently using this port: " + port;
				logger.warn(msg);
				throw new SynapseException(msg);
			}
		} else {
			dataStore.registerListeningEndpoint(port, tenantDomain,
			                           InboundRequestProcessorFactoryImpl.Protocols.cxf_ws_rm.toString(), name, params);
			return true;
		}
	}

	public void registerCXFInboundEndpoint(int port, InboundRMHttpListener inboundRMHttpListener) {
		cxfInboundEndpointMap.put(port, inboundRMHttpListener);
	}

	public void unregisterCXFInboundEndpoint(int port) {
		cxfInboundEndpointMap.remove(port);
		PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		String tenantDomain = cc.getTenantDomain();
		dataStore.unregisterListeningEndpoint(port, tenantDomain);
	}

	/**
	 * Starts the CXF Inbound endpoints of tenants when the ESB starts
	 * @param port port of the inbound endpoint
	 * @param inboundEndpointInfoDTO endpoint information
	 */
	public void startCXFEndpoint(int port, InboundEndpointInfoDTO inboundEndpointInfoDTO) {
		if (!cxfInboundEndpointMap.containsKey(port)) {
			InboundProcessorParams inboundParams = inboundEndpointInfoDTO.getInboundParams();
			new InboundRMHttpListener(inboundParams).startListener();
		}
	}

	/**
	 * Creates a new InboundRMHttpListener instance if needed
	 * For tenants, if an inbound endpoint already exists for the given port, that is returned
	 * @param params Inbound endpoint parameters
	 * @return InboundRMHttpListener instance
	 */
	public InboundRequestProcessor getCXFEndpoint(InboundProcessorParams params) {

		int port = Integer.parseInt(params.getProperties().getProperty(RMConstants.INBOUND_CXF_RM_PORT));
		String name = params.getName();

		PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		String tenantDomain = carbonContext.getTenantDomain();

		//For the Super tenant, create a new InboundRMHttpListener
		if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
			return new InboundRMHttpListener(params);
		} else {
			String epName = dataStore.getListeningEndpointName(port, tenantDomain);
			if (epName != null) {
				if (epName.equalsIgnoreCase(name)) {
					//For tenants, if that tenant has a server running on the requested port, it is returned.
					if (cxfInboundEndpointMap.containsKey(port)) {
						return cxfInboundEndpointMap.get(port);
					} else {
						//This scenario should not happen
						return new InboundRMHttpListener(params);
					}
				} else {
					String msg = "Another endpoint named : " + epName + " is currently using this port: " + port;
					logger.warn(msg);
					throw new SynapseException(msg);
				}
			} else {
				return new InboundRMHttpListener(params);
			}
		}
	}

	@Override
	public boolean startListener(int port, String name, InboundProcessorParams params) {
		return true;
	}

	@Override public boolean startEndpoint(int port, String name, InboundProcessorParams params) {
		return true;
	}

	@Override public void closeEndpoint(int port) {
	}
}
