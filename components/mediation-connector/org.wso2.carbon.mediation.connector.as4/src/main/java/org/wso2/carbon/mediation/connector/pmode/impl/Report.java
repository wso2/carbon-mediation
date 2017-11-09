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
 * Jaxb bean class for <Report></Report> element.
 */
@XmlRootElement(name = "Report")
@XmlAccessorType(XmlAccessType.FIELD)
public class Report {
    @XmlElement(name="ProcessErrorNotifyProducer")
    private boolean processErrorNotifyProducer;

    @XmlElement(name="DeliveryFailuresNotifyProducer")
    private boolean deliveryFailuresNotifyProducer;

    @XmlElement(name="AsResponse")
    private boolean asResponse;

    public boolean isProcessErrorNotifyProducer() {
        return processErrorNotifyProducer;
    }

    public void setProcessErrorNotifyProducer(boolean processErrorNotifyProducer) {
        this.processErrorNotifyProducer = processErrorNotifyProducer;
    }

    public boolean isDeliveryFailuresNotifyProducer() {
        return deliveryFailuresNotifyProducer;
    }

    public void setDeliveryFailuresNotifyProducer(boolean deliveryFailuresNotifyProducer) {
        this.deliveryFailuresNotifyProducer = deliveryFailuresNotifyProducer;
    }

    public boolean isAsResponse() {
        return asResponse;
    }

    public void setAsResponse(boolean asResponse) {
        this.asResponse = asResponse;
    }
}