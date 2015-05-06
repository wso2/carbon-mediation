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
import org.wso2.carbon.inbound.endpoint.protocol.http.management.EndpointListenerManager;
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

        org.apache.synapse.MessageContext synCtx;

        //get already created axis2 message context
        MessageContext axis2MsgContext = getRequestContext();

        if (request != null) {
            String method = request.getRequest() != null ? request.getRequest().
                    getRequestLine().getMethod().toUpperCase() : "";

            //check the validity of message routing to axis2 path
            boolean isAxis2Path = isAllowedAxis2Path(axis2MsgContext);

            if (isAxis2Path) {
                //Create Axis2 message context using request properties
                processHttpRequestUri(axis2MsgContext, method);

                // setting Inbound related properties
                setInboundProperties(axis2MsgContext);

                if (!isRESTRequest(axis2MsgContext, method)) {
                    if (request.isEntityEnclosing()) {
                        processEntityEnclosingRequest(axis2MsgContext, isAxis2Path);
                    } else {
                        processNonEntityEnclosingRESTHandler(null, axis2MsgContext, isAxis2Path);
                    }
                }
            } else {
                try {
                    //create Synapse Message Context
                    synCtx = createSynapseMessageContext(request, axis2MsgContext);
                    MessageContext axisCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();

                    // setting Inbound related properties
                    setInboundProperties(synCtx);

                    processHttpRequestUri(axisCtx, method);

                    String tenantDomain = getTenantDomain();

                    String endpointName =
                            EndpointListenerManager.getInstance().getEndpointName(port, tenantDomain);

                    if (endpointName == null) {
                        handleException("Endpoint not found for port : " + port + "" +
                                        " tenant domain : " + tenantDomain);
                    }

                    InboundEndpoint endpoint = synCtx.getConfiguration().getInboundEndpoint(endpointName);

                    if (endpoint == null) {
                        log.error("Cannot find deployed inbound endpoint " + endpointName + "for process request");
                        return;
                    }

                    if (!isRESTRequest(axisCtx, method)) {
                        if (request.isEntityEnclosing()) {
                            processEntityEnclosingRequest(axisCtx, isAxis2Path);
                        } else {
                            processNonEntityEnclosingRESTHandler(null, axisCtx, isAxis2Path);
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
                        //If message is not dispatched to an API, dispatch into the sequence

                        // Get injecting sequence for synapse engine
                        SequenceMediator injectingSequence =
                                (SequenceMediator) synCtx.getSequence(endpoint.getInjectingSeq());
                        if (endpoint.getOnErrorSeq() != null) {
                            SequenceMediator faultSequence = (SequenceMediator) synCtx.getSequence(endpoint.getOnErrorSeq());

                            MediatorFaultHandler mediatorFaultHandler = new MediatorFaultHandler(faultSequence);
                            synCtx.pushFaultHandler(mediatorFaultHandler);
                        }

                        // handover synapse message context to synapse environment for inject it to given sequence in
                        //synchronous manner
                        if (log.isDebugEnabled()) {
                            log.debug("injecting message to sequence : " + endpoint.getInjectingSeq());
                        }
                        synCtx.getEnvironment().injectMessage(synCtx, injectingSequence);
                    }
                    // send ack for client if needed
                    sendAck(axisCtx);
                } catch (Exception e) {
                    log.error("Exception occurred when running " + InboundHttpServerWorker.class.getName(), e);
                }
            }
        } else {
            log.error("InboundSourceRequest cannot be null");
        }
    }

    // Create Synapse Message Context
    private org.apache.synapse.MessageContext createSynapseMessageContext(
            SourceRequest inboundSourceRequest, MessageContext axis2Context) throws AxisFault {

        // Create super tenant message context
        MessageContext axis2MsgCtx = axis2Context;
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MsgCtx.setServiceContext(svcCtx);
        axis2MsgCtx.setOperationContext(opCtx);


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

    // Setting Inbound Related Properties for Synapse Message Context
    private void setInboundProperties(org.apache.synapse.MessageContext msgContext) {
        msgContext.setProperty(SynapseConstants.IS_INBOUND, true);
        msgContext.setProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER,
                               new InboundHttpResponseSender());
        msgContext.setWSAAction(request.getHeaders().get(InboundHttpConstants.SOAP_ACTION));
    }

    // Setting Inbound Related Properties for Axis2 Message Context
    private void setInboundProperties(MessageContext axis2Context){
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

    private boolean isAllowedAxis2Path(MessageContext msgContext) {
        boolean isProxy = false;
        String reqUri = request.getUri();

        /*
           Get the operation part from the request URL
           e.g. /services/TestProxy > TestProxy when service path is '/service/'
         */
        String serviceOpPart = Utils.getServiceAndOperationPart(reqUri,
                                                                msgContext.getConfigurationContext().getServiceContextPath());

        if (serviceOpPart != null) {
            isProxy = true;
        }
        return isProxy;
    }


}
