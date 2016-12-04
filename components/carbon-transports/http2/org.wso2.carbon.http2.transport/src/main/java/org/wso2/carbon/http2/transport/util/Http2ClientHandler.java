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
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2Frame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;
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

public class Http2ClientHandler extends ChannelDuplexHandler{


    private Http2RequestWriter writer;
    private Http2ResponseReceiver receiver;
    private Http2Connection connection;
    private Http2ConnectionEncoder encoder;
    private ChannelHandlerContext chContext;
    private Map<Integer,MessageContext> sentRequests;
    private Map<Integer,Http2StreamFrame> pendingResponses;

    public Http2ClientHandler(Http2Connection connection) {
        this.connection=connection;
        sentRequests=new TreeMap<>();
        pendingResponses=new TreeMap<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        if(msg)
    }


























    /*@Deprecated
    public void put(int streamId, Object request) {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
       if(msg instanceof Http2DataFrame){
           handleHttp2Response((Http2Frame)msg);
       }else if(msg instanceof Http2DataFrame){
            handleHttp2Response((Http2Frame)msg);
        }else{
           handleHttpResponse((FullHttpResponse)msg);
       }
    }

    private void handleHttp2Response(Http2Frame msg) {

        log.info("Message received as a http2 frame");
    }

    private void handleHttpResponse(FullHttpResponse msg) {
        Integer streamId = (msg).headers()
                .getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());

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
            FullHttpResponse res = (FullHttpResponse) msg;
            response = new Http2Response(res);
            responseMap.put(streamId, response);
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
    }*/
}
