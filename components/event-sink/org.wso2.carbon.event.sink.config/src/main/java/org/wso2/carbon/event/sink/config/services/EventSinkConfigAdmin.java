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
package org.wso2.carbon.event.sink.config.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.sink.EventSink;
import org.wso2.carbon.event.sink.EventSinkException;
import org.wso2.carbon.event.sink.config.EventSinkXmlReader;
import org.wso2.carbon.event.sink.config.EventSinkXmlWriter;

import java.util.List;

/**
 * Admin service class to expose all web services
 */
public class EventSinkConfigAdmin extends AbstractAdmin {
	private static final Log log = LogFactory.getLog(EventSinkConfigAdmin.class);

	/**
	 * Invokes Appropriate class methods to write event sink xml file
	 *
	 * @param name             String to set as Event Sink name
	 * @param username         String to set as Event Sink username
	 * @param password         String to set as Event Sink password
	 * @param receiverUrl      String to set as Event Sink receiverUrl
	 * @param authenticatorUrl String to set as Event Sink authenticatorUrl
	 */
	public boolean writeEventSink(String name, String username, String password, String receiverUrl,
	                           String authenticatorUrl) throws EventSinkException {
		try {
			return new EventSinkXmlWriter().writeEventSink(new EventSink(name, username, password, receiverUrl,
			                                                       authenticatorUrl));
		} catch (EventSinkException e) {
			throw new EventSinkException("Event Sink "+name+" cannot be created.",e);
		}
	}

	/**
	 * Invokes appropriate methods to get All Event Sinks
	 *
	 * @return list of Evenn Sinks in the deployment directory
	 */
	public List<EventSink> getAllEventSinks() {
		return new EventSinkXmlReader().getAllEventSinks();
	}

	/**
	 * Invokes appropriate methods to get specific Event Sink
	 *
	 * @param name Event Sink name to be retrieved.
	 * @return Requested Event Sink
	 */
	public EventSink getEventSinkFromName(String name) throws EventSinkException {
		try {
			return new EventSinkXmlReader().getEventSinkFromName(name);
		} catch (EventSinkException e) {
			throw new EventSinkException("Event Sink "+name+" cannot be found.",e);
		}
	}

	/**
	 * Invokes appropriate methods to delete specific Event Sink
	 *
	 * @param name Event Sink to be deleted
	 * @return Status of the deletion as boolean value
	 */
	public boolean deleteEventSink(String name) throws EventSinkException {
		try {
			return new EventSinkXmlReader().deleteEventSinkFromName(name);
		} catch (EventSinkException e) {
			throw new EventSinkException("Event Sink " + name + " cannot be deleted.", e);
		}
	}

	/**
	 * Invokes appropriate methods to update specific Event Sink
	 *
	 * @param name             String to set as Event Sink name
	 * @param username         String to set as Event Sink username
	 * @param password         String to set as Event Sink password
	 * @param receiverUrl      String to set as Event Sink receiverUrl
	 * @param authenticatorUrl String to set as Event Sink authenticatorUrl
	 * @return Status of the update as boolean value
	 */
	public boolean updateEventSink(String name, String username, String password, String receiverUrl,
	                               String authenticatorUrl) throws EventSinkException {
		try {
			return new EventSinkXmlWriter()
					.updateEventSink(new EventSink(name, username, password, receiverUrl, authenticatorUrl));
		} catch (EventSinkException e) {
			throw new EventSinkException("Event Sink " + name + " cannot be updated.", e);
		}
	}
}