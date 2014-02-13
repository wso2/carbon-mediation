/**
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 **/
package org.wso2.carbon.mediator.rmsequence;

import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.SynapseException;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;

public class RMSequenceMediator extends AbstractMediator {
    private static final QName ATT_CORR = new QName("correlation");
    private static final QName ATT_LASTMSG = new QName("last-message");
    private static final QName ATT_VERSION = new QName("version");
    private static final QName ATT_SINGLE = new QName("single");

    private SynapseXPath correlation = null;
    private SynapseXPath lastMessage = null;
    private Boolean single = null;
    private String version = null;


    public String getTagLocalName() {
        return "RMSequence";
    }

    public OMElement serialize(OMElement parent) {
        OMElement sequence = fac.createOMElement("RMSequence", synNS);
        saveTracingState(sequence, this);

        if(single && correlation != null) {
            throw new MediatorException("Invalid RMSequence mediator. A RMSequence can't have both a "
                    + "single attribute value of true and a correlation attribute specified.");
        }
        if(single && lastMessage != null) {
            throw new MediatorException("Invalid RMSequence mediator. A RMSequence can't have both a "
                    + "single attribute value of true and a last-message attribute specified.");
        }

        if (single) {
            sequence.addAttribute(fac.createOMAttribute("single", nullNS, String.valueOf(single)));
        } else if (correlation != null) {
            SynapseXPathSerializer.serializeXPath(
                correlation, sequence, "correlation");
        } else {
            throw new MediatorException("Invalid RMSequence mediator. Specify a single message sequence "
                    + "or a correlation attribute.");
        }

        if (lastMessage != null) {
            SynapseXPathSerializer.serializeXPath(
                lastMessage, sequence, "last-message");
        }

        if (version != null) {
            sequence.addAttribute(fac.createOMAttribute("version", nullNS, version));
        }

        if (parent != null) {
            parent.addChild(sequence);
        }
        return sequence;
    }

    public void build(OMElement elem) {
        OMAttribute correlation = elem.getAttribute(ATT_CORR);
        OMAttribute lastMessage = elem.getAttribute(ATT_LASTMSG);
        OMAttribute single = elem.getAttribute(ATT_SINGLE);
        OMAttribute version = elem.getAttribute(ATT_VERSION);

        if (single == null && correlation == null) {
            String msg = "The 'single' attribute value of true or a 'correlation' attribute is " +
                "required for the configuration of a RMSequence mediator";
            throw new MediatorException(msg);
        }

        if (correlation != null) {
            if (correlation.getAttributeValue() != null &&
                correlation.getAttributeValue().trim().length() == 0) {
                String msg = "Invalid attribute value specified for correlation";
                throw new MediatorException(msg);

            } else {
                try {
                    this.correlation =
                        SynapseXPathFactory.getSynapseXPath(elem, ATT_CORR);
		    this.single = false;
                } catch (JaxenException e) {
                    String msg = "Invalid XPath expression for attribute correlation : "
                        + correlation.getAttributeValue();
                    throw new MediatorException(msg);
                }
            }
        }

        if (single != null) {
            this.single = Boolean.valueOf(single.getAttributeValue());
        }

        if (this.single && this.correlation != null) {
            String msg = "Invalid RMSequence mediator. A RMSequence can't have both a "
                + "single attribute value of true and a correlation attribute specified.";
            throw new MediatorException(msg);

        } else if (!this.single && correlation == null) {
            String msg = "Invalid RMSequence mediator. A RMSequence must have a "
                + "single attribute value of true or a correlation attribute specified.";
            throw new SynapseException(msg);
        }

        if (lastMessage != null) {
            if (lastMessage.getAttributeValue() != null &&
                lastMessage.getAttributeValue().trim().length() == 0) {
                String msg = "Invalid attribute value specified for last-message";
                throw new MediatorException(msg);

            } else {
                try {
                    this.lastMessage =
                        SynapseXPathFactory.getSynapseXPath(elem, ATT_LASTMSG);
                } catch (JaxenException e) {
                    String msg = "Invalid XPath expression for attribute last-message : "
                        + lastMessage.getAttributeValue();
                    throw new MediatorException(msg);
                }
            }
        }

        if (this.single && this.lastMessage != null) {
            String msg = "Invalid RMSequence mediator. A RMSequence can't have both a "
                + "single attribute value of true and a last-message attribute specified.";
            throw new SynapseException(msg);
        }

        if (version != null) {
            if (!XMLConfigConstants.SEQUENCE_VERSION_1_0.equals(version.getAttributeValue()) &&
                !XMLConfigConstants.SEQUENCE_VERSION_1_1.equals(version.getAttributeValue())) {
                String msg = "Only '" + XMLConfigConstants.SEQUENCE_VERSION_1_0 + "' or '" +
                    XMLConfigConstants.SEQUENCE_VERSION_1_1
                    + "' values are allowed for attribute version for a RMSequence mediator"
                    + ", Unsupported version " + version.getAttributeValue();
                throw new MediatorException(msg);
            }
            this.version = version.getAttributeValue();
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
    }

    public boolean isSingle() {
        return getSingle() != null && getSingle();
    }

    public SynapseXPath getCorrelation() {
        return correlation;
    }

    public void setCorrelation(SynapseXPath correlation) {
        this.correlation = correlation;
    }

    public SynapseXPath getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(SynapseXPath lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Boolean getSingle() {
        return single;
    }

    public void setSingle(Boolean single) {
        this.single = single;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
