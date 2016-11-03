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

package org.wso2.carbon.inbound.endpoint.protocol.http2.common;

public class InboundHttp2Constants {

    public static final String HTTP2 = "http2";
    public static final String HTTPS2 = "https2";

    public static final String INBOUND_PORT = "inbound.http2.port";
    public static final String INBOUND_ENDPOINT_PARAMETER_DISPATCH_FILTER_PATTERN = "dispatch.filter.pattern";

    public static final String INBOUND_BOSS_THREAD_POOL_SIZE = "http2.boss.thread.pool.size";
    public static final String INBOUND_WORKER_THREAD_POOL_SIZE = "http2.worker.thread.pool.size";
    public static final String INBOUND_SO_BACKLOG = "http2.so.backlog";

    public static final String INBOUND_SSL_KEY_STORE_FILE = "https2.ssl.key.store.file";
    public static final String INBOUND_SSL_KEY_STORE_PASS = "https2.ssl.key.store.pass";
    public static final String INBOUND_SSL_TRUST_STORE_FILE = "https2.ssl.trust.store.file";
    public static final String INBOUND_SSL_TRUST_STORE_PASS = "https2.ssl.trust.store.pass";
    public static final String INBOUND_SSL_CERT_PASS = "https2.ssl.cert.pass";

}
