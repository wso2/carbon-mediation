/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.http.utils;


public class InboundConstants {

    public final static int MAXIMUM_CONNECTIONS_QUEUED = 1024;
    public final static int MAXIMUM_CHUNK_SIZE_AGGREGATOR = 1048576;
    public final static int WORKER_POOL_SIZE = 10;
    public final static int REQUEST_BUFFER_CAPACITY = 1024;
    public final static int SYNAPSE_RESPONSE_BUFFER_SIZE=8192;
    public final static String IS_INBOUND_ENDPOINT = "inbound-endpoint";
    public final static String TEXT_XML = "text/xml";

    public final static String SOAP_ACTION = "SOAPAction";
    public final static int SOAP_11 = 1;
    public final static int SOAP_12 = 2;
    public final static String PASS_THROUGH_TARGET_BUFFER = "pass-through.pipe";
    public final static String CONTENT_TYPE = "ContentType";
    public static final String HTTP_INBOUND_TRANSPORT_WORKER_POOL =
            "PASS_THROUGH_TRANSPORT_WORKER_POOL";
    public static final String HTTP_INBOUND_SOURCE_CONFIGURATION =
            "PASS_THROUGH_SOURCE_CONFIGURATION";
    public static final String HTTP_INBOUND_SOURCE_CONNECTION = "pass-through.Source-Connection";
    public static final String HTTP_INBOUND_SOURCE_REQUEST = "pass-through.Source-Request";
    public static final String HTTP_INBOUND_TARGET_CONNECTION = "pass-through.Target-Connection";
    public static final String HTTP_INBOUND_TARGET_RESPONSE = "pass-through.Target-Response";
    public static final String CHANNEL_HANDLER_CONTEXT = "ChannelHandlerContext";
    public static final String INBOUND_ENDPOINT_INTERVAL = "interval";
    public static final String INBOUND_ENDPOINT_PARAMETER_HTTP_PORT = "inbound.http.port";



}