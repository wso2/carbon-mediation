/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.connector.googlespreadsheet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

public class GoogleSpreadsheetLoginUser extends AbstractConnector {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    private static Log log = LogFactory.getLog(GoogleSpreadsheetLoginUser.class);

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        try {
        	org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        	axis2MsgCtx.getOperationContext().removeProperty(GoogleSpreadsheetConstants.GOOGLE_AUTH_INSTANCE);
            String username = GoogleSpreadsheetUtils.lookupFunctionParam(messageContext, USERNAME);
            String password = GoogleSpreadsheetUtils.lookupFunctionParam(messageContext, PASSWORD);
            if (username == null || "".equals(username.trim())
					|| password == null
					|| "".equals(password.trim())) {
				log.error("Please make sure you have given a valid input for the username and password");
                ConnectException connectException = new ConnectException("Please make sure you have given a valid input for the username and password");
                GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, connectException);
                return;
			}

            GoogleSpreadsheetUtils.storeLoginUsernamePassword(messageContext, username, password);
            
        } catch (Exception e) {
            log.error("Failed to login user: " + e.getMessage(), e);
            GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, e);
        }
    }
}
