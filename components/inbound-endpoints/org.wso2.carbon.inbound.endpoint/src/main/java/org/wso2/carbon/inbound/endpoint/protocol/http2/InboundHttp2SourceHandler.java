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

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2GoAwayFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.InboundMessageHandler;

/**
 * Handle Inbound Endpoint Requests and Responses
 */
@Sharable
public class InboundHttp2SourceHandler extends ChannelDuplexHandler {
	private static final Log log = LogFactory.getLog(InboundHttp2SourceHandler.class);
	private final InboundHttp2Configuration config;
	private ChannelHandlerContext chContext;
	private Http2RequestReader reader;
	private Http2ResponseWriter writer;

	public InboundHttp2SourceHandler(InboundHttp2Configuration config, Http2Connection connection,
	                                 Http2ConnectionEncoder encoder) {
		this.config = config;
		this.reader = new Http2RequestReader();
		this.writer = new Http2ResponseWriter();
		writer.setEncoder(encoder);
		writer.setConnection(connection);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		ctx.close();
		throw new AxisFault("connection error occurred", cause);
	}

	/**
	 * Read peer's frames
	 *
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		reader.setChContext(ctx);
		if (chContext == null)
			writer.setChContext(ctx);
		if (msg instanceof Http2HeadersFrame) {
			Http2HeadersFrame frame = (Http2HeadersFrame) msg;
			reader.onHeaderRead(frame);

		} else if (msg instanceof Http2DataFrame) {
			reader.onDataRead((Http2DataFrame) msg);

		} else if (msg instanceof Http2GoAwayFrame) {
			reader.onGoAwayRead((Http2GoAwayFrame) msg);

		} else {
			super.channelRead(ctx, msg);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.chContext = ctx;
		reader.setChContext(ctx);
		writer.setChContext(ctx);
		reader.setMessageHandler(
				new InboundMessageHandler(new InboundHttp2ResponseSender(this), config));
		writer.setConfig(config);
	}

	/**
	 * Send Responses to the peer
	 *
	 * @param msgCtx
	 * @throws AxisFault
	 */
	public synchronized void sendResponse(MessageContext msgCtx) throws AxisFault {
		org.apache.axis2.context.MessageContext axisMessage =
				((Axis2MessageContext) msgCtx).getAxis2MessageContext();

		String responseType = null;

		if (axisMessage.getProperty(Http2Constants.HTTP2_REQUEST_TYPE) != null) {
			responseType = axisMessage.getProperty(Http2Constants.HTTP2_REQUEST_TYPE).toString();
		}
		if (responseType == null || responseType.equals(Http2Constants.HTTP2_CLIENT_SENT_REQEUST)) {
			writer.writeNormalResponse(msgCtx);

		} else if (responseType.equals(Http2Constants.HTTP2_PUSH_PROMISE_REQEUST)) {
			writer.writePushPromiseResponse(msgCtx);

		} else if (responseType.equals(Http2Constants.HTTP2_GO_AWAY_REQUEST)) {
			writer.writeGoAwayResponse(msgCtx);
		} else {
			throw new AxisFault("Uncaught response type : " + responseType);
		}
	}

}
