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

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.wso2.carbon.inbound.endpoint.protocol.http2.configuration.NettyThreadPoolConfiguration;

/**
 * Managing thread groups for inbound
 */
public class InboundHttp2EventExecutor {

	private EventLoopGroup workerGroup;
	private EventLoopGroup bossGroup;

	public InboundHttp2EventExecutor(NettyThreadPoolConfiguration configuration) {
		workerGroup = new NioEventLoopGroup(configuration.getBossThreadPoolSize());
		bossGroup = new NioEventLoopGroup(configuration.getBossThreadPoolSize());
	}

	/**
	 * @return Boss Thread Group
	 */
	public EventLoopGroup getBossGroupThreadPool() {
		return bossGroup;
	}

	/**
	 * @return worker Thread Group
	 */
	public EventLoopGroup getWorkerGroupThreadPool() {
		return workerGroup;
	}

	/**
	 * Shutdown Thread Groups
	 */
	public void shutdownEventExecutor() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

}
