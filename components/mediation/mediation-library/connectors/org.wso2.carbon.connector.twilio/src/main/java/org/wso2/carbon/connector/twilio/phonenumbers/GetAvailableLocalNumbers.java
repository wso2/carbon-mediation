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
public class GetAvailableLocalNumbers extends AbstractConnector {

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
		log.auditLog("Start: get available local numbers");
		Map<String, String> params = getParameter(messageContext);
		String country =
		                 (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                             TwilioUtil.PARAM_COUNTRY);
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
			                                                               TwilioUtil.API_LOCAL,
			                                                       "GET", params);
			OMElement omResponse = TwilioUtil.parseResponse(response);
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e);
			TwilioUtil.handleException(e, "0005", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: get available local numbers");
	}

	private Map<String, String> getParameter(MessageContext messageContext) {
		String areaCode =
		                  (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                              TwilioUtil.PARAM_AREACODE);
		String contains =
		                  (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                              TwilioUtil.PARAM_CONTAINS);
		String region =
		                (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                            TwilioUtil.PARAM_IN_REGION);
		String postalCode =
		                    (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                TwilioUtil.PARAM_IN_POSTAL_CODE);
		String nearLat =
		                 (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                             TwilioUtil.PARAM_NEAR_LAT_LONG);
		String nearNumber =
		                    (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                TwilioUtil.PARAM_NEAR_NUMBER);
		String inLata =
		                (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                            TwilioUtil.PARAM_IN_LATA);
		String inRateCenter =
		                      (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  TwilioUtil.PARAM_IN_RATE_CENTER);
		String distance =
		                  (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                              TwilioUtil.PARAM_DISTANCE);

		Map<String, String> params = new HashMap<String, String>();
		if (areaCode != null) {
			params.put(TwilioUtil.TWILIO_AREACODE, areaCode);
		}
		if (contains != null) {
			params.put(TwilioUtil.TWILIO_CONTAINS, contains);
		}
		if (region != null) {
			params.put(TwilioUtil.TWILIO_IN_REGION, region);
		}
		if (postalCode != null) {
			params.put(TwilioUtil.TWILIO_IN_POSTAL_CODE, postalCode);
		}
		if (nearLat != null) {
			params.put(TwilioUtil.TWILIO_NEAR_LAT_LONG, nearLat);
		}
		if (nearNumber != null) {
			params.put(TwilioUtil.TWILIO_NEAR_NUMBER, nearNumber);
		}
		if (inLata != null) {
			params.put(TwilioUtil.TWILIO_IN_LATA, inLata);
		}
		if (inRateCenter != null) {
			params.put(TwilioUtil.TWILIO_IN_RATE_CENTER, inRateCenter);
		}
		if (distance != null) {
			params.put(TwilioUtil.TWILIO_DISTANCE, distance);
		}
		return params;
	}
}
