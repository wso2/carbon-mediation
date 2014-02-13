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

import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMAttribute;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class ClassMediator extends AbstractMediator {
    private String mediator;

    private final Map<String, Object> properties = new HashMap<String, Object>();

    public String getMediator() {
        return mediator;
    }

    public void setMediator(String mediator) {
        this.mediator = mediator;
    }

    public void addProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Map getProperties() {
        return this.properties;
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

        Iterator itr = properties.keySet().iterator();
        while(itr.hasNext()) {
            String propName = (String) itr.next();
            Object o = properties.get(propName);
            OMElement prop = fac.createOMElement(PROP_Q);
            prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));

            if (o instanceof String) {
                prop.addAttribute(fac.createOMAttribute("value", nullNS, (String) o));
            } else {
                prop.addChild((OMNode) o);
            }
            clazz.addChild(prop);
        }

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
        
        for (Iterator it = elem.getChildrenWithName(PROP_Q); it.hasNext();) {
            OMElement child = (OMElement) it.next();

            String propName = child.getAttribute(ATT_NAME).getAttributeValue();
            if (propName == null) {
                throw new MediatorException(
                    "A Class mediator property must specify the name attribute");
            } else {
                if (child.getAttribute(ATT_VALUE) != null) {
                    String value = child.getAttribute(ATT_VALUE).getAttributeValue();
                    properties.put(propName, value);
                } else {
                    OMNode omElt = child.getFirstElement();
                    if (omElt != null) {
                        properties.put(propName, omElt);
                    } else {
                        throw new MediatorException("A Class mediator property must specify " +
                            "name and value attributes, or a name and a child XML fragment");
                    }
                }
            }
        }

        processAuditStatus(this, elem);        
    }

    public String getTagLocalName() {
        return "class";
    }
}
