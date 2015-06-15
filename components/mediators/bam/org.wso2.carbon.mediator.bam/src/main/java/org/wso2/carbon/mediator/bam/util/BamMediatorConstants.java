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
package org.wso2.carbon.mediator.bam.util;

/**
 * Constants used in the BAM mediator.
 */
public final class BamMediatorConstants {

    private BamMediatorConstants() {}

    public static final String BAM_HEADER_NAMESPACE_URI = "http://wso2.org/ns/2010/10/bam";
    public static final String BAM_EVENT = "BAMEvent";
    public static final String ACTIVITY_ID = "activityID";
    public static final String DIRECTION_IN = "IN";
    public static final String DIRECTION_OUT = "OUT";

    public static final String REMOTE_ADDRESS = "remote_address";

    public static final String SERVICE_NAME = "service_name";
    public static final String OPERATION_NAME = "operation_name";
    public static final String MSG_BAM_ACTIVITY_ID = "bam_activity_id";
    public static final String MSG_STR_ACTIVITY_ID = "activity_id";
    public static final String MSG_ID = "message_id";
    public static final String MSG_DIRECTION = "message_direction";

    public static final String VERSION = "version";
    public static final String NICK_NAME = "nickName";
    public static final String DESCRIPTION = "description";
    public static final String TENANT_ID = "tenant_id";

    public static final String REQUEST_RECEIVED_TIME = "timestamp";
    public static final String HTTP_METHOD = "http_method";
    public static final String CHARACTER_SET_ENCODING = "character_set_encoding";
    public static final String TRANSPORT_IN_URL = "transport_in_url";
    public static final String MESSAGE_TYPE = "message_type";
    public static final String REMOTE_HOST = "remote_host";
    public static final String SERVICE_PREFIX = "service_prefix";
    public static final String HOST = "host";
    //public static final String HOST_ADDRESS = "host_address";
    public static final String STRING = "STRING";
    public static final String INT = "INT";
    public static final String LONG = "Long";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String CORRELATION_DATA = "correlationData";
    public static final String META_DATA = "metaData";
    public static final String PAYLOAD_DATA = "payloadData";

    public static final String EMPTY_STRING = "";

    public static final int NUM_OF_CONST_CORRELATION_PARAMS = 1;
    public static final int NUM_OF_CONST_META_PARAMS = 9;
    public static final int NUM_OF_CONST_PAYLOAD_PARAMS = 5;

}
