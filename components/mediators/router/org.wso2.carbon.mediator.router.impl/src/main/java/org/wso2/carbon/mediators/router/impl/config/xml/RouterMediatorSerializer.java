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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.TargetSerializer;
import org.wso2.carbon.mediators.router.impl.Route;
import org.wso2.carbon.mediators.router.impl.RouterMediator;

/**
 * <p>Serializes the <code>RouterMediator</code> into the following configuration</p>
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
public class RouterMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator mediator) {

        if (!(mediator instanceof RouterMediator)) {
            handleException("Unsupported mediator passed in for serialization : "
                + mediator.getType());
        }

        RouterMediator m = (RouterMediator) mediator;

        OMElement routerElem = fac.createOMElement("router", synNS);
        saveTracingState(routerElem, m);

        if (m.isContinueAfter()) {
            routerElem.addAttribute(fac.createOMAttribute("continueAfter", nullNS, "true"));
        }

        for (Route route : m.getRoutes()) {

            OMElement routeElem = fac.createOMElement("route", synNS);

            if (route.getExpression() != null) {
                SynapseXPathSerializer.serializeXPath(
                    route.getExpression(), routeElem, "expression");
            } else {
                handleException("Incomplete route has been found in the " +
                    "serialization of the RouterMediator");
            }

            if (route.getMatch() != null) {
                routeElem.addAttribute(
                    fac.createOMAttribute("match", nullNS, route.getMatch().pattern()));
            }

            if (!route.isBreakRouter()) {
                routeElem.addAttribute(fac.createOMAttribute("breakRouter", nullNS, "false"));
            }

            if (route.getTarget() != null) {
                routeElem.addChild(TargetSerializer.serializeTarget(route.getTarget()));
            } else {
                handleException("Route without a target has been found in the " +
                    "serialization of the RouterMediator");
            }

            routerElem.addChild(routeElem);
        }
        serializeComments(routerElem, m.getCommentsList());
        return routerElem;
    }

    public String getMediatorClassName() {
        return RouterMediator.class.getName();
    }
}