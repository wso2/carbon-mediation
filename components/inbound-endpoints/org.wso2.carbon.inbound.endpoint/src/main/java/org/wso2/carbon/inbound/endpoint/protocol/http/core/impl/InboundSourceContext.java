package org.wso2.carbon.inbound.endpoint.protocol.http.core.impl;


import org.apache.http.nio.NHttpConnection;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.ProtocolState;
import org.apache.synapse.transport.passthru.SourceContext;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundConfiguration;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InboundSourceContext {

    public static final String CONNECTION_INFORMATION = "CONNECTION_INFORMATION";

    private InboundConfiguration sourceConfiguration;

    private ProtocolState state = ProtocolState.REQUEST_READY;

    private InboundHttpSourceRequest request;

    private InboundHttpSourceResponse response;

    /**
     * Mark the connection to be shut down after the current request-response is completed.
     */
    private boolean shutDown = false;

    private Pipe reader;

    private Pipe writer;

    private Lock lock = new ReentrantLock();

    public InboundSourceContext(InboundConfiguration sourceConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
    }

    public ProtocolState getState() {
        return state;
    }

    public void setState(ProtocolState state) {
        this.state = state;
    }

    public InboundHttpSourceRequest getRequest() {
        return request;
    }

    public void setRequest(InboundHttpSourceRequest request) {
        this.request = request;
    }

    public InboundHttpSourceResponse getResponse() {
        return response;
    }

    public void setResponse(InboundHttpSourceResponse response) {
        this.response = response;
    }

    /**
     * Reset the resources associated with this context
     */
    public void reset() {
        reset(false);
    }

    /**
     * Reset the resources associated with this context
     *
     * @param isError whether an error is causing this shutdown of the connection.
     *                It is very important to set this flag correctly.
     *                When an error causing the shutdown of the connections we should not
     *                release associated writer buffer to the pool as it might lead into
     *                situations like same buffer is getting released to both source and target
     *                buffer factories
     */
    public void reset(boolean isError) {
        this.request = null;
        this.response = null;
        this.state = ProtocolState.REQUEST_READY;

        if (writer != null) {
            if (!isError) {      // If there is an error we do not release the buffer to the factory
                ByteBuffer buffer = writer.getBuffer();
                sourceConfiguration.getBufferFactory().release(buffer);
            }
        }

        this.reader = null;
        this.writer = null;
    }

    public Lock getLock() {
        return lock;
    }

    public boolean isShutDown() {
        return shutDown;
    }

    public void setShutDown(boolean shutDown) {
        this.shutDown = shutDown;
    }

    public Pipe getReader() {
        return reader;
    }

    public void setReader(Pipe reader) {
        this.reader = reader;
    }

    public Pipe getWriter() {
        return writer;
    }

    public void setWriter(Pipe writer) {
        this.writer = writer;
    }

    public static void create(NHttpConnection conn, ProtocolState state,
                              InboundConfiguration configuration) {
        InboundSourceContext info = new InboundSourceContext(configuration);

        conn.getContext().setAttribute(CONNECTION_INFORMATION, info);

        info.setState(state);
    }

    public static void updateState(NHttpConnection conn, ProtocolState state) {
        InboundSourceContext info = (InboundSourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        if (info != null) {
            info.setState(state);
        } else {
            throw new IllegalStateException("Connection information should be present");
        }
    }

    public static boolean assertState(NHttpConnection conn, ProtocolState state) {
        InboundSourceContext info = (InboundSourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null && info.getState() == state;

    }

    public static ProtocolState getState(NHttpConnection conn) {
        InboundSourceContext info = (InboundSourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getState() : null;
    }

    public static void setRequest(NHttpConnection conn, InboundHttpSourceRequest request) {
        InboundSourceContext info = (InboundSourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        if (info != null) {
            info.setRequest(request);
        } else {
            throw new IllegalStateException("Connection information should be present");
        }
    }

    public static void setResponse(NHttpConnection conn, InboundHttpSourceResponse response) {
        InboundSourceContext info = (InboundSourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        if (info != null) {
            info.setResponse(response);
        } else {
            throw new IllegalStateException("Connection information should be present");
        }
    }

    public static InboundHttpSourceRequest getRequest(NHttpConnection conn) {
        InboundSourceContext info = (InboundSourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getRequest() : null;
    }

    public static InboundHttpSourceResponse getResponse(NHttpConnection conn) {
        InboundSourceContext info = (InboundSourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getResponse() : null;
    }

    public static InboundSourceContext get(NHttpConnection conn) {
        return (InboundSourceContext) conn.getContext().getAttribute(CONNECTION_INFORMATION);
    }

    public static Lock getLock(NHttpConnection conn) {
        InboundSourceContext info = (InboundSourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getLock() : null;
    }
}
