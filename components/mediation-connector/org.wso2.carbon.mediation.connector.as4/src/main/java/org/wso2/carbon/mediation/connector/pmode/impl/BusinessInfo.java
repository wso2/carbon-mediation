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

package org.wso2.carbon.mediation.connector.pmode.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Jaxb bean class for <BusinessInfo></BusinessInfo> element.
 */
@XmlRootElement(name = "BusinessInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class BusinessInfo {

    @XmlElement(name="Service")
    private String service;

    @XmlElement(name="Action")
    private String action;

    @XmlElement(name="PayloadProfile")
    private PayloadProfile payloadProfile;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public PayloadProfile getPayloadProfile() {
        return payloadProfile;
    }

    public void setPayloadProfile(PayloadProfile payloadProfile) {
        this.payloadProfile = payloadProfile;
    }
}
