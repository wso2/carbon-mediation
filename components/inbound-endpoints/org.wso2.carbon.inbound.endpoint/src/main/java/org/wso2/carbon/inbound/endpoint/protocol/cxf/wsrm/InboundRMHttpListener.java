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
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.ClientAuthentication;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;
import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.w3c.dom.Document;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.interceptor.RequestInterceptor;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.interceptor.ResponseInterceptor;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.invoker.InboundRMHttpInvoker;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.management.CXFEndpointListenerManager;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.utils.RMConstants;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;


/**
 * Creates an endpoint that supports WS-RM using Apache CXF
 */
public class InboundRMHttpListener implements InboundRequestProcessor {

    private static final Logger logger = Logger.getLogger(InboundRMHttpListener.class);
    private String injectingSequence;
    private String onErrorSequence;
    private SynapseEnvironment synapseEnvironment;
    private int port;
    private Bus bus;
    private InboundRMHttpInvoker invoker;
    private String cxfServerConfigFileLoc;
    private String name;
    private String host;

    //For Secured inbound endpoints
    private Boolean enableSSL = false;
    private String axis2FilePath;
    private String keyStoreLocation;
    private String keyStorePassword;
    private String keyPassword;
    private String trustStoreLocation;
    private String trustStorePassword;



    public InboundRMHttpListener(InboundProcessorParams params) {

        this.port = Integer.parseInt(params.getProperties().getProperty(RMConstants.INBOUND_CXF_RM_PORT));
        this.injectingSequence = params.getInjectingSeq();
        this.onErrorSequence = params.getOnErrorSeq();
        this.synapseEnvironment = params.getSynapseEnvironment();
        this.cxfServerConfigFileLoc = params.getProperties().getProperty(RMConstants.INBOUND_CXF_RM_CONFIG_FILE);
        this.host = params.getProperties().getProperty(RMConstants.INBOUND_CXF_RM_HOST);
        this.enableSSL = Boolean.parseBoolean(params.getProperties().getProperty(RMConstants.CXF_ENABLE_SSL));
        this.axis2FilePath = params.getProperties().getProperty(RMConstants.AXIS2_FILE_PATH);
        this.name = params.getName();
    }

    /**
     * Starts a new CXF WS-RM Inbound Endpoint
     */
    @Override
    public void init() {

        if (!CXFEndpointListenerManager.getInstance().authorizeCXFInboundEndpoint(port, name)) {
            logger.info("A CXF RM inbound endpoint is already running on port " + port);
            return;
        }

        logger.info("Starting CXF RM Listener on " + this.host + ":" + this.port);
        SpringBusFactory bf = new SpringBusFactory();
        /*
         * Create the CXF Bus using the server config file
         */
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

        BusFactory.setDefaultBus(bus);
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
        invoker = new InboundRMHttpInvoker(RMServiceImpl, synapseEnvironment, injectingSequence, onErrorSequence);
        serverFactory.setInvoker(invoker);

        serverFactory.setServiceBean(RMServiceImpl);

        if (enableSSL) {
            try {
                readAxis2ConfigFile();
            } catch (Exception e) {
                throw new SynapseException("Error while obtaining keystore details from the axis2.xml file", e);
            }
            //set the host and port to listen to
            serverFactory.setAddress("https://" + host + ":" + port);

            try {
                serverFactory = configureSSLOnTheServer(serverFactory, port);
            } catch (GeneralSecurityException e) {
                throw new SynapseException("Security configuration failed with the following: " + e.getCause());
            } catch (IOException e) {
                throw new SynapseException("IO Exception while configuring security for the CXF inbound endpoint", e);
            }
        } else {
            //set the host and port to listen to
            serverFactory.setAddress("http://" + host + ":" + port);
        }
        serverFactory.create();
        CXFEndpointListenerManager.getInstance().addCXFEndpoint(port, bus);
    }

    /**
     * Shuts down the CXF WS-RM Inbound Endpoint
     */
    @Override
    public void destroy() {
        CXFEndpointListenerManager.getInstance().unregisterCXFInboundEndpoint(port);
        if (bus != null) {
            bus.shutdown(true);
        }
        invoker.getExecutorService().shutdown();
        logger.info("CXF-WS-RM Inbound Listener on " + host + ":" + port + " is shutting down");
    }

    private void readAxis2ConfigFile() throws Exception {

        File fXmlFile = new File(axis2FilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr;

        expr = xpath.compile("/axisconfig/transportReceiver[@name='https']/parameter[@name='keystore']/KeyStore/Location");
        keyStoreLocation = expr.evaluate(doc);
        expr = xpath.compile("/axisconfig/transportReceiver[@name='https']/parameter[@name='keystore']/KeyStore/Password");
        keyStorePassword = expr.evaluate(doc);
        expr = xpath.compile("/axisconfig/transportReceiver[@name='https']/parameter[@name='keystore']/KeyStore/KeyPassword");
        keyPassword = expr.evaluate(doc);
        expr = xpath.compile("axisconfig/transportReceiver[@name='https']/parameter[@name='truststore']/TrustStore/Location");
        trustStoreLocation = expr.evaluate(doc);
        expr = xpath.compile("axisconfig/transportReceiver[@name='https']/parameter[@name='truststore']/TrustStore/Password");
        trustStorePassword = expr.evaluate(doc);
    }

    private ServerFactoryBean configureSSLOnTheServer(ServerFactoryBean sf, int port)
            throws GeneralSecurityException, IOException {

        TLSServerParameters tlsParams = new TLSServerParameters();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        File truststore = new File(keyStoreLocation);

        keyStore.load(new FileInputStream(truststore), keyStorePassword.toCharArray());
        KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(keyStore, keyStorePassword.toCharArray());
        KeyManager[] keyManagers = keyFactory.getKeyManagers();
        tlsParams.setKeyManagers(keyManagers);

        truststore = new File(trustStoreLocation);
        keyStore.load(new FileInputStream(truststore), trustStorePassword.toCharArray());
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(keyStore);
        TrustManager[] tm = trustFactory.getTrustManagers();
        tlsParams.setTrustManagers(tm);
        FiltersType filter = new FiltersType();
        filter.getInclude().add(".*_EXPORT_.*");
        filter.getInclude().add(".*_EXPORT1024_.*");
        filter.getInclude().add(".*_WITH_DES_.*");
        filter.getInclude().add(".*_WITH_NULL_.*");
        filter.getExclude().add(".*_DH_anon_.*");
        tlsParams.setCipherSuitesFilter(filter);
        ClientAuthentication ca = new ClientAuthentication();
        ca.setRequired(true);
        ca.setWant(true);
        tlsParams.setClientAuthentication(ca);
        JettyHTTPServerEngineFactory factory = new JettyHTTPServerEngineFactory();
        factory.setTLSServerParametersForPort(host, port, tlsParams);

        return sf;
    }
}
