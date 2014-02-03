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
package org.wso2.carbon.connector.twilio.phonenumbers;

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
 * Class mediator for getting available local numbers in an account.
 * For more information,
 * http://www.twilio.com/docs/api/rest/available-phone-numbers
 */
public class GetAvailableTollFreeNumbers extends AbstractConnector {

	// Basic filter parameters
	// See
	// http://www.twilio.com/docs/api/rest/available-phone-numbers#local-get-basic-filters

	// Advance filter parameters (only for numbers in the Unites States and
	// Canada).
	// See
	// https://www.twilio.com/docs/api/rest/available-phone-numbers#local-get-advanced-filters

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {
		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: get available toll free numbers");

		String country =
		                 (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                             TwilioUtil.PARAM_COUNTRY);
		String areaCode =
		                  (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                              TwilioUtil.PARAM_AREACODE);
		String contains =
		                  (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                              TwilioUtil.PARAM_CONTAINS);

		Map<String, String> params = new HashMap<String, String>();
		if (areaCode != null) {
			params.put(TwilioUtil.TWILIO_AREACODE, areaCode);
		}
		if (contains != null) {
			params.put(TwilioUtil.TWILIO_CONTAINS, contains);
		}

		try {

			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);
			TwilioRestResponse response =
			                              twilioRestClient.request(TwilioUtil.API_URL +
			                                                               "/" +
			                                                               TwilioUtil.API_VERSION +
			                                                               "/" +
			                                                               TwilioUtil.API_ACCOUNTS +
			                                                               "/" +
			                                                               twilioRestClient.getAccountSid() +
			                                                               "/" +
			                                                               TwilioUtil.API_AVAILABLE_PHONE_NUMBERS +
			                                                               "/" + country + "/" +
			                                                               TwilioUtil.API_TOLLFREE,
			                                                       "GET", params);
			OMElement omResponse = TwilioUtil.parseResponse(response);
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e);
			TwilioUtil.handleException(e, "0005", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: get available  toll free numbers");
	}
}
