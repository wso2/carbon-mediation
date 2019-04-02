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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.ValueSerializer;
import org.wso2.carbon.mediator.event.EventMediator;


public class EventMediatorSerializer extends AbstractMediatorSerializer {
    @Override
    protected OMElement serializeSpecificMediator(Mediator mediator) {
        EventMediator eventMediator = null;
        if (mediator instanceof EventMediator) {
            eventMediator = (EventMediator) mediator;
        } else {
            throw new SynapseException("Expecting an instance of EventMediator");
        }

        OMElement eventElement = fac.createOMElement("event", synNS);

        ValueSerializer vs = new ValueSerializer();
        if (eventMediator.getTopic() != null) {
            vs.serializeValue(eventMediator.getTopic(), "topic", eventElement);
        }

        if (eventMediator.getExpression() != null) {
            SynapseXPathSerializer.serializeXPath(eventMediator.getExpression(),
                    eventElement, "expression");
        }

        serializeComments(eventElement, ((EventMediator)mediator).getCommentsList());

        return eventElement;
    }

    @Override
    public String getMediatorClassName() {
        return EventMediator.class.getName();
    }
}
