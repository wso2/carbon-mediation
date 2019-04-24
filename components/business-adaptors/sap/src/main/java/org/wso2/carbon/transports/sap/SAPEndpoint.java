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

package org.wso2.carbon.transports.sap;

import org.apache.axis2.transport.base.ProtocolEndpoint;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import com.sap.conn.jco.server.JCoServerTIDHandler;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerState;

import java.util.concurrent.TimeUnit;

/**
 * <code>SAPEndpoint </code> describes a SAP addresible endpoint, the properties decscribe by the
 * endpoint are SAP server specifi. The full list of properties can be found at 
 * http://help.sap.com/saphelp_xmii115/helpdata/en/System_Management/iDoc_Configuration_Editor.htm
 */
public abstract class SAPEndpoint extends ProtocolEndpoint {

    protected static Log log;

    /**
     * The sap server name that the endpoint will be connecting to
     */
    protected String serverName;

    /**
     * Is the error handler for this endpoint is enabled ?
     */
    protected boolean errorListenerEnabled;

    /**
     * Is this a tx session ?
     */
    protected boolean tidHandlerEnabled;

    /**
     * The no. of concurrent connections to R/* system 
     */
    protected int connections;

    /**
     * Custom error handler
     */
    protected String customErrorListener;

    /**
     * Custom exception handler
     */
    protected String customExceptionListener;

    /**
     * Custom tx handler
     */
    protected String customTIDHandler;

    /**
     * Max timeout to allow server to stop
     */
    protected static int serverStopTimeout = 30000;

    public SAPEndpoint() {
        log = LogFactory.getLog(getClass());
    }

    public EndpointReference[] getEndpointReferences(AxisService service, String ip) throws AxisFault {
        return null;
    }

    /**
     * Load the SAP configuration
     * @param params the transport level params
     * @return properties loaded sucessfully ?
     * @throws AxisFault throws in case of an error
     */
    public boolean loadConfiguration(ParameterInclude params) throws AxisFault {
        if (!(params instanceof AxisService)) {
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("Loading configuration for the SAP endpoint");
        }

        serverName = ParamUtils.getOptionalParam(params, SAPConstants.SERVER_NAME_PARAM);
        if (serverName == null) {
            return false;
        }

        connections = ParamUtils.getOptionalParamInt(params,
                SAPConstants.CONNECTIONS_PARAM,
                SAPConstants.SAP_SERVER_DEFAULT_CONNECTIONS);
        errorListenerEnabled = SAPConstants.SAP_ENABLED.equals(
                ParamUtils.getOptionalParam(params, SAPConstants.ENABLE_ERROR_LISTENER_PARAM));
        tidHandlerEnabled = SAPConstants.SAP_ENABLED.equals(
                ParamUtils.getOptionalParam(params, SAPConstants.ENABLE_TID_HANDLER_PARAM));
        customErrorListener = ParamUtils.getOptionalParam(params,
                SAPConstants.CUSTOM_ERROR_LISTENER_PARAM);
        customExceptionListener = ParamUtils.getOptionalParam(params,
                SAPConstants.CUSTOM_EXCEPTION_LISTENER_PARAM);
        customTIDHandler = ParamUtils.getOptionalParam(params,
                SAPConstants.CUSTOM_TID_HANDLER_PARAM);
        return true;
    }

    protected void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }


    /**
     * Set up option feature
     * @param server the jco server
     * @throws Exception throws in case of an error
     */
    protected void setupOptionalFeatures(JCoServer server) throws Exception {
        server.setConnectionCount(connections);

        if (errorListenerEnabled) {
            DefaultErrorListener errorListener = new DefaultErrorListener();
            if (customErrorListener == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Engaging the default error listener for : " + serverName);
                }
                server.addServerErrorListener(errorListener);
            }
            if (customExceptionListener == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Engaging the default exception listener for : " + serverName);
                }
                server.addServerExceptionListener(errorListener);
            }
        }

        if (tidHandlerEnabled && customTIDHandler == null) {
            if (log.isDebugEnabled()) {
                log.debug("Engaging the default TID handler for : " + serverName);
            }
            server.setTIDHandler(new DefaultTIDHandler());
        }               

        if (customErrorListener != null) {
            Class clazz = this.getClass().getClassLoader().loadClass(customErrorListener);
            server.addServerErrorListener((JCoServerErrorListener) clazz.newInstance());
        }

        if (customExceptionListener != null) {
            Class clazz = this.getClass().getClassLoader().loadClass(customExceptionListener);
            server.addServerExceptionListener((JCoServerExceptionListener) clazz.newInstance());
        }

        if (customTIDHandler != null) {
            Class clazz = this.getClass().getClassLoader().loadClass(customTIDHandler);
            server.setTIDHandler((JCoServerTIDHandler) clazz.newInstance());
        }
    }

    /**
     * Block until server state is stopped or maximum timeout reached.
     *
     * @param server jco server for which we wait to be stopped.
     * @return true if the server is stopped before the timeout is exceeded
     */
    protected boolean waitForServerStop(JCoServer server) {
        long timeStamp = System.currentTimeMillis();
        while (server.getState() != JCoServerState.STOPPED
               && timeStamp + serverStopTimeout > System.currentTimeMillis()) {
            if (log.isDebugEnabled()) {
                log.debug("Waiting for server to stop...");
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                //do nothing, just continue the loop
            }
        }

        if (server.getState() == JCoServerState.STOPPED) {
            return true;
        } else {
            return false;
        }
    }

    public abstract void startEndpoint(WorkerPool workerPool) throws AxisFault;
    public abstract void stopEndpoint();
}
