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
package org.wso2.carbon.mediator.clazz;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.util.MediatorProperty;

import java.util.ArrayList;
import java.util.List;

public class ClassMediator extends AbstractMediator {
    private String mediator;

    private final List<MediatorProperty> properties = new ArrayList<MediatorProperty>();

    public String getMediator() {
        return mediator;
    }

    public void setMediator(String mediator) {
        this.mediator = mediator;
    }

    public void addProperty(MediatorProperty property) {
        properties.add(property);
    }

    public List<MediatorProperty> getProperties() {
        return this.properties;
    }

    public void addAllProperties(List<MediatorProperty> propertyList) {
        properties.addAll(propertyList);
    }

    public OMElement serialize(OMElement parent) {
        OMElement clazz = fac.createOMElement("class", synNS);
        saveTracingState(clazz, this);

        if (mediator != null) {
            clazz.addAttribute(fac.createOMAttribute(
                "name", nullNS, mediator));
        } else {
            throw new MediatorException("Invalid class mediator. The class name is required");
        }

        serializeMediatorProperties(clazz, properties, PROP_Q);

        if (parent != null) {
            parent.addChild(clazz);
        }
        return clazz;
    }

    public void build(OMElement elem) {
        OMAttribute name = elem.getAttribute(ATT_NAME);
        if (name == null) {
            String msg = "The name of the actual mediator class is a required attribute";
            throw new MediatorException(msg);
        }
        this.mediator = name.getAttributeValue();

        addAllProperties(getMediatorProperties(elem));

        processAuditStatus(this, elem);        
    }

    public String getTagLocalName() {
        return "class";
    }
}
