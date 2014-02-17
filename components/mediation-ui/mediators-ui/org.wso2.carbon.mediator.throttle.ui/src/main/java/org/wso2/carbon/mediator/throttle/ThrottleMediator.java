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
package org.wso2.carbon.mediator.throttle;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;

import javax.xml.namespace.QName;

public class ThrottleMediator extends AbstractListMediator {
    private String policyKey = null;
    private OMElement inLinePolicy = null;
    private String onRejectSeqKey = null;
    private String onAcceptSeqKey = null;
    private String id;

    public ThrottleMediator() {
        addChild(new OnAcceptMediator());
        addChild(new OnRejectMediator());
    }

    public String getTagLocalName() {
        return "throttle";
    }

    public OMElement serialize(OMElement parent) {
        OMElement throttle = fac.createOMElement("throttle", synNS);
        OMElement policy = fac.createOMElement("policy", synNS);

        if (policyKey != null) {
            policy.addAttribute(fac.createOMAttribute(
                    "key", nullNS, policyKey));
            throttle.addChild(policy);
        } else {
            if (inLinePolicy != null) {
                policy.addChild(inLinePolicy);
                throttle.addChild(policy);
            }
        }
        saveTracingState(throttle, this);

        if (id != null) {
            throttle.addAttribute(fac.createOMAttribute(
                    "id", nullNS, id));
        }

        if (onRejectSeqKey != null) {
            throttle.addAttribute(fac.createOMAttribute(XMLConfigConstants.ONREJECT, nullNS,
                    onRejectSeqKey));
        } else {
            for (Mediator m : getList()) {
                if (m instanceof OnRejectMediator) {
                    m.serialize(throttle);
                }
            }
        }

        if (onAcceptSeqKey != null) {
            throttle.addAttribute(fac.createOMAttribute(XMLConfigConstants.ONACCEPT, nullNS,
                    onAcceptSeqKey));
        } else {
            for (Mediator m : getList())  {
                if (m instanceof OnAcceptMediator) {
                    m.serialize(throttle);
                }
            }
        }
        if (parent != null) {
            parent.addChild(throttle);
        }
        return throttle;
    }

    public void build(OMElement elem) {
        getList().clear();
        this.policyKey = null;
        this.onAcceptSeqKey = null;
        this.onRejectSeqKey = null;
        this.inLinePolicy = null;
        this.id = null;
        
        OMElement policy = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "policy"));
        if (policy != null) {
            OMAttribute key = policy.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));
            if (key != null) {
                String keyValue = key.getAttributeValue();
                if (keyValue != null && !"".equals(keyValue)) {
                    policyKey = keyValue;
                } else {
                    throw new MediatorException("key attribute should have a value ");
                }
            } else {
                OMElement inLine = policy.getFirstElement();
                if (inLine != null) {
                    inLinePolicy = inLine;
                }
            }
        }
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);

        String id = elem.getAttributeValue(new QName(XMLConfigConstants.NULL_NAMESPACE, "id"));
        if (id != null && !"".equals(id)) {
            this.id = id.trim();
        } else {
           throw new MediatorException("Idy attribute must have defined ");
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
    }

    public String getPolicyKey() {
        return policyKey;
    }

    public void setPolicyKey(String policyKey) {
        this.policyKey = policyKey;
    }

    public OMElement getInLinePolicy() {
        return inLinePolicy;
    }

    public void setInLinePolicy(OMElement inLinePolicy) {
        this.inLinePolicy = inLinePolicy;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;        
    }
}
