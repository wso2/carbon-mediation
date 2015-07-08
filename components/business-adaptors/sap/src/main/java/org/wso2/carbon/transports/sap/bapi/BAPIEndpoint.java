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

import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.transports.sap.SAPEndpoint;
import com.sap.conn.jco.server.*;

/**
 * BAPIEndpoint class
 * <p>
 * This class represents a AXIS2 endpoint for Bapi Function calls.Handles all incoming bapi/rfc calls
 * through registered handlers
 * </p>
 * @see com.sap.conn.jco.server.JCoServerFunctionHandler
 */
public class BAPIEndpoint extends SAPEndpoint {

    private JCoServer server;

    public void startEndpoint(WorkerPool workerPool) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Starting the JCo endpoint : " + serverName);
        }

        try {
            server = JCoServerFactory.getServer(serverName);
            DefaultServerHandlerFactory.FunctionHandlerFactory factory =
                    new DefaultServerHandlerFactory.FunctionHandlerFactory();
            // register message handlers for bapirfc calls
            factory.registerGenericHandler(new Axis2RFCHandler(this, workerPool));
            // register the server hanlder factory
            server.setCallHandlerFactory(factory);
            setupOptionalFeatures(server);
            server.start();
            log.info("JCo server started with server name : " + serverName + " and " +
                    "program ID : " + server.getProgramID());
        } catch (Exception e) {
            handleException("Error while initializing the SAP JCo server", e);   
        }
    }

    public void stopEndpoint() {
        if (log.isDebugEnabled()) {
            log.debug("Stopping the JCo endpoint : " + serverName);
        }
        server.stop();
        server.release();

        if (!waitForServerStop(server)) {
            log.warn("JCo server : " + serverName + " is taking an unusually long time to stop.");
        } else {
            log.info("JCo server : " + serverName + " stopped");
        }
    }
}
