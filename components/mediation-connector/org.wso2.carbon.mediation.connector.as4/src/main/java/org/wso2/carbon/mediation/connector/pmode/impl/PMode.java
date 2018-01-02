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
 * Jaxb bean class for <PMode></PMode> element.
 */
@XmlRootElement(name = "PMode")
@XmlAccessorType(XmlAccessType.FIELD)
public class PMode {
    @XmlElement(name="ID")
    private String id;

    @XmlElement(name="Agreement")
    private Agreement agreement;

    @XmlElement(name="MEP")
    private String mep;

    @XmlElement(name="MEPbinding")
    private String mepBinding;

    @XmlElement(name="Responder")
    private Responder responder;

    @XmlElement(name="Initiator")
    private Initiator initiator;

    @XmlElement(name="Protocol")
    private Protocol protocol;

    @XmlElement(name="BusinessInfo")
    private BusinessInfo businessInfo;

    @XmlElement(name="PayloadService")
    private PayloadService payloadService;

    @XmlElement(name="ErrorHandling")
    private ErrorHandling errorHandling;

    @XmlElement(name="ReceptionAwareness")
    private ReceptionAwareness receptionAwareness;

    @XmlElement(name="Security")
    private Security security;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Agreement getAgreement() {
        return agreement;
    }

    public void setAgreement(Agreement agreement) {
        this.agreement = agreement;
    }

    public String getMep() {
        return mep;
    }

    public void setMep(String mep) {
        this.mep = mep;
    }

    public String getMepBinding() {
        return mepBinding;
    }

    public void setMepBinding(String mepBinding) {
        this.mepBinding = mepBinding;
    }

    public Responder getResponder() {
        return responder;
    }

    public void setResponder(Responder responder) {
        this.responder = responder;
    }

    public Initiator getInitiator() {
        return initiator;
    }

    public void setInitiator(Initiator initiator) {
        this.initiator = initiator;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public BusinessInfo getBusinessInfo() {
        return businessInfo;
    }

    public void setBusinessInfo(BusinessInfo businessInfo) {
        this.businessInfo = businessInfo;
    }

    public PayloadService getPayloadService() {
        return payloadService;
    }

    public void setPayloadService(PayloadService payloadService) {
        this.payloadService = payloadService;
    }

    public ErrorHandling getErrorHandling() {
        return errorHandling;
    }

    public void setErrorHandling(ErrorHandling errorHandling) {
        this.errorHandling = errorHandling;
    }

    public ReceptionAwareness getReceptionAwareness() {
        return receptionAwareness;
    }

    public void setReceptionAwareness(ReceptionAwareness receptionAwareness) {
        this.receptionAwareness = receptionAwareness;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }
}
