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
package org.wso2.carbon.mediator.throttle.client;

import org.wso2.carbon.mediation.throttle.stub.ThrottleAdminServiceStub;
import org.wso2.carbon.mediation.throttle.stub.ThrottleComponentExceptionException;
import org.wso2.carbon.mediation.throttle.stub.types.InternalData;
import org.wso2.carbon.mediation.throttle.stub.types.ThrottlePolicy;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.util.*;
import java.rmi.RemoteException;
import javax.servlet.http.HttpServletRequest;


public class ThrottleClient {

    private ThrottleAdminServiceStub stub;

    private ResourceBundle bundle;

    private static final String BUNDLE = "org.wso2.carbon.mediator.throttle.ui.i18n.Resources";

    private static final Log log = LogFactory.getLog(ThrottleClient.class);

    public ThrottleClient(String cookie,
                          String backendServerURL,
                          ConfigurationContext configCtx, Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "ThrottleAdminService";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new ThrottleAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    public void updateBackEnd(HttpServletRequest request) throws AxisFault, NumberFormatException {

        String service = request.getParameter("serviceName");
        String operation = request.getParameter("opName");
        String policyID = request.getParameter("policyID");
        boolean global = false;
        boolean operationLevel = false;
        boolean mediator = policyID != null && !"".equals(policyID);
        boolean engagedAtHigherLevel = false;
        if (!mediator) {
            if (service == null) {
                global = true;
            } else if (service != null && operation != null) {
                operationLevel = true;
            }
        }

        /**
         * If throttling is enabled on the UI, fill the configs in to an
         * Array of InternalData
         */
        try {
            if ("Yes".equals(request.getParameter("enable"))) {
                ArrayList<InternalData> list = new ArrayList<InternalData>();
                String value = "";
                int i = 0;
                while (value != null) {
                    i++;
                    InternalData temp = new InternalData();
                    if (request.getParameter("data" + i + "1") == null) {
                        break;
                    }
                    for (int j = 1; j < 7; j++) {
                        value = request.getParameter("data" + i + "" + j);
                        if (value != null && !value.equals("")) {
                            switch (j) {
                                case 1:
                                    temp.setRange(value);
                                    break;
                                case 2:
                                    temp.setMaxRequestCount(Integer.parseInt(value));
                                    break;
                                case 3:
                                    temp.setUnitTime(Integer.parseInt(value));
                                    break;
                                case 4:
                                    temp.setProhibitTimePeriod(Integer.parseInt(value));
                                    break;
                                case 5:
                                    if (value.equals("Control")) {
                                        temp.setAccessLevel(0);
                                    } else if (value.equals("Deny")) {
                                        temp.setAccessLevel(1);
                                    } else {
                                        temp.setAccessLevel(2);
                                    }
                                    break;
                                case 6:
                                    temp.setRangeType(value);
                                    break;
                                default:
                            }
                        }
                    }
                    list.add(temp);
                }

                InternalData[] data = new InternalData[list.size()];
                for (int p = 0; p < list.size(); p++) {
                    data[p] = list.get(p);
                }

                //Create a ThrottlePolicy object to be sent
                ThrottlePolicy policy = new ThrottlePolicy();
                policy.setEngaged(true);
                policy.setInternalConfigs(data);
                if (request.getParameter("maxAccess") != null &&
                        !request.getParameter("maxAccess").equals("")) {
                    policy.setMaxConcurrentAccesses(Integer.parseInt(request.getParameter("maxAccess")));
                }
                if (global) {
                    stub.globallyEngageThrottling(policy);
                } else if (operationLevel) {
                    engagedAtHigherLevel = stub.engageThrottlingForOperation(policy, service, operation);
                } else if (mediator) {
                    String policyXMl = stub.throttlePolicyToString(policy);
                    if (policyXMl != null && !"".equals(policyXMl)) {
                        Map policyXMLMap = (Map) request.getSession().getAttribute("throttle_policy_map");
                        if (policyXMLMap == null) {
                            policyXMLMap = new HashMap();
                        }
                        policyXMLMap.put(policyID, policyXMl);
                        request.getSession().setAttribute("throttle_policy_map", policyXMLMap);
                    }
                } else {
                    stub.enableThrottling(service, policy);
                }
            } else {
                //If throttling is not enabled, send a disable call
                if (global) {
                    stub.disengageGlobalThrottling();
                } else if (operationLevel) {
                    engagedAtHigherLevel = stub.disengageThrottlingForOperation(service, operation);
                } else {
                    stub.disableThrottling(service);
                }
            }

            String msg;
            if (engagedAtHigherLevel) {
                msg = bundle.getString("throttling.applied.at.higher.level");
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.WARNING, request);
            } else {
                msg = bundle.getString("throttling.successfully.applied");
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
            }

        } catch (NumberFormatException e) {
            String msg = bundle.getString("throttling.numbers.invalid");
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.WARNING, request);
            throw e;
        } catch (Exception e) {
            String msg = bundle.getString("throttling.error.while.communication.with.back.end");
            handleException(msg, e);
        }
    }


    public ThrottlePolicy getExistingConfigs(HttpServletRequest request,
                                             boolean global, boolean operationLevel) throws AxisFault {

        ThrottlePolicy policy = null;
        try {
            String service = request.getParameter("serviceName");
            String operation = request.getParameter("opName");
            String loadDef = request.getParameter("loadDefault");

            ThrottlePolicy defThrottlePolicy= getDefaultThrottlePolicy();
            if (loadDef != null && loadDef.equals("true")) {
                //Set a default policy
                defThrottlePolicy.setEngaged(true);
                policy = defThrottlePolicy;
            } else {
                if (global) {
                    policy = stub.getGlobalPolicyConfigs();
                } else if (operationLevel) {
                    policy = stub.getOperationPolicyConfigs(service, operation);
                } else {
                    policy = stub.getPolicyConfigs(service);
                }
                if (!policy.getEngaged() && policy.getMaxConcurrentAccesses() == 0 &&
                        (policy.getInternalConfigs() == null || policy.getInternalConfigs()[0] == null)) {
                    policy = defThrottlePolicy;
                }
            }
        } catch (Exception e) {
            String msg = bundle.getString("throttling.cannot.get.existing.data");
            handleException(msg, e);
        }
        return policy;
    }

    public ThrottlePolicy toThrottlePolicy(String xml, String loadDefault) throws RemoteException, ThrottleComponentExceptionException {
        if (xml == null || "".equals(xml) || "true".equals(loadDefault)) {
            return getDefaultThrottlePolicy();
        } else {
            return stub.toThrottlePolicy(xml);
        }
    }

    public String throttlePolicyToString(ThrottlePolicy throttlePolicy) throws RemoteException, ThrottleComponentExceptionException {
        return stub.throttlePolicyToString(throttlePolicy);
    }

    private ThrottlePolicy getDefaultThrottlePolicy() {
        ThrottlePolicy defThrottlePolicy = new ThrottlePolicy();
        InternalData[] defData = new InternalData[2];
        defThrottlePolicy.setInternalConfigs(defData);
        defData[0] = new InternalData();
        defData[0].setRange("other");
        defData[0].setRangeType("IP");
        defData[0].setAccessLevel(2);

        defData[1] = new InternalData();
        defData[1].setRange("other");
        defData[1].setRangeType("DOMAIN");
        defData[1].setAccessLevel(2);
        return defThrottlePolicy;

    }
    
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

}
