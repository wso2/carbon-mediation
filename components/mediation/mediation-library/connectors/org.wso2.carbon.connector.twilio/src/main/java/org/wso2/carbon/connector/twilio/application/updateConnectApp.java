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
import com.twilio.sdk.resource.instance.ConnectApp;

/*
 * Class mediator for updating a connect app instance with optional parameters
 * For more information, see http://www.twilio.com/docs/api/rest/connect-apps
 */
public class updateConnectApp extends AbstractConnector {

	public void connect(MessageContext messageContext) {
		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: update connect application");
		Map<String, String> params = createParameterMap(messageContext);
		String connectSid =
		                    (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                TwilioUtil.PARAM_CONNECT_APP_SID);
		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);
			// Retrieve the matching Connect App based on its Sid
			ConnectApp connectApp = twilioRestClient.getAccount().getConnectApp(connectSid);
			// update the relevant connect app with the parameters specified.
			connectApp.update(params);
			OMElement omResponse = TwilioUtil.parseResponse("conapp.update.success");
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_CONNECT_APP_SID, connectApp.getSid());
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0002", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: update connect application");
	}

	/**
	 * Create a map containing the parameters required to update the
	 * application, which has been defined
	 * 
	 * @return The map containing the defined parameters
	 */
	private Map<String, String> createParameterMap(MessageContext messageContext) {
		// Required
		String friendlyName =
		                      (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                  TwilioUtil.PARAM_FRIENDLY_NAME);
		String authorizedRedirectUrl =
		                               (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                           TwilioUtil.PARAM_AUTHORIZED_REDIRECT_URL);
		String deauthorizedCallbackUrl =
		                                 (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                             TwilioUtil.PARAM_DEAUTHORIZE_CALLBACK_URL);
		String deauthorizedCallbackMethod =
		                                    (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                                TwilioUtil.PARAM_DEAUTHORIZE_CALLBACK_METHOD);
		String permissions =
		                     (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                 TwilioUtil.PARAM_PERMISSIONS);
		String description =
		                     (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                 TwilioUtil.PARAM_DESCRIPTION);
		String companyName =
		                     (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                 TwilioUtil.PARAM_COMPANY_NAME);
		String homepageUrl =
		                     (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                 TwilioUtil.PARAM_HOMEPAGE_URL);

		// creating the map for optional parameters
		Map<String, String> params = new HashMap<String, String>();

		// null-checking and addition to map
		if (friendlyName != null) {
			params.put(TwilioUtil.TWILIO_FRIENDLY_NAME, friendlyName);
		}
		if (authorizedRedirectUrl != null) {
			params.put(TwilioUtil.TWILIO_AUTHORIZED_REDIRECT_URL, authorizedRedirectUrl);
		}
		if (deauthorizedCallbackUrl != null) {
			params.put(TwilioUtil.TWILIO_DEAUTHORIZED_CALLBACK_URL, deauthorizedCallbackUrl);
		}
		if (deauthorizedCallbackMethod != null) {
			params.put(TwilioUtil.TWILIO_DEAUTHORIZED_CALLBACK_METHOD, deauthorizedCallbackMethod);
		}
		if (permissions != null) {
			params.put(TwilioUtil.TWILIO_PERMISSIONS, permissions);
		}
		if (description != null) {
			params.put(TwilioUtil.TWILIO_DESCRIPTION, description);
		}
		if (companyName != null) {
			params.put(TwilioUtil.TWILIO_COMPANYNAME, companyName);
		}
		if (homepageUrl != null) {
			params.put(TwilioUtil.TWILIO_HOMEPAGE_URL, homepageUrl);
		}
		return params;
	}
}
