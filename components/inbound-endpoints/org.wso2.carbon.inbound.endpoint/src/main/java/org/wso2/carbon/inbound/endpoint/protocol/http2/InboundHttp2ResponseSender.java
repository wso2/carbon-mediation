/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.http2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.SourceHandler;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class InboundHttp2ResponseSender implements InboundResponseSender {

    private static final Log log = LogFactory.getLog(InboundHttp2ResponseSender.class);
    private SourceHandler sourceHandler;

    public InboundHttp2ResponseSender(SourceHandler sourceHandler) {
        this.sourceHandler = sourceHandler;
    }

    public void sendBack(MessageContext synCtx) {
        if (synCtx != null) {
            try {
                RelayUtils.buildMessage(((Axis2MessageContext) synCtx).getAxis2MessageContext());
                sourceHandler.sendResponse(synCtx);
            } catch (IOException iEx) {
                log.error("Error while building the message", iEx);
            } catch (XMLStreamException ex) {
                log.error("Failed to convert message to specified output format", ex);
            }
        } else {
            log.debug("send back message is null");
        }
    }

}
