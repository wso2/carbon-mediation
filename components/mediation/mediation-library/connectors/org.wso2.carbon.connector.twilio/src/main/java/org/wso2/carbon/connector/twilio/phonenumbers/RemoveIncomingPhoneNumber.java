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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.util.ConnectorUtils;
import org.wso2.carbon.connector.twilio.util.TwilioUtil;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;

/*
 * Class mediator for removing an incoming a phone numbers.
 * For more information, see
 * http://www.twilio.com/docs/api/rest/incoming-phone-numbers#instance-delete
 */
public class RemoveIncomingPhoneNumber extends AbstractConnector {

	// //Authorization details
	// private String accountSid;
	// private String authToken;
	//
	// //Sid of the required number
	// private String incomingPhoneNumberSid;

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		log.auditLog("Start: remove phone number");

		// Must be provided
		String incomingPhoneNumberSid =
		                                (String) ConnectorUtils.lookupTemplateParamater(messageContext,
		                                                                            TwilioUtil.PARAM_INCOMING_PHONE_SID);

		try {
			TwilioRestClient twilioRestClient = TwilioUtil.getTwilioRestClient(messageContext);
			// Get an incoming phone number allocated to the Twilio account by
			// its Sid.
			IncomingPhoneNumber number =
			                             twilioRestClient.getAccount()
			                                             .getIncomingPhoneNumber(incomingPhoneNumberSid);
			OMElement omResponse = null;
			if (number.delete()) {
				omResponse = TwilioUtil.parseResponse("phonenumber.delete.success");
			} else {
				omResponse = TwilioUtil.parseResponse("phonenumber.delete.fail");
			}
			
			TwilioUtil.addElement(omResponse, TwilioUtil.PARAM_INCOMING_PHONE_SID, number.getSid());
			TwilioUtil.preparePayload(messageContext, omResponse);
		} catch (Exception e) {
			log.error(e.getMessage());
			TwilioUtil.handleException(e, "0005", messageContext);
			throw new SynapseException(e);
		}
		log.auditLog("End: remove phone number");
	}
}
