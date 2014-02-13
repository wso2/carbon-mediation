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
package org.wso2.carbon.mediator.router;

import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;

import javax.xml.namespace.QName;

public class RouterMediator extends AbstractListMediator {
    private static final QName ATT_CONTINUE_AFTER = new QName("continueAfter");

    private boolean continueAfter = false;

    public boolean isContinueAfter() {
        return continueAfter;
    }

    public void setContinueAfter(boolean continueAfter) {
        this.continueAfter = continueAfter;
    }

    public OMElement serialize(OMElement parent) {
        OMElement routerElem = fac.createOMElement("router", synNS);
        saveTracingState(routerElem, this);

        if (continueAfter) {
            routerElem.addAttribute(fac.createOMAttribute("continueAfter", nullNS, "true"));
        }

        serializeChildren(routerElem, getList());

        if (parent != null) {
            parent.addChild(routerElem);
        }
        return routerElem;
    }

    public void build(OMElement elem) {
        OMAttribute continueAfterAttr = elem.getAttribute(ATT_CONTINUE_AFTER);
        if (continueAfterAttr != null && continueAfterAttr.getAttributeValue() != null) {
            continueAfter = Boolean.parseBoolean(continueAfterAttr.getAttributeValue());
        }
        addChildren(elem, this);
    }

    public String getTagLocalName() {
        return "router";
    }
}
