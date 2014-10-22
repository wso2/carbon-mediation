/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.inbound.endpoint.protocol.http.utils;


import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.BasicHttpParams;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.*;
import org.apache.synapse.transport.passthru.config.BaseConfiguration;
import org.apache.synapse.transport.passthru.config.PassThroughConfiguration;
import org.apache.synapse.transport.passthru.util.BufferFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * configurations related to Inbound Http
 */
public class InboundConfiguration extends BaseConfiguration {
    private static PassThroughConfiguration conf = PassThroughConfiguration.getInstance();
    private static int iOBufferSize = conf.getIOBufferSize();
    private static BufferFactory bufferFactory = new BufferFactory(iOBufferSize, new HeapByteBufferAllocator(), 512);
    private InboundSourceConnections sourceConnections = null;
    private HttpResponseFactory httpResponseFactory;
    private HttpProcessor httpProcessor = null;
    private static volatile  ExecutorService executorService;
    private static final String INBOUND_THREAD_FACTORY = "inbound_request";


    public InboundConfiguration() {
        super(null, null, null, null);
        this.sourceConnections = new InboundSourceConnections();
        this.httpResponseFactory = new DefaultHttpResponseFactory();
        httpProcessor = new ImmutableHttpProcessor(
                new HttpResponseInterceptor[]{
                        new ResponseDate(),
                        new ResponseServer(),
                        new ResponseContent(),
                        new ResponseConnControl()}
        );
    }

    /**
     * buildHttpParams
     *
     * @return
     */
    public HttpParams buildHttpParams() {
        HttpParams params = new BasicHttpParams();
        params.
                setIntParameter(HttpConnectionParams.SO_TIMEOUT,
                        conf.getIntProperty(HttpConnectionParams.SO_TIMEOUT, 60000)).
                setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
                        conf.getIntProperty(HttpConnectionParams.CONNECTION_TIMEOUT, 0)).
                setIntParameter(HttpConnectionParams.SOCKET_BUFFER_SIZE,
                        conf.getIntProperty(HttpConnectionParams.SOCKET_BUFFER_SIZE, 8 * 1024)).
                setParameter(HttpProtocolParams.ORIGIN_SERVER,
                        conf.getStringProperty(HttpProtocolParams.ORIGIN_SERVER, "WSO2-PassThrough-HTTP")).
                setParameter(HttpProtocolParams.USER_AGENT,
                        conf.getStringProperty(HttpProtocolParams.USER_AGENT, "Synapse-PT-HttpComponents-NIO")).
                setParameter(HttpProtocolParams.HTTP_ELEMENT_CHARSET,
                        conf.getStringProperty(HttpProtocolParams.HTTP_ELEMENT_CHARSET, HTTP.DEFAULT_PROTOCOL_CHARSET));
        //TODO:This does not works with HTTPCore 4.3

        return params;
    }

    /**
     * IO Reactor Properties
     *
     * @return
     */
    public IOReactorConfig buildIOReactorConfig() {
        IOReactorConfig config = new IOReactorConfig();
        config.setIoThreadCount(conf.getIOThreadsPerReactor());
        config.setSoTimeout(conf.getIntProperty(HttpConnectionParams.SO_TIMEOUT, 60000));
        config.setConnectTimeout(conf.getIntProperty(HttpConnectionParams.CONNECTION_TIMEOUT, 0));
        config.setTcpNoDelay(conf.getBooleanProperty(HttpConnectionParams.TCP_NODELAY, true));
        config.setSoLinger(conf.getIntProperty(HttpConnectionParams.SO_LINGER, -1));
        config.setSoReuseAddress(conf.getBooleanProperty(HttpConnectionParams.SO_REUSEADDR, false));
        config.setInterestOpQueued(conf.getBooleanProperty("http.nio.interest-ops-queueing", false));
        config.setSelectInterval(conf.getIntProperty("http.nio.select-interval", 1000));
        return config;
    }

    public BufferFactory getBufferFactory() {
        return bufferFactory;
    }

    public InboundSourceConnections getSourceConnections() {
        return sourceConnections;
    }

    public HttpResponseFactory getHttpResponseFactory() {
        return httpResponseFactory;
    }

    public HttpProcessor getHttpProcessor() {
        return httpProcessor;
    }


    /**
     * Executor Service Provider
     *
     * @return
     */
    public static ExecutorService getInboundExecutorService() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(InboundConstants.WORKER_POOL_SIZE,
                    new InboundThreadFactory(INBOUND_THREAD_FACTORY));
            return executorService;
        }
        return executorService;
    }


}
