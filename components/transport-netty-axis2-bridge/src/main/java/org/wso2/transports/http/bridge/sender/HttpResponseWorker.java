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
package org.wso2.transports.http.bridge.sender;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.wsdl.WSDLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transports.http.bridge.BridgeConstants;

import java.util.Map;
import java.util.TreeMap;

/**
 * {@code HttpResponseWorker} is the Thread which does the response processing.
 */
public class HttpResponseWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HttpResponseWorker.class);

    private HttpCarbonMessage httpResponse;
    private MessageContext requestMsgCtx;

    HttpResponseWorker(MessageContext requestMsgCtx, HttpCarbonMessage httpResponse) {
        this.httpResponse = httpResponse;
        this.requestMsgCtx = requestMsgCtx;
    }

    @Override
    public void run() {

        MessageContext responseMsgCtx;
        try {
            responseMsgCtx = requestMsgCtx.getOperationContext().
                    getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
        } catch (AxisFault ex) {
            LOG.error(BridgeConstants.BRIDGE_LOG_PREFIX + "Error getting response message context " +
                    "from the operation context", ex);
            return;
        }

        responseMsgCtx.setServerSide(true);
        responseMsgCtx.setDoingREST(requestMsgCtx.isDoingREST());
        responseMsgCtx.setProperty(MessageContext.TRANSPORT_IN,
                requestMsgCtx.getProperty(MessageContext.TRANSPORT_IN));
        responseMsgCtx.setTransportIn(requestMsgCtx.getTransportIn());
        responseMsgCtx.setTransportOut(requestMsgCtx.getTransportOut());
        responseMsgCtx.setAxisMessage(requestMsgCtx.getOperationContext().getAxisOperation().
                getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
        responseMsgCtx.setOperationContext(requestMsgCtx.getOperationContext());
        responseMsgCtx.setConfigurationContext(requestMsgCtx.getConfigurationContext());
        responseMsgCtx.setTo(null);
        // Set headers
        Map<String, String> headers = new TreeMap<>(String::compareToIgnoreCase);
        httpResponse.getHeaders().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
        responseMsgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, headers);
        String contentType = httpResponse.getHeader(BridgeConstants.CONTENT_TYPE_HEADER);
        responseMsgCtx.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);

        String charSetEncoding = BuilderUtil.getCharSetEncoding(contentType);
        if (contentType != null) {
            responseMsgCtx.setProperty(
                    Constants.Configuration.CHARACTER_SET_ENCODING,
                    contentType.indexOf("charset") >= 1 ?
                            charSetEncoding : MessageContext.DEFAULT_CHAR_SET_ENCODING);
        }

        // Set payload
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        try {
            responseMsgCtx.setEnvelope(envelope);
        } catch (AxisFault axisFault) {
            LOG.error(BridgeConstants.BRIDGE_LOG_PREFIX + "Error occurred while setting SOAP envelope", axisFault);
        }

        // Set status code
        int statusCode = (int) httpResponse.getProperty(BridgeConstants.HTTP_STATUS_CODE);
        responseMsgCtx.setProperty(BridgeConstants.HTTP_STATUS_CODE_PROP, statusCode);
        responseMsgCtx.setProperty(BridgeConstants.HTTP_STATUS_CODE_DESCRIPTION_PROP,
                httpResponse.getProperty(BridgeConstants.HTTP_REASON_PHRASE));
        responseMsgCtx.setServerSide(true);

        // Set rest of the properties
        responseMsgCtx.setProperty(BridgeConstants.HTTP_CARBON_MESSAGE, httpResponse);
        responseMsgCtx.setProperty(BridgeConstants.HTTP_CLIENT_REQUEST_CARBON_MESSAGE,
                requestMsgCtx.getProperty(BridgeConstants.HTTP_CLIENT_REQUEST_CARBON_MESSAGE));

        // Handover message to the axis engine for processing
        try {
            AxisEngine.receive(responseMsgCtx);
        } catch (AxisFault ex) {
            LOG.error(BridgeConstants.BRIDGE_LOG_PREFIX + "Error occurred while processing " +
                    "response message through Axis2", ex);
        }
    }
}
