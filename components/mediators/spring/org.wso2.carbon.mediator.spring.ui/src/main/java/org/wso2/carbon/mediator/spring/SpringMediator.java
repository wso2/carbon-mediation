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
package org.wso2.carbon.mediator.spring;

import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAttribute;
import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;

public class SpringMediator extends AbstractMediator {

    private String beanName = null;  
    private String configKey = null;

    private static final OMNamespace sprNS =
            fac.createOMNamespace(XMLConfigConstants.SYNAPSE_NAMESPACE, "spring");

    public String getBeanName() {
        return beanName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getTagLocalName() {
        return "spring";
    }

    public OMElement serialize(OMElement parent) {
        OMElement spring = fac.createOMElement("spring", sprNS);

        if (beanName != null) {
            spring.addAttribute(fac.createOMAttribute(
                "bean", nullNS, beanName));
        } else {
            throw new MediatorException("Invalid mediator. Bean name required.");
        }
        saveTracingState(spring, this);

        if (configKey != null) {
            spring.addAttribute(fac.createOMAttribute(
                "key", nullNS, configKey));
        }

        if (parent != null) {
            parent.addChild(spring);
        }

        return spring;
    }

    public void build(OMElement elem) {
        OMAttribute bean = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "bean"));
        OMAttribute key  = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));

        if (bean == null) {
            throw new MediatorException("The 'bean' attribute is required for a Spring mediator definition");
        } else if (key == null) {
            throw new MediatorException("A 'key' attribute is required for a Spring mediator definition");
        } else {

             // after successfully creating the mediator
             // set its common attributes such as tracing etc
            processAuditStatus(this, elem);
            this.beanName = bean.getAttributeValue();
            this.configKey = key.getAttributeValue();
        }        
    }
}
