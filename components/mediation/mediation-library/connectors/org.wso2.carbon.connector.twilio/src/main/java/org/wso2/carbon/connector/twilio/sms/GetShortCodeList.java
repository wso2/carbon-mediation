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
package org.wso2.carbon.connector.twilio.sms;

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
 * Class mediator for getting the short code based on the Sid.
 * For more information, see http://www.twilio.com/docs/api/rest/short-codes
 */
public class GetShortCodeList extends AbstractConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: get Short Code List");

		String friendlyName =
		                      (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  TwilioUtil.PARAM_FRIENDLY_NAME);
		String shortCodeSid =
		                      (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  TwilioUtil.PARAM_SHORTCODE);

		try {
			// Creates a Map containing the parameters which are needed to be
			// get list
			Map<String, String> params = new HashMap<String, String>();
			if (shortCodeSid != null) {
				params.put(TwilioUtil.TWILIO_SHORT_CODE, shortCodeSid);
			}
			if (friendlyName != null) {
				params.put(TwilioUtil.TWILIO_FRIENDLY_NAME, friendlyName);
			}

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
			                                                               TwilioUtil.API_SMS_SHORTCODES,
			                                                       "GET", params);

			OMElement omResponse = TwilioUtil.parseResponse(response);

			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0007", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: get Short Code List");
	}

}
