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

package org.wso2.carbon.http2.transport.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.CharsetUtil;
import org.apache.commons.collections.map.MultiValueMap;

import java.util.*;

public class Http2Response {

    private Map<String, String> headers = new HashMap();
    private Map excessHeaders = new MultiValueMap();
    private boolean endOfStream = false;
    private boolean expectResponseBody = false;
    private int status = 200;
    private String statusLine = "OK";
    private boolean responseFromHttp2Server = true;
    private byte[] data;

    public boolean isEndOfStream() {
        return endOfStream;
    }

    public boolean isExpectResponseBody() {
        return expectResponseBody;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map getExcessHeaders() {
        return excessHeaders;
    }

    public int getStatus() {
        return status;
    }

    public String getHeader(String contentType) {
        return headers.get(contentType);
    }

    public String getStatusLine() {
        return this.statusLine;
    }

    public Http2Response(FullHttpResponse response) {
        responseFromHttp2Server = false;
        endOfStream = true;
        List<Map.Entry<String, String>> headerList = response.headers().entries();
        for (Map.Entry header : headerList) {
            if (header.getKey().toString().equalsIgnoreCase(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.toString())) {
                continue;
            }
            String key = header.getKey().toString();
            key = (key.charAt(0) == ':') ? key.substring(1) : key;
            if (this.headers.containsKey(key)) {
                this.excessHeaders.put(key, header.getValue().toString());
            } else {
                this.headers.put(key, header.getValue().toString());
            }
        }
        this.status = response.status().code();
        this.statusLine = response.status().reasonPhrase();
        if (response.headers().contains(HttpHeaderNames.CONTENT_TYPE)) {
            expectResponseBody = true;
            setData(response);
        }


    }

    public Http2Response(Http2HeadersFrame frame) {
        responseFromHttp2Server = true;
        if (frame.isEndStream()) {
            endOfStream = true;
        }
        Iterator<Map.Entry<CharSequence, CharSequence>> iterator = frame.headers().iterator();
        while (iterator.hasNext()) {
            Map.Entry<CharSequence, CharSequence> header = iterator.next();
            String key = header.getKey().toString();
            key = (key.charAt(0) == ':') ? key.substring(1) : key;

            if (this.headers.containsKey(key)) {
                this.excessHeaders.put(key, header.getValue().toString());
            } else {
                this.headers.put(key, header.getValue().toString());
            }
        }
        if (headers.containsKey("status")) {
            status = Integer.parseInt(headers.get("status").toString());
        }
        if (headers.containsKey(HttpHeaderNames.CONTENT_TYPE)) {
            expectResponseBody = true;
        }
    }

    public void setDataFrame(Http2DataFrame data) {
        setData(data);
        expectResponseBody = true;
        if (data.isEndStream()) {
            endOfStream = true;
        }
    }

    public byte[] getBytes() {
        return data;
    }

    private void setData(Object res) {
        String response = "";
        ByteBuf content;

        if (res instanceof Http2DataFrame)
            content = ((Http2DataFrame) res).content();
        else
            content = ((FullHttpResponse) res).content();
        if (content.isReadable()) {
            int contentLength = content.readableBytes();
            byte[] arr = new byte[contentLength];
            content.readBytes(arr);
            response = new String(arr, 0, contentLength, CharsetUtil.UTF_8);
        }
        data = response.getBytes();
    }
}
