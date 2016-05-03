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

package org.wso2.carbon.mediators.router.impl;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.eip.Target;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Routes the messages going through this mediator in to the specified target sequecne or to the
 * taregt endpoint if the specified conditions under a particular route is matching. There can be
 * several routes and one can specify to break after the first matching route using the
 * <code>breakRouter</code> attirbute in the routes and hence the route order has some impact on the
 * routings</p>
 * <p/>
 * <p>Normally message routing is redirecting the message, so this mediator also permits further
 * mediation in this path where as the message is mediated using the roted path(s) by default, but
 * it can be configured by the <code>continueAfter</code> attribute if required to continue further
 * mediation of the path</p>
 * <p/>
 * <p>Individual routes are stored as <code>Route</code> objects keeping there conditions inside it
 * with the routing targets as <code>Target<code> objects inside a particualr route and there is a
 * linked list of <code>Route</code> per mediator.</p>
 *
 * @see Route
 * @see org.apache.synapse.mediators.eip.Target
 * @see org.apache.synapse.Mediator
 * @see org.apache.synapse.mediators.AbstractMediator
 */
public class RouterMediator extends AbstractMediator implements ManagedLifecycle {

    /**
     * <p>List of routes to be checked and routed on arrival of a message. These routes has an order
     * when executing and hence kept in a <code>LinkedList</code>. These route will be taken in to
     * the order and checked for matching of the conditions specified inside the routes and then the
     * routing will be called over the route (i.e. message will be mediated using the target)</p>
     * <p/>
     * <p>By default if there is a matching route then the next routes will not be invoked because
     * the message has been routed, but this can be modified by using the <code>breakRouter</code>
     * attribute in the route.</p>
     *
     * @see Route
     * @see java.util.LinkedList
     */
    private List<Route> routes = new LinkedList<Route>();

    /**
     * <p>Specifies whether to continue further mediation on the current path apart from the routed
     * path. If the value is set to true this will not permit further mediation along the path, but
     * the defautl value for this is false implying by default the router stops the current path
     * mediation and redirects the message to the routed path</p>
     */
    private boolean continueAfter = false;

    /**
     * <p>Routes the message depending on the specified set of routes if the routing condition is
     * matching over the provided message. There can be a list of routers specified in to a
     * particualr order, and these routes will be invoked to route the message in the specified
     * order.</p>
     * <p/>
     * <p>If there is a matching route found over the message then the routing will occur and after
     * this synchronized routing further mediation of the message through other routes specified
     * after the matching route will be decided by the value of the <code>breakRouter</code>
     * attribute of the route.</p>
     * <p/>
     * <p>By default this mediator will drop the message stopping further medaition along the
     * current path and this also can be modified by using the <code>continueAfter</code> attribute
     * of the mediator.</p>
     *
     * @param synCtx message to be routed
     * @return whether to continue further mediaiton or not as a boolean value
     * @see org.apache.synapse.Mediator#mediate(org.apache.synapse.MessageContext)
     * @see Route#doRoute(org.apache.synapse.MessageContext, SynapseLog)
     */
    public boolean mediate(MessageContext synCtx) {

        if (synCtx.getEnvironment().isDebuggerEnabled()) {
            if (super.divertMediationRoute(synCtx)) {
                return true;
            }
        }

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Router mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        for (Route route : routes) {

            int routeState = route.doRoute(synCtx, synLog);

            // if the message does not match the route conditions
            if (Route.ROUTE_NOT_MACHING_STATE == routeState && synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("The message does not matches the routing conditions : " +
                        "Expression = '" + route.getExpression() + "'" + (route.getMatch() != null ?
                        " Pattern = '" + route.getMatch().toString() + "'" : ""));
            } else {

                // get the route target for logging purposes
                Target routeTarget = route.getTarget();
                String targetString = null;

                if (routeTarget != null) {

                    // prepare a target string for loggin purposes
                    if (routeTarget.getSequenceRef() != null) {
                        targetString = "Sequence <" + routeTarget.getSequenceRef() + ">";
                    } else if (routeTarget.getSequence() != null) {
                        targetString = "Sequence <annonymous>";
                    } else if (routeTarget.getEndpointRef() != null) {
                        targetString = "Endpoint <" + routeTarget.getEndpointRef() + ">";
                    } else if (routeTarget.getEndpoint() != null) {
                        targetString = "Endpoint <annonymous>";
                    } else {
                        targetString = "without an endpoint or a sequence";
                    }
                }

                // if routed and the message may route furhter using the existing routes
                if (Route.ROUTED_WITH_CONTINUE_STATE == routeState && synLog.isTraceOrDebugEnabled()) {

                    synLog.traceOrDebug("The message has been routed to the target : "
                            + targetString + ", but further routings are allowed");

                    // if routed and the message should not be routed with other remaining routes
                } else if (Route.ROUTED_WITH_BREAK_STATE == routeState) {

                    synLog.traceOrDebug("The message has been routed to the target : "
                            + targetString + ", and no further routes are allowed");

                    // break this router permitting further routings
                    break;
                } else if (Route.ROUTED_AND_DROPPED_STATE == routeState) {

                    synLog.traceOrDebug("The message has been routed to the target : "
                            + targetString + ", and the message is droped on the route");

                    return false;
                }
            }
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : Router mediator");
        }

        // normally message routing is redirecting the message, so this permits further mediation
        // in this path where as the message is mediated using the roted path(s) by default, but it
        // can be configured by the continueAfter attribute if required
        return continueAfter;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public boolean isContinueAfter() {
        return continueAfter;
    }

    public void setContinueAfter(boolean continueAfter) {
        this.continueAfter = continueAfter;
    }

    /**
     * called on Router startup. initializes inline sequences and endpoints
     *
     * @param synapseEnvironment
     */
    public void init(SynapseEnvironment synapseEnvironment) {
        Iterator<Route> allRoutes = routes.iterator();
        //for all routes
        while (allRoutes.hasNext()) {
            Route route = allRoutes.next();
            Target routingTarget = route.getTarget();
            SequenceMediator synSeqForRoute;
            if (routingTarget != null) {
                synSeqForRoute = routingTarget.getSequence();
                //init routing sequence so that each inline endpoints,etc get initialized
                if (synSeqForRoute != null) {
                    synSeqForRoute.init(synapseEnvironment);
                }

                //init routing address endpoints
                Endpoint endpoint = routingTarget.getEndpoint();
                if (endpoint != null) {
                    endpoint.init(synapseEnvironment);
                }
            }
        }
    }

    /**
     * called when Router is destroyed
     */
    public void destroy() {
        Iterator<Route> allRoutes = routes.iterator();
        //for all routes
        while (allRoutes.hasNext()) {
            Route route = allRoutes.next();
            Target routingTarget = route.getTarget();
            SequenceMediator synSeqForRoute;
            if (routingTarget != null) {
                synSeqForRoute = routingTarget.getSequence();
                //destroy and clean up each inline seq
                if (synSeqForRoute != null) {
                    synSeqForRoute.destroy();
                }
            }
        }

    }
}