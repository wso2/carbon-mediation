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

package org.wso2.carbon.mediator.event.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.mediators.Value;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.event.EventMediator;

import javax.xml.namespace.QName;
import java.util.Properties;

public class EventMediatorFactory extends AbstractMediatorFactory {
    public static final QName EVENT_Q = new QName(
            SynapseConstants.SYNAPSE_NAMESPACE, "event");

    @Override
    protected Mediator createSpecificMediator(OMElement element, Properties properties) {
        EventMediator event = new EventMediator();

        OMAttribute topicAttr = element.getAttribute(new QName("topic"));
        if (topicAttr != null) {
            ValueFactory vf = new ValueFactory();
            Value value = vf.createValue("topic", element);

            event.setTopic(value);
        }

        OMAttribute expression = element.getAttribute(new QName("expression"));
        if (expression != null) {
            try {
                event.setExpression(SynapseXPathFactory.getSynapseXPath(element, new QName("expression")));
            } catch (JaxenException e) {
                handleException("Error parsing the expression: " + expression.getAttributeValue());
            }
        }

        addAllCommentChildrenToList(element, event.getCommentsList());

        return event;
    }

    @Override
    public QName getTagQName() {
        return EVENT_Q;
    }
}
