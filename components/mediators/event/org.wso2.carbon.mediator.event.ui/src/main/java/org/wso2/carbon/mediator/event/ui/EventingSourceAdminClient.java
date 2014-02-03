/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mediator.event.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.wso2.carbon.mediator.event.stub.service.EventSourceAdminServiceStub;
import org.wso2.carbon.mediator.event.stub.service.xsd.dto.EventSourceDTO;

import java.rmi.RemoteException;

public class EventingSourceAdminClient {

	private static final Log log = LogFactory.getLog(EventingSourceAdminClient.class);

	private EventSourceAdminServiceStub stub;

	/**
	 *
	 * @param cookie
	 * @param backendServerURL
	 * @param configCtx
	 * @throws org.apache.axis2.AxisFault
	 */
	public EventingSourceAdminClient(String cookie, String backendServerURL,
			ConfigurationContext configCtx) throws AxisFault {
		String serviceURL = backendServerURL + "EventSourceAdminService";
		stub = new EventSourceAdminServiceStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

	/**
	 *
	 * @return
	 * @throws AxisFault
	 */
	public EventSourceDTO[] getEventSources() throws AxisFault {

		try {
			return stub.getEventSources();
		} catch (RemoteException e) {
			handleException("Error while retreiving the eventsources", e);
		}

		return null;
	}

	/**
	 * Get EventSource by name
	 *
	 * @param eventSourceName
	 * @return
	 * @throws AxisFault
	 */
	public EventSourceDTO getEventSource(String eventSourceName) throws AxisFault {

		try {
			return stub.getEventSource(eventSourceName);
		} catch (RemoteException e) {
			handleException("Error while retreiving the eventsource " + eventSourceName, e);
		}

		return null;
	}

	/**
	 *
	 * @param eventsource
	 * @throws AxisFault
	 */
	public void addEventSource(EventSourceDTO eventsource) throws AxisFault {
		try {
			stub.addEventSource(eventsource);
		} catch (RemoteException e) {
			handleException("Error while adding the eventsource " + eventsource.getName(), e);
		}
	}

	/**
	 *
	 * @param eventsource
	 * @throws AxisFault
	 */
	public void saveEventSource(EventSourceDTO eventsource) throws AxisFault {
		try {
			stub.saveEventSource(eventsource);
		} catch (RemoteException e) {
			handleException("Error while adding the eventsource " + eventsource.getName(), e);
		}
	}

	/**
	 *
	 * @param name
	 * @throws AxisFault
	 */
	public void removeEventSource(String name) throws AxisFault {
		try {
			stub.removeEventSource(name);
		} catch (RemoteException e) {
			handleException("Error while removing the eventsource " + name, e);
		}
	}

	/**
	 *
	 * @param msg
	 * @param e
	 * @throws AxisFault
	 */
	private void handleException(String msg, Exception e) throws AxisFault {
		log.error(msg, e);
		throw new AxisFault(msg, e);
	}
}