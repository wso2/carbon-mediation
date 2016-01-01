/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.inbound.endpoint.protocol.tcp.core;

import org.apache.http.nio.reactor.IOSession;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * per connection/session IO event handler. IO reactor triggers each
 * event accordingly
 */
public class TCPMultiIOHandler extends TCPSourceHandler {
    private static final Logger log = Logger.getLogger(TCPMultiIOHandler.class);
    public ConcurrentHashMap<Integer, TCPSourceHandler> handlers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, TCPProcessor> processorMap;

    public TCPMultiIOHandler(ConcurrentHashMap<Integer, TCPProcessor> processorMap) {
        super();
        this.processorMap = processorMap;
    }

    /**
     * Invoked by IOReactor when a client is connected.
     *
     * @param session contain the client information unique to one client
     */
    @Override
    public void connected(IOSession session) {
        InetSocketAddress remoteIsa = (InetSocketAddress) session.getRemoteAddress();
        InetSocketAddress localIsa = (InetSocketAddress) session.getLocalAddress();
        TCPSourceHandler handler = new TCPSourceHandler(processorMap.get(localIsa.getPort()));
        handlers.put(remoteIsa.getPort(), handler);
        handler.connected(session);
    }

    /**
     * Invoked by IOReactor when a client data is available.
     *
     * @param session contain the client information unique to one client
     */
    @Override
    public void inputReady(IOSession session) {
        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        TCPSourceHandler handler = handlers.get(isa.getPort());
        handler.inputReady(session);
    }

    /**
     * Invoked by IOReactor when a client data is ready to write.
     *
     * @param session contain the client information unique to one client
     */
    @Override
    public void outputReady(IOSession session) {
        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        TCPSourceHandler handler = handlers.get(isa.getPort());
        handler.outputReady(session);
    }

    @Override
    public void timeout(IOSession session) {
        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        TCPSourceHandler handler = handlers.get(isa.getPort());
        handler.timeout(session);
        handlers.remove(handler);
    }

    /**
     * Invoked by IOReactor when a client is disconnected.
     *
     * @param session contain the client information unique to one client
     */
    @Override
    public void disconnected(IOSession session) {
        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        if (isa == null) {
            return;
        }
        TCPSourceHandler handler = handlers.get(isa.getPort());
        handler.disconnected(session);
        handlers.remove(handler);
    }
}
