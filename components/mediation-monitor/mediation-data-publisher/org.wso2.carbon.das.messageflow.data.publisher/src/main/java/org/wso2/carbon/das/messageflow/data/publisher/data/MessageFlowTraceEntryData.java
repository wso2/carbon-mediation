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



import java.util.ArrayList;
import java.util.List;

public class MessageFlowTraceEntryData {

    private String messageId;
    private String entryType;
    private String messageFlow;
    private String timeStamp;

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageFlow() {
        return messageFlow;
    }

    public void setMessageFlow(String messageFlow) {
        this.messageFlow = messageFlow;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public List<Object> getPayloadData() {
        List<Object> payloadData = new ArrayList<Object>();

        payloadData.add(this.getMessageId());
        payloadData.add(this.getEntryType());
        payloadData.add(this.getMessageFlow());
        payloadData.add(this.getTimeStamp());

        return payloadData;
    }
}
