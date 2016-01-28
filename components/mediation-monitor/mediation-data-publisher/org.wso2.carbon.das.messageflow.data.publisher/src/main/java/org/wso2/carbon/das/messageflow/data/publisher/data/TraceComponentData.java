/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.das.messageflow.data.publisher.data;



import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TraceComponentData {

    private String messageId;
    private String componentId;
    private String componentName;
    private String payload;
    private String timestamp;
    private Boolean response;
    private Boolean start;
    private Map<String, Object> propertyMap;
    private Map<String, Object> transportPropertyMap;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getResponse() {
        return response;
    }

    public void setResponse(Boolean response) {
        this.response = response;
    }

    public Boolean getStart() {
        return start;
    }

    public void setStart(Boolean start) {
        this.start = start;
    }

    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<String, Object> propertyMap) {
        this.propertyMap = propertyMap;
    }

    public Map<String, Object> getTransportPropertyMap() {
        return transportPropertyMap;
    }

    public void setTransportPropertyMap(Map<String, Object> transportPropertyMap) {
        this.transportPropertyMap = transportPropertyMap;
    }

    public List<Object> getPayloadData() {
        List<Object> payloadData = new ArrayList<Object>();

        payloadData.add(this.getMessageId());
        payloadData.add(this.getComponentId());
        payloadData.add(this.getComponentName());
        payloadData.add(this.getPayload());
        payloadData.add(this.getTimestamp());
        payloadData.add(this.getResponse());
        payloadData.add(this.getStart());
        payloadData.add(JSONObject.toJSONString(this.getPropertyMap()));
        payloadData.add(JSONObject.toJSONString(this.getTransportPropertyMap()));

        return payloadData;
    }
}
