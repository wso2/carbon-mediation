/*
 * Copyright WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.cloud.gateway.agent.observer;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.gateway.agent.service.CGAgentAdminService;
import org.wso2.carbon.cloud.gateway.common.CGConstant;
import org.wso2.carbon.cloud.gateway.common.CGException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;

/**
 * Observer for backend service changes
 */
public class CGServiceObserver implements AxisObserver {

    private static Log log = LogFactory.getLog(CGServiceObserver.class);

    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        //set tenantIDs for tenant-aware logging
        //better solution may be to improve tenant aware loggers without introducing this
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

        CGAgentAdminService csgAgentAdminService = new CGAgentAdminService();
        try {
            String status = csgAgentAdminService.getServiceStatus(axisService.getName());
            if (status != null && CGConstant.CG_SERVICE_STATUS_AUTO_MATIC.equals(status)) {
                csgAgentAdminService.doServiceUpdate(axisService.getName(), axisEvent.getEventType());
            }
        } catch (CGException e) {
            log.error("Error while updating the service event for the service '" + axisService.getName() + "'");
        }
    }

    public void moduleUpdate(AxisEvent axisEvent, AxisModule axisModule) {
    }

    public void serviceGroupUpdate(AxisEvent axisEvent, AxisServiceGroup axisServiceGroup) {
    }

    public void init(AxisConfiguration axisConfiguration) {
    }

    public void addParameter(Parameter parameter) throws AxisFault {
    }

    public void removeParameter(Parameter parameter) throws AxisFault {
    }

    public void deserializeParameters(OMElement omElement) throws AxisFault {
    }

    public Parameter getParameter(String s) {
        return null;
    }

    public ArrayList<Parameter> getParameters() {
        return null;
    }

    public boolean isParameterLocked(String s) {
        return false;
    }
}
