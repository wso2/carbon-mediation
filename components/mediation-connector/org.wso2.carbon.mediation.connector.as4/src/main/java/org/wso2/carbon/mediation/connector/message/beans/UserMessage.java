/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.connector.message.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Jaxb bean class for <UserMessage></UserMessage> element.
 */
@XmlRootElement(name="UserMessage")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserMessage {
    @XmlElement(name="MessageInfo")
    private MessageInfo messageInfo;

    @XmlElement(name="PartyInfo")
    private PartyInfo partyInfo;

    @XmlElement(name="CollaborationInfo")
    private CollaborationInfo collaborationInfo;

    @XmlElement(name="PayloadInfo")
    private PayloadInfo payloadInfo;

    public MessageInfo getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(MessageInfo messageInfo) {
        this.messageInfo = messageInfo;
    }

    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    public void setPartyInfo(PartyInfo partyInfo) {
        this.partyInfo = partyInfo;
    }

    public CollaborationInfo getCollaborationInfo() {
        return collaborationInfo;
    }

    public void setCollaborationInfo(CollaborationInfo collaborationInfo) {
        this.collaborationInfo = collaborationInfo;
    }

    public PayloadInfo getPayloadInfo() {
        return payloadInfo;
    }

    public void setPayloadInfo(PayloadInfo payloadInfo) {
        this.payloadInfo = payloadInfo;
    }
}
