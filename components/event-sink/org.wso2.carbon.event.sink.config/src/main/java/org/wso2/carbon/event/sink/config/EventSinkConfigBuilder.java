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

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.event.sink.EventSink;
import org.wso2.carbon.event.sink.EventSinkConstants;
import org.wso2.carbon.event.sink.EventSinkException;

/**
 * Creates the Event Sink with given configuration XML.
 */
public class EventSinkConfigBuilder {

	/**
	 * Creates the Event Sink with given configuration XML.
	 *
	 * @param eventSinkConfigElement OMElement to be converted EventSink Object
	 * @param name Event Sink name to be set
	 * @return Created EventSink Object
	 * @throws EventSinkException
	 */
	public EventSink createEventSinkConfig(OMElement eventSinkConfigElement, String name) throws EventSinkException {
		EventSink eventSink = new EventSink();

		eventSink.setName(name);

		OMElement receiverUrl = eventSinkConfigElement.getFirstChildWithName(EventSinkConstants.RECEIVER_URL_Q);
		if (receiverUrl == null) {
			throw new EventSinkException(EventSinkConstants.RECEIVER_URL_Q.getLocalPart() + " element missing");
		}
		eventSink.setReceiverUrlSet(receiverUrl.getText());

		OMElement authenticatorUrl =
				eventSinkConfigElement.getFirstChildWithName(EventSinkConstants.AUTHENTICATOR_URL_Q);
		if (authenticatorUrl == null) {
			throw new EventSinkException(EventSinkConstants.AUTHENTICATOR_URL_Q.getLocalPart() + " element missing");
		}
		eventSink.setAuthenticationUrlSet(authenticatorUrl.getText());

		OMElement username = eventSinkConfigElement.getFirstChildWithName(EventSinkConstants.USERNAME_Q);
		if (username == null) {
			throw new EventSinkException(EventSinkConstants.USERNAME_Q.getLocalPart() + " element missing");
		}
		eventSink.setUsername(username.getText());

		OMElement password = eventSinkConfigElement.getFirstChildWithName(EventSinkConstants.PASSWORD_Q);
		if (password == null) {
			throw new EventSinkException(EventSinkConstants.PASSWORD_Q.getLocalPart() + " element missing");
		}
		eventSink.setPassword(password.getText());
		return eventSink;
	}

}