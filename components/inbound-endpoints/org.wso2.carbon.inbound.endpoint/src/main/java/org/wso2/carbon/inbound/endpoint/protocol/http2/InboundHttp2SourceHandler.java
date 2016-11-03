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
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.InboundMessageHandler;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.SourceHandler;

import java.util.*;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

@Sharable
public class InboundHttp2SourceHandler extends ChannelDuplexHandler implements SourceHandler {
    private static final Log log = LogFactory.getLog(InboundHttp2SourceHandler.class);
    private InboundMessageHandler messageHandler;
    private InboundHttp2ResponseSender responseSender;
    private final InboundHttp2Configuration config;
    private HashMap<Integer, HTTP2SourceRequest> streams = new HashMap<Integer, HTTP2SourceRequest>();
    private Map<String, String> headerMap = new TreeMap<String, String>(new Comparator<String>() {
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    });

    public InboundHttp2SourceHandler(InboundHttp2Configuration config) {
        this.config = config;

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            onHeadersRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            onDataRead(ctx, (Http2DataFrame) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.responseSender = new InboundHttp2ResponseSender(this);
        this.messageHandler = new InboundMessageHandler(this.responseSender, this.config);
    }

    public void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) throws Exception {

        if (data.isEndStream()) {
            int streamId = data.streamId();
            HTTP2SourceRequest request = null;
            request = streams.get(streamId);
            request.setChannel(ctx);

            request.addFrame(Http2FrameTypes.DATA, data);
            messageHandler.processRequest(request);
            streams.remove(request.getStreamID());
        }
    }

    /**
     * Headers frames sends to start a new stream
     */
    public void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers)
            throws Exception {

        int streamId = headers.streamId();
        HTTP2SourceRequest request = null;
        if (streams.containsKey(streamId)) {
            request = streams.get(streamId);
        } else {
            request = new HTTP2SourceRequest(streamId, ctx);
            streams.put(streamId, request);
        }
        Map r_headers = request.getHeaders();
        Set<CharSequence> headerSet = headers.headers().names();
        for (CharSequence header : headerSet) {
            if (log.isDebugEnabled()) {
                log.debug("headers for stream id:" + streamId + ":-" + header.toString());
            }
            request.setHeader(header.toString(), headers.headers().get(header).toString());
        }
        if (headers.isEndStream() && !r_headers.containsKey("http2-settings")) {
            messageHandler.processRequest(request);
            streams.remove(request.getStreamID());
        }
    }

    public synchronized void sendResponse(MessageContext msgCtx) throws AxisFault {
        ChannelHandlerContext channel = (ChannelHandlerContext) msgCtx
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
        channel.writeAndFlush(new DefaultHttp2DataFrame(content, true));
    }

}
