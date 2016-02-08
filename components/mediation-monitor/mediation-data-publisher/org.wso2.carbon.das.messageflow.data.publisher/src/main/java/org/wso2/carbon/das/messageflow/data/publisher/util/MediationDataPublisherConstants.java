/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.das.messageflow.data.publisher.util;


import org.wso2.carbon.core.RegistryResources;

public class MediationDataPublisherConstants {

    public static final String DAS_MEDIATION_MESSAGE_FLOW_REG_PATH = RegistryResources.COMPONENTS
                                                               + "org.wso2.carbon.das.messageflow.data.publisher/";

    public static final String DAS_SERVER_LIST_REG_PATH = DAS_MEDIATION_MESSAGE_FLOW_REG_PATH + "servers";


    // key, value constants

    public static final String MESSAGE_ID = "messageId";
    public static final String COMPONENT_ID = "componentId";
    public static final String COMPONENT_NAME = "componentName";
    public static final String PAYLOAD = "payload";
    public static final String TRACE_TIMESTAMP = "timestamp";
    public static final String PROPERTY_MAP = "propertyMap";
    public static final String TRANSPORT_PROPERTY_MAP = "transportPropertyMap";
    public static final String ENTRY_TYPE = "entryType";
    public static final String PARENT = "parent";


    public static final String DEFAULT_DAS_SERVER_URL = "tcp://127.0.0.1:7611";

    public static final String COMPONENT_STREAM_NAME = "esb-flow_tracer-component-entry-stream";
    public static final String COMPONENT_STREAM_VERSION = "1.0.0";

    public static final String TRACE_STREAM_NAME = "esb-flow_tracer-trace-entry-stream";
    public static final String TRACE_STREAM_VERSION = "1.0.0";

}
