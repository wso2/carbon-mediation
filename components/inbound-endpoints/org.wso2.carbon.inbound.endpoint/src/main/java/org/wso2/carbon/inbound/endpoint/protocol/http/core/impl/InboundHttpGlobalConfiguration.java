package org.wso2.carbon.inbound.endpoint.protocol.http.core.impl;


import org.apache.axis2.transport.base.threads.NativeThreadFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.log4j.Logger;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundConfiguration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;


public class InboundHttpGlobalConfiguration {
    private static Logger log = Logger.getLogger(InboundHttpGlobalConfiguration.class);
    private static final Map<Integer, InboundHttpSourceHandler> inboundHttpSourceHandlerMap = new HashMap<Integer, InboundHttpSourceHandler>();
    private static final Map<Integer, ListenerEndpoint> listenerEndpointMap = new HashMap<Integer, ListenerEndpoint>();
    private static MultiListnerIODispatch multiListnerIODispatch;
    private static DefaultListeningIOReactor ioReactor;
    private static boolean isIOReactorStarted;
    private static InboundConfiguration inboundConfiguration;
    ;


    public static Map<Integer, InboundHttpSourceHandler> getInboundHttpSourceHandlerMap() {
        return inboundHttpSourceHandlerMap;
    }

    public static void addInboundHttpSourceHandler(int port, InboundHttpSourceHandler inboundHttpSourceHandler) {
        if (inboundHttpSourceHandlerMap.get(port) == null) {
            inboundHttpSourceHandlerMap.put(port, inboundHttpSourceHandler);
        } else {
            log.error("Cannot add http inbound endpoint for port " + port + " already added");

        }
    }

    public static MultiListnerIODispatch getMultiListnerIODispatch() {
        if (multiListnerIODispatch == null) {
            multiListnerIODispatch = new MultiListnerIODispatch(inboundHttpSourceHandlerMap);
        }
        return multiListnerIODispatch;
    }


    public static DefaultListeningIOReactor startIoReactor() {
        if (!isIOReactorStarted) {
            if (inboundConfiguration == null) {
                inboundConfiguration = new InboundConfiguration();
            }
            try {
                ioReactor = new DefaultListeningIOReactor(
                        inboundConfiguration.buildIOReactorConfig(),
                        new NativeThreadFactory(new ThreadGroup("Inbound http thread group"), "Inbound http core impl"));

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
                            if (multiListnerIODispatch != null) {
                                ioReactor.execute(multiListnerIODispatch);
                                isIOReactorStarted = true;
                            } else {
                                multiListnerIODispatch = InboundHttpGlobalConfiguration.getMultiListnerIODispatch();
                                ioReactor.execute(multiListnerIODispatch);
                                isIOReactorStarted = true;
                            }
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
        return ioReactor;
    }

    public static void startEndpoint(InetSocketAddress inetSocketAddress)  {
        ListenerEndpoint endpoint = null;
        if (ioReactor != null) {
            endpoint = ioReactor.listen(inetSocketAddress);
            listenerEndpointMap.put(inetSocketAddress.getPort(), endpoint);
        } else {
            ioReactor = InboundHttpGlobalConfiguration.startIoReactor();
            endpoint = ioReactor.listen(inetSocketAddress);
            listenerEndpointMap.put(inetSocketAddress.getPort(), endpoint);
            return;
        }
        try {
            endpoint.waitFor();
            if (log.isInfoEnabled()) {
                InetSocketAddress address = (InetSocketAddress) endpoint.getAddress();
                if (!address.isUnresolved()) {
                    log.info("Inbound http Listner started on " +
                            address.getHostName() + ":" + address.getPort());
                } else {
                    log.info("Inbound http Listner started on" + address);
                }
            }
        } catch (InterruptedException e) {
            log.error("Listener startup was interrupted", e);
            inboundHttpSourceHandlerMap.remove(inetSocketAddress.getPort());
            listenerEndpointMap.remove(inetSocketAddress.getPort());

        }
    }

    public static void closeEndpoint(int port)  {
        if (listenerEndpointMap.get(port) != null) {
            listenerEndpointMap.get(port).close();
            listenerEndpointMap.remove(port);
            inboundHttpSourceHandlerMap.remove(port);

            if (listenerEndpointMap.size() == 0 && inboundHttpSourceHandlerMap.size() == 0) {
                try {
                    ioReactor.shutdown();
                    isIOReactorStarted = false;
                } catch (IOException e) {
                   log.error(e.getMessage());
                }
            }
        } else {
            log.error("cannot find Endpoint relevant to inbound port " + port);

        }

    }

    public static boolean isIsIOReactorStarted() {
        return isIOReactorStarted;
    }


}
