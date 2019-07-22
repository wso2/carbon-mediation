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

package org.wso2.carbon.inbound.endpoint.ext.wsrm.management;

/*import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericConstants;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericInboundListener;
import org.wso2.carbon.endpoint.ext.wsrm.InboundRMHttpListener;
import org.wso2.carbon.endpoint.ext.wsrm.utils.RMConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;*/


public class CXFEndpointManager {

/*
    private static final Log logger = LogFactory.getLog(CXFEndpointManager.class);

    */
/**
     * Returns a CXFEndpointManager instance
     *
     * @return CXFEndpointManager instance
     *//*

	public static synchronized CXFEndpointManager getInstance() {
		return (CXFEndpointManager) GenericEndpointManager.getManagerInstance(CXFEndpointManager.class.getName());
	}

    */
/**
     * Creates a new InboundRMHttpListener instance if needed
     * For tenants, if an inbound endpoint already exists for the given port, that is returned
     * @param params Inbound endpoint parameters
     * @return InboundRMHttpListener instance
     *//*

    @Override
    public GenericInboundListener getEndpoint(InboundProcessorParams params) {
        int port = Integer.parseInt(params.getProperties().getProperty(GenericConstants.LISTENING_INBOUND_PORT));
        String name = params.getName();

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();

        // For the Super tenant, create a new InboundRMHttpListener
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return new InboundRMHttpListener(params);
        } else {
            String epName = dataStore.getListeningEndpointName(port, tenantDomain);
            if (epName != null) {
                if (epName.equalsIgnoreCase(name)) {
                    // For tenants, if that tenant has a server running on the requested port, it is returned.
                    if (genericInboundEndpointMap.containsKey(port)) {
                        return genericInboundEndpointMap.get(port);
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

    */
/**
     * Starts the CXF Inbound endpoints of tenants when the ESB starts
     * @param port port of the inbound endpoint
     * @param name endpoint information
     *//*

    @Override
    public boolean startListener(int port, String name, InboundProcessorParams params) {
        if (!genericInboundEndpointMap.containsKey(port)) {
            return new InboundRMHttpListener(params).startListener();
        }
        return false;
    }

	*/
/**
	 * Checks if it is possible to start an inbound endpoint on the given port
	 * @param port port
	 * @param name name of the inbound endpoint
	 * @param params inbound endpoint parameters
	 * @return true if it is possible
	 *//*

    @Override
	public boolean startEndpoint(int port, String name, InboundProcessorParams params) {

		PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		String tenantDomain = carbonContext.getTenantDomain();
		String epName = dataStore.getListeningEndpointName(port, tenantDomain);

		if (epName != null) {
			if (epName.equalsIgnoreCase(name)) {
				if (genericInboundEndpointMap.containsKey(port)) {
					// If it a tenant we will not create a new server
					if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
						return false;
					}
                    genericInboundEndpointMap.get(port).destroy();
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
                                                RMConstants.CXF_INBOUND_PROTOCOL_NAME, name, params);
			return true;
		}
	}

    */
/**
     * Add CXF endpoint to endpoint map
     *//*

	public void registerCXFInboundEndpoint(int port, InboundRMHttpListener inboundRMHttpListener) {
        genericInboundEndpointMap.put(port, inboundRMHttpListener);
	}
*/
}
