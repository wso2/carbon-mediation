/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.https;


import org.apache.log4j.Logger;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.core.ssl.SSLConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http.InboundHttpConstants;
import org.wso2.carbon.inbound.endpoint.protocol.http.InboundHttpListener;
import org.wso2.carbon.inbound.endpoint.protocol.http.config.WorkerPoolConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http.management.HTTPEndpointManager;

public class InboundHttpsListener extends InboundHttpListener {

    private static final Logger log = Logger.getLogger(InboundHttpListener.class);

    private SSLConfiguration sslConfiguration;
    private int port;
    private String name;
    private InboundProcessorParams processorParams;

    public InboundHttpsListener(InboundProcessorParams params) {
        super(params);
        processorParams = params;
        String portParam = params.getProperties().getProperty(
                   InboundHttpConstants.INBOUND_ENDPOINT_PARAMETER_HTTP_PORT);
        try {
            port = Integer.parseInt(portParam);
        } catch (NumberFormatException e) {
            handleException("Please provide port number as integer  instead of  port  " + portParam, e);
        }
        name = params.getName();
        String keyStoreParam = params.getProperties().getProperty(InboundHttpConstants.KEY_STORE);
        String trustStoreParam = params.getProperties().getProperty(InboundHttpConstants.TRUST_STORE);
        String clientAuthParam = params.getProperties().getProperty(InboundHttpConstants.SSL_VERIFY_CLIENT);
        String sslProtocol = params.getProperties().getProperty(InboundHttpConstants.SSL_PROTOCOL);
        String httpsProtocols = params.getProperties().getProperty(InboundHttpConstants.HTTPS_PROTOCOL);
        String certificateRevocation = params.getProperties().getProperty(InboundHttpConstants.CLIENT_REVOCATION);
        sslConfiguration = new SSLConfiguration(keyStoreParam, trustStoreParam, clientAuthParam,
                                                httpsProtocols, certificateRevocation, sslProtocol);

    }

    @Override
    public void init() {
        if (isPortUsedByAnotherApplication(port)) {
            log.warn("Port " + port + "used by inbound endpoint " + name + " is already used by another application " +
                     "hence undeploying inbound endpoint");
            this.destroy();
        } else {
            HTTPEndpointManager.getInstance().startSSLEndpoint(port, name, sslConfiguration, processorParams);
        }

    }

}
