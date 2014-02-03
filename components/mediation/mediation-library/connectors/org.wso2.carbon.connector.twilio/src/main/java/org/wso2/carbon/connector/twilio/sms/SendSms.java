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
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.twilio.util.TwilioUtil;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Sms;

/*
 * Class mediator for sending an SMS.
 * For more information, see http://www.twilio.com/docs/api/rest/sending-sms
 */
public class SendSms extends AbstractConnector {

	public void connect(MessageContext messageContext) {

		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: send SMS");

		String to =
		            (String) ConnectorUtils.lookupTemplateParamater(messageContext, TwilioUtil.PARAM_TO);
		String from =
		              (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                          TwilioUtil.PARAM_FROM);
		String body =
		              (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                          TwilioUtil.PARAM_BODY);
		// optional parameters
		// see
		// http://www.twilio.com/docs/api/rest/sending-sms#post-parameters-optional
		// for more details.
		String statusCallBackUrl =
		                           (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                       TwilioUtil.PARAM_STATUS_CALLBACK_URL);
		String applicationSid =
		                        (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                    TwilioUtil.PARAM_APPLICATION_SID);

		// the map used for passing parameters
		Map<String, String> params = new HashMap<String, String>();
		// add the optional parameters to the map
		params.put(TwilioUtil.TWILIO_TO, to);
		params.put(TwilioUtil.TWILIO_FROM, from);
		params.put(TwilioUtil.TWILIO_BODY, body);

		if (applicationSid != null) {
			params.put(TwilioUtil.TWILIO_APPLICATION_SID, applicationSid);
		}
		if (statusCallBackUrl != null) {
			params.put(TwilioUtil.TWILIO_STATUS_CALLBACK, statusCallBackUrl);
		}

		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);
			// Creates a SMS and sends it
			SmsFactory messageFactory = twilioRestClient.getAccount().getSmsFactory();
			Sms message = messageFactory.create(params);
			
			OMElement omResponse = TwilioUtil.parseResponse("sms.create.success");
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_MESSAGE_SID, message.getSid());
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_STATUS, message.getStatus());
			TwilioUtil.preparePayload(messageContext, omResponse);			
		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0007", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: send SMS");
	}
}
