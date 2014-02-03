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
import com.twilio.sdk.resource.factory.OutgoingCallerIdFactory;
import com.twilio.sdk.resource.instance.CallerIdValidation;

/*
 * Class mediator for purchasing a phone numbers.
 * For more information, see
 * http://www.twilio.com/docs/api/rest/incoming-phone-numbers#list-post
 */
public class AddOutgoingPhoneNumber extends AbstractConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: add outgoing phone number");
		Map<String, String> params = getParameter(messageContext);

		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);
			OutgoingCallerIdFactory numberFactory =
			                                        twilioRestClient.getAccount()
			                                                        .getOutgoingCallerIdFactory();
			CallerIdValidation number = numberFactory.create(params);

			OMElement omResponse = TwilioUtil.parseResponse("outgoingphonenumber.create.success");
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_PHONENUMBER, number.getPhoneNumber());
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_CALL_SID, number.getProperty("call_sid"));
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_VERIFICATION_CODE,number.getValidationCode());
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0005", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: add outgoing phone number");
	}

	private Map<String, String> getParameter(MessageContext messageContext) {

		String phoneNumber =
		                     (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                 TwilioUtil.PARAM_PHONENUMBER);
		String friendlyName =
		                      (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  TwilioUtil.PARAM_FRIENDLY_NAME);
		String callDelay =
		                   (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                               TwilioUtil.PARAM_CALL_DELAY);
		String extension =
		                   (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                               TwilioUtil.PARAM_EXTENSION);
		String callback =
		                  (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                              TwilioUtil.PARAM_STATUS_CALLBACK);
		String callbackMethod =
		                        (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                    TwilioUtil.PARAM_STATUS_CALLBACK_METHOD);

		Map<String, String> params = new HashMap<String, String>();
		params.put(TwilioUtil.TWILIO_PHONENUMBER, phoneNumber);
		if (friendlyName != null) {
			params.put(TwilioUtil.TWILIO_FRIENDLY_NAME, friendlyName);
		}
		if (callDelay != null) {
			params.put(TwilioUtil.TWILIO_CALL_DELAY, callDelay);
		}
		if (extension != null) {
			params.put(TwilioUtil.TWILIO_EXTENSION, extension);
		}
		if (callback != null) {
			params.put(TwilioUtil.TWILIO_STATUS_CALLBACK, callback);
		}
		if (callbackMethod != null) {
			params.put(TwilioUtil.TWILIO_STATUS_CALLBACKMETHOD, callbackMethod);
		}
		return params;
	}
}
