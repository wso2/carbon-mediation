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

import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.transports.sap.SAPEndpoint;
import com.sap.conn.idoc.jco.JCoIDocServer;
import com.sap.conn.idoc.jco.JCoIDoc;
/**
 * IDocEndpoint class
 * <p>
 * This class represents a AXIS2 endpoint for IDOC data exchange.Handles all incoming IDOC transfer
 * calls are handled through registered handlers
 * </p>
 * @see com.sap.conn.idoc.jco.JCoIDocHandlerFactory
 */
public class IDocEndpoint extends SAPEndpoint {

    private JCoIDocServer server;

    /**
     * Start the IDoc endpoint
     * @param workerPool the worker thread pool
     * @throws AxisFault throws in case of an error
     */
    public void startEndpoint(WorkerPool workerPool) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Starting the IDoc endpoint : " + serverName);
        }
        try {
            server = JCoIDoc.getServer(serverName);
            server.setIDocHandlerFactory(new Axis2IDocHandlerFactory(this, workerPool));
            setupOptionalFeatures(server);
            server.start();
            log.info("IDoc server started with server name : " + serverName + " and " +
                    "program ID : " + server.getProgramID());
        } catch (Exception e) {
            handleException("Error while initializing the SAP IDoc server", e);
        }
    }

    public void stopEndpoint() {
        if (log.isDebugEnabled()) {
            log.debug("Stopping the IDoc endpoint : " + serverName);
        }

        server.stop();
        server.release();
        log.info("IDoc server : " + serverName + " stopped");
    }
}
