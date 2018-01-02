/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediator.conditionalrouter;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;

import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;

public class ConditionalRouterMediator extends AbstractListMediator {

    private List<ConditionalRouteMediator> routes = new LinkedList<ConditionalRouteMediator>();
    private boolean continueAfter;


    private static final String CONTINUE_AFTER = "continueAfter";
    private static final String CONDITIONAL_ROUTER = "conditionalRouter";
    private static final QName CONTINUE_AFTER_Q = new QName(CONTINUE_AFTER);



    public OMElement serialize(OMElement parent) {
        OMElement conditionalRouterElem = fac.createOMElement(CONDITIONAL_ROUTER, synNS);
        if (String.valueOf(continueAfter) != null) {
            conditionalRouterElem.addAttribute(CONTINUE_AFTER, String.valueOf(continueAfter), null);
        }

        serializeChildren(conditionalRouterElem, getList());
        if (parent != null) {
            parent.addChild(conditionalRouterElem);
        }
        return conditionalRouterElem;
    }

    public void build(OMElement omElement) {
        if (omElement.getAttributeValue(CONTINUE_AFTER_Q) != null) {
            continueAfter = Boolean.parseBoolean(omElement.getAttributeValue(CONTINUE_AFTER_Q));
        }
        addChildren(omElement, this);
    }

    public String getTagLocalName() {
        return "conditionalRouter";
    }


    public List<ConditionalRouteMediator> getRoutes() {
        return routes;
    }

    public void setRoutes(List<ConditionalRouteMediator> routes) {
        this.routes = routes;
    }

    public boolean isContinueAfter() {
        return continueAfter;
    }

    public void setContinueAfter(boolean continueAfter) {
        this.continueAfter = continueAfter;
    }
}
