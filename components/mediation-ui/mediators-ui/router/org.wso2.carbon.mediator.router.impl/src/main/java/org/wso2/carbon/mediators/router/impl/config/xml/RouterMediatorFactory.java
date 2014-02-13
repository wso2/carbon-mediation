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

package org.wso2.carbon.mediators.router.impl.config.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.TargetFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediators.router.impl.Route;
import org.wso2.carbon.mediators.router.impl.RouterMediator;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * <p>Builds the <code>RouterMediator</code> using the following configuration</p>
 *
 * <pre>
 * &lt;router [continueAfter=(true | false)]&gt;
 *   &lt;route expression="xpath" [match="regEx"] [breakRouter=(true | false)]&gt;
 *     &lt;target [sequence="string"] [endpoint="string"]&gt;
 *       &lt;sequence ....../&gt;?
 *       &lt;endpoint ....../&gt;?
 *     &lt;/target&gt;
 *   &lt;/route&gt;+
 * &lt;/router&gt;
 * </pre>
 */
public class RouterMediatorFactory extends AbstractMediatorFactory {

    private static final QName ROUTER_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "router");
    private static final QName ROUTE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "route");
    private static final QName TARGET_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "target");

    private static final QName ATT_CONTINUE_AFTER = new QName("continueAfter");
    private static final QName ATT_MATCH = new QName("match");
    private static final QName ATT_BREAK_ROUTER = new QName("breakRouter");

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        if (!ROUTER_Q.equals(elem.getQName())) {
            handleException("Unable to create the Router mediator. " +
                "Unexpected element as the Router mediator configuration");
        }

        RouterMediator m = new RouterMediator();

        OMAttribute continueAfterAttr = elem.getAttribute(ATT_CONTINUE_AFTER);
        if (continueAfterAttr != null && continueAfterAttr.getAttributeValue() != null) {
            m.setContinueAfter(Boolean.parseBoolean(continueAfterAttr.getAttributeValue()));
        }

        for (Iterator itr = elem.getChildrenWithName(ROUTE_Q); itr.hasNext();) {

            OMElement routeElement = (OMElement) itr.next();

            OMAttribute expressionAttr = routeElement.getAttribute(ATT_EXPRN);
            OMAttribute matchAttr = routeElement.getAttribute(ATT_MATCH);
            OMAttribute breakRouterAttr = routeElement.getAttribute(ATT_BREAK_ROUTER);
            OMElement targetElem = routeElement.getFirstChildWithName(TARGET_Q);

            Route route = new Route();

            if (expressionAttr != null && expressionAttr.getAttributeValue() != null) {

                try {
                    route.setExpression(
                        SynapseXPathFactory.getSynapseXPath(routeElement, ATT_EXPRN));
                } catch (JaxenException e) {
                    handleException("Couldn't build the xpath from the expression : "
                        + expressionAttr.getAttributeValue(), e);
                }
            } else {

                handleException("Route without an expression attribute has been found, " +
                    "but it is required to have an expression for all routes");
            }

            if (matchAttr != null && matchAttr.getAttributeValue() != null) {
                route.setMatch(Pattern.compile(matchAttr.getAttributeValue()));
            }

            if (breakRouterAttr != null && breakRouterAttr.getAttributeValue() != null) {
                route.setBreakRouter(Boolean.parseBoolean(breakRouterAttr.getAttributeValue()));
            }

            if (targetElem != null) {

                route.setTarget(TargetFactory.createTarget(targetElem, properties));

            } else {
                handleException("Route has to have a target for it, " +
                    "missing the taregt of the route");
            }

            m.addRoute(route);
        }

        return m;
    }

    public QName getTagQName() {
        return ROUTER_Q;
    }
}