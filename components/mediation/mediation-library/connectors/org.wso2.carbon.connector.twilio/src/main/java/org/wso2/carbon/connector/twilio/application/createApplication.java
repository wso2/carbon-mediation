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
package org.wso2.carbon.connector.twilio.application;

import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.twilio.util.TwilioUtil;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.factory.ApplicationFactory;
import com.twilio.sdk.resource.instance.Application;

/*
 * Class mediator for updating an application instance with optional parameters
 * For more information, see http://www.twilio.com/docs/api/rest/applications
 */
public class createApplication extends AbstractConnector {

	public void connect(MessageContext messageContext) {
		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: create application");
		Map<String, String> params = createParameterMap(messageContext);

		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);
			// creating a new application under the specified AccountSID
			ApplicationFactory appFactory = twilioRestClient.getAccount().getApplicationFactory();
			Application application = appFactory.create(params);
			OMElement omResponse = TwilioUtil.parseResponse("application.create.success");
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_APPLICATION_SID,
			                      application.getSid());
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0002", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: create application");
	}

	/**
	 * Create a map containing the parameters required to create the
	 * application, which has been defined
	 * 
	 * @return The map containing the defined parameters
	 */
	private Map<String, String> createParameterMap(MessageContext messageContext) {
		// Required
		String friendlyName =
		                      (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  TwilioUtil.PARAM_FRIENDLY_NAME);
		// optional parameters for creating the application retrieved by the
		// Sid. See
		// http://www.twilio.com/docs/api/rest/applications#list-post-optional-parameters
		String apiVersion =
		                    (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                TwilioUtil.PARAM_API_VERSION);
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
		String smsUrl =
		                (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                            TwilioUtil.PARAM_SMS_URL);
		String smsMethod =
		                   (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                               TwilioUtil.PARAM_SMS_METHOD);
		String smsFallbackUrl =
		                        (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                    TwilioUtil.PARAM_SMS_FALLBACK_URL);
		String smsFallbackMethod =
		                           (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                       TwilioUtil.PARAM_SMS_FALLBACKMETHOD);
		String smsStatusCallback =
		                           (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                       TwilioUtil.PARAM_SMS_STATUS_CALLBACK);

		// creating the map for optional parameters
		Map<String, String> params = new HashMap<String, String>();
		params.put(TwilioUtil.TWILIO_FRIENDLY_NAME, friendlyName);

		// null-checking and addition to map
		if (apiVersion != null) {
			params.put(TwilioUtil.TWILIO_API_VERSION, apiVersion);
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
		if (smsStatusCallback != null) {
			params.put(TwilioUtil.TWILIO_SMS_STATUS_CALLBACK, smsStatusCallback);
		}
		return params;
	}
}
