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
package org.wso2.carbon.connector.twilio.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestResponse;

public class TwilioUtil {

	public static final String API_URL = "https://api.twilio.com";
	public static final String API_VERSION = "2010-04-01";
	public static final String API_ACCOUNTS = "Accounts";
	public static final String API_SMS_SHORTCODES = "SMS/ShortCodes";
	public static final String API_SMS_MESSAGES = "SMS/Messages";
	public static final String API_AVAILABLE_PHONE_NUMBERS = "AvailablePhoneNumbers";
	public static final String API_TOLLFREE = "TollFree";
	public static final String API_LOCAL = "Local";
	public static final String API_TRANSCRIPTIONS = "Transcriptions";
	public static final String API_RECORDINGS = "Recordings";
	public static final String API_CALLS = "Calls";
	public static final String API_INCOMING_PHONENUMBER = "IncomingPhoneNumbers";
	public static final String API_OUTGOING_PHONENUMBER = "OutgoingCallerIds";
	public static final String API_PARTICIPANTS = "Participants";
	public static final String API_CONFERENCES = "Conferences";
	public static final String API_QUEUES = "Queues";
	public static final String API_MEMBERS = "Members";
	public static final String API_APPLICATIONS = "Applications";
	public static final String API_AUTHORIZED_CONNECT_APPS = "AuthorizedConnectApps";
	public static final String API_CONNECT_APPS = "ConnectApps";
	public static final String API_USAGE = "Usage";
	public static final String API_USAGE_RECORDS = "Records";
	public static final String API_USAGE_TRIGGERS = "Triggers";

	public static final String PARAM_FRIENDLY_NAME = "friendlyName";
	public static final String PARAM_STATUS = "status";
	public static final String PARAM_SUB_ACCOUNT_SID = "subAccountSid";
	public static final String PARAM_SHORTCODE_SID = "shortCodeSid";
	public static final String PARAM_SHORTCODE = "shortCode";
	public static final String PARAM_MESSAGE_SID = "messageSid";
	public static final String PARAM_TO = "to";
	public static final String PARAM_FROM = "from";
	public static final String PARAM_FALLBACKURL = "fallbackUrl";
	public static final String PARAM_FALLBACK_METHOD = "fallbackMethod";
	public static final String PARAM_URL = "url";
	public static final String PARAM_METHOD = "method";
	public static final String PARAM_DATESENT = "dateSent";
	public static final String PARAM_BODY = "body";
	public static final String PARAM_STATUS_CALLBACK_URL = "statusCallBackUrl";
	public static final String PARAM_APPLICATION_SID = "applicationSid";
	public static final String PARAM_API_VERSION = "apiVersion";
	public static final String PARAM_SMS_URL = "smsUrl";
	public static final String PARAM_SMS_METHOD = "smsMethod";
	public static final String PARAM_SMS_FALLBACKURL = "smsFallbackUrl";
	public static final String PARAM_SMS_FALLBACKMETHOD = "smsFallbackMethod";
	public static final String PARAM_INCOMING_PHONE_SID = "incomingCallerId";
	public static final String PARAM_OUTGOING_PHONE_SID = "outgoingCallerId";
	public static final String PARAM_VOICEURL = "voiceUrl";
	public static final String PARAM_VOICEMETHOD = "voiceMethod";
	public static final String PARAM_VOICEFALLBACKURL = "voiceFallbackUrl";
	public static final String PARAM_VOICEFALLBACKMETHOD = "voiceFallbackMethod";
	public static final String PARAM_STATUS_CALLBACK = "statusCallback";
	public static final String PARAM_STATUS_CALLBACK_METHOD = "statusCallbackMethod";
	public static final String PARAM_VOICE_CALLERID_LOOKUP = "voiceCallerIdLookup";
	public static final String PARAM_VOICE_APPLICATION_SID = "voiceApplicationSid";
	public static final String PARAM_SMS_APPLICATION_SID = "smsApplicationSid";
	public static final String PARAM_SMS_FALLBACK_URL = "smsFallbackUrl";
	public static final String PARAM_SMS_STATUS_CALLBACK = "smsStatusCallback";
	public static final String PARAM_ACCOUNT_SID = "accountSid";
	public static final String PARAM_COUNTRY = "country";
	public static final String PARAM_AREACODE = "areaCode";
	public static final String PARAM_CONTAINS = "contains";
	public static final String PARAM_IN_REGION = "inRegion";
	public static final String PARAM_IN_POSTAL_CODE = "inPostalCode";
	public static final String PARAM_NEAR_LAT_LONG = "nearLatLong";
	public static final String PARAM_NEAR_NUMBER = "nearNumber";
	public static final String PARAM_IN_LATA = "inLata";
	public static final String PARAM_IN_RATE_CENTER = "inRateCenter";
	public static final String PARAM_DISTANCE = "distance";
	public static final String PARAM_PHONENUMBER = "phoneNumber";
	public static final String PARAM_SEND_DIGITS = "sendDigits";
	public static final String PARAM_IF_MACHINE = "ifMachine";
	public static final String PARAM_IF_TIMEOUT = "timeout";
	public static final String PARAM_IF_RECORD = "record";
	public static final String PARAM_CALL_SID = "callSid";
	public static final String PARAM_DATE_CREATED = "dateCreated";
	public static final String PARAM_DATE_UPDATED = "dateUpdated";
	public static final String PARAM_TRANSCRIPTION_SID = "transcriptionSid";
	public static final String PARAM_RECORDING_SID = "recordingSid";
	public static final String PARAM_STARTTIME = "startTime";
	public static final String PARAM_PARENT_CALL_SID = "parentCallSid";
	public static final String PARAM_CONFERENCE_SID = "conferenceSid";
	public static final String PARAM_MUTED = "muted";
	public static final String PARAM_QUEUE_SID = "queueSid";
	public static final String PARAM_MAX_SIZE = "maxSize";
	public static final String PARAM_AUTHORIZED_CONNECT_APP_SID = "authorizedConnectAppSid";
	public static final String PARAM_CONNECT_APP_SID = "connectAppSid";
	public static final String PARAM_AUTHORIZED_REDIRECT_URL = "authorizeRedirectUrl";
	public static final String PARAM_DEAUTHORIZE_CALLBACK_URL = "deauthorizeCallbackUrl";
	public static final String PARAM_DEAUTHORIZE_CALLBACK_METHOD = "deauthorizeCallbackMethod";
	public static final String PARAM_PERMISSIONS = "permissions";
	public static final String PARAM_DESCRIPTION = "description";
	public static final String PARAM_COMPANY_NAME = "companyName";
	public static final String PARAM_HOMEPAGE_URL = "homepageUrl";
	public static final String PARAM_VERIFICATION_CODE = "verificationCode";
	public static final String PARAM_CALL_DELAY = "callDelay";
	public static final String PARAM_EXTENSION = "extension";
	public static final String PARAM_OUTGOING_CALLERID = "outgoingCallerId";
	public static final String PARAM_CATEGORY = "category";
	public static final String PARAM_START_DATE = "startDate";
	public static final String PARAM_END_DATE = "endDate";
	public static final String PARAM_RECURRING = "recurring";
	public static final String PARAM_USAGE_CATEGORY = "usageCategory";
	public static final String PARAM_TRIGGERBY = "triggerBy";
	public static final String PARAM_USAGE_TRIGGER_SID = "usageTriggerSid";
	public static final String PARAM_CALLBACK_URL = "callbackUrl";
	public static final String PARAM_CALLBACK_METHOD = "callbackMethod";
	public static final String PARAM_TRIGGER_VALUE = "triggerValue";

	public static final String TWILIO_FRIENDLY_NAME = "FriendlyName";
	public static final String TWILIO_MAX_SIZE = "MaxSize";
	public static final String TWILIO_STATUS = "Status";
	public static final String TWILIO_SHORT_CODE = "ShortCode";
	public static final String TWILIO_TO = "To";
	public static final String TWILIO_FROM = "From";
	public static final String TWILIO_URL = "Url";
	public static final String TWILIO_DATESENT = "DateSent";
	public static final String TWILIO_BODY = "Body";
	public static final String TWILIO_APPLICATION_SID = "ApplicationSid";
	public static final String TWILIO_STATUS_CALLBACK = "StatusCallback";
	public static final String TWILIO_API_VERSION = "ApiVersion";
	public static final String TWILIO_SMS_URL = "SmsUrl";
	public static final String TWILIO_SMS_METHOD = "SmsMethod";
	public static final String TWILIO_SMS_FALLBACKURL = "SmsFallbackUrl";
	public static final String TWILIO_SMS_FALLBACKMETHOD = "SmsFallbackMethod";
	public static final String TWILIO_VOICE_URL = "VoiceUrl";
	public static final String TWILIO_VOICE_METHOD = "VoiceMethod";
	public static final String TWILIO_VOICE_FALLBACKURL = "VoiceFallbackUrl";
	public static final String TWILIO_VOICE_FALLBACKMETHOD = "VoiceFallbackMethod";
	public static final String TWILIO_STATUS_CALLBACKMETHOD = "StatusCallbackMethod";
	public static final String TWILIO_VOICE_CALLERID_LOOKUP = "VoiceCallerIdLookup";
	public static final String TWILIO_VOICE_APPLICATION_SID = "VoiceApplicationSid";
	public static final String TWILIO_SMS_APPLICATION_SID = "SmsApplicationSid";
	public static final String TWILIO_ACCOUNT_SID = "AccountSid";
	public static final String TWILIO_CALL_SID = "CallSid";
	public static final String TWILIO_AREACODE = "AreaCode";
	public static final String TWILIO_CONTAINS = "Contains";
	public static final String TWILIO_IN_REGION = "InRegion";
	public static final String TWILIO_IN_POSTAL_CODE = "InPostalCode";
	public static final String TWILIO_NEAR_LAT_LONG = "NearLatLong";
	public static final String TWILIO_NEAR_NUMBER = "NearNumber";
	public static final String TWILIO_IN_LATA = "InLata";
	public static final String TWILIO_IN_RATE_CENTER = "InRateCenter";
	public static final String TWILIO_DISTANCE = "Distance";
	public static final String TWILIO_PHONENUMBER = "PhoneNumber";
	public static final String TWILIO_METHOD = "Method";
	public static final String TWILIO_FALLBACK_URL = "FallbackUrl";
	public static final String TWILIO_FALLBACK_METHOD = "FallbackMethod";
	public static final String TWILIO_SEND_DIGITS = "SendDigits";
	public static final String TWILIO_IF_MACHINE = "IfMachine";
	public static final String TWILIO_TIMEOUT = "Timeout";
	public static final String TWILIO_RECORD = "Record";
	public static final String TWILIO_STARTTIME = "StartTime";
	public static final String TWILIO_PARENT_CALL_SID = "ParentCallSid";
	public static final String TWILIO_DATECREATED = "DateCreated";
	public static final String TWILIO_DATEUPDATED = "DateUpdated";
	public static final String TWILIO_MUTED = "Muted";
	public static final String TWILIO_SMS_STATUS_CALLBACK = "SmsStatusCallback";
	public static final String TWILIO_AUTHORIZED_REDIRECT_URL = "AuthorizeRedirectUrl";
	public static final String TWILIO_DEAUTHORIZED_CALLBACK_URL = "DeauthorizeCallbackUrl";
	public static final String TWILIO_DEAUTHORIZED_CALLBACK_METHOD = "DeauthorizeCallbackMethod";
	public static final String TWILIO_PERMISSIONS = "Permissions";
	public static final String TWILIO_DESCRIPTION = "Description";
	public static final String TWILIO_COMPANYNAME = "CompanyName";
	public static final String TWILIO_HOMEPAGE_URL = "HomepageUrl";
	public static final String TWILIO_CALL_DELAY = "CallDelay";
	public static final String TWILIO_EXTENSION = "Extension";
	public static final String TWILIO_CATEGORY = "Category";
	public static final String TWILIO_START_DATE = "StartDate";
	public static final String TWILIO_END_DATE = "EndDate";
	public static final String TWILIO_RECURRING = "Recurring";
	public static final String TWILIO_USAGE_CATEGORY = "UsageCategory";
	public static final String TWILIO_TRIGGERBY = "TriggerBy";
	public static final String TWILIO_CALLBACK_URL = "CallbackUrl";
	public static final String TWILIO_CALLBACK_METHOD = "CallbackMethod";
	public static final String TWILIO_TRIGGER_VALUE = "TriggerValue";

	private static final OMFactory fac = OMAbstractFactory.getOMFactory();
	private static final OMNamespace omNs = fac.createOMNamespace("http://wso2.org/twilio/adaptor",
	                                                              "twilio");

	public synchronized static TwilioRestClient getTwilioRestClient(MessageContext messageContext)
	                                                                                              throws ConnectException {
		// Authorization details
		// Get parameters from the messageContext
		// Getting Transport Headers
		Axis2MessageContext axis2smc = (Axis2MessageContext) messageContext;
		axis2smc.getAxis2MessageContext();
		String accountSid =
		                    (String) axis2smc.getAxis2MessageContext().getOperationContext()
		                                     .getProperty("twilio.accountSid");
		String authToken =
		                   (String) axis2smc.getAxis2MessageContext().getOperationContext()
		                                    .getProperty("twilio.authToken");
		return new TwilioRestClient(accountSid, authToken);
	}

	public static OMElement parseResponse(String strMessageKey) {
		String strResponse = getMessage(strMessageKey);
		OMElement omElement = fac.createOMElement("response", omNs);
		OMElement subValue = fac.createOMElement("message", omNs);
		subValue.addChild(fac.createOMText(omElement, strResponse));
		omElement.addChild(subValue);
		return omElement;
	}

	private static String getMessage(String strMessageKey) {
		Properties prop = new Properties();
		try {
			prop.load(TwilioUtil.class.getResourceAsStream("/messages/message.properties"));
			return (String) prop.getProperty(strMessageKey);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return "Error getting the message for key:" + strMessageKey;
	}

	public static OMElement addElement(OMElement omElement, String strElement, String strValue) {
		OMElement subValue = fac.createOMElement(strElement, omNs);
		subValue.addChild(fac.createOMText(omElement, strValue));
		omElement.addChild(subValue);
		return omElement;
	}

	public static OMElement addElement(OMElement omElement, String strElement, boolean bValue) {
		OMElement subValue = fac.createOMElement(strElement, omNs);
		subValue.addChild(fac.createOMText(omElement, bValue));
		omElement.addChild(subValue);
		return omElement;
	}

	public static OMElement parseResponse(TwilioRestResponse restResponse) {
		OMElement omElement;
		try {
			omElement = AXIOMUtil.stringToOM(restResponse.getResponseText());
		} catch (Exception e) {
			omElement = fac.createOMElement("error", omNs);
			OMElement subValue = fac.createOMElement("errorMessage", omNs);
			subValue.addChild(fac.createOMText(omElement, e.getMessage()));
			omElement.addChild(subValue);
		}
		return omElement;
	}

	public static void preparePayload(MessageContext messageContext, OMElement element) {
		SOAPBody soapBody = messageContext.getEnvelope().getBody();
		for (Iterator itr = soapBody.getChildElements(); itr.hasNext();) {
			OMElement child = (OMElement) itr.next();
			child.detach();
		}
		for (Iterator itr = element.getChildElements(); itr.hasNext();) {
			OMElement child = (OMElement) itr.next();
			soapBody.addChild(child);
		}
	}

	public static void preparePayload(MessageContext messageContext, Exception e) {		 
		OMElement omElement = fac.createOMElement("error", omNs);
		OMElement subValue = fac.createOMElement("errorMessage", omNs);
		subValue.addChild(fac.createOMText(omElement, e.getMessage()));	
		omElement.addChild(subValue);
		preparePayload(messageContext, omElement);
	}
	
	public static void handleException(Exception e,String erroCode, MessageContext messageContext){		
		messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION, e);
		messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, e.getMessage());
		messageContext.setProperty(SynapseConstants.ERROR_CODE, erroCode);
		preparePayload(messageContext, e);
	}
	
}
