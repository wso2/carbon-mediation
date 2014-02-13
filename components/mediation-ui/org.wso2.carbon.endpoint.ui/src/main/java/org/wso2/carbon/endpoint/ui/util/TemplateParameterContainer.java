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
package org.wso2.carbon.endpoint.ui.util;

import java.util.HashMap;
import java.util.Map;

public class TemplateParameterContainer {

    private Map<EndpointDefKey ,String> templateMappings = new HashMap<EndpointDefKey ,String>();

    public enum EndpointDefKey{
        address, reliableMessagingOn, addressingOn, addressingVersion, securityOn, wsRMPolicyKey,
        wsSecPolicyKey, inboundWsSecPolicyKey, outboundWsSecPolicyKey, useSeparateListener,
        optimize, format, charSetEncoding,  retryDurationOnTimeout,
        timeoutDuration, timeoutAction, initialSuspendDuration, suspendProgressionFactor,
        suspendMaximumDuration, suspendErrorCodes, timeoutErrorCodes, retriesOnTimeoutBeforeSuspend,
        separateListener, policy, retryDisabledErrorCodes
    }

    public void addTemplateMapping(EndpointDefKey key, String value) {
        templateMappings.put(key, value);
    }

    public String getTemplateMapping(EndpointDefKey key) {
        return templateMappings.get(key);
    }

    public boolean contains(EndpointDefKey key){
        return templateMappings.containsKey(key);
    }
}
