package org.wso2.carbon.inbound.endpoint.protocol.hl7;

/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.nio.charset.Charset;

public class MLLPConstants {

    public static final byte[] CR          = {0x0D};
    public static final byte[] HL7_TRAILER = {0x1C,CR[0]};
    public static final byte[] HL7_HEADER  = {0x0B};

    // default charset
    public final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public final static String MLLP_CONTEXT = "HL7_MLLP_CONTEXT";

    public final static String PARAM_HL7_PORT = "inbound.hl7.port";

    public final static String PARAM_HL7_AUTO_ACK = "inbound.hl7.AutoAck";
}
