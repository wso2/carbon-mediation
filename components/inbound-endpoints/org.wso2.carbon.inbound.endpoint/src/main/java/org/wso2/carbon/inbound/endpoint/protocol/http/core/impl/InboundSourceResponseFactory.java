/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint.protocol.http.core.impl;


import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.nhttp.util.MessageFormatterDecoratorFactory;
import org.apache.synapse.transport.nhttp.util.NhttpUtil;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.util.PassThroughTransportUtils;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundConfiguration;

import java.util.*;

/**
 * Class responsible for create Inbound Response from Message Context
 */
public class InboundSourceResponseFactory {

    /**
     * @param msgContext          <> Axis2 Message Context</>
     * @param sourceRequest       <> Assocaited Inbound Request</>
     * @param sourceConfiguration <>Associated Inbound Configuration</>
     * @return <> InboundHttpSourceResponse</>
     */
    public static InboundHttpSourceResponse create(MessageContext msgContext,
                                                   InboundHttpSourceRequest sourceRequest,
                                                   InboundConfiguration sourceConfiguration) {
        // determine the status code to be sent
        int statusCode = PassThroughTransportUtils.determineHttpStatusCode(msgContext);

        InboundHttpSourceResponse sourceResponse =
                new InboundHttpSourceResponse(sourceConfiguration, statusCode, sourceRequest);


        if (msgContext.getProperty(MessageContext.TRANSPORT_HEADERS) instanceof Map) {
            // set any transport headers
            Map<String, String> transportHeaders =
                    (Map<String, String>) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            boolean forceContentLength =
                    msgContext.isPropertyTrue(NhttpConstants.FORCE_HTTP_CONTENT_LENGTH);
            boolean forceContentLengthCopy =
                    msgContext.isPropertyTrue(PassThroughConstants.COPY_CONTENT_LENGTH_FROM_INCOMING);

            if (forceContentLength && forceContentLengthCopy && msgContext.getProperty
                    (PassThroughConstants.ORGINAL_CONTEN_LENGTH) != null) {
                sourceResponse.addHeader(HTTP.CONTENT_LEN, (String) msgContext.getProperty
                        (PassThroughConstants.ORGINAL_CONTEN_LENGTH));
            }

            if (transportHeaders != null && msgContext.getProperty(Constants.Configuration.MESSAGE_TYPE) != null) {
                if (msgContext.getProperty(Constants.Configuration.CONTENT_TYPE) != null
                        && msgContext.getProperty(Constants.Configuration.CONTENT_TYPE).toString().contains
                        (PassThroughConstants.CONTENT_TYPE_MULTIPART_RELATED)) {
                    transportHeaders.put(Constants.Configuration.MESSAGE_TYPE,
                            PassThroughConstants.CONTENT_TYPE_MULTIPART_RELATED);
                } else {
                    Pipe pipe = (Pipe) msgContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
                    if (pipe != null && !Boolean.TRUE.equals(msgContext.getProperty
                            (PassThroughConstants.MESSAGE_BUILDER_INVOKED))) {
                        transportHeaders.put(HTTP.CONTENT_TYPE, (String) msgContext.getProperty
                                (Constants.Configuration.CONTENT_TYPE));
                    }
                }
            }

            // Adding formatters according to message context type
            Boolean noEntityBody = (Boolean) msgContext.getProperty(NhttpConstants.NO_ENTITY_BODY);
            if (noEntityBody == null || noEntityBody) {
                OMOutputFormat format = NhttpUtil.getOMOutputFormat(msgContext);
                transportHeaders = new HashMap<String, String>();
                MessageFormatter messageFormatter =
                        MessageFormatterDecoratorFactory.createMessageFormatterDecorator(msgContext);
                if (msgContext.getProperty(Constants.Configuration.MESSAGE_TYPE) == null) {
                    transportHeaders.put(HTTP.CONTENT_TYPE, messageFormatter.
                            getContentType(msgContext, format, msgContext.getSoapAction()));
                }
                addResponseHeader(sourceResponse, transportHeaders);


            }


            // Add excess response header.
            String excessProp = NhttpConstants.EXCESS_TRANSPORT_HEADERS;
            if (msgContext.getProperty(excessProp) instanceof Map) {
                Map<String, Collection<String>> excessHeaders =
                        (Map<String, Collection<String>>) msgContext.getProperty(excessProp);
                if (excessHeaders != null) {
                    for (Map.Entry<String, Collection<String>> entry : excessHeaders.entrySet()) {
                        for (String excessVal : entry.getValue()) {
                            sourceResponse.addHeader(entry.getKey(), excessVal);
                        }
                    }
                }
            }
        }
        return sourceResponse;
    }

    private static void addResponseHeader(InboundHttpSourceResponse sourceResponse, Map transportHeaders) {
        for (Object entryObj : transportHeaders.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            if (entry.getValue() != null && entry.getKey() instanceof String &&
                    entry.getValue() instanceof String) {
                sourceResponse.addHeader((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }
}
