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

package org.wso2.carbon.inbound.endpoint.protocol.http.netty.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.inbound.InboundMessageContextQueue;
import org.apache.synapse.transport.passthru.Pipe;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundHttpConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Sends responses to requests that are sent to the InboundEndpoint
 */

public class InboundSourceResponseSender implements Runnable{


    private Logger logger = Logger.getLogger(InboundSourceResponseSender.class);

    public void run() {

        while (true) {
            try {
                MessageContext smc = InboundMessageContextQueue.getInstance().getMessageContextQueue().take();

                    ChannelHandlerContext ctx = (ChannelHandlerContext) smc.getProperty(SynapseConstants.CHANNEL_HANDLER_CONTEXT);
                    //Retrieve the SOAP envelope from the MessageContext
                    SOAPEnvelope envelope = smc.getEnvelope();
                    String contentType = (String) ((Axis2MessageContext) smc).getAxis2MessageContext().getProperty(InboundHttpConstants.CONTENT_TYPE);
                    if (envelope.getBody().getFirstElement() == null) {
                        Pipe pipe = (Pipe) ((Axis2MessageContext) smc).getAxis2MessageContext().getProperty(InboundHttpConstants.PASS_THROUGH_TARGET_BUFFER);
                        FullHttpResponse fullHttpResponse = getHttpResponseFrombyte(pipe.getInputStream(), contentType);
                        //Send the envelope using the ChannelHandlerContext
                        ctx.writeAndFlush(fullHttpResponse);
                    }
                    else {
                        FullHttpResponse fullHttpResponse = getHttpResponse(envelope, contentType);
                        //Send the envelope using the ChannelHandlerContext
                        ctx.writeAndFlush(fullHttpResponse);
                    }

            } catch (InterruptedException e) {
                logger.error(e.getMessage());

        }
    }
    }

    private FullHttpResponse getHttpResponse(SOAPEnvelope soapEnvelope, String ContentType) {
        byte[] bytes = soapEnvelope.toString().getBytes();
        ByteBuf CONTENT = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(bytes));
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, CONTENT.duplicate());
        response.headers().set(CONTENT_TYPE, ContentType);
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        return response;
    }

    private FullHttpResponse getHttpResponseFrombyte(InputStream inputStream, String ContentType) {
        byte[] bytes = new byte[0];
        try {
            bytes = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
           logger.info(e.getMessage());
        }
        ;
        ByteBuf CONTENT = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(bytes));
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, CONTENT.duplicate());
        response.headers().set(CONTENT_TYPE, ContentType);
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        return response;

    }






    private SOAPEnvelope toSOAPENV(InputStream inputStream, int version) throws XMLStreamException {

        try {
            XMLStreamReader reader =
                    XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            SOAPFactory f = null;
            if (version == InboundHttpConstants.SOAP_11) {
                f = new SOAP11Factory();
            } else if (version == InboundHttpConstants.SOAP_12) {
                f = new SOAP12Factory();
            }
            StAXSOAPModelBuilder builder =

                    OMXMLBuilderFactory.createStAXSOAPModelBuilder(f, reader);
            SOAPEnvelope soapEnvelope = builder.getSOAPEnvelope();

            return soapEnvelope;

        } catch (XMLStreamException e) {
           logger.error("Error creating a OMElement from an input stream : ",
                   e);
            throw new XMLStreamException(e);
        }
    }



}
