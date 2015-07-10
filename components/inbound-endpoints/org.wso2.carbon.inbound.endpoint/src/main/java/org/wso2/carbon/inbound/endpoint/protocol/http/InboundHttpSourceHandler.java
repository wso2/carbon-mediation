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

package org.wso2.carbon.inbound.endpoint.protocol.http;

import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.http.HttpException;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.log4j.Logger;
import org.apache.synapse.transport.passthru.ProtocolState;
import org.apache.synapse.transport.passthru.SourceContext;
import org.apache.synapse.transport.passthru.SourceHandler;
import org.apache.synapse.transport.passthru.SourceRequest;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http.config.WorkerPoolConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http.management.HTTPEndpointManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * Handler Class for process HTTP Requests
 */
public class InboundHttpSourceHandler extends SourceHandler {

    private static final Logger log = Logger.getLogger(InboundHttpSourceHandler.class);

    private final SourceConfiguration sourceConfiguration;
    private int port;
    private WorkerPool workerPool;

    public InboundHttpSourceHandler(int port, SourceConfiguration sourceConfiguration) {
        super(sourceConfiguration);
        this.sourceConfiguration = sourceConfiguration;
        this.port = port;
    }

    @Override
    public void requestReceived(NHttpServerConnection conn) {
        try {
            //Create Source Request related to HTTP Request
            SourceRequest request = getSourceRequest(conn);
            if (request == null) {
                return;
            }
            String method = request.getRequest() != null ? request.getRequest().getRequestLine().getMethod().toUpperCase() : "";
            //Get output Stream for write response for HTTP GET and HEAD methods
            OutputStream os = getOutputStream(method, request);
            // Handover Request to Worker Pool

            Pattern dispatchPattern = null;

            String tenantDomain = getTenantDomain(request);

            if (tenantDomain != null) {
                WorkerPoolConfiguration workerPoolConfiguration =
                           HTTPEndpointManager.getInstance().getWorkerPoolConfiguration(tenantDomain, port);
                if (workerPoolConfiguration != null) {
                    workerPool = sourceConfiguration.getWorkerPool(workerPoolConfiguration.getWorkerPoolCoreSize(),
                                                                   workerPoolConfiguration.getWorkerPoolSizeMax(),
                                                                   workerPoolConfiguration.getWorkerPoolThreadKeepAliveSec(),
                                                                   workerPoolConfiguration.getWorkerPoolQueuLength(),
                                                                   workerPoolConfiguration.getThreadGroupID(),
                                                                   workerPoolConfiguration.getThreadID());
                }

                dispatchPattern = HTTPEndpointManager.getInstance().getPattern(tenantDomain, port);
            }
            if (workerPool == null) {
                workerPool = sourceConfiguration.getWorkerPool();
            }
            workerPool.execute
                    (new InboundHttpServerWorker(port, tenantDomain, request, sourceConfiguration, os, dispatchPattern));
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

    private String getTenantDomain(SourceRequest request) {
        String tenant = MultitenantUtils.getTenantDomainFromUrl(request.getUri());
        if (tenant.equals(request.getUri())) {
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return tenant;
    }
}
