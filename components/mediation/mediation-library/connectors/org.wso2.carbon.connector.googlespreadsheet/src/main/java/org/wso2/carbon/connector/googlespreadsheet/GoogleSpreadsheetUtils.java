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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

public class GoogleSpreadsheetUtils {
	 public static String lookupFunctionParam(MessageContext ctxt, String paramName) {
	        return (String)ConnectorUtils.lookupTemplateParamater(ctxt, paramName);
	    }

	    public static void storeLoginUser(MessageContext ctxt, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret){
	        ctxt.setProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_CONSUMER_KEY, consumerKey);
	        ctxt.setProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_CONSUMER_SECRET, consumerSecret);
	        ctxt.setProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_ACCESS_TOKEN, accessToken);
	        ctxt.setProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_ACCESS_TOKEN_SECRET, accessTokenSecret);
	    }
	    
	    public static void storeLoginUsernamePassword(MessageContext ctxt, String userName, String password){
	        ctxt.setProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_USERNAME, userName);
	        ctxt.setProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_PASSWORD, password);
	    }

	    public static void storeErrorResponseStatus(MessageContext ctxt, Exception e) {
	        ctxt.setProperty(SynapseConstants.ERROR_EXCEPTION, e);
	        ctxt.setProperty(SynapseConstants.ERROR_MESSAGE, e.getMessage());

            if(ctxt.getEnvelope().getBody().getFirstElement() != null) {
                ctxt.getEnvelope().getBody().getFirstElement().detach();
            }

            OMFactory factory   = OMAbstractFactory.getOMFactory();
            OMNamespace ns      = factory.createOMNamespace("http://org.wso2.esbconnectors.googlespreadsheet", "ns");
            OMElement searchResult  = factory.createOMElement("ErrorResponse", ns);
            OMElement errorMessage      = factory.createOMElement("ErrorMessage", ns);
            searchResult.addChild(errorMessage);
            errorMessage.setText(e.getMessage());
            ctxt.getEnvelope().getBody().addChild(searchResult);
	    }

}
