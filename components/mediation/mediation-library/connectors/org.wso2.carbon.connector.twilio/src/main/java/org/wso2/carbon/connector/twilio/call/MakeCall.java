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
package org.wso2.carbon.connector.twilio.call;

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
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Call;

/*
 * Class mediator for making a call.
 * For more information, see http://www.twilio.com/docs/api/rest/making-calls
 */
public class MakeCall extends AbstractConnector {
	// Parameter details. For specifications and formats, see
	// http://www.twilio.com/docs/api/rest/making-calls#post-parameters-required
	// and
	// http://www.twilio.com/docs/api/rest/making-calls#post-parameters-optional.

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {
		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: Make Call");

		Map<String, String> callParams = createParameterMap(messageContext);

		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);
			CallFactory callFactory = twilioRestClient.getAccount().getCallFactory();
			Call call = callFactory.create(callParams);
			OMElement omResponse = TwilioUtil.parseResponse("call.create.success");
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_CALL_SID, call.getSid());
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0003", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: Make Call");
	}

	/**
	 * Create a map containing the parameters required to make the call, which
	 * has been defined
	 * 
	 * @return The map containing the defined parameters
	 */
	private Map<String, String> createParameterMap(MessageContext messageContext) {

		// These are compulsory
		String to =
		            (String) ConnectorUtils.lookupTemplateParamater(messageContext, TwilioUtil.PARAM_TO);
		String from =
		              (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                          TwilioUtil.PARAM_FROM);
		// One of the below
		String callUrl =
		                 (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                             TwilioUtil.PARAM_URL);
		String applicationSid =
		                        (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                    TwilioUtil.PARAM_APPLICATION_SID);
		// Optional parameters
		String method =
		                (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                            TwilioUtil.PARAM_METHOD);
		String fallbackUrl =
		                     (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                 TwilioUtil.PARAM_FALLBACKURL);
		String fallbackMethod =
		                        (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                    TwilioUtil.PARAM_FALLBACK_METHOD);
		String statusCallback =
		                        (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                    TwilioUtil.PARAM_STATUS_CALLBACK);
		String statusCallbackMethod =
		                              (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                          TwilioUtil.PARAM_STATUS_CALLBACK_METHOD);
		String sendDigits =
		                    (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                TwilioUtil.PARAM_SEND_DIGITS);
		String ifMachine =
		                   (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                               TwilioUtil.PARAM_IF_MACHINE);
		String timeout =
		                 (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                             TwilioUtil.PARAM_IF_TIMEOUT);
		String record =
		                (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                            TwilioUtil.PARAM_IF_RECORD);

		Map<String, String> callParams = new HashMap<String, String>();
		callParams.put(TwilioUtil.TWILIO_TO, to);
		callParams.put(TwilioUtil.TWILIO_FROM, from);
		// Only one of the below must be provided
		if (callUrl != null) {
			callParams.put(TwilioUtil.TWILIO_URL, callUrl);
		} else {
			callParams.put(TwilioUtil.TWILIO_APPLICATION_SID, applicationSid);
		}
		// These are optional parameters. Need to check whether the parameters
		// have been defined
		if (method != null) {
			callParams.put(TwilioUtil.TWILIO_METHOD, method);
		}
		if (fallbackUrl != null) {
			callParams.put(TwilioUtil.TWILIO_FALLBACK_URL, fallbackUrl);
		}
		if (fallbackMethod != null) {
			callParams.put(TwilioUtil.TWILIO_FALLBACK_METHOD, fallbackMethod);
		}
		if (statusCallback != null) {
			callParams.put(TwilioUtil.TWILIO_STATUS_CALLBACK, statusCallback);
		}
		if (statusCallbackMethod != null) {
			callParams.put(TwilioUtil.TWILIO_STATUS_CALLBACKMETHOD, statusCallbackMethod);
		}
		if (sendDigits != null) {
			callParams.put(TwilioUtil.TWILIO_SEND_DIGITS, sendDigits);
		}
		if (ifMachine != null) {
			callParams.put(TwilioUtil.TWILIO_IF_MACHINE, ifMachine);
		}
		if (timeout != null) {
			callParams.put(TwilioUtil.TWILIO_TIMEOUT, timeout);
		}
		if (record != null) {
			callParams.put(TwilioUtil.TWILIO_RECORD, record);
		}
		return callParams;
	}
}