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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.eip.Target;
import org.apache.synapse.util.MessageHelper;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Specifies the <code>Route</code> including the route conditions and the routing of a
 * particular route for the <code>RouterMediator</code>. This stores the properties of a particular
 * route and will be used by the <code>RouterMedaitor</code> in order to route the messages.</p>
 *
 * <p>This stores the routing path as a <code>Target</code> whihc can contain either a sequence or
 * else and endpoint as the routing path and in the endpoint case the message will be delivered to
 * the specified endpoint</p>
 *
 * @see RouterMediator
 * @see org.apache.synapse.mediators.eip.Target
 */
public class Route {

    /**
     * <p>The state of the <code>Route</code> after calling the <code>doRoute</code> method is this
     * particular route over the specified message does not match the conditions specified in this
     * route.</p>
     *
     * @see Route#doRoute(org.apache.synapse.MessageContext, SynapseLog)
     */
    public static final int ROUTE_NOT_MACHING_STATE = 0;

    /**
     * <p>The state of the <code>Route</code> after calling the <code>doRoute</code> method is; this
     * particular route over the specified message matches the conditions specified in this
     * route and hence the message is routed, and the message should not go though any more routes
     * within this router.</p>
     *
     * @see Route#doRoute(org.apache.synapse.MessageContext, SynapseLog)
     */
    public static final int ROUTED_WITH_BREAK_STATE = 1;

    /**
     * <p>The state of the <code>Route</code> after calling the <code>doRoute</code> method is; this
     * particular route over the specified message matches the conditions specified in this
     * route and hence the message is routed, but the message may go through any other matching
     * routes within this router for further routing.</p>
     *
     * @see Route#doRoute(org.apache.synapse.MessageContext, SynapseLog)
     */
    public static final int ROUTED_WITH_CONTINUE_STATE = 2;

    /**
     * <p>The state of the <code>Route</code> after calling the <code>doRoute</code> method is; this
     * particular route over the specified message matches the conditions specified in this
     * route and hence the message is routed, at the same time the message must not go through any
     * other matching routes within this router.</p>
     *
     * @see Route#doRoute(org.apache.synapse.MessageContext, SynapseLog)
     */
    public static final int ROUTED_AND_DROPPED_STATE = 3;

    /**
     * <p>The state of the routing, when there is an error in routing after executing the
     * <code>doRoute</code> method.<p>
     *
     * @see Route#doRoute(org.apache.synapse.MessageContext, SynapseLog)
     */
    public static final int UNEXPECTED_ROUTING_STATE = 4;

    /**
     * <p>XPath describing the element or the attirbute of the message which will be matched
     * againset the <code>match</code> to check the matching. If there is no <code>match</code>
     * then the presence of this expression will be taken as the matching</p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath expression;

    /**
     * <p>Regular expression which will be evaluated to see the string value of the
     * <code>expression</code> evaluated over the current message to check whether that value is
     * matching to the specified pattern to check the matching</p>
     *
     * @see java.util.regex.Pattern
     */
    private Pattern match;

    /**
     * <p>Specifies whether to break the route or to continue on further routes on matching this
     * <code>route</code>. If the value is true, and if the <code>route</code> is matching then no
     * further routes will be evaluated inside the <code>router</code>, where as if the value is
     * false the next route will be evaluated to check the matching regardless of whether the
     * current route matches or not.</p>
     *
     * <p>Default value for the <code>breakRouter</code> is true implying that the router will be
     * breaked after the route. (i.e. no further routes will occur in this router)</p>
     */
    private boolean breakRouter = true;

    /**
     * <p>Represents the <code>target</code> of the route and it can specify a <code>sequence</code>
     * or an <code>endpoint</code> for the matching messages whihc needs to be routed to. At the
     * same time it can also specify the <code>to</code> address as well as <code>soapAction</code>
     * for the routing.</p>
     */
    private Target target;

    /**
     * <p>Holds the log4j based log for the loggin purposes</p>
     */
    private static final Log log = LogFactory.getLog(Route.class);

    /**
     * <p>Checks whether this route is matching for the given message or not. In general if the
     * <code>expression</code> is provided without a <code>match</code> then the presence of a
     * element or attribute in the message specified by the <code>expression</code> will be taken as
     * matching</p>
     *
     * <p>If both the <code>expression</code> and the <code>match</code> is provided then the
     * evaluated string value of the <code>expression</code> over the message will be matched
     * againset the given regular expression <code>match</code> to test whether the string value is
     * matching for the pattern or not</p>
     *
     * @param synCtx message to be matched to check the route condition
     * @return whether the route matches the given message or not
     */
    public boolean isMatching(MessageContext synCtx, SynapseLog synLog) {
        // expression is required to perform the match
        if (expression != null) {

            // if the match Pattern is not there then we consider the
            // existance of the xpath in the message
            if (match == null) {

                try {
                    // check whether the xpath is present in the message or not
                    return expression.booleanValueOf(synCtx.getEnvelope());
                } catch (JaxenException e) {
                    handleException("Error evaluating XPath expression : " + expression, e, synCtx);
                }

            } else {

                String sourceString = expression.stringValueOf(synCtx);

                if (sourceString == null) {
                    if (synLog.isTraceOrDebugEnabled()) {
                        synLog.traceOrDebug("Source String : " + expression + " evaluates to null");
                    }
                    return false;
                }

                Matcher matcher = match.matcher(sourceString);

                if (matcher == null) {
                    if (synLog.isTraceTraceEnabled()) {
                        synLog.traceOrDebug("Regex pattern matcher for : " + match.pattern() +
                                "against source : " + sourceString + " is null");
                    }
                    return false;
                }

                // matchese the string value of the xpath over the message and
                // evalutaes with the specifeid regEx
                return matcher.matches();
            }

        } else {
            handleException("Couldn't find the routing expression", synCtx);
        }

        return false; // never executes
    }

    /**
     * <p>Routes the message in to the specified <code>target</code> whihc can be one of the
     * <code>sequence</code> or else and <code>endpoint</code> after checking whether the conditions
     * specified in this route matches the provided message.</p>
     *
     * <p>Also if this particular router specifies the <code>breakRouter</code> to be false then the
     * specified status will be returned to not to break the route and route further matchings in
     * the router. Otherwise it will break the route</p>
     *
     * @param synCtx message to be routed
     * @return state of the routing and this can be one of the ROUTE_NOT_MACHING_STATE, or
     * ROUTED_WITH_BREAK_STATE, or ROUTED_WITH_CONTINUE state.
     *
     * @see Route#isMatching(org.apache.synapse.MessageContext, SynapseLog)
     */
    public int doRoute(MessageContext synCtx, SynapseLog synLog) {
        // first check whether this Route matches the specified conditions
        if (isMatching(synCtx, synLog)) {

            // route the messages to the specified target
            if (target != null) {

                boolean mediationResult = true;

                    target.setAsynchronous(false);
                    target.mediate(synCtx);
                return breakRouter ? ROUTED_WITH_BREAK_STATE : ROUTED_WITH_CONTINUE_STATE;

            } else {

                handleException("Couldn't find the route target for the message", synCtx);
            }

        } else {

            // the route conditions do not match the message
            return ROUTE_NOT_MACHING_STATE;
        }

        // does not execute under normal circumstances
        return UNEXPECTED_ROUTING_STATE;
    }

    private void sendToEndpoint(MessageContext synCtx) {
        if (target.getEndpoint() != null) {
            target.getEndpoint().send(synCtx);
        } else if (target.getEndpointRef() != null) {
            Endpoint epr = synCtx.getConfiguration().getEndpoint(target.getEndpointRef());
            if (epr != null) {
                epr.send(synCtx);
            }
        }
    }

    public Pattern getMatch() {
        return match;
    }

    public void setMatch(Pattern match) {
        this.match = match;
    }

    public SynapseXPath getExpression() {
        return expression;
    }

    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }

    public boolean isBreakRouter() {
        return breakRouter;
    }

    public void setBreakRouter(boolean breakRouter) {
        this.breakRouter = breakRouter;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    private void handleException(String msg, Exception e, MessageContext msgContext) {
        log.error(msg, e);
        if (msgContext.getServiceLog() != null) {
            msgContext.getServiceLog().error(msg, e);
        }
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg, MessageContext msgContext) {
        log.error(msg);
        if (msgContext.getServiceLog() != null) {
            msgContext.getServiceLog().error(msg);
        }
        throw new SynapseException(msg);
    }
}