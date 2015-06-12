/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.tcp.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.reactor.IOSession;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * per connection/session IO event handler
 */

public class TCPMultiIOHandler extends TCPSourceHandler {
    private static final Log log = LogFactory.getLog(TCPMultiIOHandler.class);

    public ConcurrentHashMap<Integer, TCPSourceHandler> handlers = new ConcurrentHashMap<Integer, TCPSourceHandler>();

    private ConcurrentHashMap<Integer, TCPProcessor> processorMap;

    public TCPMultiIOHandler(ConcurrentHashMap<Integer, TCPProcessor> processorMap) {
        super();
        this.processorMap = processorMap;
    }

    @Override public void connected(IOSession session) {

        InetSocketAddress remoteIsa = (InetSocketAddress) session.getRemoteAddress();
        InetSocketAddress localIsa = (InetSocketAddress) session.getLocalAddress();

        TCPSourceHandler handler = new TCPSourceHandler(processorMap.get(localIsa.getPort()));
        handlers.put(remoteIsa.getPort(), handler);

        handler.connected(session);
    }

    @Override public void inputReady(IOSession session) {

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        TCPSourceHandler handler = handlers.get(isa.getPort());
        handler.inputReady(session);

    }

    @Override public void outputReady(IOSession session) {

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        TCPSourceHandler handler = handlers.get(isa.getPort());
        handler.outputReady(session);

    }

    @Override public void timeout(IOSession session) {
        //log.info("Time out method called...");
        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        TCPSourceHandler handler = handlers.get(isa.getPort());
        handler.timeout(session);
        handlers.remove(handler);

    }

    @Override public void disconnected(IOSession session) {
        //log.info("Disconnected method called...");

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        if (isa == null) {
            return;
        }
        TCPSourceHandler handler = handlers.get(isa.getPort());
        handler.disconnected(session);
        handlers.remove(handler);

    }
}