/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.http.netty.impl;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.apache.synapse.core.SynapseEnvironment;

/**
 * actuall event handling class for netty
 */
public class InboundHttpTransportSourceHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = Logger.getLogger(InboundHttpTransportSourceHandler.class);
    private SynapseEnvironment synapseEnvironment;
    private String injectSeq;
    private String faultSeq;


    public InboundHttpTransportSourceHandler(SynapseEnvironment synapseEnvironment, String injectSeq, String faultSeq) {
        this.synapseEnvironment = synapseEnvironment;
        this.injectSeq = injectSeq;
        this.faultSeq = faultSeq;

    }


    private InboundRequestQueue inboundRequestQueue = new InboundRequestQueue();

    /**
     * activating registerd hndler to accept events.
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    /**
     * reciving events through netty.
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InboundSourceRequest inboundSourceRequest = new InboundSourceRequest();
        if (msg instanceof DefaultFullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            HttpHeaders headers = fullHttpRequest.headers();
            for (String val : headers.names()) {
                inboundSourceRequest.addHttpheaders(val, headers.get(val));
            }
           inboundSourceRequest.setTo(fullHttpRequest.getUri());
            ByteBuf buf = fullHttpRequest.content();
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            inboundSourceRequest.setContentBytes(bytes);
            HttpHeaders trailingHeaders = fullHttpRequest.trailingHeaders();
            for (String val : trailingHeaders.names()) {
                inboundSourceRequest.addHttpTrailingheaders(val, trailingHeaders.get(val));
            }
        }
        inboundSourceRequest.setChannelHandlerContext(ctx);
        inboundSourceRequest.setSynapseEnvironment(this.synapseEnvironment);
        inboundSourceRequest.setInjectSeq(this.injectSeq);
        inboundSourceRequest.setFaultSeq(this.faultSeq);
        inboundRequestQueue.publish(inboundSourceRequest);
    }

}
