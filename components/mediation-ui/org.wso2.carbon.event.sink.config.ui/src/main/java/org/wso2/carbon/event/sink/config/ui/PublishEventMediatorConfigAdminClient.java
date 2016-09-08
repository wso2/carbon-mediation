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

package org.wso2.carbon.event.sink.config.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.sink.config.stub.PublishEventMediatorConfigAdminEventSinkException;
import org.wso2.carbon.event.sink.config.stub.PublishEventMediatorConfigAdminStub;
import org.wso2.carbon.event.sink.xsd.EventSink;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Admin client that uses the backend persistence and cryptographic facilities
 */
public class PublishEventMediatorConfigAdminClient {

    private static final Log log = LogFactory.getLog(PublishEventMediatorConfigAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.event.sink.config.ui.i18n.Resources";
    private PublishEventMediatorConfigAdminStub stub;
    private ResourceBundle bundle;

    public PublishEventMediatorConfigAdminClient(String cookie, String backendServerURL,
                                                 ConfigurationContext configCtx, Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "PublishEventMediatorConfigAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new PublishEventMediatorConfigAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Invokes services' writeEventSinkXml method to get All Event Sinks
     *
     * @param eventSink details of the Event Sink to be written
     */
    public void writeEventSinkXml(EventSink eventSink) throws PublishEventMediatorConfigAdminEventSinkException {
        try {
            stub.writeEventSink(eventSink.getName(), eventSink.getUsername(), eventSink.getPassword(),
                    eventSink.getReceiverUrlSet(), eventSink.getAuthenticationUrlSet());
        } catch (RemoteException e) {
            log.error("Error occurred while wring Event Sink, Error: " + e.getLocalizedMessage());
        }
    }

    /**
     * Invokes services' getAllEventSinks method to get All Event Sinks
     *
     * @return list of Event Sinks in the deployment directory
     */
    public EventSink[] getAllEventSinks() {
        EventSink[] eventSinkList = new EventSink[0];
        try {
            eventSinkList = stub.getAllEventSinks();
        } catch (RemoteException e) {
            log.error("Error Occurred while obtaining list of Event Sinks, Error: " +
                    e.getLocalizedMessage());
        }
        return eventSinkList == null ? new EventSink[0] : eventSinkList;
    }

    /**
     * Invokes services' getEventSinkFromName method to get Specific Event Sink  detail
     *
     * @return Event Sink corresponds to the given name
     */
    public org.wso2.carbon.event.sink.xsd.EventSink getEventSinkByName(String name)
            throws PublishEventMediatorConfigAdminEventSinkException {
        org.wso2.carbon.event.sink.xsd.EventSink eventSink =
                new org.wso2.carbon.event.sink.xsd.EventSink();
        try {
            eventSink = stub.getEventSinkFromName(name);

        } catch (RemoteException e) {
            log.error("Event Sink cannot be deleted, Error: " +
                    e.getLocalizedMessage());
        }
        return eventSink;
    }

    /**
     * Invokes services' deleteEventSink method to get All Event Sinks
     *
     * @return status of the deletion as boolean value
     */
    public boolean deleteEventSink(String name) throws PublishEventMediatorConfigAdminEventSinkException {
        try {
            return stub.deleteEventSink(name);
        } catch (RemoteException e) {
            log.error("Event Sink cannot be deleted, Error: " + e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Invokes services' updateEventSink method to get All Event Sinks
     *
     * @return status of the update as boolean value
     */
    public boolean updateEventSink(String name, String username, String password, String receiverUrl,
                                   String authenticatorUrl) throws PublishEventMediatorConfigAdminEventSinkException {
        try {
            return stub.updateEventSink(name, username, password, receiverUrl, authenticatorUrl);
        } catch (RemoteException e) {
            log.error("Error occurred while updating Event Sink, Error: " +
                    e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Checks the provided string empty or null
     *
     * @param string property which needs check empty or null
     * @return true if provided property is null and not empty else false
     */
    public boolean isNotNullAndNotEmpty(String string) {
        return string != null && !string.equals("");
    }

    /**
     * Method checks whether a socket can be created for the given ip and port
     *
     * @param ip   ip of analytics server
     * @param port port of the analytics server
     * @return true if socket can be created else false
     */
    public String backendServerExists(String ip, String port) {
        Socket socket = null;
        try {
            socket = new Socket(ip, Integer.parseInt(port));
            return "true";
        } catch (UnknownHostException e) {
            return "false";
        } catch (IOException e) {
            return "false";
        } catch (Exception e) {
            return "false";
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing the server socket when trying to test the connectivity");
                }
            }
        }
    }
}