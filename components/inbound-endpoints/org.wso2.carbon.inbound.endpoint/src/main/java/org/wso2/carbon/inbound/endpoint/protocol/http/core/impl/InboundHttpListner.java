/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.http.core.impl;


import org.apache.axis2.transport.base.threads.NativeThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundListner;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundConfiguration;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Listener class for Inbound Http Inbounds
 */
public class InboundHttpListner implements InboundListner {
    protected Log log = LogFactory.getLog(this.getClass());

    private String injectingSequence;
    private String onErrorSequence;
    private String outSequence;
    private SynapseEnvironment synapseEnvironment;
    private String port;
    private static Map<Integer, InboundHttpSourceHandler> inboundHttpSourceHandlerMap;
    private static MultiListnerIODispatch multiListnerIODispatch;
    private InboundConfiguration inboundConfiguration;
    private static DefaultListeningIOReactor ioReactor;
    private static boolean isIOReactorStarted = false;
    private InboundHttpSourceResponseWorker inboundHttpSourceResponseWorker;
    private static boolean isResponseWorkerStarted;


    public InboundHttpListner(String port, SynapseEnvironment synapseEnvironment, String injectSeq, String onErrorSeq, String outSeq) {
        this.port = port;
        this.injectingSequence = injectSeq;
        this.onErrorSequence = onErrorSeq;
        this.outSequence = outSeq;
        this.inboundHttpSourceHandlerMap = new HashMap<Integer, InboundHttpSourceHandler>();
        this.inboundConfiguration = new InboundConfiguration();
        this.synapseEnvironment = synapseEnvironment;
        if (!isResponseWorkerStarted) {
            inboundHttpSourceResponseWorker = new InboundHttpSourceResponseWorker();
        }
    }

    public void start() {
        InboundHttpSourceHandler inboundHttpSourceHandler = new InboundHttpSourceHandler(this.inboundConfiguration, synapseEnvironment, injectingSequence, onErrorSequence, outSequence);
        this.inboundHttpSourceHandlerMap.put(Integer.parseInt(port), inboundHttpSourceHandler);
        startIOReactor();
        startEndpoint();
        startResponseWorker();
    }


    public void shutDown() {

    }

    /**
     * start endpoints for ports specified
     */
    private void startEndpoint() {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(Integer.parseInt(port));
        ListenerEndpoint endpoint = null;
        if (ioReactor != null) {
            endpoint = ioReactor.listen(inetSocketAddress);
        } else {
            log.error("io Reactor cannot be null");
            return;
        }
        try {
            endpoint.waitFor();
            if (log.isInfoEnabled()) {
                InetSocketAddress address = (InetSocketAddress) endpoint.getAddress();
                if (!address.isUnresolved()) {
                    log.info("Inbound http Listner started on " +
                            address.getHostName() + ":" + address.getPort());
                    System.out.println(("Inbound http Listner started on " +
                            address.getHostName() + ":" + address.getPort()));
                } else {
                    log.info("Inbound http Listner started on" + address);
                    System.out.println("Inbound http Listner started on" + address);
                }
            }
        } catch (InterruptedException e) {
            log.error("Listener startup was interrupted", e);
        }
    }


    private DefaultListeningIOReactor startIOReactor() {
        if (!isIOReactorStarted) {
            try {
                ioReactor = new DefaultListeningIOReactor(
                        inboundConfiguration.buildIOReactorConfig(),
                        new NativeThreadFactory(new ThreadGroup("Inbound http thread group"), "Inbound http core impl"));
                multiListnerIODispatch = new MultiListnerIODispatch(inboundHttpSourceHandlerMap);

                ioReactor.setExceptionHandler(new IOReactorExceptionHandler() {

                    public boolean handle(IOException ioException) {
                        log.warn("System may be unstable: Inbound http " +
                                " ListeningIOReactor encountered a checked exception : " +
                                ioException.getMessage(), ioException);
                        return true;
                    }

                    public boolean handle(RuntimeException runtimeException) {
                        log.warn("System may be unstable: inbound http " +
                                " ListeningIOReactor encountered a runtime exception : "
                                + runtimeException.getMessage(), runtimeException);
                        return true;
                    }
                });

                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            ioReactor.execute(multiListnerIODispatch);
                            isIOReactorStarted = true;
                        } catch (Exception e) {
                            log.fatal("Exception encountered in the Inbounf http" + " Listener. " +
                                    "No more connections will be accepted by this transport", e);
                        }
                        log.info(" Inbound http Listener shutdown.");
                    }
                }, "Inbound http Listener");
                t.start();
                return ioReactor;
            } catch (IOReactorException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            if (ioReactor != null) {
                return ioReactor;
            } else {
                log.error("ioReactor null can not start io Reactor");
            }
        }
        return null;
    }


    private void startResponseWorker() {
        if (inboundHttpSourceResponseWorker != null && !isResponseWorkerStarted) {
            Thread thread = new Thread(inboundHttpSourceResponseWorker);
            thread.start();
            isResponseWorkerStarted = true;
            log.info("start http inbound response worker");
        } else {
            log.error("cannot start inbound http response worker");
        }
    }
}
