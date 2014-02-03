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
package org.wso2.carbon.business.messaging.salesforce.mediator.handler;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.business.messaging.salesforce.core.SalesforceProxy;
import org.wso2.carbon.business.messaging.salesforce.core.impl.SalesforceProxyImpl;

public final class SalesforceProxyFactory {

    private static org.wso2.carbon.business.messaging.salesforce.core.SalesforceProxy proxy;
    private static final String SALESFORCE_PROXY_INSTANCE = "SALESFORCE_PROXY_INSTANCE";

    public static SalesforceProxy getSalesforceProxyInstance(
            org.apache.axis2.context.ConfigurationContext configurationContext) {
        return new SalesforceProxyImpl(configurationContext);
    }

    public static SalesforceProxy getSalesforceProxyInstance(
            org.apache.axis2.context.ConfigurationContext configurationContext,
            MessageContext synCtxt) {
        SalesforceProxy proxyInstance = (SalesforceProxy) synCtxt.getProperty(SALESFORCE_PROXY_INSTANCE);
        if (null == proxyInstance) {
            proxyInstance = new SalesforceProxyImpl(configurationContext);
            synCtxt.setProperty(SALESFORCE_PROXY_INSTANCE, proxyInstance);
        }
        return proxyInstance;
    }
}
