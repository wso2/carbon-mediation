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

import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http2.Http2DataFrame;
import org.apache.http.nio.ContentDecoder;

import java.io.IOException;
import java.nio.ByteBuffer;

public class HTTP2Decoder implements ContentDecoder {
    Http2DataFrame dataFrame = null;
    boolean complete = false;

    public HTTP2Decoder(Http2DataFrame dataFrame) {
        this.dataFrame = dataFrame;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        complete = false;
        byte[] data = ByteBufUtil.getBytes(dataFrame.content());
        dst.put(data);
        complete = true;
        return data.length;
    }

    @Override
    public boolean isCompleted() {
        return dataFrame.isEndStream() & complete;
    }
}
