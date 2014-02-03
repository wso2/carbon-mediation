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
import com.twilio.sdk.resource.instance.Call;

/*
 * Class mediator for modifying a live call.
 * For more information, see
 * http://www.twilio.com/docs/api/rest/change-call-state
 */
public class ModifyLiveCall extends AbstractConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {
		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: Update Live Call");

		// Must be provided
		String callSid =
		                 (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                             TwilioUtil.PARAM_CALL_SID);
		Map<String, String> params = getParameters(messageContext);
		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);
			// Get the call to be modified
			Call call = twilioRestClient.getAccount().getCall(callSid);
			// update the matching live call with the specified parameters
			call.update(params);
			OMElement omResponse = TwilioUtil.parseResponse("call.update.success");
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_CALL_SID, call.getSid());
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e);
			TwilioUtil.handleException(e, "0003", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: Update Live Call");
	}

	private Map<String, String> getParameters(MessageContext messageContext) {
		// Optional parameters. For specifications and formats, see
		// http://www.twilio.com/docs/api/rest/change-call-state
		// Available parameters to be modified (Optional)
		String url =
		             (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                         TwilioUtil.PARAM_URL);
		String status =
		                (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                            TwilioUtil.PARAM_STATUS);
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

		// Map for optional parameters
		Map<String, String> params = new HashMap<String, String>();
		if (url != null) {
			params.put(TwilioUtil.TWILIO_URL, url);
		}
		if (status != null) {
			params.put(TwilioUtil.TWILIO_STATUS, status);
		}
		if (method != null) {
			params.put(TwilioUtil.TWILIO_METHOD, method);
		}
		if (fallbackUrl != null) {
			params.put(TwilioUtil.TWILIO_FALLBACK_URL, fallbackUrl);
		}
		if (fallbackMethod != null) {
			params.put(TwilioUtil.TWILIO_FALLBACK_METHOD, fallbackMethod);
		}
		if (statusCallback != null) {
			params.put(TwilioUtil.TWILIO_STATUS_CALLBACK, statusCallback);
		}
		if (statusCallbackMethod != null) {
			params.put(TwilioUtil.TWILIO_STATUS_CALLBACKMETHOD, statusCallbackMethod);
		}
		return params;
	}
}
