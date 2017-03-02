/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.websocket;

public class WSConstants {

    private WSConstants() {

    }

    public static final String TEXT = "text";
    public static final String BINARY = "binary";
    public static final String BACKEND_MESSAGE_TYPE = "backendMessageType";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String CONNECTION_TERMINATE = "connection.terminate";
    public static final String CLIENT_ID = "clientId";
    public static final String IS_CONNECTION_ALIVE = "isConnectionAlive";
    public static final String DEFAULT_CONTENT_TYPE = "ws.default.content.type";
    public static final String WS_CLOSE_FRAME_STATUS_CODE = "websocket.close.frame.status.code";
    public static final String WS_CLOSE_FRAME_REASON_TEXT = "websocket.close.frame.reason.text";

}
