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
import com.twilio.sdk.resource.factory.IncomingPhoneNumberFactory;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;

/*
 * Class mediator for purchasing a phone numbers.
 * For more information, see
 * http://www.twilio.com/docs/api/rest/incoming-phone-numbers#list-post
 */
public class PurchasePhoneNumber extends AbstractConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: purchase phone number");
		Map<String, String> params = getParamList(messageContext);

		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);
			IncomingPhoneNumberFactory numberFactory =
			                                           twilioRestClient.getAccount()
			                                                           .getIncomingPhoneNumberFactory();
			IncomingPhoneNumber number = numberFactory.create(params);
			
			OMElement omResponse = TwilioUtil.parseResponse("phonenumber.purchase.success");
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_INCOMING_PHONE_SID, number.getSid());
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_FRIENDLY_NAME, number.getFriendlyName());
			TwilioUtil.preparePayload(messageContext, omResponse);			
		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0005", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: purchase phone number");
	}

	private Map<String, String> getParamList(MessageContext messageContext) {
		String phoneNumber =
		                     (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                 TwilioUtil.PARAM_PHONENUMBER);
		String areaCode =
		                  (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                              TwilioUtil.PARAM_AREACODE);
		String friendlyName =
		                      (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  TwilioUtil.PARAM_FRIENDLY_NAME);
		String voiceUrl =
		                  (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                              TwilioUtil.PARAM_VOICEURL);
		String voiceMethod =
		                     (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                 TwilioUtil.PARAM_VOICEMETHOD);
		String voiceFallbackUrl =
		                          (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                      TwilioUtil.PARAM_VOICEFALLBACKURL);
		String voiceFallbackMethod =
		                             (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                         TwilioUtil.PARAM_VOICEFALLBACKMETHOD);
		String statusCallback =
		                        (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                    TwilioUtil.PARAM_STATUS_CALLBACK);
		String statusCallbackMethod =
		                              (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                          TwilioUtil.PARAM_STATUS_CALLBACK_METHOD);
		String voiceCallerIdLookup =
		                             (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                         TwilioUtil.PARAM_VOICE_CALLERID_LOOKUP);
		String voiceApplicationSid =
		                             (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                         TwilioUtil.PARAM_VOICE_APPLICATION_SID);
		String smsUrl =
		                (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                            TwilioUtil.PARAM_SMS_URL);
		String smsMethod =
		                   (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                               TwilioUtil.PARAM_SMS_METHOD);
		String smsFallbackUrl =
		                        (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                    TwilioUtil.PARAM_SMS_FALLBACKURL);
		String smsFallbackMethod =
		                           (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                       TwilioUtil.PARAM_SMS_FALLBACKMETHOD);
		String smsApplicationSid =
		                           (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                       TwilioUtil.PARAM_SMS_APPLICATION_SID);
		String apiVersion =
		                    (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                TwilioUtil.API_VERSION);

		Map<String, String> params = new HashMap<String, String>();
		if (phoneNumber != null) {
			params.put(TwilioUtil.TWILIO_PHONENUMBER, phoneNumber);
		}
		if (areaCode != null) {
			params.put(TwilioUtil.TWILIO_AREACODE, areaCode);
		}
		if (friendlyName != null) {
			params.put(TwilioUtil.TWILIO_FRIENDLY_NAME, friendlyName);
		}
		if (voiceUrl != null) {
			params.put(TwilioUtil.TWILIO_VOICE_URL, voiceUrl);
		}
		if (voiceMethod != null) {
			params.put(TwilioUtil.TWILIO_VOICE_METHOD, voiceMethod);
		}
		if (voiceFallbackUrl != null) {
			params.put(TwilioUtil.TWILIO_VOICE_FALLBACKURL, voiceFallbackUrl);
		}
		if (voiceFallbackMethod != null) {
			params.put(TwilioUtil.TWILIO_VOICE_FALLBACKMETHOD, voiceFallbackMethod);
		}
		if (statusCallback != null) {
			params.put(TwilioUtil.TWILIO_STATUS_CALLBACK, statusCallback);
		}
		if (statusCallbackMethod != null) {
			params.put(TwilioUtil.TWILIO_STATUS_CALLBACKMETHOD, statusCallbackMethod);
		}
		if (voiceCallerIdLookup != null) {
			params.put(TwilioUtil.TWILIO_VOICE_CALLERID_LOOKUP, voiceCallerIdLookup);
		}
		if (voiceApplicationSid != null) {
			params.put(TwilioUtil.TWILIO_VOICE_APPLICATION_SID, voiceApplicationSid);
		}
		if (smsUrl != null) {
			params.put(TwilioUtil.TWILIO_SMS_URL, smsUrl);
		}
		if (smsMethod != null) {
			params.put(TwilioUtil.TWILIO_SMS_METHOD, smsMethod);
		}
		if (smsFallbackUrl != null) {
			params.put(TwilioUtil.TWILIO_SMS_FALLBACKURL, smsFallbackUrl);
		}
		if (smsFallbackMethod != null) {
			params.put(TwilioUtil.TWILIO_SMS_FALLBACKMETHOD, smsFallbackMethod);
		}
		if (smsApplicationSid != null) {
			params.put(TwilioUtil.TWILIO_SMS_APPLICATION_SID, smsApplicationSid);
		}
		if (apiVersion != null) {
			params.put(TwilioUtil.TWILIO_API_VERSION, apiVersion);
		}
		return params;
	}
}
