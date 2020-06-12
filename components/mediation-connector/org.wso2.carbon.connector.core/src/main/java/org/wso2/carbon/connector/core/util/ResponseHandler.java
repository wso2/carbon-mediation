/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.connector.core.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayConstants;
import org.apache.synapse.transport.passthru.util.StreamingOnRequestDataSource;
import org.wso2.carbon.connector.core.exception.ContentBuilderException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Utils for setting content as the message payload in the message context
 */
public final class ResponseHandler {

    // Content Types
    private static final String APPLICATION_XML = "application/xml";
    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_XML = "text/xml";
    private static final String TEXT_CSV = "text/csv";
    private static final String TEXT_PLAIN = "text/plain";

    private static final QName TEXT_ELEMENT = new QName("http://ws.apache.org/commons/ns/payload",
            "text");

    private ResponseHandler() {

    }

    /**
     * Builds content according to the content type and set in the message body
     *
     * @param messageContext Current message content
     * @param inputStream    Content to be built as an input stream
     * @param contentType    Content Type of the content
     * @throws ContentBuilderException if failed to build the content
     */
    public static void setContent(MessageContext messageContext, InputStream inputStream, String contentType)
            throws ContentBuilderException {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        try {
            if (TEXT_XML.equalsIgnoreCase(contentType)
                    || APPLICATION_XML.equalsIgnoreCase(contentType)) {
                setXMLContent(inputStream, axis2MessageContext);
                handleSpecialProperties(APPLICATION_XML, axis2MessageContext);
            } else if (APPLICATION_JSON.equalsIgnoreCase(contentType)) {
                setJSONPayload(inputStream, axis2MessageContext);
                handleSpecialProperties(APPLICATION_JSON, axis2MessageContext);
            } else if (TEXT_PLAIN.equalsIgnoreCase(contentType)
                    || TEXT_CSV.equalsIgnoreCase(contentType)) {
                setTextContent(inputStream, axis2MessageContext);
                handleSpecialProperties(TEXT_PLAIN, axis2MessageContext);
            } else {
                setBinaryContent(inputStream, axis2MessageContext);
            }
        } catch (AxisFault e) {
            throw new ContentBuilderException("Failed to build content.", e);
        }
    }

    /**
     * Changes the content type and handles other headers
     *
     * @param resultValue     Value to be set in header
     * @param axis2MessageCtx Axis2 Message Context
     */
    public static void handleSpecialProperties(Object resultValue,
                                               org.apache.axis2.context.MessageContext axis2MessageCtx) {

        axis2MessageCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, resultValue);
        axis2MessageCtx.setProperty(Constants.Configuration.CONTENT_TYPE, resultValue);
        Map headers = (Map) axis2MessageCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            headers.remove(HTTP.CONTENT_TYPE);
            headers.put(HTTP.CONTENT_TYPE, resultValue);
        }
    }

    /**
     * Converts the XML String to XML Element and sets in message context
     *
     * @param messageContext The message context that is processed
     * @param xmlString      Output response
     */
    public static void preparePayload(MessageContext messageContext, String xmlString) throws ContentBuilderException {

        String error = "Failed to set response in payload.";
        OMElement element;
        try {
            element = AXIOMUtil.stringToOM(xmlString);
            setPayloadInEnvelope(((Axis2MessageContext) messageContext).getAxis2MessageContext(), element);
        } catch (XMLStreamException e) {
            throw new ContentBuilderException(error, e);
        }
    }

    /**
     * Overrides the payload in message body
     *
     * @param axis2MsgCtx Axis2MessageContext
     * @param payload     Payload to be set in the body
     * @throws ContentBuilderException if failed to set payload
     */
    public static void setPayloadInEnvelope(org.apache.axis2.context.MessageContext axis2MsgCtx, OMElement payload)
            throws ContentBuilderException {

        JsonUtil.removeJsonPayload(axis2MsgCtx);
        try {
            axis2MsgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(payload));
        } catch (AxisFault e) {
            throw new ContentBuilderException("Failed to set XML content.", e);
        }
    }

    /**
     * Builds and sets Binary content
     *
     * @param inputStream         Content as an input stream
     * @param axis2MessageContext Axis2 Message Context
     */
    private static void setBinaryContent(InputStream inputStream,
                                         org.apache.axis2.context.MessageContext axis2MessageContext) throws AxisFault {

        SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
        OMNamespace ns = factory.createOMNamespace(
                RelayConstants.BINARY_CONTENT_QNAME.getNamespaceURI(), "ns");
        OMElement element = factory.createOMElement(
                RelayConstants.BINARY_CONTENT_QNAME.getLocalPart(), ns);

        StreamingOnRequestDataSource ds = new StreamingOnRequestDataSource(inputStream);
        DataHandler dataHandler = new DataHandler(ds);

        //create an OMText node with the above DataHandler and set optimized to true
        OMText textData = factory.createOMText(dataHandler, true);
        element.addChild(textData);
        axis2MessageContext.setEnvelope(TransportUtils.createSOAPEnvelope(element));
    }

    /**
     * Builds and sets text content
     *
     * @param inputStream         Content as an input stream
     * @param axis2MessageContext Axis2 Message Context
     * @throws ContentBuilderException if failed to set text content
     */
    private static void setTextContent(InputStream inputStream,
                                       org.apache.axis2.context.MessageContext axis2MessageContext)
            throws ContentBuilderException {

        try {
            String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            setPayloadInEnvelope(axis2MessageContext, getTextElement(text));
        } catch (IOException e) {
            throw new ContentBuilderException("Failed to set text content.", e);
        }
    }

    /**
     * Builds and sets JSON content
     *
     * @param inputStream         Content as an input stream
     * @param axis2MessageContext Axis2 Message Context
     * @throws ContentBuilderException if failed to set JSON content
     */
    private static void setJSONPayload(InputStream inputStream,
                                       org.apache.axis2.context.MessageContext axis2MessageContext)
            throws ContentBuilderException {

        try {
            String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            JsonUtil.getNewJsonPayload(axis2MessageContext, text, true, true);
        } catch (IOException e) {
            throw new ContentBuilderException("Failed to set JSON content.", e);
        }
    }

    /**
     * Builds and sets XML content
     *
     * @param inputStream         Content as an input stream
     * @param axis2MessageContext Axis2 Message Context
     * @throws ContentBuilderException if failed to set XML content
     */
    private static void setXMLContent(InputStream inputStream,
                                      org.apache.axis2.context.MessageContext axis2MessageContext)
            throws ContentBuilderException {

        try {
            String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            OMElement omXML = AXIOMUtil.stringToOM(text);
            setPayloadInEnvelope(axis2MessageContext, omXML);
        } catch (IOException | XMLStreamException e) {
            throw new ContentBuilderException("Failed to set XML content.", e);
        }
    }

    /**
     * Gets text element
     *
     * @param content Content to be wrapped
     * @return Text Element
     */
    private static OMElement getTextElement(String content) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement textElement = factory.createOMElement(TEXT_ELEMENT);
        if (content == null) {
            content = StringUtils.EMPTY;
        }
        textElement.setText(content);
        return textElement;
    }

}
