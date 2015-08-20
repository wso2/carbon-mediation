/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.cloud.gateway;

import org.apache.axis2.description.WSDL2Constants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

/**
 * This mediator will check the in coming message and set the property "OUT_ONLY" appropriately.
 * Synapse uses the "OUT_ONLY" property to send a 202 to the client back for a one message.
 */
public class CGMEPHandlingMediator extends AbstractMediator {
    public boolean mediate(MessageContext synCtx) {
        if (synCtx instanceof Axis2MessageContext) {
            Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
            org.apache.axis2.context.MessageContext axis2MessageCtx =
                    axis2smc.getAxis2MessageContext();

            if (axis2MessageCtx != null) {
                String mep = axis2MessageCtx.getOperationContext().getAxisOperation().
                        getMessageExchangePattern();
                if (WSDL2Constants.MEP_URI_IN_ONLY.equals(mep)) {
                    synCtx.setProperty(SynapseConstants.OUT_ONLY, "true");
                }
                // do nothing if two way
            }
        }
        return true;
    }
}
