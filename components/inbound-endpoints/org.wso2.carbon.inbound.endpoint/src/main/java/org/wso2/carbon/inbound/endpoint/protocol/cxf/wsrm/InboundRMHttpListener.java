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
package org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.log4j.Logger;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.interceptor.RequestInterceptor;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.interceptor.ResponseInterceptor;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.invoker.InboundRMHttpInvoker;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.management.CXFEndpointManager;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.utils.RMConstants;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Creates an endpoint that supports WS-RM using Apache CXF
 */
public class InboundRMHttpListener implements InboundRequestProcessor {

    private static final Logger logger = Logger.getLogger(InboundRMHttpListener.class);
    private String injectingSequence;
    private String onErrorSequence;
    private int port;
    private InboundRMHttpInvoker invoker;
    private String cxfServerConfigFileLoc;
    private String name;
    private String host;
    private Server server;
    private InboundProcessorParams params;

    //For Secured inbound endpoints
    private Boolean enableSSL = false;

    public InboundRMHttpListener(InboundProcessorParams params) {

        this.port = Integer.parseInt(params.getProperties().getProperty(RMConstants.INBOUND_CXF_RM_PORT));
        this.injectingSequence = params.getInjectingSeq();
        this.onErrorSequence = params.getOnErrorSeq();
        this.cxfServerConfigFileLoc = params.getProperties().getProperty(RMConstants.INBOUND_CXF_RM_CONFIG_FILE);
        this.host = params.getProperties().getProperty(RMConstants.INBOUND_CXF_RM_HOST);
        this.enableSSL = Boolean.parseBoolean(params.getProperties().getProperty(RMConstants.CXF_ENABLE_SSL));
        this.name = params.getName();
        this.params = params;
    }

    @Override
    public void init() {
        if (CXFEndpointManager.getInstance().authorizeCXFInboundEndpoint(port, name, params)) {
            startListener();
        }
    }

    /**
     * Starts a new CXF WS-RM Inbound Endpoint
     */
    public void startListener() {
        logger.info("Starting CXF RM Listener on " + this.host + ":" + this.port);
        SpringBusFactory bf = new SpringBusFactory();
        /*
         * Create the CXF Bus using the server config file
         */
        Bus bus;
        if (cxfServerConfigFileLoc != null) {
            File cxfServerConfigFile = new File(cxfServerConfigFileLoc);
            try {
                URL busFile = cxfServerConfigFile.toURI().toURL();
                bus = bf.createBus(busFile.toString());
            } catch (MalformedURLException e) {
                logger.error("The provided CXF RM configuration file location is invalid", e);
                return;
            }
        } else {
            logger.error("CXF RM Inbound endpoint creation failed. " +
                         "The CXF RM inbound endpoint requires a configuration file to initialize");
            return;
        }
        /*
         * Create a dummy class to act as the service class of the CXF endpoint
         */
        InboundRMServiceImpl RMServiceImpl = new InboundRMServiceImpl();
        ServerFactoryBean serverFactory = new ServerFactoryBean();
        serverFactory.setBus(bus);

        //Add an interceptor to remove the unnecessary interceptors from the CXF Bus
        serverFactory.getInInterceptors().add(new RequestInterceptor());
        //Add an interceptor to alter the outgoing messages
        serverFactory.getOutInterceptors().add(new ResponseInterceptor());
        //Add an invoker to extract the message and inject to Synapse
        invoker = new InboundRMHttpInvoker(RMServiceImpl, injectingSequence, onErrorSequence);
        serverFactory.setInvoker(invoker);

        serverFactory.setServiceBean(RMServiceImpl);

        String protocol = "http";
        if (enableSSL) {
            protocol = "https";
        }
        //set the host and port to listen to
        serverFactory.setAddress(protocol + "://" + host + ":" + port);
        server = serverFactory.create();
        CXFEndpointManager.getInstance().registerCXFInboundEndpoint(port, this);
    }

    @Override
    public void close() {

    }

    /**
     * Shuts down the CXF WS-RM Inbound Endpoint
     */
    @Override
    public void destroy() {
        CXFEndpointManager.getInstance().unregisterCXFInboundEndpoint(port);
        if (server != null) {
            server.stop();
            server.destroy();
        }
        invoker.getExecutorService().shutdown();
        logger.info("CXF-WS-RM Inbound Listener on " + host + ":" + port + " is shutting down");
    }
}
