/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.business.messaging.salesforce.core.session;

import org.apache.axis2.context.ConfigurationContext;

import java.util.Hashtable;

/**
 * salesforce API session handling is implemented by this Session Manager
 * <p>
 * idea behind this object is to encapsulate how salesforce handle sessions. Session Manager will
 * facilitate session handling for proxy instances that will use same authenticated sessions without
 * requiring additional logins for requests that are coming for same username + password combination
 * ,
 * </p>
 */
public class SessionManager {
    private static volatile SessionManager singleton;

    private static final String SALESFORCE_SESSION = "SALESFORCE_SESSION";

    private Hashtable<String, SalesforceSession> proxyMap;


    private SessionManager() {
        proxyMap = new Hashtable<String, SalesforceSession>();
    }

    public static SessionManager getManager() {
        if (singleton == null) {
            synchronized (SessionManager.class) {
                if (singleton == null) {
                    singleton = new SessionManager();
                    return singleton;
                }
            }
        }
        return singleton;
    }

    public static SalesforceSession createNewSession(String user, String password,
                                                     ConfigurationContext configurationContext) {
        //check if session available in global level
        String key = createKey(user, password);
        SalesforceSession newSession = new SalesforceSession(key);
        newSession.setUsername(user);
        newSession.setPassword(password);
        /*getManager().addSalesforceSession(key, newSession);*/
        return newSession;
    }


    public void addSalesforceSession(String key, SalesforceSession session) {
        if (key != null && session != null && !proxyMap.contains(session)) {
            proxyMap.put(key, session);
        }
    }

    public SalesforceSession getSalesforceSession(String key) {
        return proxyMap.get(key);
    }

    public void removeSession(String user, String password) {
        String key = createKey(user, password);
        removeSession(key);
    }

    public void removeSession(String key) {
        if (key != null) {
            proxyMap.remove(key);
        }
    }

    //TODO implement a hash based key generation

    public static String createKey(String user, String password) {
        if (user != null && password != null) {
            return user + "[:[" + password;
        }
        return null;
    }

}
