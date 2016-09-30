/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.websocket;

public class InboundWebsocketConstants {

    public static final String WS = "ws";
    public static final String WSS = "wss";
    public static final String INBOUND_ENDPOINT_PARAMETER_WEBSOCKET_PORT = "inbound.ws.port";

    public static final String INBOUND_BOSS_THREAD_POOL_SIZE = "ws.boss.thread.pool.size";
    public static final String INBOUND_WORKER_THREAD_POOL_SIZE = "ws.worker.thread.pool.size";

    public static final String INBOUND_SSL_KEY_STORE_FILE = "wss.ssl.key.store.file";
    public static final String INBOUND_SSL_KEY_STORE_PASS = "wss.ssl.key.store.pass";
    public static final String INBOUND_SSL_TRUST_STORE_FILE = "wss.ssl.trust.store.file";
    public static final String INBOUND_SSL_TRUST_STORE_PASS = "wss.ssl.trust.store.pass";
    public static final String INBOUND_SSL_CERT_PASS = "wss.ssl.cert.pass";

    public static final String WEBSOCKET_SOURCE_HANDSHAKE_PRESENT = "websocket.source.handshake.present";
    public static final String WEBSOCKET_TARGET_HANDSHAKE_PRESENT = "websocket.target.handshake.present";

    public static final String WEBSOCKET_TARGET_HANDLER_CONTEXT = "websocket.target.handler.context";
    public static final String WEBSOCKET_SOURCE_HANDLER_CONTEXT = "websocket.source.handler.context";

    public static final String WEBSOCKET_BINARY_FRAME_PRESENT = "websocket.binary.frame.present";
    public static final String WEBSOCKET_BINARY_FRAME = "websocket.binary.frame";

    public static final String WEBSOCKET_TEXT_FRAME_PRESENT = "websocket.text.frame.present";
    public static final String WEBSOCKET_TEXT_FRAME = "websocket.text.frame";

    public static final String WEBSOCKET_CLIENT_SIDE_BROADCAST_LEVEL = "ws.client.side.broadcast.level";

    public static final String WEBSOCKET_OUTFLOW_DISPATCH_SEQUENCE = "ws.outflow.dispatch.sequence";
    public static final String WEBSOCKET_OUTFLOW_DISPATCH_FAULT_SEQUENCE = "ws.outflow.dispatch.fault.sequence";

    public static final String INBOUND_SUBPROTOCOL_HANDLER_CLASS = "ws.subprotocol.handler.class";

    public static final String SYNAPSE_SUBPROTOCOL_PREFIX = "synapse";
    public static final String WEBSOCKET_SUBSCRIBER_PATH = "websocket.subscriber.path";
    public static final String INBOUND_HANDSHAKE_HANDLER_CLASS = "ws.handshake.handler.class";
    public static final String CUSTOM_SEQUENCE = "dispatch.custom.sequence";
}
