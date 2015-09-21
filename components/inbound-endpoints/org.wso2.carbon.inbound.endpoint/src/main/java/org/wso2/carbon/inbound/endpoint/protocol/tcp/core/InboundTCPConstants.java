/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.tcp.core;

import java.nio.charset.Charset;

/**
 * TCP Message related constants
 */
public class InboundTCPConstants {

    public static final byte[] CR = { 0x0D };
    public static final byte[] TCP_HEADER = { 0x0B };
    public static final byte[] TCP_TRAILER = { 0x1C, CR[0] };

    //variables from the input config file
    public final static String INBOUND_TCP_PORT = "inbound.tcp.Port";

    public final static String PARAM_TCP_CHARSET = "inbound.tcp.CharSet";

    public final static String TCP_MSG_CONTENT_TYPE = "inbound.tcp.ContentType";

    public final static String TCP_MSG_HEADER = "inbound.tcp.Header";

    public final static String TCP_MSG_TRAILER_BYTE1 = "inbound.tcp.Trailer.byte1";

    public final static String TCP_MSG_TRAILER_BYTE2 = "inbound.tcp.Trailer.byte2";

    public final static String TCP_MSG_LENGTH = "inbound.tcp.msg.Length";

    public final static String TCP_MSG_TAG = "inbound.tcp.enclosure.tag";

    public final static String TCP_MSG_ONE_WAY = "inbound.tcp.sendOnly";

    public final static String TCP_CONTEXT = "TCP_CONTEXT";

    public final static int ONE_TCP_MESSAGE_IS_DECODED = 1;

    public final static String TCP_INBOUND_TENANT_DOMAIN = "TCP_INBOUND_TENANT_DOMAIN";

    public final static String INBOUND_PARAMS = "TCP_INBOUND_PARAMS";

    public final static String INBOUND_TCP_BUFFER_FACTORY = "INBOUND_TCP_BUFFER_FACTORY";

    public final static String TCP_REQ_PROC = "TCP_REQ_PROCESSOR";

    public final static String PARAM_TCP_TIMEOUT = "inbound.tcp.TimeOut";

    public final static int DEFAULT_TCP_TIMEOUT = 10000;

    public final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public final static String TCP_CHARSET_DECODER = "TCP_CHARSET_DECODER";

    public final static String TCP_INBOUND_MSG_ID = "TCP_INBOUND_MSG_ID";

    //TCP message decoding mode
    public final static int NOT_DECIDED_YET = 0;

    public final static int DECODE_BY_HEADER_TRAILER = 1;

    public final static int DECODE_BY_TAG = 2;

    public final static int DECODE_BY_LENGTH = 3;

    //tcp level constants
    public static class TCPConstants {

        public final static String IO_THREAD_COUNT = "io_thread_count";

        public final static String CONNECT_TIMEOUT = "connect_timeout";

        public final static String TCP_NO_DELAY = "tcp_no_delay";
        //Whether to send available data immediately rather than buffering it

        public final static String SO_KEEP_ALIVE = "so_keep_alive";
        //How long to keep the socket open to allow pending sends to complete

        public final static String SO_TIMEOUT = "so_timeout";//The timeout for blocking socket operations.

        public final static String SELECT_INTERVAL = "select_interval";

        public final static String SHUTDOWN_GRACE_PERIOD = "shutdown_grace_period";

        public final static String SO_RCVBUF = "so_rcvbuf";

        public final static String SO_SNDBUF = "so_sndbuf";

        public final static String WORKER_THREADS_CORE = "worker_threads_core";

        public final static int WORKER_THREADS_CORE_DEFAULT = 100;

    }
}
