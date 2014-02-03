/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.priority.executors.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.priority.executors.stub.PriorityMediationAdminStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.rmi.RemoteException;

public class PriorityAdminClient{

    private static final Log log = LogFactory.getLog(PriorityAdminClient.class);

    private PriorityMediationAdminStub stub;

    public PriorityAdminClient(ServletConfig config, HttpSession session) throws AxisFault {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext)
                config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serviceURL = backendServerURL + "PriorityMediationAdmin";
        stub = new PriorityMediationAdminStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void add(String name, Executor executor) throws RemoteException {
        OMElement e = executor.serialize();
        if (e != null) {
            stub.add(name, e);
        }
    }

    public Executor getExecutor(String name) throws RemoteException {
        OMElement e = stub.getExecutor(name);
        if (e != null) {
            OMElement execElement = e.getFirstElement();
            Executor ex = new Executor();
            ex.build(execElement);
            return ex;
        }
        return null;
    }

    public List<String> getExecutors() throws RemoteException {
        String []list = stub.getExecutorList();

        List<String> l = new ArrayList<String>();
        if (list != null && !(list.length == 1 && list[0] == null)) {
            l.addAll(Arrays.asList(list));
        }

        return l;
    }

    public void update(String name, Executor executor) throws RemoteException {
        OMElement e = executor.serialize();

        stub.update(name, e);
    }

    public void remove(String name) throws RemoteException {
        stub.remove(name);
    }    
}
