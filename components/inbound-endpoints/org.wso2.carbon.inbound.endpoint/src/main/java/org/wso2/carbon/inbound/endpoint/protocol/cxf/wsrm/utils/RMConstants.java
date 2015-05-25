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
package org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.utils;

/**
 * Holds the constants used by the CXF RM Inbound endpoint
 */
public class RMConstants {

    public static final String CXF_RM_MESSAGE_PAYLOAD = "cxf-rm.message-payload";
    public static final String CXF_RM_SYNAPSE_MEDIATED = "cxf-rm.synapse-mediated";
    public static final String SOAP_ENVELOPE = "cxf-rm.soap-envelope";
    public static final String SOAP_ACTION = "SOAPAction";
    public static final String CXF_CONTINUATION = "cxf-rm.continuation";
    public static final String CXF_EXCHANGE = "cxf-rm.exchange";
    public static final String INBOUND_CXF_RM_PORT = "inbound.cxf.rm.port";
    public static final String INBOUND_CXF_RM_HOST = "inbound.cxf.rm.host";
    public static final String INBOUND_CXF_RM_CONFIG_FILE = "inbound.cxf.rm.config-file";
    public static final String PASS_THROUGH_TARGET_BUFFER = "pass-through.pipe";
    public static final String AXIS2_FILE_PATH = "axis2.file.path";
    public static final String CXF_ENABLE_SSL = "enableSSL";
    public static final String SOCKET_LAYER_PROTOCOL = "socket.layer.protocol";

    public final static int THREAD_POOL_SIZE = 100;
}
