/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transports.sap.bapi;

import com.sap.conn.jco.server.JCoServerFunctionHandler;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.AbapClassException;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axiom.soap.SOAPEnvelope;
import org.wso2.carbon.transports.sap.SAPConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This class handles BAPI calls returned from the SAP gateway.
 * <p>
 * This class encapsulates workers for each bapi/rfc request recieved. Workers are responsible for
 * handling  and converting them to SOAP format
 * </p>
 */
public class Axis2RFCHandler implements JCoServerFunctionHandler {

    private static final Log log = LogFactory.getLog(Axis2RFCHandler.class);

    private WorkerPool workerPool;
    private BAPIEndpoint endpoint;

    public Axis2RFCHandler(BAPIEndpoint endpoint, WorkerPool workerPool) {
        this.endpoint = endpoint;
        this.workerPool = workerPool;
    }

    /**
     * handle bapi requests coming through SAP gateway
     *
     * @param jCoServerContext JCO Server environment configuration
     * @param jCoFunction      bAPI/rfc function being called
     * @throws AbapException
     * @throws AbapClassException
     */
    public void handleRequest(JCoServerContext jCoServerContext,
                              JCoFunction jCoFunction) throws AbapException, AbapClassException {

        if (log.isDebugEnabled()) {
            log.debug("New BAPI function call received");
        }
        String xml = jCoFunction.toXML();
        workerPool.execute(new BAPIWorker(xml));
    }

    private class BAPIWorker implements Runnable {

        private JCoServerContext serverContext;
        private JCoFunction function;
        private String xmlContent;

        public BAPIWorker(JCoServerContext serverContext, JCoFunction function) {
            this.serverContext = serverContext;
            this.function = function;
        }

        BAPIWorker(String xmlContent) {
            this.xmlContent = xmlContent;
        }

        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Starting a new BAPI worker thread to process the incoming request");
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(this.xmlContent.getBytes());
            MessageContext msgContext = null;
            try {
                msgContext = endpoint.createMessageContext();
                msgContext.setIncomingTransportName(SAPConstants.SAP_BAPI_PROTOCOL_NAME);
                if (log.isDebugEnabled()) {
                    log.debug("Creating SOAP envelope from the BAPI function call");
                }
                SOAPEnvelope envelope = TransportUtils.createSOAPMessage(msgContext, bais,
                        SAPConstants.SAP_CONTENT_TYPE);
                msgContext.setEnvelope(envelope);
                //pass the constructed IDOC message through Axis engine
                AxisEngine.receive(msgContext);
            } catch (Exception e) {
                log.error("Error while processing the BAPI call through the Axis engine", e);
            } finally {
                try {
                    bais.close();
                } catch (IOException e) {
                    log.error("Error while closing the stream", e);
                }
            }
        }
    }
}
