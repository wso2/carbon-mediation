/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint;

import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointInfoDTO;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointsDataStore;
import org.wso2.carbon.inbound.endpoint.protocol.http.InboundHttpConstants;
import org.wso2.carbon.inbound.endpoint.protocol.http.management.HTTPEndpointManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for starting Listeners( like HTTP, HTTPS, HL7) on server startup for
 * Listening Inbound Endpoints.
 */
public class EndpointListenerLoader {


    /**
     * Start listeners for all the Listening Inbound Endpoints. This should be called in the
     * server startup to load all the required listeners for endpoints in all tenants
     *
     * Inbound Endpoint Persistence service need to be up and running before calling this method.
     * So the ServiceBusInitializer activate() method is the ideal place as it
     * guarantee that Inbound Endpoint Persistence Service is activated.
     * We cannot make this a osgi service as this is a fragment of synapse-core.
     * So to make sure persistence service is available, we need to depend on some other technique
     * like the one described above
     */
    public static void loadListeners() {

        Map<Integer, List<InboundEndpointInfoDTO>> tenantData =
                InboundEndpointsDataStore.getInstance().getAllEndpointData();

        for (Map.Entry tenantInfoEntry : tenantData.entrySet()) {
            int port = (Integer) tenantInfoEntry.getKey();

            InboundEndpointInfoDTO inboundEndpointInfoDTO =
                       (InboundEndpointInfoDTO) ((ArrayList) tenantInfoEntry.getValue()).get(0);

            if (inboundEndpointInfoDTO.getProtocol().equals(InboundHttpConstants.HTTP)) {
                HTTPEndpointManager.getInstance().
                        startListener(port, inboundEndpointInfoDTO.getEndpointName());
            } else if (inboundEndpointInfoDTO.getProtocol().equals(InboundHttpConstants.HTTPS)) {
                HTTPEndpointManager.getInstance().
                           startSSLListener(port, inboundEndpointInfoDTO.getEndpointName(),
                                         inboundEndpointInfoDTO.getSslConfiguration());
            }

        }
    }

}
