/*
 * Copyright (c) 2019. WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.transports.http.bridge.util;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.ApplicationXMLFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.SOAPMessageFormatter;
import org.apache.axis2.transport.http.XFormURLEncodedFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transports.http.bridge.BridgeConstants;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;


/**
 * Class MessageUtils contains helper methods that are used to build the payload.
 */
public class MessageUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtils.class);
    private static final DeferredMessageBuilder messageBuilder = new DeferredMessageBuilder();

    private static boolean noAddressingHandler = false;

    private static volatile Handler addressingInHandler = null;

    public static void buildMessage(MessageContext msgCtx) {

        if (Boolean.TRUE.equals(msgCtx.getProperty(BridgeConstants.MESSAGE_BUILDER_INVOKED))) {
            return;
        }

        HttpCarbonMessage httpCarbonMessage =
                (HttpCarbonMessage) msgCtx.getProperty(BridgeConstants.HTTP_CARBON_MESSAGE);

        HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);

        long contentLength = BridgeConstants.NO_CONTENT_LENGTH_FOUND;
        String lengthStr = httpCarbonMessage.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
        try {
            contentLength = lengthStr != null ? Long.parseLong(lengthStr) : contentLength;
            if (contentLength == BridgeConstants.NO_CONTENT_LENGTH_FOUND) {
                // read one byte to make sure the incoming stream has data
                httpCarbonMessage.countMessageLengthTill(BridgeConstants.ONE_BYTE);
            }
        } catch (NumberFormatException e) {
            LOGGER.error("NumberFormatException");
        }

        InputStream in = httpMessageDataStreamer.getInputStream();

        // TODO: implement earlyBuild

        OMElement element = null;
        try {
            element = messageBuilder.getDocument(msgCtx, in);
            if (element != null) {
                msgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(element));
                msgCtx.setProperty(DeferredMessageBuilder.RELAY_FORMATTERS_MAP,
                        messageBuilder.getFormatters());
                msgCtx.setProperty(BridgeConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);

                // TODO: implement XML/JSON force validation
            }
        } catch (IOException | XMLStreamException e) {
            msgCtx.setProperty(BridgeConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
            // handleException("Error while building Passthrough stream", e);
        }
    }

    /**
     * Function to check given inputstream is empty or not
     * Used to check whether content of the payload input stream is empty or not.
     *
     * @param inputStream target inputstream
     * @return true if it is a empty stream
     * @throws IOException
     */
    public static boolean isEmptyPayloadStream(InputStream inputStream) throws IOException {

        boolean isEmptyPayload = true;

        if (inputStream != null) {
            // read ahead few characters to see if the stream is valid.

            /**
             * Checks for all empty or all whitespace streams and if found  sets isEmptyPayload to false. The while
             * loop exits if found any character other than space or end of stream reached.
             **/
            int c = inputStream.read();
            while (c != -1) {
                if (c != 32) {
                    //if not a space, should be some character in entity body
                    isEmptyPayload = false;
                    break;
                }
                c = inputStream.read();
            }
            inputStream.reset();
        }

        return isEmptyPayload;
    }

    /**
     * This selects the formatter for a given message format based on the the content type of the received message.
     * content-type to builder mapping can be specified through the Axis2.xml.
     *
     * @param msgContext axis2 MessageContext
     * @return the formatter registered against the given content-type
     */
    public static MessageFormatter getMessageFormatter(MessageContext msgContext) {

        MessageFormatter messageFormatter = null;
        String messageFormatString = getMessageFormatterProperty(msgContext);
        messageFormatString = getContentTypeForFormatterSelection(messageFormatString, msgContext);
        if (messageFormatString != null) {
            messageFormatter = msgContext.getConfigurationContext()
                    .getAxisConfiguration().getMessageFormatter(messageFormatString);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Message format is: " + messageFormatString
                             + "; message formatter returned by AxisConfiguration: " + messageFormatter);
            }
        }
        if (messageFormatter == null) {
            messageFormatter = (MessageFormatter) msgContext.getProperty(Constants.Configuration.MESSAGE_FORMATTER);
            if (messageFormatter != null) {
                return messageFormatter;
            }
        }
        if (messageFormatter == null) {

            // If we are doing rest better default to Application/xml formatter
            if (msgContext.isDoingREST()) {
                String httpMethod = (String) msgContext.getProperty(Constants.Configuration.HTTP_METHOD);
                if (Constants.Configuration.HTTP_METHOD_GET.equals(httpMethod) ||
                    Constants.Configuration.HTTP_METHOD_DELETE.equals(httpMethod)) {
                    return new XFormURLEncodedFormatter();
                }
                return new ApplicationXMLFormatter();
            } else {
                // Lets default to SOAP formatter
                messageFormatter = new SOAPMessageFormatter();
            }
        }
        return messageFormatter;
    }

    private static String getMessageFormatterProperty(MessageContext msgContext) {
        String messageFormatterProperty = null;
        Object property = msgContext
                .getProperty(Constants.Configuration.MESSAGE_TYPE);
        if (property != null) {
            messageFormatterProperty = (String) property;
        }
        if (messageFormatterProperty == null) {
            Parameter parameter = msgContext
                    .getParameter(Constants.Configuration.MESSAGE_TYPE);
            if (parameter != null) {
                messageFormatterProperty = (String) parameter.getValue();
            }
        }
        return messageFormatterProperty;
    }

    private static String getContentTypeForFormatterSelection(String type, MessageContext msgContext) {
        /*
         * Handle special case where content-type : text/xml and SOAPAction = null consider as
         * POX (REST) message not SOAP 1.1.
         *
         * 1.) it's required use the Builder associate with "application/xml" here but should not
         * change content type of current message.
         */
        String cType = type;
        if (msgContext.isDoingREST() && HTTPConstants.MEDIA_TYPE_TEXT_XML.equals(type)) {
            cType = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, HTTPConstants.MEDIA_TYPE_TEXT_XML);
        }
        return cType;
    }

    public static OMOutputFormat getOMOutputFormat(MessageContext msgContext) {

        OMOutputFormat format = null;
        if (msgContext.getProperty(BridgeConstants.MESSAGE_OUTPUT_FORMAT) != null) {
            format = (OMOutputFormat) msgContext.getProperty(BridgeConstants.MESSAGE_OUTPUT_FORMAT);
        } else {
            format = new OMOutputFormat();
        }

        msgContext.setDoingMTOM(TransportUtils.doWriteMTOM(msgContext));
        msgContext.setDoingSwA(TransportUtils.doWriteSwA(msgContext));
        msgContext.setDoingREST(TransportUtils.isDoingREST(msgContext));

        /*
         *  PassThroughConstants.INVOKED_REST set to true here if isDoingREST is true -
         *  this enables us to check whether the original request to the endpoint was a
         * REST request inside DefferedMessageBuilder (which we need to convert
         * text/xml content type into application/xml if the request was not a SOAP
         * request.
         */
        if (msgContext.isDoingREST()) {
            msgContext.setProperty(BridgeConstants.INVOKED_REST, true);
        }
        format.setSOAP11(msgContext.isSOAP11());
        format.setDoOptimize(msgContext.isDoingMTOM());
        format.setDoingSWA(msgContext.isDoingSwA());

        format.setCharSetEncoding(TransportUtils.getCharSetEncoding(msgContext));
        Object mimeBoundaryProperty = msgContext.getProperty(Constants.Configuration.MIME_BOUNDARY);
        if (mimeBoundaryProperty != null) {
            format.setMimeBoundary((String) mimeBoundaryProperty);
        }

        return format;
    }
}
