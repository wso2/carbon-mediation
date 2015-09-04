/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.ui.internal;

public class InboundClientConstants {

    public static final String TYPE_HTTP = "http";
	public static final String TYPE_HTTPS = "https";
	public static final String TYPE_FILE = "file";
	public static final String TYPE_JMS = "jms";
    public static final String TYPE_HL7 = "hl7";
    public static final String TYPE_KAFKA = "kafka";
    public static final String TYPE_MQTT = "mqtt";
    public static final String TYPE_RABBITMQ = "rabbitmq";
    public static final String TYPE_RSS = "FeedEP";
    public static final String TYPE_CLASS = "class";
	public static final String EXCEPTION = "INBOUND_EXCEPTION";
	public static final String STRING_SPLITTER = " ~:~ ";

    public static final String[] LISTENER_TYPES = {TYPE_HTTP, TYPE_HTTPS, TYPE_HL7};

    public static final String[] LISTENER_PORT_PARAMS = {"inbound.http.port", "inbound.hl7.Port"};
}
