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
package org.wso2.carbon.mediator.target;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.endpoints.Endpoint;
import org.wso2.carbon.mediator.service.builtin.SequenceMediator;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Properties;

public class TargetMediator extends AbstractListMediator {

    /** An optional To address to be set on the message when handing over to the target */
    private String toAddress = null;

    /** An optional Action to be set on the message when handing over to the target */
    private String soapAction = null;    

    /** The target sequence reference key */
    private String sequenceRef = null;

    /** The inlined target endpoint definition */
    private Endpoint endpoint = null;

    /** The target endpoint reference key */
    private String endpointRef = null;

    public String getToAddress() {
        return toAddress;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public String getSequenceRef() {
        return sequenceRef;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public String getEndpointRef() {
        return endpointRef;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public void setSequenceRef(String sequenceRef) {
        this.sequenceRef = sequenceRef;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void setEndpointRef(String endpointRef) {
        this.endpointRef = endpointRef;
    }

    public String getTagLocalName() {
        return "target";
    }

    public OMElement serialize(OMElement parent) {
        OMElement targetElem = fac.createOMElement("target", synNS);
        if (toAddress != null) {
            targetElem.addAttribute("to", toAddress, nullNS);
        }

        if (soapAction != null) {
            targetElem.addAttribute("soapAction", soapAction, nullNS);
        }

        if (sequenceRef != null) {
            targetElem.addAttribute("sequence", sequenceRef, nullNS);
        }

        if (endpointRef != null) {
            targetElem.addAttribute("endpoint", endpointRef, nullNS);
        }

        if (getList() != null && getList().size() > 0) {
            List<Mediator> list = getList();
            SequenceMediator seq = new SequenceMediator();
            seq.setAnonymous(true);
            for (Mediator m : list) {
                seq.addChild(m);
            }
            seq.serialize(targetElem);
        }

        if (endpoint != null && endpointRef == null) {
            targetElem.addChild(EndpointSerializer.getElementFromEndpoint(endpoint));
        }

        if (parent != null) {
            parent.addChild(targetElem);
        }
        
        return targetElem;
    }

    public void build(OMElement elem) {
        this.getList().clear();
        endpointRef = null;
        sequenceRef = null;
        toAddress = null;
        soapAction = null;
        OMAttribute toAttr = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "to"));
        if (toAttr != null && toAttr.getAttributeValue() != null) {
            toAddress = toAttr.getAttributeValue();
        }

        OMAttribute soapAction = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "soapAction"));
        if (soapAction != null && soapAction.getAttributeValue() != null) {
            this.soapAction = soapAction.getAttributeValue();
        }

        OMAttribute sequenceAttr = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "sequence"));
        if (sequenceAttr != null && sequenceAttr.getAttributeValue() != null) {
            sequenceRef = sequenceAttr.getAttributeValue();
        }

        OMAttribute endpointAttr = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "endpoint"));
        if (endpointAttr != null && endpointAttr.getAttributeValue() != null) {
            endpointRef = endpointAttr.getAttributeValue();
        }

        OMElement sequence = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "sequence"));
        if (sequence != null) {
            SequenceMediator seq = new SequenceMediator();
            seq.setAnonymous(true);
            seq.build(sequence);

            if (seq.getList() != null && seq.getList().size() > 0) {
                for (Mediator m : seq.getList()) {
                    this.addChild(m);
                }
            }
        }

        OMElement endpoint = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "endpoint"));
        if (endpoint != null) {
            this.endpoint = EndpointFactory.getEndpointFromElement(endpoint, true, new Properties());
        }
    }
}
