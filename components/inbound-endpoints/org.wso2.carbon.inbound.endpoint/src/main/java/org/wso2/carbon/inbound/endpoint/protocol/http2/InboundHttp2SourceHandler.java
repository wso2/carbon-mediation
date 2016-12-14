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

package org.wso2.carbon.inbound.endpoint.protocol.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.*;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.api.PassThroughInboundEndpointHandler;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.InboundMessageHandler;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.SourceHandler;

import java.util.*;

@Sharable
public class InboundHttp2SourceHandler extends ChannelDuplexHandler implements SourceHandler {
    private static final Log log = LogFactory.getLog(InboundHttp2SourceHandler.class);
    /*private InboundMessageHandler messageHandler;
    private InboundHttp2ResponseSender responseSender;

    private HashMap<Integer, Http2SourceRequest> streams = new HashMap<Integer, Http2SourceRequest>();
    private Map<String, String> headerMap = new TreeMap<String, String>(new Comparator<String>() {
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    });*/

    private final InboundHttp2Configuration config;
    private SourceConfiguration sourceConfiguration;
    private Http2Connection connection;
    private Http2ConnectionEncoder encoder;
    private ChannelHandlerContext chContext;
    private Http2RequestReader reader;
    private Http2ResponseWriter writer;


    public InboundHttp2SourceHandler(InboundHttp2Configuration config, Http2Connection connection,
            Http2ConnectionEncoder encoder) {
        this.config = config;
        this.connection = connection;
        this.encoder = encoder;
        this.reader=new Http2RequestReader();
        this.writer=new Http2ResponseWriter();
        writer.setEncoder(encoder);
        writer.setConnection(connection);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        reader.setChContext(ctx);
        if(chContext==null)
            writer.setChContext(ctx);
        if (msg instanceof Http2HeadersFrame) {
            Http2HeadersFrame frame=(Http2HeadersFrame)msg;
            reader.onHeaderRead(frame);

        }else if (msg instanceof Http2DataFrame) {
            reader.onDataRead((Http2DataFrame)msg);

        }else if(msg instanceof Http2GoAwayFrame){
            reader.onGoAwayRead((Http2GoAwayFrame)msg);

        }else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.chContext=ctx;
        reader.setChContext(ctx);
        writer.setChContext(ctx);
        reader.setMessageHandler(new InboundMessageHandler(new InboundHttp2ResponseSender(this),config));
    }

    public void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) throws Exception {

           /* int streamId = data.streamId();
            Http2SourceRequest request = null;
            request = streams.get(streamId);

            if(request==null){
                return;
            }
            request.setChannel(ctx);

            Pipe pipe=request.getPipe();
            if(pipe==null){
                pipe=new Pipe(new HTTP2Producer(),sourceConfiguration.getBufferFactory().getBuffer(), "source", sourceConfiguration);
                request.setPipe(pipe);
            }
            pipe.produce(new HTTP2Decoder(data));
            //request.addFrame(Http2FrameTypes.DATA, data);

        if(!request.isProcessedReq()){
            messageHandler.processRequest(request);
            request.setProcessedReq(true);
        }
        if (data.isEndStream())
            streams.remove(request.getStreamID());*/
    }

    public void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers)
            throws Exception {

       /* int streamId = headers.streamId();
        Http2SourceRequest request = null;
        if (streams.containsKey(streamId)) {
            request = streams.get(streamId);
        } else {
            request = new Http2SourceRequest(streamId, ctx);
            streams.put(streamId, request);
        }
        Set<CharSequence> headerSet = headers.headers().names();
        for (CharSequence header : headerSet) {
            if (log.isDebugEnabled()) {
                log.debug("headers for stream id:" + streamId + ":-" + header.toString());
            }
            request.setHeader(header.toString(), headers.headers().get(header).toString());
        }
        if (headers.isEndStream() && !request.getHeaders().containsKey("http2-settings")) {
            messageHandler.processRequest(request);
            streams.remove(request.getStreamID());
        }*/
    }

    @Override
    public synchronized void sendResponse(MessageContext msgCtx) throws AxisFault {
        /**
         * get RequestType; if is null or client-request process as same
         * if push promise first write push promise and then process as same
         * if goaway send goaway and stop connection
         * we are not handling rest-stream requests
         */
        org.apache.axis2.context.MessageContext axisMessage=((Axis2MessageContext) msgCtx).getAxis2MessageContext();

        String requestType=null;

        if(axisMessage.getProperty(Http2Constants.HTTP2_REQUEST_TYPE)!=null){
            requestType=axisMessage.getProperty(Http2Constants.HTTP2_REQUEST_TYPE).toString();
        }
        if(requestType==null || requestType.equals(Http2Constants.HTTP2_CLIENT_SENT_REQEUST)){
            writer.writeNormalResponse(msgCtx);

        }else if(requestType.equals(Http2Constants.HTTP2_PUSH_PROMISE_REQEUST)){
            writer.writePushPromiseResponse(msgCtx);

        }else if(requestType.equals(Http2Constants.HTTP2_GO_AWAY_REQUEST)){
            writer.writeGoAwayResponse(msgCtx);
        }else{
            log.error("Uncaught request type : "+requestType);
        }



       /* ChannelHandlerContext channel = (ChannelHandlerContext) msgCtx
                .getProperty("stream-channel");

        ByteBuf content = channel.alloc().buffer();

        String response = messageHandler
                .messageFormatter(((Axis2MessageContext) msgCtx).getAxis2MessageContext());

        content.writeBytes(response.getBytes());

        // Send a frame for the response status
        Http2Headers headers = new DefaultHttp2Headers().status(OK.codeAsText());
        String contentType = messageHandler
                .getContentType(((Axis2MessageContext) msgCtx).getAxis2MessageContext());
        if (contentType != null) {
            headers.add(HttpHeaderNames.CONTENT_TYPE, contentType);
        }

        channel.write(new DefaultHttp2HeadersFrame(headers));
        channel.writeAndFlush(new DefaultHttp2DataFrame(content, true));*/
    }

}
