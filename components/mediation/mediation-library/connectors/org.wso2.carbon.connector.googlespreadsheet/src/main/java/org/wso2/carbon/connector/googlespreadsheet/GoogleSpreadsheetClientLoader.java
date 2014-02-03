/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector.googlespreadsheet;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.ServiceException;

public class GoogleSpreadsheetClientLoader {

	private MessageContext messageContext;

	public GoogleSpreadsheetClientLoader(MessageContext ctxt) {
		this.messageContext = ctxt;
	}

	public SpreadsheetService loadSpreadsheetService() throws ServiceException {
		org.apache.axis2.context.MessageContext axis2MsgCtx =
		                                                      ((Axis2MessageContext) messageContext).getAxis2MessageContext();
		SpreadsheetService spreadsheetService = null;

		if (axis2MsgCtx.getOperationContext()
		               .getProperty(GoogleSpreadsheetConstants.GOOGLE_AUTH_INSTANCE) != null) {
			spreadsheetService =
			                     (SpreadsheetService) axis2MsgCtx.getOperationContext()
			                                                     .getProperty(GoogleSpreadsheetConstants.GOOGLE_AUTH_INSTANCE);
			return spreadsheetService;
		}

		if (messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_USERNAME) != null &&
		    messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_PASSWORD) != null) {
			GoogleSpreadsheetService gssService = new GoogleSpreadsheetService();
			spreadsheetService = gssService.getSpreadsheetService();
			GoogleSpreadsheetAuthentication gssAuthentication =
			                                                    new GoogleSpreadsheetAuthentication();
			gssAuthentication.login(messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_USERNAME)
			                                      .toString(),
			                        messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_PASSWORD)
			                                      .toString(), spreadsheetService);
		} else if (messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_CONSUMER_KEY) != null &&
		           messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_CONSUMER_SECRET) != null &&
		           messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_ACCESS_TOKEN) != null &&
		           messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_ACCESS_TOKEN_SECRET) != null) {
			GoogleSpreadsheetService gssService = new GoogleSpreadsheetService();
			spreadsheetService = gssService.getSpreadsheetService();
			GoogleSpreadsheetAuthentication gssAuthentication =
			                                                    new GoogleSpreadsheetAuthentication();
			gssAuthentication.loginOAuth2(messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_CONSUMER_KEY)
			                                            .toString(),
			                              messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_CONSUMER_SECRET)
			                                            .toString(),
			                              messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_ACCESS_TOKEN)
			                                            .toString(),
			                              messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_ACCESS_TOKEN_SECRET)
			                                            .toString(), spreadsheetService);
		}
		axis2MsgCtx.getOperationContext()
		           .setProperty(GoogleSpreadsheetConstants.GOOGLE_AUTH_INSTANCE, spreadsheetService);
		return spreadsheetService;
	}

}