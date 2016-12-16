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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Frame;
import org.apache.log4j.Logger;
import org.apache.synapse.transport.passthru.Pipe;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Http2SourceRequest {
    private Pipe pipe = null;
    private Logger log = Logger.getLogger(Http2SourceRequest.class);
    private int streamID;
    private ChannelHandlerContext channel;
    private HashMap<Byte, Http2Frame> frames = new HashMap<Byte, Http2Frame>();
    private Map<String, String> headers = new TreeMap<String, String>(new Comparator<String>() {
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    });
    private String method = null;
    private String uri = null;
    private String scheme = null;
    private boolean processedReq = false;

    private String requestType = null;
    private Map<String, String> excessHeaders = new TreeMap<String, String>();

    public Http2SourceRequest(int streamID, ChannelHandlerContext channel) {
        this.streamID = streamID;
        this.channel = channel;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getScheme() {
        if (scheme != null) {
            return scheme;
        } else if (headers.containsKey("scheme")) {
            return headers.get("scheme");
        } else {
            return "http";
        }
    }

    public int getStreamID() {
        return streamID;
    }

    public Map<String, String> getExcessHeaders() {
        return excessHeaders;
    }

    public ChannelHandlerContext getChannel() {
        return channel;
    }

    public void setChannel(ChannelHandlerContext channel) {
        this.channel = channel;
    }

    @Deprecated
    public Map<String, String> getHeaders() {

        return headers;
    }

    @Deprecated
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getMethod() {
        if (method != null) {
            return method;
        } else if (headers.containsKey("method")) {
            return headers.get("method");
        } else {
            return null;
        }
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHeader(String key) {
        if (headers.containsKey(key)) {
            return headers.get(key);
        } else {
            return null;
        }
    }

    public String getUri() {
        if (uri != null) {
            return uri;
        } else if (headers.containsKey("path")) {
            uri = headers.get("path");
            if (uri.charAt(0) != '/')
                uri = '/' + uri;
            return uri;
        } else {
            return null;
        }
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        String name = "";
        name += "Stream Id:" + streamID + "/n";
        if (headers.size() > 0) {
            name += "Headers:/n";
            for (Map.Entry h : headers.entrySet()) {
                name += h.getKey() + ":" + h.getValue() + "/n";
            }
        }
        if (frames.size() > 0) {
            name += "Frames : /n";
            for (Map.Entry h : frames.entrySet()) {
                name += h.getKey().toString() + ":" + h.getValue() + "/n";
            }
        }
        return name;
    }

    public void setHeader(String key, String value) {
        if (key.charAt(0) == ':') {
            key = key.substring(1);
        }
        if (key.equalsIgnoreCase("authority")) {
            key = "host";
        }
        if (headers.containsKey(key)) {
            excessHeaders.put(key, value);
        } else {
            headers.put(key, value);
        }
    }

    public boolean isProcessedReq() {
        return processedReq;
    }

    public void setProcessedReq(boolean processedReq) {
        this.processedReq = processedReq;
    }

    public Pipe getPipe() {
        return pipe;
    }

    public void setPipe(Pipe pipe) {
        this.pipe = pipe;
    }
}

