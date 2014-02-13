/**
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 **/
package org.wso2.carbon.mediator.event.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;

public class EventMediator extends AbstractMediator {
    private Value topic = null;
    private SynapseXPath expression = null;

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

    public OMElement serialize(OMElement parent) {
        OMElement eventElement = fac.createOMElement("event", synNS);

        ValueSerializer vs = new ValueSerializer();
        if (getTopic() != null) {
            vs.serializeValue(getTopic(), "topic", eventElement);
        }

        if (getExpression() != null) {
            SynapseXPathSerializer.serializeXPath(getExpression(),
                    eventElement, "expression");
        }

        if (parent != null) {
            parent.addChild(eventElement);
        }

        return eventElement;
    }

    public void build(OMElement element) {
        OMAttribute topicAttr = element.getAttribute(new QName("topic"));
        if (topicAttr != null) {
            ValueFactory vf = new ValueFactory();
            Value value = vf.createValue("topic", element);

            setTopic(value);
        }

        OMAttribute expression = element.getAttribute(new QName("expression"));
        if (expression != null) {
            try {
                setExpression(SynapseXPathFactory.getSynapseXPath(element, new QName("expression")));
            } catch (JaxenException e) {
            }
        }
    }

    public String getTagLocalName() {
        return "event";
    }
}
