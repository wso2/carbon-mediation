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
package org.wso2.carbon.rest.api.ui.client;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.authenticator.proxy.AuthenticationAdminClient;
import org.wso2.carbon.core.common.AuthenticationException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;

public class AuthWrapper {

    AuthenticationAdminClient authAdmin;
    private DHttpSession _httpSession;

    public AuthWrapper() throws AxisFault {
        this._httpSession = this.new DHttpSession();
        authAdmin = new AuthenticationAdminClient(null, AuthAdminServiceClient.SERVICE_URL,
                                                  null, _httpSession, false);

    }

    public HttpSession getSession() {
        return _httpSession;
    }

    public boolean login(String user, String pass, String host) throws AuthenticationException {
        return authAdmin.login(user, pass, host);
    }

    public class DHttpSession implements HttpSession {
        private Object session;

        public long getCreationTime() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getId() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public long getLastAccessedTime() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ServletContext getServletContext() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setMaxInactiveInterval(int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public int getMaxInactiveInterval() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public HttpSessionContext getSessionContext() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object getAttribute(String s) {
            return session;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object getValue(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Enumeration getAttributeNames() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String[] getValueNames() {
            return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setAttribute(String s, Object o) {
            session = o;
        }

        public void putValue(String s, Object o) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void removeAttribute(String s) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void removeValue(String s) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void invalidate() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean isNew() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
