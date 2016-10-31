/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.http2.transport.util;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;

import java.util.*;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;

import java.util.SortedMap;
import java.util.TreeMap;


public class Http2ClientHandler extends SimpleChannelInboundHandler<Object> {

    private volatile SortedMap<Integer, Map.Entry<ChannelFuture, ChannelPromise>> streamidPromiseMap;
    private volatile TreeMap<Integer, MessageContext> requests;
    private Log log = LogFactory.getLog(Http2ClientHandler.class);
    private int currentStreamId = 3;
    private Channel channel;
    private TargetConfiguration targetConfig;
    private volatile TreeMap<Integer, Http2Response> responseMap;
    private boolean streamIdOverflow = false;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    public Http2ClientHandler() {
        streamidPromiseMap = new TreeMap<Integer, Map.Entry<ChannelFuture, ChannelPromise>>();
        requests = new TreeMap<Integer, MessageContext>();
        responseMap = new TreeMap<>();
    }


    public void put(int streamId, Object request) {
        streamidPromiseMap.put(streamId,
                new AbstractMap.SimpleEntry<ChannelFuture, ChannelPromise>(channel.writeAndFlush(request), channel.newPromise()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Integer streamId = ((FullHttpResponse) msg).headers().getInt(
                HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());

        if (streamId == null) {
            log.error("unexpected message received: " + msg);
            return;
        }
        MessageContext request = requests.get(streamId);
        if (request == null) {
            log.error("Message received without a request");
            return;
        }
        log.info("Respond received for stream id:" + streamId);
        Http2Response response;
        if (responseMap.containsKey(streamId)) {
            response = responseMap.get(streamId);
        } else {
            if (msg instanceof Http2HeadersFrame) {
                Http2HeadersFrame res = (Http2HeadersFrame) msg;
                response = new Http2Response(res);
            } else {
                FullHttpResponse res = (FullHttpResponse) msg;
                response = new Http2Response(res);
            }
            responseMap.put(streamId, response);
        }
        if (msg instanceof Http2DataFrame) {
            response.setDataFrame((Http2DataFrame) msg);
        }
        if (response.isEndOfStream()) {
            Http2ClientWorker clientWorker = new Http2ClientWorker(targetConfig, request, response);
            clientWorker.injectToAxis2Engine();
            requests.remove(streamId);
            responseMap.remove(streamId);
            streamidPromiseMap.remove(streamId);
        }

    }

    public int getStreamId() {
        int returnId = currentStreamId;
        if (currentStreamId > Integer.MAX_VALUE - 10) {   //Max stream_id is Integer.Max-10
            streamIdOverflow = true;
        }
        currentStreamId += 2;
        return returnId;
    }

    public boolean isStreamIdOverflow() {
        return streamIdOverflow;
    }

    public synchronized void setRequest(int streamId, MessageContext msgCtx) {
        requests.put(streamId, msgCtx);
    }

    public void setTargetConfig(TargetConfiguration targetConfiguration) {
        this.targetConfig = targetConfiguration;
    }
}
