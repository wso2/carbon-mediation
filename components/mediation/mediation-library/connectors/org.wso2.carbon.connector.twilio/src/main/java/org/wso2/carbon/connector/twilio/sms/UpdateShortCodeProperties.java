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
import com.twilio.sdk.resource.instance.ShortCode;

/*
 * Class mediator for updating the properties of a short code.
 * For more information, see http://www.twilio.com/docs/api/rest/short-codes
 */
public class UpdateShortCodeProperties extends AbstractConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: update short code properties");

		String shortCodeSid =
		                      (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  TwilioUtil.PARAM_SHORTCODE_SID);
		String friendlyName =
		                      (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  TwilioUtil.PARAM_FRIENDLY_NAME);
		String apiVersion =
		                    (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                TwilioUtil.PARAM_API_VERSION);
		String smsUrl =
		                (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                            TwilioUtil.PARAM_SMS_URL);
		String smsMethod =
		                   (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                               TwilioUtil.PARAM_SMS_METHOD);
		String smsFallBackMethod =
		                           (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                       TwilioUtil.PARAM_SMS_FALLBACKMETHOD);
		String smsFallBackUrl =
		                        (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                    TwilioUtil.PARAM_SMS_FALLBACKURL);

		// optional parameters passed through this map
		Map<String, String> params = new HashMap<String, String>();

		if (friendlyName != null) {
			params.put(TwilioUtil.TWILIO_FRIENDLY_NAME, friendlyName);
		}
		if (apiVersion != null) {
			params.put(TwilioUtil.TWILIO_API_VERSION, apiVersion);
		}
		if (smsUrl != null) {
			params.put(TwilioUtil.TWILIO_SMS_URL, smsUrl);
		}
		if (smsMethod != null) {
			params.put(TwilioUtil.TWILIO_SMS_METHOD, smsMethod);
		}
		if (smsFallBackUrl != null) {
			params.put(TwilioUtil.TWILIO_SMS_FALLBACKURL, smsFallBackUrl);
		}
		if (smsFallBackMethod != null) {
			params.put(TwilioUtil.TWILIO_SMS_FALLBACKMETHOD, smsFallBackMethod);
		}

		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);
			ShortCode shortCode = twilioRestClient.getAccount().getShortCode(shortCodeSid);
			shortCode.update(params);

			OMElement omResponse = TwilioUtil.parseResponse("shortcode.update.success");
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_SHORTCODE_SID, shortCode.getSid());
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0007", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: update short code properties");
	}
}
