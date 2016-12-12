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

public class Http2Constants {
    public static final String HTTP2 = "http2";
    public static final String HTTPS2 = "https2";

    public static final String TRUST_STORE_LOCATION = "h2.trust.store.location";
    public static final String TRUST_STORE_PASSWORD = "h2.trust.store.Password";
    public static final String TRUST_STORE_CONFIG_ELEMENT = "h2.trust.store";



    public static final String HTTP2_REQUEST_TYPE="http2.reqeust.type";
    public static final String HTTP2_CLIENT_SENT_REQEUST="http2.client.sent.reqeust";
    public static final String HTTP2_PUSH_PROMISE_REQEUST="http2.push.promise.reqeust";
    public static final String HTTP2_PUSH_PROMISE_REQEUST_ENABLED="http2.push.promise.reqeust.enable";
    public static final String HTTP2_RESET_REQEUST="http2.reset.reqeust";
    public static final String HTTP2_GO_AWAY_REQUEST="http2.go.away.reqeust";
    public static final String HTTP2_PING_REQUEST="http2.ping.request";
    public static final String HTTP2_RESPONSE_SENT="http2.response.sent";

    public static final String HTTP2_PUSH_PROMISE_ID="http2.push.promise.id";
    public static final String HTTP2_PUSH_PROMISE_HEADERS="http2.push.promise.headers";


    public static final String HTTP2_SERVER_STREAM_ID="http2.server.stream.id";
    public static final String HTTP2_ERROR_CODE="http2.error.code";


    public static final String PASSTHROUGH_TARGET="passthru.target";
    public static final String PASS_THROUGH_SOURCE_CONFIGURATION = "PASS_THROUGH_SOURCE_CONFIGURATION";
    public static final String PASS_THROUGH_SOURCE_CONNECTION = "pass-through.Source-Connection";
    public static final String PASS_THROUGH_SOURCE_REQUEST = "pass-through.Source-Request";
    public static final String PASS_THROUGH_TARGET_CONNECTION = "pass-through.Target-Connection";
    public static final String PASS_THROUGH_TARGET_RESPONSE = "pass-through.Target-Response";

    public static final String HTTP2_DISPATCH_SEQUENCE="http2.dispatch.sequence";
    public static final String HTTP2_ERROR_SEQUENCE="http2.error.sequence";


}
