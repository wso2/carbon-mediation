/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.message.flow.tracer.services.tenant;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.message.flow.tracer.datastore.MessageFlowTraceDataStore;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class MessageFlowTraceServiceImpl implements MessageFlowTraceService {

    private MessageFlowTraceDataStore tenantTraceStore;

    private int tenantId = MultitenantConstants.SUPER_TENANT_ID;

    private ConfigurationContext configurationContext;

    public MessageFlowTraceServiceImpl(MessageFlowTraceDataStore messageFlowTraceDataStore,
                                       int tenantId, ConfigurationContext configurationContext) {
        tenantTraceStore = messageFlowTraceDataStore;
        this.tenantId = tenantId;
        this.configurationContext = configurationContext;
    }

    public MessageFlowTraceDataStore getTraceDataStore() {
        return tenantTraceStore;
    }

    public int getTenantId() {
        return tenantId;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }
}
