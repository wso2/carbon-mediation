/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediator.event;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.ws.internal.exception.WSEventException;
import org.wso2.carbon.event.ws.internal.util.EventBrokerUtils;

public class EventMediator extends AbstractMediator {
    private Value topic = null;

    private SynapseXPath expression = null;

    public boolean mediate(MessageContext synCtx) {

        if (synCtx.getEnvironment().isDebugEnabled()) {
            if (super.divertMediationRoute(synCtx)) {
                return true;
            }
        }

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Event mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        String topicValue = null;
        if (topic != null) {
            topicValue = topic.evaluateValue(synCtx);
        } else {
            org.apache.axis2.context.MessageContext mc =
                    ((Axis2MessageContext)synCtx).getAxis2MessageContext();
            try {
                topicValue = EventBrokerUtils.extractTopicFromMessage(mc);
            } catch (WSEventException e) {
                handleException("Error extracting the topic from the message", e, synCtx);
            }
        }

        if (topicValue == null) {
            handleException("Topic for the event cannot be found", synCtx);
        } else {
            org.apache.axis2.context.MessageContext mc =
                    ((Axis2MessageContext)synCtx).getAxis2MessageContext();
            EventBroker broker = (EventBroker) mc.getConfigurationContext().
                    getProperty("mediation.event.broker");
            if (broker == null) {
                handleException("EventBroker cannot be found", synCtx);
            } else {
                Message message = new Message();
                if (expression == null) {
                    message.setMessage(synCtx.getEnvelope().getBody().getFirstElement());
                } else {
                    try {
                        Object o = expression.selectSingleNode(synCtx);
                        if (o instanceof OMElement)  {
                            message.setMessage((OMElement)o);
                        } else {
                            handleException("The result of the expression:" +
                                    expression + " should be an OMElement", synCtx);
                        }
                    } catch (JaxenException e) {
                        handleException("Error evaluating the expression: " +
                                expression, synCtx);
                    }
                }

                try {
                    broker.publish(message, topicValue);
                } catch (EventBrokerException e) {
                    handleException("Error publishing the event to the broker", e, synCtx);
                } catch (Exception e) {
                    log.error("Error in setting tenant information", e);
                }
            }
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : Event mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        return true;
    }

    public Value getTopic() {
        return topic;
    }

    public SynapseXPath getExpression() {
        return expression;
    }

    public void setTopic(Value topic) {
        this.topic = topic;
    }

    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }
}
