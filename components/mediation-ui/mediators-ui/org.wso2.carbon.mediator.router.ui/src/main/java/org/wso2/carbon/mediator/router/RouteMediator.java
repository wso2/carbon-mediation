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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.target.TargetMediator;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.regex.Pattern;

public class RouteMediator extends AbstractListMediator {
    private static final QName ATT_MATCH = new QName("match");
    private static final QName ATT_BREAK_ROUTER = new QName("breakRouter");

    private SynapseXPath expression;
    private Pattern match;
    private boolean breakRouter = true;

    public SynapseXPath getExpression() {
        return expression;
    }

    public Pattern getMatch() {
        return match;
    }

    public boolean isBreakRouter() {
        return breakRouter;
    }

    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }

    public void setMatch(Pattern match) {
        this.match = match;
    }

    public void setBreakRouter(boolean breakRouter) {
        this.breakRouter = breakRouter;
    }

    public RouteMediator() {
        addChild(new TargetMediator());
    }

    public OMElement serialize(OMElement parent) {
        OMElement routeElem = fac.createOMElement("route", synNS);

        if (expression != null) {
            SynapseXPathSerializer.serializeXPath(
                    expression, routeElem, "expression");
        } else {
            throw new MediatorException("Incomplete route has been found in the " +
                    "serialization of the RouterMediator");
        }

        if (match != null) {
            routeElem.addAttribute(
                    fac.createOMAttribute("match", nullNS, match.pattern()));
        }

        if (!breakRouter) {
            routeElem.addAttribute(fac.createOMAttribute("breakRouter", nullNS, "false"));
        }

        serializeChildren(routeElem, getList());

        if (parent != null) {
            parent.addChild(routeElem);
        }
        return routeElem;
    }

    public void build(OMElement elem) {
        OMAttribute expressionAttr = elem.getAttribute(ATT_EXPRN);
        OMAttribute matchAttr = elem.getAttribute(ATT_MATCH);
        OMAttribute breakRouterAttr = elem.getAttribute(ATT_BREAK_ROUTER);

        expression = null;
        match = null;
        breakRouter = true;
        getList().clear();
        if (expressionAttr != null && expressionAttr.getAttributeValue() != null) {

            try {
                expression =
                        SynapseXPathFactory.getSynapseXPath(elem, ATT_EXPRN);
            } catch (JaxenException e) {
                throw new MediatorException("Couldn't build the xpath from the expression : "
                        + expressionAttr.getAttributeValue());
            }
        } else {

            throw new MediatorException("Route without an expression attribute has been found, " +
                    "but it is required to have an expression for all routes");
        }

        if (matchAttr != null && matchAttr.getAttributeValue() != null) {
            match = Pattern.compile(matchAttr.getAttributeValue());
        }

        if (breakRouterAttr != null && breakRouterAttr.getAttributeValue() != null) {
            breakRouter = Boolean.parseBoolean(breakRouterAttr.getAttributeValue());
        }

        addChildren(elem, this);        
    }

    public String getTagLocalName() {
        return "route";
    }
}
