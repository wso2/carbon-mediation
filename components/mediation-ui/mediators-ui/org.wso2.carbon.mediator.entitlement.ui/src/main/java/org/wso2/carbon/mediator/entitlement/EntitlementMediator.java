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
package org.wso2.carbon.mediator.entitlement;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;

import javax.xml.namespace.QName;

public class EntitlementMediator extends AbstractListMediator {
    private String remoteServiceUserName;
    private String remoteServicePassword;
    private String remoteServiceUrl;
    private String remoteServiceUserNameKey;
    private String remoteServicePasswordKey;
    private String remoteServiceUrlKey;
    private String callbackClass;
    private String thriftHost;
    private String thriftPort;
    private String client;
    private static final QName PROP_NAME_SERVICE_EPR = new QName("remoteServiceUrl");
    private static final QName PROP_NAME_USER = new QName("remoteServiceUserName");
    private static final QName PROP_NAME_PASSWORD = new QName("remoteServicePassword");
    private static final QName PROP_NAME_SERVICE_EPR_KEY = new QName("remoteServiceUrlKey");
    private static final QName PROP_NAME_USER_KEY = new QName("remoteServiceUserNameKey");
    private static final QName PROP_NAME_PASSWORD_KEY = new QName("remoteServicePasswordKey");
    private static final QName PROP_NAME_CALLBACK_CLASS = new QName("callbackClass");
    private static final QName PROP_NAME_THRIFT_HOST = new QName("thriftHost");
    private static final QName PROP_NAME_THRIFT_PORT = new QName("thriftPort");
    private static final QName PROP_NAME_CLIENT_CLASS = new QName("client");
    private static final String ADVICE = "advice";
    private static final String OBLIGATIONS = "obligations";
    private String onRejectSeqKey = null;
    private String onAcceptSeqKey = null;
    private String adviceSeqKey = null;
    private String obligationsSeqKey = null;

    public EntitlementMediator() {
        addChild(new OnAcceptMediator());
        addChild(new OnRejectMediator());
        addChild(new ObligationsMediator());
        addChild(new AdviceMediator());
    }

    /**
     * {@inheritDoc}
     */
    public OMElement serialize(OMElement parent) {
        OMElement entitlementService = fac.createOMElement("entitlementService", synNS);

        if (remoteServiceUrl != null && remoteServiceUrl.trim().length() != 0) {
            entitlementService.addAttribute(fac.createOMAttribute("remoteServiceUrl", nullNS,
                    remoteServiceUrl));
        } else if (remoteServiceUrlKey != null && remoteServiceUrlKey.trim().length() != 0) {
            entitlementService.addAttribute(fac.createOMAttribute("remoteServiceUrlKey", nullNS,
                    remoteServiceUrlKey));
        } else {
            throw new MediatorException(
                    "Invalid Entitlement mediator.Entitlement service epr required");
        }

        if (remoteServiceUserName != null && remoteServiceUserName.trim().length() != 0) {
            entitlementService.addAttribute(fac.createOMAttribute("remoteServiceUserName", nullNS,
                    remoteServiceUserName));
        } else if (remoteServiceUserNameKey != null && remoteServiceUserNameKey.trim().length() != 0) {
            entitlementService.addAttribute(fac.createOMAttribute("remoteServiceUserNameKey", nullNS,
                    remoteServiceUserNameKey));
        } else {
            throw new MediatorException(
                    "Invalid Entitlement mediator. Remote service user name required");
        }

        if (remoteServicePassword != null && remoteServicePassword.trim().length() != 0) {
            entitlementService.addAttribute(fac.createOMAttribute("remoteServicePassword", nullNS,
                    remoteServicePassword));
        } else if (remoteServicePasswordKey != null && remoteServicePasswordKey.trim().length() != 0) {
            entitlementService.addAttribute(fac.createOMAttribute("remoteServicePasswordKey", nullNS,
                    remoteServicePasswordKey));
        } else {
            throw new MediatorException(
                    "Invalid Entitlement mediator. Remote service password required");
        }

        if (callbackClass != null && !"".equalsIgnoreCase(callbackClass)) {
            entitlementService.addAttribute(fac.createOMAttribute("callbackClass", nullNS, callbackClass));
        }

        if (client != null && !"".equalsIgnoreCase(client)) {
            entitlementService.addAttribute(fac.createOMAttribute("client", nullNS, client));
        }

        if (thriftHost != null && !"".equalsIgnoreCase(thriftHost)) {
            entitlementService.addAttribute(fac.createOMAttribute("thriftHost", nullNS, thriftHost));
        }

        if (thriftPort != null && !"".equalsIgnoreCase(thriftPort)) {
            entitlementService.addAttribute(fac.createOMAttribute("thriftPort", nullNS, thriftPort));
        }

        if (onRejectSeqKey != null) {
            entitlementService.addAttribute(fac.createOMAttribute(XMLConfigConstants.ONREJECT, nullNS,
                    onRejectSeqKey));
        } else {
            for (Mediator m : getList()) {
                if (m instanceof OnRejectMediator) {
                    m.serialize(entitlementService);
                }
            }
        }

        if (onAcceptSeqKey != null) {
            entitlementService.addAttribute(fac.createOMAttribute(XMLConfigConstants.ONACCEPT, nullNS,
                    onAcceptSeqKey));
        } else {
            for (Mediator m : getList())  {
                if (m instanceof OnAcceptMediator) {
                    m.serialize(entitlementService);
                }
            }
        }

        if (adviceSeqKey != null) {
            entitlementService.addAttribute(fac.createOMAttribute(ADVICE, nullNS,
                    adviceSeqKey));
        } else {
            for (Mediator m : getList())  {
                if (m instanceof AdviceMediator) {
                    m.serialize(entitlementService);
                }
            }
        }

        if (obligationsSeqKey != null) {
            entitlementService.addAttribute(fac.createOMAttribute(OBLIGATIONS, nullNS,
                    obligationsSeqKey));
        } else {
            for (Mediator m : getList())  {
                if (m instanceof ObligationsMediator) {
                    m.serialize(entitlementService);
                }
            }
        }

        saveTracingState(entitlementService, this);

        if (parent != null) {
            parent.addChild(entitlementService);
        }
        return entitlementService;
    }

    /**
     * {@inheritDoc}
     */
    public void build(OMElement elem) {
        getList().clear();
        OMAttribute attRemoteServiceUri = elem.getAttribute(PROP_NAME_SERVICE_EPR);
        OMAttribute attRemoteServiceUserName = elem.getAttribute(PROP_NAME_USER);
        OMAttribute attRemoteServicePassword = elem.getAttribute(PROP_NAME_PASSWORD);
        OMAttribute attRemoteServiceUriKey = elem.getAttribute(PROP_NAME_SERVICE_EPR_KEY);
        OMAttribute attRemoteServiceUserNameKey = elem.getAttribute(PROP_NAME_USER_KEY);
        OMAttribute attRemoteServicePasswordKey = elem.getAttribute(PROP_NAME_PASSWORD_KEY);
        OMAttribute attCallbackClass = elem.getAttribute(PROP_NAME_CALLBACK_CLASS);
        OMAttribute attThriftHost = elem.getAttribute(PROP_NAME_THRIFT_HOST);
        OMAttribute attThriftPort = elem.getAttribute(PROP_NAME_THRIFT_PORT);
        OMAttribute attClient = elem.getAttribute(PROP_NAME_CLIENT_CLASS);
        this.onAcceptSeqKey = null;
        this.onRejectSeqKey = null;
        this.adviceSeqKey = null;
        this.obligationsSeqKey = null;

        if (attRemoteServiceUri != null) {
            remoteServiceUrl = attRemoteServiceUri.getAttributeValue();
        } else if (attRemoteServiceUriKey != null) {
            setRemoteServiceUrlKey(attRemoteServiceUriKey.getAttributeValue());
        } else {
            throw new MediatorException(
                    "The 'remoteServiceUrl' attribute is required for the Entitlement mediator");
        }

        if (attRemoteServiceUserName != null) {
            remoteServiceUserName = attRemoteServiceUserName.getAttributeValue();
        } else if (attRemoteServiceUserNameKey != null) {
            setRemoteServiceUserNameKey(attRemoteServiceUserNameKey.getAttributeValue());
        } else {
            throw new MediatorException(
                    "The 'remoteServiceUserName' attribute is required for the Entitlement mediator");
        }

        if (attRemoteServicePassword != null) {
            remoteServicePassword = attRemoteServicePassword.getAttributeValue();
        } else if (attRemoteServicePasswordKey != null) {
            setRemoteServicePasswordKey(attRemoteServicePasswordKey.getAttributeValue());
        } else {
            throw new MediatorException(
                    "The 'remoteServicePassword' attribute is required for the Entitlement mediator");
        }

        if (attCallbackClass != null) {
            callbackClass = attCallbackClass.getAttributeValue();
        }

        if (attClient != null) {
            client = attClient.getAttributeValue();
        }

        if (attThriftHost != null) {
            thriftHost = attThriftHost.getAttributeValue();
        }

        if (attThriftPort != null) {
            thriftPort = attThriftPort.getAttributeValue();
        }

        OMAttribute onReject = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, XMLConfigConstants.ONREJECT));
        if (onReject != null) {
            String onRejectValue = onReject.getAttributeValue();
            if (onRejectValue != null) {
                onRejectSeqKey = onRejectValue.trim();
            }
        } else {
            OMElement onRejectMediatorElement = elem.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, XMLConfigConstants.ONREJECT));
            if (onRejectMediatorElement != null) {
                OnRejectMediator onRejectMediator = new OnRejectMediator();
                onRejectMediator.build(onRejectMediatorElement);
                addChild(onRejectMediator);
            }
        }
        OMAttribute onAccept = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, XMLConfigConstants.ONACCEPT));
        if (onAccept != null) {
            String onAcceptValue = onAccept.getAttributeValue();
            if (onAcceptValue != null) {
                onAcceptSeqKey = onAcceptValue;
            }
        } else {
            OMElement onAcceptMediatorElement = elem.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, XMLConfigConstants.ONACCEPT));
            if (onAcceptMediatorElement != null) {
                OnAcceptMediator onAcceptMediator = new OnAcceptMediator();
                onAcceptMediator.build(onAcceptMediatorElement);
                addChild(onAcceptMediator);
            }
        }
        OMAttribute advice = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, ADVICE));
        if (advice != null) {
            String adviceValue = advice.getAttributeValue();
            if (adviceValue != null) {
                adviceSeqKey = adviceValue;
            }
        } else {
            OMElement adviceMediatorElement = elem.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, ADVICE));
            if (adviceMediatorElement != null) {
                AdviceMediator adviceMediator = new AdviceMediator();
                adviceMediator.build(adviceMediatorElement);
                addChild(adviceMediator);
            }
        }
        OMAttribute obligations = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, OBLIGATIONS));
        if (obligations != null) {
            String obligationsValue = obligations.getAttributeValue();
            if (obligationsValue != null) {
                onAcceptSeqKey = obligationsValue;
            }
        } else {
            OMElement obligationsMediatorElement = elem.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, OBLIGATIONS));
            if (obligationsMediatorElement != null) {
                ObligationsMediator obligationsMediator = new ObligationsMediator();
                obligationsMediator.build(obligationsMediatorElement);
                addChild(obligationsMediator);
            }
        }
    }

    public String getRemoteServiceUserName() {
        return remoteServiceUserName;
    }

    public void setRemoteServiceUserName(String remoteServiceUserName) {
        this.remoteServiceUserName = remoteServiceUserName;
    }

    public String getRemoteServicePassword() {
        return remoteServicePassword;
    }

    public void setRemoteServicePassword(String remoteServicePassword) {
        this.remoteServicePassword = remoteServicePassword;
    }

    public String getRemoteServiceUrl() {
        return remoteServiceUrl;
    }

    public void setRemoteServiceUrl(String remoteServiceUrl) {
        this.remoteServiceUrl = remoteServiceUrl;
    }

    public String getCallbackClass() {
        return callbackClass;
    }

    public void setCallbackClass(String callbackClass) {
        this.callbackClass = callbackClass;
    }

    public String getThriftHost() {
        return thriftHost;
    }

    public void setThriftHost(String thriftHost) {
        this.thriftHost = thriftHost;
    }

    public String getThriftPort() {
        return thriftPort;
    }

    public void setThriftPort(String thriftPort) {
        this.thriftPort = thriftPort;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getOnRejectSeqKey() {
        return onRejectSeqKey;
    }

    public void setOnRejectSeqKey(String onRejectSeqKey) {
        this.onRejectSeqKey = onRejectSeqKey;
    }

    public String getOnAcceptSeqKey() {
        return onAcceptSeqKey;
    }

    public void setOnAcceptSeqKey(String onAcceptSeqKey) {
        this.onAcceptSeqKey = onAcceptSeqKey;
    }

    public String getAdviceSeqKey() {
        return adviceSeqKey;
    }

    public void setAdviceSeqKey(String adviceSeqKey) {
        this.adviceSeqKey = adviceSeqKey;
    }

    public String getObligationsSeqKey() {
        return obligationsSeqKey;
    }

    public void setObligationsSeqKey(String obligationsSeqKey) {
        this.obligationsSeqKey = obligationsSeqKey;
    }

    public String getRemoteServiceUserNameKey() {
        return remoteServiceUserNameKey;
    }

    public void setRemoteServiceUserNameKey(String remoteServiceUserNameKey) {
        this.remoteServiceUserNameKey = remoteServiceUserNameKey;
    }

    public String getRemoteServicePasswordKey() {
        return remoteServicePasswordKey;
    }

    public void setRemoteServicePasswordKey(String remoteServicePasswordKey) {
        this.remoteServicePasswordKey = remoteServicePasswordKey;
    }

    public String getRemoteServiceUrlKey() {
        return remoteServiceUrlKey;
    }

    public void setRemoteServiceUrlKey(String remoteServiceUrlKey) {
        this.remoteServiceUrlKey = remoteServiceUrlKey;
    }

    /**
     * {@inheritDoc}
     */
    public String getTagLocalName() {
        return "entitlementService";
    }
}
