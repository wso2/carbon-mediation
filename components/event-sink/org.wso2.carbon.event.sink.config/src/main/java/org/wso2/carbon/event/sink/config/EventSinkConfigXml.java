/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.sink.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.event.sink.EventSinkConstants;

/**
 * Creates the XML string to be written to file
 */
public class EventSinkConfigXml {

	private org.apache.axiom.om.OMFactory fac = OMAbstractFactory.getOMFactory();

	/**
	 * Creates XML representation of the Event Sink
	 *
	 * @param username         Username to be set to Event Sink OMElement
	 * @param password         Password to be set to Event Sink OMElement
	 * @param receiverUrl      Endpoint Url to be set to Event Sink OMElement
	 * @param authenticatorUrl Credential authentication url to be set to Event Sink OMElement
	 * @return The Created XML representation of Event Sink` as an OMElement
	 */
	public OMElement buildEventSink(String username, String password, String receiverUrl, String authenticatorUrl) {
		OMElement eventSinkElement = fac.createOMElement(EventSinkConstants.EVENT_SINK_Q);

		OMElement receiverUrlElement = fac.createOMElement(EventSinkConstants.RECEIVER_URL_Q);
		receiverUrlElement.setText(receiverUrl);
		eventSinkElement.addChild(receiverUrlElement);

		OMElement authenticatorUrlElement = fac.createOMElement(EventSinkConstants.AUTHENTICATOR_URL_Q);
		authenticatorUrlElement.setText(authenticatorUrl);
		eventSinkElement.addChild(authenticatorUrlElement);

		OMElement usernameElement = fac.createOMElement(EventSinkConstants.USERNAME_Q);
		usernameElement.setText(username);
		eventSinkElement.addChild(usernameElement);

		OMElement passwordElement = fac.createOMElement(EventSinkConstants.PASSWORD_Q);
		passwordElement.setText(password);
		eventSinkElement.addChild(passwordElement);

		return eventSinkElement;
	}
}