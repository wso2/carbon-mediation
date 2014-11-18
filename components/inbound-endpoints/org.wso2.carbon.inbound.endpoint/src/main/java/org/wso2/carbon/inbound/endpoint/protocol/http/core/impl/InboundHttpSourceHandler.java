/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.inbound.endpoint.protocol.http.core.impl;

import org.apache.http.HttpException;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.log4j.Logger;
import org.apache.synapse.transport.passthru.*;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;

import java.io.IOException;
import java.io.OutputStream;

public class InboundHttpSourceHandler extends SourceHandler {

    private static final Logger log = Logger.getLogger(InboundHttpSourceHandler.class);

    private final SourceConfiguration sourceConfiguration;

    private final InboundHttpConfiguration inboundHttpConfiguration;

    public InboundHttpSourceHandler
            (SourceConfiguration sourceConfiguration, InboundHttpConfiguration inboundHttpConfiguration) {
        super(sourceConfiguration);
        this.sourceConfiguration = sourceConfiguration;
        this.inboundHttpConfiguration = inboundHttpConfiguration;
    }

    @Override
    public void requestReceived(NHttpServerConnection conn) {
        try {
            //Create Source Request related to HTTP Request
            SourceRequest request = getSourceRequest(conn);
            if (request == null) {
                log.warn("No SourceRequest created for HTTP Request");
                return;
            }
            String method = request.getRequest() != null ? request.getRequest().getRequestLine().getMethod().toUpperCase() : "";
            //Get output Stream for write response
            OutputStream os = getOutputStream(method, request);
            // Handover Request to Worker Pool
            sourceConfiguration.getWorkerPool().execute
                    (new InboundHttpServerWorker(request, sourceConfiguration, inboundHttpConfiguration, os));
        } catch (HttpException e) {
            log.error("HttpException occurred when creating Source Request", e);
            informReaderError(conn);
            SourceContext.updateState(conn, ProtocolState.CLOSED);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        } catch (IOException e) {
            logIOException(conn, e);
            informReaderError(conn);
            SourceContext.updateState(conn, ProtocolState.CLOSED);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        }
    }
}
