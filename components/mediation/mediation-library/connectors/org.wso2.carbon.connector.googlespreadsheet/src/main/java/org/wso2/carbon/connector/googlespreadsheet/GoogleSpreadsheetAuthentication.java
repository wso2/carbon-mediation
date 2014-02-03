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

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.AuthenticationException;

/**
 * 
 * 
 * 
 * Class implemented for accessing google spreadsheet application programming
 * interface (API) features related to authentication
 * 
 */

public class GoogleSpreadsheetAuthentication {
	private static Log log = LogFactory
			.getLog(GoogleSpreadsheetAuthentication.class);

	private boolean USE_RSA_SIGNING = false;

	/**
	 * Log in to Google, under the Google Spreadsheets account.
	 * 
	 * @param username
	 *            name of user to authenticate (e.g. yourname@gmail.com)
	 * @param password
	 *            password to use for authentication
	 * @throws AuthenticationException
	 *             if the service is unable to validate the username and
	 *             password.
	 */
	public void login(String username, String password,
			SpreadsheetService service) throws AuthenticationException {
		service.setUserCredentials(username, password);
	}

	/**
	 * Getting access to the google account with OAuth2
	 * 
	 * @param consumerKey
	 *            consumerKey for the application which is accessing the google
	 *            account
	 * @param consumerSecret
	 *            consumerSecret for the application which is accessing the
	 *            google account
	 * @param accessToken
	 *            accessToken received from the google spreadsheet service to
	 *            accessing the google account
	 * @param accessTokenSecret
	 *            accessTokenSecret received from the google spreadsheet service
	 *            to accessing the google account
	 * @throws AuthenticationException
	 *             if the service is unable to validate the username and
	 *             password.
	 */
	public void loginOAuth2(String consumerKey, String consumerSecret,
			String accessToken, String accessTokenSecret,
			SpreadsheetService service) throws AuthenticationException {

		try {
			// Space separated list of scopes for which to request access.
			String SCOPES = "https://docs.google.com/feeds https://spreadsheets.google.com/feeds";

			GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();

			oauthParameters.setOAuthConsumerKey(consumerKey);

			OAuthSigner signer;
			if (USE_RSA_SIGNING) {

				signer = new OAuthRsaSha1Signer(consumerSecret);

			} else {
				oauthParameters.setOAuthConsumerSecret(consumerSecret);
				signer = new OAuthHmacSha1Signer();
			}

			oauthParameters.setScope(SCOPES);
			oauthParameters.setOAuthToken(accessToken);
			oauthParameters.setOAuthTokenSecret(accessTokenSecret);

			service.setOAuthCredentials(oauthParameters, signer);
		} catch (OAuthException e) {
			log.error("Authentication failed with OAuth2" + e.getMessage(), e);
		}

	}

}
