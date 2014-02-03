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
 * Class mediator for getting a an SMS resource given its Sid.
 * For more information, see http://www.twilio.com/docs/api/rest/sms
 */
public class GetSmsList extends AbstractConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: get SMS Status List");

		String to =
		            (String) ConnectorUtils.lookupTemplateParamater(messageContext, TwilioUtil.PARAM_TO);
		String from =
		              (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                          TwilioUtil.PARAM_FROM);
		String dateSent =
		                  (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                              TwilioUtil.PARAM_DATESENT);

		try {
			// the map used for passing parameters
			Map<String, String> params = new HashMap<String, String>();

			if (to != null) {
				params.put(TwilioUtil.TWILIO_TO, to);
			}

			if (from != null) {
				params.put(TwilioUtil.TWILIO_FROM, from);
			}

			if (dateSent != null) {
				// YYYY-MM-DD
				params.put(TwilioUtil.TWILIO_DATESENT, dateSent);
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
			                                                               TwilioUtil.API_SMS_MESSAGES,
			                                                       "GET", params);

			OMElement omResponse = TwilioUtil.parseResponse(response);
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0007", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: get SMS Status List");
	}
}
