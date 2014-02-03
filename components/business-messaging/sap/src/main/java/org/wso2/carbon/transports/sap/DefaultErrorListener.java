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

import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContextInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code> DefaultErrorListener </code> provides a default implementation of the error listner
 */
public class DefaultErrorListener implements JCoServerErrorListener, JCoServerExceptionListener {

    private static final Log log = LogFactory.getLog(DefaultErrorListener.class);

    public void serverErrorOccurred(JCoServer server, String connectionId,
                                    JCoServerContextInfo ctx, Error error) {
        log.error("Error occured on : " + server.getProgramID() + " and connection : " +
                connectionId, error);
    }

    public void serverExceptionOccurred(JCoServer server, String connectionId,
                                        JCoServerContextInfo ctx, Exception error) {
        log.error("Exception occured on : " + server.getProgramID() + " and connection : " +
                connectionId, error);
    }
}
