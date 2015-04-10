package org.wso2.carbon.inbound.endpoint.protocol.hl7;

/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.reactor.IOSession;
import org.apache.synapse.inbound.InboundProcessorParams;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class MultiIOHandler extends HL7ServerIOEventDispatch {

    private static final Log log = LogFactory.getLog(MultiIOHandler.class);

    public ConcurrentHashMap<Integer, HL7ServerIOEventDispatch> handlers = new ConcurrentHashMap<Integer, HL7ServerIOEventDispatch>();

    private ConcurrentHashMap<Integer, InboundProcessorParams> parameterMap;

    public MultiIOHandler(ConcurrentHashMap<Integer, InboundProcessorParams> parameterMap) {
        super();
        this.parameterMap = parameterMap;
    }

    @Override
    public void connected(IOSession session) {

        InetSocketAddress remoteIsa = (InetSocketAddress) session.getRemoteAddress();
        InetSocketAddress localIsa = (InetSocketAddress) session.getLocalAddress();

        HL7RequestProcessor requestProcessor = new HL7RequestProcessor(localIsa.getPort(), parameterMap);

        HL7ServerIOEventDispatch handler = new HL7ServerIOEventDispatch(requestProcessor);
        handlers.put(remoteIsa.getPort(), handler);

        handler.connected(session);

    }

    @Override
    public void inputReady(IOSession session) {

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        HL7ServerIOEventDispatch handler = handlers.get(isa.getPort());
        handler.inputReady(session);

    }

    @Override
    public void outputReady(IOSession session) {

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        HL7ServerIOEventDispatch handler = handlers.get(isa.getPort());
        handler.outputReady(session);

    }

    @Override
    public void timeout(IOSession session) {

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        HL7ServerIOEventDispatch handler = handlers.get(isa.getPort());
        handler.timeout(session);
        handlers.remove(handler);

    }

    @Override
    public void disconnected(IOSession session) {

        InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
        HL7ServerIOEventDispatch handler = handlers.get(isa.getPort());
        handler.disconnected(session);
        handlers.remove(handler);

    }

}
