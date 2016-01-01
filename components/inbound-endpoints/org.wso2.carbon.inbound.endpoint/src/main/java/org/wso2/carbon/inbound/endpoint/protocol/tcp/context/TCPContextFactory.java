/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.inbound.endpoint.protocol.tcp.context;

import org.apache.http.nio.reactor.IOSession;
import org.apache.log4j.Logger;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.util.BufferFactory;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.InboundTCPConstants;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.TCPProcessor;

import java.nio.charset.CharsetDecoder;

/**
 * Create TCP context to store TCP message parameters
 */
public class TCPContextFactory {
    private static final Logger log = Logger.getLogger(TCPContextFactory.class);

    /**
     * Create & return a TCPContext.
     *
     * @param session   client information
     * @param processor TCPProcessor object
     * @return a TCPContext
     */
    public static TCPContext createTCPContext(IOSession session, TCPProcessor processor) {
        CharsetDecoder decoder =
                (CharsetDecoder) processor.getInboundParameterMap().get(InboundTCPConstants.TCP_CHARSET_DECODER);
        BufferFactory bufferFactory =
                (BufferFactory) processor.getInboundParameterMap().get(InboundTCPConstants.INBOUND_TCP_BUFFER_FACTORY);

        //adding inbound params to TCPContext
        InboundProcessorParams params =
                (InboundProcessorParams) processor.getInboundParameterMap().get(InboundTCPConstants.INBOUND_PARAMS);
        TCPContext tcpContext = new TCPContext(session, decoder, bufferFactory, params);

        //setting the TCP decoding mode parameters
        tcpContext.getCodec().setDecodeMode(processor.getDecodeMode());
        switch (processor.getDecodeMode()) {
            case InboundTCPConstants.DECODE_BY_HEADER_TRAILER: {
                tcpContext.getCodec().setHeader(processor.getHeader());
                tcpContext.getCodec().setTrailer(processor.getTrailer());
                break;
            }
            case InboundTCPConstants.DECODE_BY_TAG: {
                byte[] delimiterTag = processor.getTag().getBytes(decoder.charset());
                tcpContext.getCodec().setTag(processor.getTag());
                tcpContext.getCodec().setDelimiterTag(delimiterTag);
                break;
            }
            case InboundTCPConstants.DECODE_BY_LENGTH: {
                tcpContext.getCodec().setMsgLength(processor.getMsgLength());
                break;
            }
            default: {
                throw new RuntimeException("Could not decide the decode mode");
            }
        }
        return tcpContext;
    }
}
