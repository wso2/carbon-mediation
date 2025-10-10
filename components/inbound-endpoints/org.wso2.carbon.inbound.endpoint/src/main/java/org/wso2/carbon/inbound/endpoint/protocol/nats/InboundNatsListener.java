/*
 * Copyright 2020 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.nats;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.inbound.endpoint.protocol.nats.management.NatsEndpointManager;

import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;

/**
 * Listener class for NATS Inbound Endpoint which is triggered by inbound core
 */
public class InboundNatsListener implements InboundRequestProcessor {

    private static final Log LOGGER = LogFactory.getLog(InboundNatsListener.class);

    private String name;
    private InboundProcessorParams processorParams;
    private final boolean startInPausedMode;

    public InboundNatsListener(InboundProcessorParams params) {
        processorParams = params;
        name = params.getName();
        startInPausedMode = params.startInPausedMode();
    }

    @Override public void init() {
        /*
         * The activate/deactivate functionality is not currently implemented
         * for this Inbound Endpoint type.
         *
         * Therefore, the following check has been added to immediately return if the "suspend"
         * attribute is set to true in the inbound endpoint configuration due to the fixes done
         * in Synapse level - https://github.com/wso2/wso2-synapse/pull/2261.
         *
         * Note: This implementation is temporary and should be revisited and improved once
         * the activate/deactivate capability is implemented.
         */
        if (startInPausedMode) {
            LOGGER.info("Inbound endpoint [" + name + "] is currently suspended.");
            return;
        }
        NatsEndpointManager.getInstance().startEndpoint(0, name, processorParams);
    }

    @Override public void destroy() {
        NatsEndpointManager.getInstance().closeEndpoint(0);
    }

    @Override
    public void pause() {

    }

    @Override
    public boolean activate() {

        return false;
    }

    @Override
    public boolean deactivate() {

        return false;
    }

    @Override
    public boolean isDeactivated() {
        // Need to properly implement this logic.
        return false;
    }
}
