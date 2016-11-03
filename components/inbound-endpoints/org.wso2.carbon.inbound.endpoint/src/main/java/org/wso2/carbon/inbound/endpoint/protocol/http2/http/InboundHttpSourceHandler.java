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

package org.wso2.carbon.inbound.endpoint.protocol.http2.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.Http2FrameTypes;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.inbound.endpoint.protocol.http2.HTTP2SourceRequest;
import org.wso2.carbon.inbound.endpoint.protocol.http2.InboundHttp2Configuration;
import org.wso2.carbon.inbound.endpoint.protocol.http2.InboundHttp2ResponseSender;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.InboundMessageHandler;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.SourceHandler;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class InboundHttpSourceHandler extends SimpleChannelInboundHandler<FullHttpRequest>
        implements SourceHandler {
    private static final Log log = LogFactory.getLog(InboundHttpSourceHandler.class);
    private ChannelHandlerContext channelCtx;
    private boolean keepAlive;
    private InboundMessageHandler messageHandler;
    private InboundHttp2ResponseSender responseSender;

    public InboundHttpSourceHandler(InboundHttp2Configuration config) {
        responseSender = new InboundHttp2ResponseSender(this);
        messageHandler = new InboundMessageHandler(responseSender, config);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        this.channelCtx = ctx;
        if (HttpUtil.is100ContinueExpected(req)) {
            channelCtx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
        HTTP2SourceRequest h2Request = wrapToHttp2SourceRequest(req);
        messageHandler.processRequest(h2Request);
    }

    public void sendResponse(MessageContext msgCtx) throws AxisFault {

        ByteBuf content = channelCtx.alloc().buffer();
        String res = messageHandler
                .messageFormatter(((Axis2MessageContext) msgCtx).getAxis2MessageContext());
        content.writeBytes(res.getBytes());

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
        String contentType = messageHandler
                .getContentType(((Axis2MessageContext) msgCtx).getAxis2MessageContext());
        if (contentType != null) {
            response.headers().add(CONTENT_TYPE, contentType);
            response.headers().add(CONTENT_LENGTH, response.content().readableBytes());
        }
        keepAlive = (msgCtx.getProperty(HttpHeaderValues.KEEP_ALIVE.toString()) != null) ?
                (boolean) msgCtx.getProperty(HttpHeaderValues.KEEP_ALIVE.toString()) :
                false;

        if (!keepAlive) {
            channelCtx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            channelCtx.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public HTTP2SourceRequest wrapToHttp2SourceRequest(FullHttpRequest req) {
        HTTP2SourceRequest http2Req = new HTTP2SourceRequest(1, channelCtx);
        List<Map.Entry<String, String>> headers = req.headers().entries();
        for (Map.Entry header : headers) {
            http2Req.setHeader(header.getKey().toString(), header.getValue().toString());
        }
        http2Req.setUri(req.uri());
        http2Req.setMethod(req.method().toString());
        http2Req.setScheme(req.protocolVersion().protocolName());
        if ((req.method() != HttpMethod.GET) && (req.method() != HttpMethod.DELETE) && (req.method()
                != HttpMethod.HEAD)) {
            http2Req.addFrame(Http2FrameTypes.DATA, new DefaultHttp2DataFrame(req.content()));
        }
        return http2Req;
    }

}
