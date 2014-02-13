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

import com.sap.conn.jco.server.JCoServerTIDHandler;
import com.sap.conn.jco.server.JCoServerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>DefaultTIDHandler </code> provides a defaul implementation of the SAP tx handling
 * mechanishm
 */
public class DefaultTIDHandler implements JCoServerTIDHandler {

    private static final Log log = LogFactory.getLog(DefaultTIDHandler.class);

    public boolean checkTID(JCoServerContext jCoServerContext, String s) {
        log.info("Checking TID: " + s);
        return true;
    }

    public void commit(JCoServerContext jCoServerContext, String s) {
        log.info("Committing TID: " + s);
    }

    public void confirmTID(JCoServerContext jCoServerContext, String s) {
        log.info("Confirming TID: " + s);
    }

    public void rollback(JCoServerContext jCoServerContext, String s) {
        log.info("Rolling back TID: " + s);
    }
}
