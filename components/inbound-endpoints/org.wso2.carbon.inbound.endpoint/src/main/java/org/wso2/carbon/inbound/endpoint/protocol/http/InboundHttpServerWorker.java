/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.inbound.InboundEndpoint;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.mediators.MediatorFaultHandler;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.RESTRequestHandler;
import org.apache.synapse.transport.passthru.ServerWorker;
import org.apache.synapse.transport.passthru.SourceRequest;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.inbound.endpoint.protocol.http.management.HTTPEndpointManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.OutputStream;

/**
 * Create SynapseMessageContext from HTTP Request and inject it to the sequence in a synchronous manner
 * This is the worker for HTTP inbound related requests.
 */
public class InboundHttpServerWorker extends ServerWorker {

    private static final Log log = LogFactory.getLog(InboundHttpServerWorker.class);

    private SourceRequest request = null;
    private int port;
    private RESTRequestHandler restHandler;

    public InboundHttpServerWorker(int port, SourceRequest sourceRequest,
                                   SourceConfiguration sourceConfiguration,
                                   OutputStream outputStream) {
        super(sourceRequest, sourceConfiguration, outputStream);
        this.request = sourceRequest;
        this.port = port;
        restHandler = new RESTRequestHandler();

    }

    public void run() {
        if (request != null) {
            try {
                //get already created axis2 context from ServerWorker
                MessageContext axis2MsgContext = getRequestContext();

                //create Synapse Message Context
                org.apache.synapse.MessageContext synCtx =
                        createSynapseMessageContext(request, axis2MsgContext);
                updateAxis2MessageContextForSynapse(synCtx);

                setInboundProperties(synCtx);
                String method = request.getRequest() != null ? request.getRequest().
                        getRequestLine().getMethod().toUpperCase() : "";
                processHttpRequestUri(axis2MsgContext, method);

                String tenantDomain = getTenantDomain();
                String endpointName =
                        HTTPEndpointManager.getInstance().getEndpointName(port, tenantDomain);
                if (endpointName == null) {
                    handleException("Endpoint not found for port : " + port + "" +
                                    " tenant domain : " + tenantDomain);
                }
                InboundEndpoint endpoint = synCtx.getConfiguration().getInboundEndpoint(endpointName);

                if (endpoint == null) {
                    log.error("Cannot find deployed inbound endpoint " + endpointName + "for process request");
                    return;
                }

                if (!isRESTRequest(axis2MsgContext, method)) {
                    if (request.isEntityEnclosing()) {
                        processEntityEnclosingRequest(axis2MsgContext, false);
                    } else {
                        processNonEntityEnclosingRESTHandler(null, axis2MsgContext, false);
                    }
                }

                boolean processedByAPI = false;

                String apiDispatchingParam =
                        (String) endpoint.getParameter(
                                InboundHttpConstants.INBOUND_ENDPOINT_PARAMETER_API_DISPATCHING_ENABLED);
                if (apiDispatchingParam != null && Boolean.valueOf(apiDispatchingParam)) {
                    // Trying to dispatch to an API
                    processedByAPI = restHandler.process(synCtx);
                    if (log.isDebugEnabled()) {
                        log.debug("Dispatch to API state : enabled, Message is "
                                  + (!processedByAPI ? "NOT" : "") + "processed by an API");
                    }
                }

                if (!processedByAPI) {
                    //check the validity of message routing to axis2 path
                    boolean isAxis2Path = isAllowedAxis2Path(synCtx);

                    if (isAxis2Path) {
                        //create axis2 message context again to avoid settings updated above
                        axis2MsgContext = createMessageContext(null, request);

                        processHttpRequestUri(axis2MsgContext, method);

                        //set inbound properties for axis2 context
                        setInboundProperties(axis2MsgContext);

                        if (!isRESTRequest(axis2MsgContext, method)) {
                            if (request.isEntityEnclosing()) {
                                processEntityEnclosingRequest(axis2MsgContext, isAxis2Path);
                            } else {
                                processNonEntityEnclosingRESTHandler(null, axis2MsgContext, isAxis2Path);
                            }
                        }
                    } else {
                        // Get injecting sequence for synapse engine
                        SequenceMediator injectingSequence =
                                (SequenceMediator) synCtx.getSequence(endpoint.getInjectingSeq());
                        if (endpoint.getOnErrorSeq() != null) {
                            SequenceMediator faultSequence = (SequenceMediator) synCtx.getSequence(endpoint.getOnErrorSeq());

                            MediatorFaultHandler mediatorFaultHandler = new MediatorFaultHandler(faultSequence);
                            synCtx.pushFaultHandler(mediatorFaultHandler);
                        }

                        /* handover synapse message context to synapse environment for inject it to given sequence in
                        synchronous manner*/
                        if (log.isDebugEnabled()) {
                            log.debug("injecting message to sequence : " + endpoint.getInjectingSeq());
                        }
                        synCtx.getEnvironment().injectMessage(synCtx, injectingSequence);
                    }
                    // send ack for client if needed
                    sendAck(axis2MsgContext);
                }
            } catch (Exception e) {
                log.error("Exception occurred when running " + InboundHttpServerWorker.class.getName(), e);
            }
        } else {
            log.error("InboundSourceRequest cannot be null");
        }
    }

    /**
     * Set Inbound Related Properties for Synapse Message Context
     *
     * @param msgContext Synapse Message Context of incoming request
     */
    private void setInboundProperties(org.apache.synapse.MessageContext msgContext) {
        msgContext.setProperty(SynapseConstants.IS_INBOUND, true);
        msgContext.setProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER,
                               new InboundHttpResponseSender());
        msgContext.setWSAAction(request.getHeaders().get(InboundHttpConstants.SOAP_ACTION));
    }

    /**
     * Set Inbound Related Properties for Axis2 Message Context
     *
     * @param axis2Context Axis2 Message Context of incoming request
     */
    private void setInboundProperties(MessageContext axis2Context) {
        axis2Context.setProperty(SynapseConstants.IS_INBOUND, true);
    }

    private String getTenantDomain() {
        String tenant = MultitenantUtils.getTenantDomainFromUrl(request.getUri());
        if (tenant.equals(request.getUri())) {
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return tenant;
    }

    protected void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    /**
     * Checks whether the message should be routed to Axis2 path
     *
     * @param synapseMsgContext Synapse Message Context of incoming message
     * @return true if the message should be routed, false otherwise
     */
    private boolean isAllowedAxis2Path(org.apache.synapse.MessageContext synapseMsgContext) {
        boolean isProxy = false;

        String reqUri = request.getUri();
        String tenant = MultitenantUtils.getTenantDomainFromUrl(request.getUri());
        String servicePath = getSourceConfiguration().getConfigurationContext().getServicePath();

        //for tenants, service path will be appended by tenant name
        if (!reqUri.equalsIgnoreCase(tenant)) {
            servicePath = servicePath + "/t/" + tenant;
        }

        //Get the operation part from the request URL
        // e.g. '/services/TestProxy/' > TestProxy when service path is '/service/' > result 'TestProxy/'
        String serviceOpPart = Utils.getServiceAndOperationPart(reqUri,
                                                                servicePath);
        //if proxy, then check whether it is deployed in the environment
        if (serviceOpPart != null) {
            isProxy = isProxyDeployed(synapseMsgContext, serviceOpPart);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Requested Proxy Service '" + serviceOpPart + "' is not deployed");
            }
        }
        return isProxy;
    }

    /**
     * Checks whether the given proxy is deployed in synapse environment
     *
     * @param synapseContext   Synapse Message Context of incoming message
     * @param serviceOpPart String name of the service operation
     * @return true if the proxy is deployed, false otherwise
     */
    private boolean isProxyDeployed(org.apache.synapse.MessageContext synapseContext,
                                    String serviceOpPart) {
        boolean isDeployed = false;

        //extract proxy name from serviceOperation, get the first portion split by '/'
        String proxyName = serviceOpPart.split("/")[0];

        //check whether the proxy is deployed in synapse environment
        if (synapseContext.getConfiguration().getProxyService(proxyName) != null) {
            isDeployed = true;
        }
        return isDeployed;
    }

    /**
     * Creates synapse message context from axis2 context
     *
     * @param inboundSourceRequest Source Request of inbound
     * @param axis2Context         Axis2 message context of message
     * @return Synapse Message Context instance
     * @throws AxisFault
     */
    private org.apache.synapse.MessageContext createSynapseMessageContext(
            SourceRequest inboundSourceRequest, MessageContext axis2Context) throws AxisFault {

        // Create super tenant message context
        MessageContext axis2MsgCtx = axis2Context;

        String tenantDomain = getTenantDomain();
        // If not super tenant, assign tenant configuration context
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            ConfigurationContext tenantConfigCtx =
                    TenantAxisUtils.getTenantConfigurationContext(tenantDomain,
                                                                  axis2MsgCtx.getConfigurationContext());
            axis2MsgCtx.setConfigurationContext(tenantConfigCtx);
            axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, tenantDomain);
        }
        return MessageContextCreatorForAxis2.getSynapseMessageContext(axis2MsgCtx);
    }

    /**
     * Updates additional properties in Axis2 Message Context from Synapse Message Context
     *
     * @param synCtx Synapse Message Context
     * @return Updated Axis2 Message Context
     * @throws AxisFault
     */
    private org.apache.synapse.MessageContext updateAxis2MessageContextForSynapse(
            org.apache.synapse.MessageContext synCtx) throws AxisFault {

        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);

        ((Axis2MessageContext) synCtx).getAxis2MessageContext().setServiceContext(svcCtx);
        ((Axis2MessageContext) synCtx).getAxis2MessageContext().setOperationContext(opCtx);

        return synCtx;
    }
}