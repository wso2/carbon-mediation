/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.transports.http.bridge;

/**
 * {@code BridgeConstants} contains the constants related to netty axis2 bridge.
 */
public class BridgeConstants {
    public static final String BRIDGE_LOG_PREFIX = "[Bridge] ";

    public static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";

    public static final String REMOTE_HOST = "REMOTE_HOST";
    /* Thread pool related constants */
    public static final int DEFAULT_WORKER_POOL_SIZE_CORE = 400;
    public static final int DEFAULT_WORKER_POOL_SIZE_MAX = 500;
    public static final int DEFAULT_WORKER_THREAD_KEEPALIVE_SEC = 60;
    public static final int DEFAULT_WORKER_POOL_QUEUE_LENGTH = -1;
    public static final String HTTP_WORKER_THREAD_GROUP_NAME = "HTTP Worker Thread Group";
    public static final String HTTP_WORKER_THREAD_ID = "HTTPWorker";

    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String HTTP_STATUS_CODE = "HTTP_STATUS_CODE";
    public static final String HTTP_REASON_PHRASE = "HTTP_REASON_PHRASE";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String SOAP_ACTION_HEADER = "SOAPAction";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    public static final String JSON_CONTENT_TYPE = "application/json";

    public static final String CONTENT_LEN = "Content-Length";

    public static final String HTTP_STATUS_CODE_PROP = "HTTP_SC";
    public static final String HTTP_STATUS_CODE_DESCRIPTION_PROP = "HTTP_SC_DESC";

    public static final String REST_URL_POSTFIX = "REST_URL_POSTFIX";
    public static final String SERVICE_PREFIX = "SERVICE_PREFIX";

    public static final String REST_REQUEST_CONTENT_TYPE = "synapse.internal.rest.contentType";
    public static final String HTTP_CARBON_MESSAGE = "HTTP_CARBON_MESSAGE";
    public static final String HTTP_CLIENT_REQUEST_CARBON_MESSAGE = "HTTP_CLIENT_REQUEST_CARBON_MESSAGE";

    public static final String MESSAGE_BUILDER_INVOKED = "message.builder.invoked";

    public static final long NO_CONTENT_LENGTH_FOUND = -1;
    public static final short ONE_BYTE = 1;

    public static final String INVOKED_REST = "invokedREST";

    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String RELAY_EARLY_BUILD = "relay_early_build";
    public static final String RAW_PAYLOAD = "RAW_PAYLOAD";

    public static final String POOLED_BYTE_BUFFER_FACTORY = "POOLED_BYTE_BUFFER_FACTORY";

    public static final String MESSAGE_OUTPUT_FORMAT = "MESSAGE_OUTPUT_FORMAT";
}
