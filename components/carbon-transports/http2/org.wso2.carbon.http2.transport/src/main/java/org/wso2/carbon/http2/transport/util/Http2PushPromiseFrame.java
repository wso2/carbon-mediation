package org.wso2.carbon.http2.transport.util;

import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2StreamFrame;

public class Http2PushPromiseFrame implements Http2StreamFrame {
    private int streamId;
    private int pushPromiseId;
    private Http2Headers headers;

    @Override
    public Http2StreamFrame setStreamId(int i) {
        streamId = i;
        return this;
    }

    @Override
    public int streamId() {
        return streamId;
    }

    @Override
    public String name() {
        return "PushPromiseFrame";
    }

    public int getPushPromiseId() {
        return pushPromiseId;
    }

    public Http2StreamFrame setPushPromiseId(int pushPromiseId) {
        this.pushPromiseId = pushPromiseId;
        return this;
    }

    public Http2Headers getHeaders() {
        return headers;
    }

    public Http2StreamFrame setHeaders(Http2Headers headers) {
        this.headers = headers;
        return this;
    }
}
