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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2GoAwayFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2ResetFrame;
import io.netty.handler.codec.http2.Http2Settings;

import org.apache.axis2.context.MessageContext;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;

import java.util.*;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class Http2ClientHandler extends ChannelDuplexHandler{

    private Http2RequestWriter writer;
    private Http2ResponseReceiver receiver;
    private Http2Connection connection;
    private Http2ConnectionEncoder encoder;
    private ChannelHandlerContext chContext;
    private Map<Integer,MessageContext> sentRequests;
    private LinkedList<MessageContext> pollReqeusts;
    private TargetConfiguration targetConfiguration;
    private ChannelHandlerContext clientChannel;
    private Map<Integer,Integer> requestResponseStreamIdMap;   //Map<streamId-server, stream_id-client>


    public Http2ClientHandler(Http2Connection connection) {
        this.connection=connection;
        sentRequests=new TreeMap<>();
        writer=new Http2RequestWriter(connection);
        pollReqeusts=new LinkedList<>();
        requestResponseStreamIdMap=new ConcurrentHashMap<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        if(msg instanceof Http2HeadersFrame){
            Http2HeadersFrame frame=(Http2HeadersFrame)msg;
            if(!sentRequests.containsKey(frame.streamId())){
                return;
            }
            receiver.onHeadersFrameRead(frame,sentRequests.get(frame.streamId()));
            if(frame.isEndStream()){
                sentRequests.remove(frame.streamId());
            }
        }else if(msg instanceof Http2DataFrame){
            Http2DataFrame frame=(Http2DataFrame) msg;
            if(!sentRequests.containsKey(frame.streamId())){
                return;
            }
            receiver.onDataFrameRead(frame,sentRequests.get(frame.streamId()));
            if(frame.isEndStream()){
                sentRequests.remove(frame.streamId());
            }
        }else if(msg instanceof Http2PushPromiseFrame){
            Http2PushPromiseFrame frame=(Http2PushPromiseFrame) msg;
            if(!sentRequests.containsKey(frame.streamId())){
                return;
            }
            MessageContext prevRequest=sentRequests.get(frame.streamId());

            //if the inbound is not accept push requests reject them
            if(prevRequest.getProperty(Http2Constants.HTTP2_PUSH_PROMISE_REQEUST_ENABLED)==null || !(boolean)prevRequest.getProperty(Http2Constants.HTTP2_PUSH_PROMISE_REQEUST_ENABLED)){
                writer.writeRestSreamRequest(frame.getPushPromiseId(),Http2Error.REFUSED_STREAM);
                return;
            }

            sentRequests.put(frame.getPushPromiseId(),prevRequest);
            receiver.onPushPromiseFrameRead(frame,prevRequest);

        }else if(msg instanceof Http2Settings){
            setChContext(ctx);
            receiver.onUnknownFrameRead(msg);

        }else if(msg instanceof Http2GoAwayFrame){
            receiver.onUnknownFrameRead(msg);

        }else if(msg instanceof Http2ResetFrame){
            receiver.onUnknownFrameRead(msg);

        }else{
            receiver.onUnknownFrameRead(msg);

        }
    }


    public void channelWrite(MessageContext request){
        if(chContext==null){
            pollReqeusts.add(request);
            return;
        }
        String requestType=(String)request.getProperty(Http2Constants.HTTP2_REQUEST_TYPE);
        if(requestType==null || requestType.equals(Http2Constants.HTTP2_CLIENT_SENT_REQEUST)){
            int streamId= writer.getNextStreamId();
            sentRequests.put(streamId,request);
            writer.writeSimpleReqeust(streamId,request);

        }else if(requestType.equals(Http2Constants.HTTP2_RESET_REQEUST)){
            int id=(int)request.getProperty(Http2Constants.HTTP2_SERVER_STREAM_ID);
            Http2Error code=(Http2Error)request.getProperty(Http2Constants.HTTP2_ERROR_CODE);
            writer.writeRestSreamRequest(id,code);

        }else if(requestType.equals(Http2Constants.HTTP2_GO_AWAY_REQUEST)) {  //Basically GoAway caused to dispose handler
            int id = (int) request.getProperty(Http2Constants.HTTP2_SERVER_STREAM_ID);
            Http2Error code = (Http2Error) request.getProperty(Http2Constants.HTTP2_ERROR_CODE);
            writer.writeGoAwayReqeust(id, code);
        }
    }


    public Http2RequestWriter getWriter() {
        return writer;
    }

    public void setWriter(Http2RequestWriter writer) {
        this.writer = writer;
    }

    public Http2ResponseReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(Http2ResponseReceiver receiver) {
        this.receiver = receiver;
    }

    public Http2Connection getConnection() {
        return connection;
    }

    public void setConnection(Http2Connection connection) {
        this.connection = connection;
    }

    public Http2ConnectionEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(Http2ConnectionEncoder encoder) {
        this.encoder = encoder;
        writer.setEncoder(encoder);

    }

    public ChannelHandlerContext getChContext() {
        return chContext;
    }

    public void setChContext(ChannelHandlerContext chContext) {
        this.chContext = chContext;
        writer.setChannelHandlerContext(chContext);

        if(!pollReqeusts.isEmpty()){
            Iterator<MessageContext> requests=pollReqeusts.iterator();
            while (requests.hasNext()){
                channelWrite(requests.next());
            }
        }
    }

    public void removeHandler() {
        if(chContext.channel().isActive()||chContext.channel().isOpen()){
            chContext.channel().close();
            chContext.executor().shutdownGracefully();
        }
    }

    public void setTargetConfiguration(TargetConfiguration targetConfiguration) {
        this.targetConfiguration = targetConfiguration;
        receiver=new Http2ResponseReceiver(targetConfiguration);
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
