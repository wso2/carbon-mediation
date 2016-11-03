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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.Http2Codec;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import org.wso2.carbon.inbound.endpoint.protocol.http2.http.InboundHttpSourceHandler;

public class InboundHttp2HttpHandler extends ApplicationProtocolNegotiationHandler {

    private static final int MAX_CONTENT_LENGTH = 2048 * 100;
    private final InboundHttp2Configuration config;

    protected InboundHttp2HttpHandler(InboundHttp2Configuration config) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.config = config;
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            ctx.pipeline()
                    .addLast(new Http2Codec(true, new InboundHttp2SourceHandler(this.config)));
            return;
        }

        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            ctx.pipeline()
                    .addLast(new HttpServerCodec(), new HttpObjectAggregator(MAX_CONTENT_LENGTH),
                            new InboundHttpSourceHandler(this.config));
            return;
        }

        throw new IllegalStateException("unknown protocol: " + protocol);
    }
}
