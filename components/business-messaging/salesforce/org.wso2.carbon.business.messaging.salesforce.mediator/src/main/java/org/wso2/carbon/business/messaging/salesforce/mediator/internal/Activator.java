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
package org.wso2.carbon.business.messaging.salesforce.mediator.internal;

import java.rmi.RemoteException;

import org.wso2.carbon.business.messaging.salesforce.stub.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.business.messaging.salesforce.core.SalesforceProxy;

import org.wso2.carbon.business.messaging.salesforce.stub.sobject.Account;
import org.wso2.carbon.business.messaging.salesforce.stub.sobject.SObject;

public class Activator implements BundleActivator {

    private static final Log log = LogFactory.getLog(Activator.class);

    private SalesforceProxy proxy;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */

    public void start(BundleContext context) throws Exception {

        ServiceReference reference = context.getServiceReference(SalesforceProxy.class.getName());
        proxy = (SalesforceProxy) context.getService(reference);
        callProxy();
        if (log.isInfoEnabled()) {
            log.info("SalesforceMediator started...");
        }
    }

    private void callProxy() throws Exception, InvalidIdFault, UnexpectedErrorFault,
            LoginFault, InvalidSObjectFault, MalformedQueryFault, InvalidFieldFault, InvalidQueryLocatorFault {

        boolean b = proxy.login("fazlan@wso.com", "0okm9ijn@BLanIlOPNIBcQd7HweKCnmxL");
        if (b) {
            QueryResult qr = proxy.query(
                    "select Name, numberOfEmployees, Id, Industry from Account");
            if (log.isDebugEnabled()) {
                log.debug("Query has " + qr.getSize() + " records total");
            }

            SObject[] sObjects = qr.getRecords();

            for (int i = 0; i < sObjects.length; i++) {
                Account sObject = (Account) sObjects[i];
                if (log.isDebugEnabled()) {
                    log.debug(i + "\t: [" + sObject.getId() + "][" + sObject.getName() + "]");
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(b);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        log.info("SalesforceMediator closed...");
    }

}
