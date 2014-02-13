/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.statistics;

import org.apache.synapse.aspects.ComponentType;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MessageTraceLog {

    public static final int FAULT_STATUS_UNDEFINED = -1;
    public static final int FAULT_STATUS_FALSE = 0;
    public static final int FAULT_STATUS_TRUE = 1;

    private String messageId;
    private String resourceId;
    private ComponentType type;
    private int requestFaultStatus = FAULT_STATUS_UNDEFINED;
    private int responseFaultStatus = FAULT_STATUS_UNDEFINED;

    /**
     * User specific Message context properties
     */
    private Map<String,Object> properties = new HashMap<String,Object>();


    MessageTraceLog(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public ComponentType getType() {
        return type;
    }

    public int getRequestFaultStatus() {
        return requestFaultStatus;
    }

    public int getResponseFaultStatus() {
        return responseFaultStatus;
    }

    void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    void setType(ComponentType type) {
        this.type = type;
    }

    void setRequestFaultStatus(int requestFaultStatus) {
        this.requestFaultStatus = requestFaultStatus;
    }

    void setResponseFaultStatus(int responseFaultStatus) {
        this.responseFaultStatus = responseFaultStatus;
    }

    public void addProperty(String name , Object o) {
        properties.put(name,o);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
