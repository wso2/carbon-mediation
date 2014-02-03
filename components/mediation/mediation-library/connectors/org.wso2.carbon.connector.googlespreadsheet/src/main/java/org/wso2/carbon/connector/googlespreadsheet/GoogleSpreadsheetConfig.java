package org.wso2.carbon.connector.googlespreadsheet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;


/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class GoogleSpreadsheetConfig extends AbstractConnector {

	private static Log log = LogFactory.getLog(GoogleSpreadsheetConfig.class);

	public static final String CONSUMER_KEY = "oauthConsumerKey";
	public static final String CONSUMER_SECRET = "oauthConsumerSecret";
	public static final String ACCESS_TOKEN = "oauthAccessToken";
	public static final String ACCESS_TOKEN_SECRET = "oauthAccessTokenSecret";

	boolean USE_RSA_SIGNING = false;

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {
		try {
			String consumerKey = GoogleSpreadsheetUtils.lookupFunctionParam(
					messageContext, CONSUMER_KEY);
			String consumerSecret = GoogleSpreadsheetUtils.lookupFunctionParam(
					messageContext, CONSUMER_SECRET);
			String accessToken = GoogleSpreadsheetUtils.lookupFunctionParam(
					messageContext, ACCESS_TOKEN);
			String accessTokenSecret = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, ACCESS_TOKEN_SECRET);

			GoogleSpreadsheetUtils.storeLoginUser(messageContext, consumerKey,
					consumerSecret, accessToken, accessTokenSecret);

		} catch (Exception e) {
			log.error("Failed to login user: " + e.getMessage(), e);
			GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, e);
		}
	}	

}
