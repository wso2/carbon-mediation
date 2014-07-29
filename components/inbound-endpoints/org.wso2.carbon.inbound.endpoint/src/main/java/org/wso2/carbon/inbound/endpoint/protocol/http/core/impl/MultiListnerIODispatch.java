package org.wso2.carbon.inbound.endpoint.protocol.http.core.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.reactor.AbstractIODispatch;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.synapse.transport.http.conn.LoggingUtils;
import org.apache.synapse.transport.http.conn.SynapseHTTPRequestFactory;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundConfiguration;

import java.io.IOException;
import java.util.Map;

/**
 * wrapping class of InboundHttpSourceHandler
 */
public class MultiListnerIODispatch extends
        AbstractIODispatch<DefaultNHttpServerConnection> {


    protected Log log = LogFactory.getLog(this.getClass());

    private final Map<Integer, InboundHttpSourceHandler> handlers;


    public MultiListnerIODispatch(final Map<Integer, InboundHttpSourceHandler> handlers) {
        this.handlers = handlers;

    }


    @Override
    protected DefaultNHttpServerConnection createConnection(IOSession ioSession) {
        return getConnection(ioSession);
    }

    @Override
    protected void onConnected(final DefaultNHttpServerConnection defaultNHttpServerConnection) {
        int localPort = defaultNHttpServerConnection.getLocalPort();
        InboundHttpSourceHandler handler = handlers.get(localPort);
        try {
            handler.connected(defaultNHttpServerConnection);
        } catch (final Exception ex) {
            handler.exception(defaultNHttpServerConnection, ex);
        }
    }

    @Override
    protected void onClosed(DefaultNHttpServerConnection defaultNHttpServerConnection) {
        int localPort = defaultNHttpServerConnection.getLocalPort();
        InboundHttpSourceHandler handler = handlers.get(localPort);
        try {
            handler.closed(defaultNHttpServerConnection);
        } catch (final Exception ex) {
            handler.exception(defaultNHttpServerConnection, ex);
        }
    }

    @Override
    protected void onException(DefaultNHttpServerConnection defaultNHttpServerConnection, IOException e) {
        int localPort = defaultNHttpServerConnection.getLocalPort();
        InboundHttpSourceHandler handler = handlers.get(localPort);
        try {
            handler.exception(defaultNHttpServerConnection, e);
        } catch (final Exception ex) {
            handler.exception(defaultNHttpServerConnection, ex);
        }
    }

    @Override
    protected void onInputReady(DefaultNHttpServerConnection defaultNHttpServerConnection) {
        int localPort = defaultNHttpServerConnection.getLocalPort();
        InboundHttpSourceHandler handler = handlers.get(localPort);
        try {
            defaultNHttpServerConnection.consumeInput(handler);
        } catch (final Exception ex) {
            handler.exception(defaultNHttpServerConnection, ex);
        }
    }

    @Override
    protected void onOutputReady(DefaultNHttpServerConnection defaultNHttpServerConnection) {
        int localPort = defaultNHttpServerConnection.getLocalPort();
        InboundHttpSourceHandler handler = handlers.get(localPort);
        try {
            defaultNHttpServerConnection.produceOutput(handler);
        } catch (final Exception ex) {
            handler.exception(defaultNHttpServerConnection, ex);
        }
    }

    @Override
    protected void onTimeout(DefaultNHttpServerConnection defaultNHttpServerConnection) {
        int localPort = defaultNHttpServerConnection.getLocalPort();
        InboundHttpSourceHandler handler = handlers.get(localPort);
        try {
            handler.timeout(defaultNHttpServerConnection);
        } catch (final Exception ex) {
            handler.exception(defaultNHttpServerConnection, ex);
        }
    }

    private DefaultNHttpServerConnection getConnection(IOSession ioSession) {
        InboundConfiguration inboundConfiguration = new InboundConfiguration();
        HttpParams httpParams = inboundConfiguration.buildHttpParams();
        DefaultNHttpServerConnection conn = LoggingUtils.createServerConnection(
                ioSession, new SynapseHTTPRequestFactory(), new HeapByteBufferAllocator(), httpParams);
        int timeout = HttpConnectionParams.getSoTimeout(httpParams);
        conn.setSocketTimeout(timeout);
        return conn;
    }


}
