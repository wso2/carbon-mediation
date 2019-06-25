/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.transports.http.bridge.listener;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transports.http.bridge.BridgeConstants;

import java.net.InetSocketAddress;

import static org.wso2.transports.http.bridge.BridgeConstants.CONTENT_TYPE_HEADER;
import static org.wso2.transports.http.bridge.BridgeConstants.SOAP_ACTION_HEADER;

/**
 * {@code HttpRequestWorker} is the Thread which does the request processing.
 */
public class HttpRequestWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestWorker.class);
    private ConfigurationContext configurationContext;
    private HttpCarbonMessage incomingCarbonMsg;

    HttpRequestWorker(HttpCarbonMessage incomingCarbonMsg, ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        this.incomingCarbonMsg = incomingCarbonMsg;
    }

    @Override
    public void run() {
        MessageContext msgCtx = RequestUtils.convertCarbonMsgToAxis2MsgCtx(configurationContext, incomingCarbonMsg);
        processHttpRequestUri(msgCtx);
        populateProperties(msgCtx);
        try {
            AxisEngine.receive(msgCtx);
        } catch (AxisFault ex) {
            LOG.error(BridgeConstants.BRIDGE_LOG_PREFIX + "Error occurred while processing the request", ex);
        }
    }

    private void processHttpRequestUri(MessageContext msgCtx) {

        String servicePrefixIndex = "://";
        String oriUri =
                (String) incomingCarbonMsg.getProperty(org.wso2.transport.http.netty.contract.Constants.REQUEST_URL);
        String restUrlPostfix = RequestUtils.getRestUrlPostfix(oriUri, configurationContext.getServicePath());

        String servicePrefix = oriUri.substring(0, oriUri.indexOf(restUrlPostfix));
        if (!servicePrefix.contains(servicePrefixIndex)) {
            InetSocketAddress localAddress =
                    (InetSocketAddress) incomingCarbonMsg.getProperty(
                            org.wso2.transport.http.netty.contract.Constants.LOCAL_ADDRESS);
            if (localAddress != null) {
                servicePrefix =
                        incomingCarbonMsg.getProperty(org.wso2.transport.http.netty.contract.Constants.PROTOCOL) +
                        servicePrefixIndex + localAddress.getHostName() + ":" +
                        incomingCarbonMsg.getProperty(
                                org.wso2.transport.http.netty.contract.Constants.LISTENER_PORT) + servicePrefix;
            }
        }
        msgCtx.setProperty(BridgeConstants.SERVICE_PREFIX, servicePrefix);
        msgCtx.setTo(new EndpointReference(restUrlPostfix));
        msgCtx.setProperty(BridgeConstants.REST_URL_POSTFIX, restUrlPostfix);
        String requestUri = (String) incomingCarbonMsg.getProperty(
                org.wso2.transport.http.netty.contract.Constants.REQUEST_URL);
        msgCtx.setTo(new EndpointReference(requestUri));
    }

    private void populateProperties(MessageContext msgCtx) {
        String contentTypeHeader = incomingCarbonMsg.getHeaders().get(CONTENT_TYPE_HEADER);
        String charSetEncoding = null;
        String contentType = null;
        if (contentTypeHeader != null) {
            charSetEncoding = BuilderUtil.getCharSetEncoding(contentTypeHeader);
            contentType = TransportUtils.getContentType(contentTypeHeader, msgCtx);
        }
        msgCtx.setProperty(Constants.Configuration.CONTENT_TYPE, contentTypeHeader);
        msgCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
        if (contentTypeHeader == null || RequestUtils.isRESTRequest(contentTypeHeader) ||
            RequestUtils.isRest(contentTypeHeader)) {
            msgCtx.setProperty(BridgeConstants.REST_REQUEST_CONTENT_TYPE, contentType);
            msgCtx.setDoingREST(true);
        }

        // get the contentType of char encoding
        if (charSetEncoding == null) {
            charSetEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }
        msgCtx.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEncoding);

        msgCtx.setProperty(BridgeConstants.HTTP_METHOD, incomingCarbonMsg.getProperty(BridgeConstants.HTTP_METHOD));
        msgCtx.setServerSide(true);

        String soapAction = incomingCarbonMsg.getHeaders().get(SOAP_ACTION_HEADER);
        if ((soapAction != null) && soapAction.startsWith("\"") && soapAction.endsWith("\"")) {
            soapAction = soapAction.substring(1, soapAction.length() - 1);
            msgCtx.setSoapAction(soapAction);
        }
        int soapVersion =
                RequestUtils.populateSOAPVersion(msgCtx, soapAction, contentTypeHeader);
        SOAPEnvelope envelope;
        if (soapVersion == 1) {
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
            envelope = fac.getDefaultEnvelope();
        } else {
            SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
            envelope = fac.getDefaultEnvelope();
        }
        try {
            msgCtx.setEnvelope(envelope);
        } catch (AxisFault ex) {
            LOG.error("{} Error occurred while setting the soap envelope", BridgeConstants.BRIDGE_LOG_PREFIX, ex);
        }
    }
}
