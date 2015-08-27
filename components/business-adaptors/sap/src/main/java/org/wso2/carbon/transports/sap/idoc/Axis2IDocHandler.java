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

package org.wso2.carbon.transports.sap.idoc;

import com.sap.conn.idoc.jco.JCoIDocHandler;
import com.sap.conn.idoc.jco.JCoIDoc;
import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.IDocXMLProcessor;
import com.sap.conn.jco.server.JCoServerContext;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transports.sap.idoc.IDocEndpoint;
import org.wso2.carbon.transports.sap.SAPConstants;
import org.wso2.carbon.transports.sap.idoc.util.IDoCAdapterUtils;

import java.io.*;

/**
 * This class handles IDOCs returned from the SAP gateway.
 * <p>
 * This class encapsulates workers for each idoc request recieved. Workers are responsible for
 * handling IDOCs and converting them to SOAP format
 * </p>
 */
public class Axis2IDocHandler implements JCoIDocHandler {

    private static final Log log = LogFactory.getLog(Axis2IDocHandler.class);

    private WorkerPool workerPool;
    private IDocEndpoint endpoint;
    private IDocXMLProcessor xmlProcessor;

    public Axis2IDocHandler(WorkerPool workerPool, IDocEndpoint endpoint) {
        this.workerPool = workerPool;
        this.endpoint = endpoint;
        this.xmlProcessor = JCoIDoc.getIDocFactory().getIDocXMLProcessor();
    }

    /**
     * Handling the incoming requests
     * @param jCoServerContext
     *              server environemnt context
     * @param iDocDocumentList
     *              IDOCs returned by the SAP gateway
     */
    public void handleRequest(JCoServerContext jCoServerContext,
                              IDocDocumentList iDocDocumentList) {
        if (log.isDebugEnabled()) {
            log.debug("New IDoc received");
        }
        workerPool.execute(new IDocWorker(jCoServerContext, iDocDocumentList));
    }

    private class IDocWorker implements Runnable {

        private JCoServerContext serverContext;
        private IDocDocumentList docList;

        private IDocWorker(JCoServerContext serverContext, IDocDocumentList docList) {
            this.serverContext = serverContext;
            this.docList = docList;
        }

        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Starting a new IDoc worker thread to process the incoming request");
            }

            ByteArrayInputStream bais = null;
            ByteArrayOutputStream baos = null;
            OutputStreamWriter osw = null;

            try {
                baos = new ByteArrayOutputStream();
                osw = new OutputStreamWriter(baos, "UTF8");
    			//convert recieved idocs to an output format
                xmlProcessor.render(docList, osw);
    			osw.flush();
                String output = baos.toString();
                if (output.contains("<?xml version=\"1.1\"?>")) {
                    output = output.replaceFirst("1.1", "1.0");
                }

                MessageContext msgContext = endpoint.createMessageContext();
                msgContext.setIncomingTransportName(SAPConstants.SAP_IDOC_PROTOCOL_NAME);

                if (log.isDebugEnabled()) {
                    log.debug("Creating SOAP envelope from the IDoc");
                }

                bais = new ByteArrayInputStream(output.getBytes());
                if (log.isDebugEnabled()) {
                    //just print the recieved idocs to System
                    StringBuffer buffer = new StringBuffer("Received IDoc content: ");
                    ByteArrayInputStream loggingStream = new ByteArrayInputStream(baos.toByteArray());
                    int len;
                    byte[] data = new byte[1024];
                    while ((len = loggingStream.read(data)) != -1) {
                        buffer.append(new String(data, 0, len));
                    }
                    log.debug(buffer.toString());
                }
                //build SOAP enevelope encapsulating the IDOCs in xml format
                SOAPEnvelope envelope = TransportUtils.createSOAPMessage(msgContext, bais,
                        SAPConstants.SAP_CONTENT_TYPE);
                msgContext.setEnvelope(envelope);
                String stampArcKey = IDoCAdapterUtils.getProperty("stamp_r_arc_key");
                if (stampArcKey == null ||!stampArcKey.equals("false")) {
                    // stamp the ARCKEY for incoming messages as well so  that there is a way to
                    // correlate IDocs at the adapter level if required. Note that most of the R/*
                    // systems by default won't add an ARCKEY for the document that they post
                    // because of the performance facts. Adapter will only stamps an ARCKEY if
                    // it's missing from the control segment.
                    IDoCAdapterUtils.stampArcKey(envelope.getBody().getFirstElement(),
                            msgContext.getMessageID());
                }

                //pass the constructed IDOC message through Axis engine
                AxisEngine.receive(msgContext);

            } catch (Exception e) {
                log.error("Error while processing the IDoc through the Axis engine", e);
            } finally {
                closeStream(osw);
                closeStream(baos);
                closeStream(bais);
            }
        }

        private void closeStream(Closeable obj) {
            try {
                obj.close();
            } catch (IOException e) {
                log.error("Error while closing the stream", e);
            }
        }
    }
}
