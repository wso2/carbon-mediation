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
package org.wso2.carbon.connector.twilio.account;

import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.twilio.util.TwilioUtil;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestResponse;

/*
 * Class mediator for retrieving a list of all accounts for an accountSID.
 * For more information, see http://www.twilio.com/docs/api/rest/account
 */
public class GetAccountsList extends AbstractConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: get account");

		// Optional parameters. For more information, see
		// http://www.twilio.com/docs/api/rest/account#list-get-filters
		String friendlyName =
		                      (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  TwilioUtil.PARAM_FRIENDLY_NAME);
		String status =
		                (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                            TwilioUtil.PARAM_STATUS);

		// Build a filter for the AccountList, i.e. filter parameters are passed
		// as a Map
		Map<String, String> filter = new HashMap<String, String>();

		if (friendlyName != null) {
			filter.put(TwilioUtil.TWILIO_FRIENDLY_NAME, friendlyName);
		}
		if (status != null) {
			filter.put(TwilioUtil.TWILIO_STATUS, status);
		}

		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);

			// Get the AccountList
			TwilioRestResponse response =
			                              twilioRestClient.request(TwilioUtil.API_URL + "/" +
			                                                               TwilioUtil.API_VERSION +
			                                                               "/" +
			                                                               TwilioUtil.API_ACCOUNTS,
			                                                       "GET",
			                                                       filter);
			OMElement omResponse = TwilioUtil.parseResponse(response);

			TwilioUtil.preparePayload(messageContext, omResponse);

		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0001", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: get accounts");
	}
}
