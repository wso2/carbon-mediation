/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.proxyadmin.observer;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ParameterObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.ProxyService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.proxyadmin.ProxyAdminException;
import org.wso2.carbon.proxyadmin.service.ProxyServiceAdmin;
import org.wso2.carbon.proxyadmin.util.ConfigHolder;


/**
 * <code>ProxyServiceParameterObserver </code> will sync Axis service level parameter changes
 * with the Synapse proxy service
 */
public class ProxyServiceParameterObserver implements ParameterObserver{

    private AxisService service;
    private int tanentId;

    private static final Log log = LogFactory.getLog(ProxyServiceParameterObserver.class);

    public ProxyServiceParameterObserver(AxisService service) {
        this.service = service;
        this.tanentId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * When an Axis2 Service parameter update happens this method will get notified
     * @param name parameter name
     * @param value parameter value
     */
    public void parameterChanged(String name, Object value) {
        SynapseConfiguration config = ConfigHolder.getInstance().
                getSynapseEnvironmentService(tanentId).getSynapseEnvironment().
                getSynapseConfiguration();

        ProxyService proxy = config.getProxyService(service.getName());
        if("passwordCallbackRef".equals(name)){
        	proxy.setModuleEngaged(true);
        }

        if(proxy != null) {

            if(service.getParameter(name) != null) {
                //if this is a parameter set
                proxy.addParameter(name,value);
            } else {
                //if  this is a parameter remove 
                proxy.getParameterMap().remove(name);
            }

			if (!isSkipPersistenceForParam(name)) {
            	try {
                    new ProxyServiceAdmin().persistProxyService(proxy);
                } catch (ProxyAdminException e) {
                    log.error("Error While persisting proxy information", e);
                }
        	}
        }else {
            log.error("Proxy Service " +name + " does not exist ");
        }
    }

    private boolean isSkipPersistenceForParam(String name) {

        /**
         * org.apache.axis2.context.externalize.AxisServiceName
         * [ESBJAVA-2573]
         * Proxy services redeployed every time when a call is made when using custom axis module.
         *
         * wso2statistics.service.response.time
         * [ESBJAVA-2464] storing service response time as a parameter of the service.
         * After the changes related to ESBJAVA-1683, service information get persisted
         * for every request. So avoiding service response time get persisted for each req.
         *
         * lastUsedTime
         * [ESBJAVA-3729]
         *
         */
        for (String param : skipParams) {
            if (param.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static String[] skipParams = {"wso2statistics.service.response.time",
                                          "org.apache.axis2.context.externalize.AxisServiceName",
                                          "lastUsedTime"};

}
