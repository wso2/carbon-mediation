/**
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.http.core.impl;


import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.transport.passthru.PassThroughHttpSender;
import org.apache.synapse.transport.passthru.api.PassThroughOutboundEndpointHandler;

import java.io.IOException;

/**
 * Get mediated message context from Source Response Worker and created InboundHttpSourceResponse
 * extract the pipe and set to the reader
 */
public class InboundHttpResponseSender implements InboundResponseSender {
    private Logger logger = Logger.getLogger(InboundHttpResponseSender.class);

    private PassThroughHttpSender passThroughHttpSender;

    public InboundHttpResponseSender() {
        //Get registered Pass-Through transport sender
        passThroughHttpSender = PassThroughOutboundEndpointHandler.getPassThroughHttpSender();
    }


    @Override
    public void sendBack(MessageContext messageContext) {

        if (messageContext != null) {
            org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) messageContext).
                    getAxis2MessageContext();
            try {
                //send for send back the response to the source
                passThroughHttpSender.invoke(msgContext);
            } catch (AxisFault e) {
               logger.error("Exception occurred when calling PassThroughHttpSender.invoke may be" +
                                                                     "message context does not have some properties",e);
            }
        } else {
            logger.error("Response MessageContext is null may be Response read error occurred");
        }
    }
}
