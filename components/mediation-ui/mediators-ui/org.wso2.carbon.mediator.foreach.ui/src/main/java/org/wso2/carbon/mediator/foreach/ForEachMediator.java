/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediator.foreach;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;

import javax.xml.namespace.QName;
import java.util.List;

public class ForEachMediator extends AbstractListMediator {

    private SynapseXPath expression = null;

    private String sequenceRef = null;

    private String id = null;

    private final String TAG_NAME = "foreach";

    public ForEachMediator() {

    }

    public String getTagLocalName() {
        return TAG_NAME;
    }

    public SynapseXPath getExpression() {
        return expression;
    }

    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }

    public String getSequenceRef() {
        return sequenceRef;
    }

    public void setSequenceRef(String sequenceRef) {
        this.sequenceRef = sequenceRef;
    }

    public OMElement serialize(OMElement parent) {
        OMElement forEachElem = fac.createOMElement(TAG_NAME, synNS);
        saveTracingState(forEachElem, this);

        if (expression != null) {
            SynapseXPathSerializer.serializeXPath(expression, forEachElem, "expression");
        } else {
            throw new MediatorException("Missing expression of the ForEach which is required.");
        }

        if (id != null) {
            forEachElem.addAttribute("id", id, nullNS);
        }

        if (sequenceRef != null) {
            forEachElem.addAttribute("sequence", sequenceRef, nullNS);
        } else if (getList() != null && getList().size() > 0) {
            List<Mediator> list = getList();
            org.wso2.carbon.mediator.service.builtin.SequenceMediator
                    seq = new org.wso2.carbon.mediator.service.builtin.SequenceMediator();
            seq.setAnonymous(true);
            for (Mediator m : list) {
                seq.addChild(m);
            }
            seq.serialize(forEachElem);
        }

        if (parent != null) {
            parent.addChild(forEachElem);
        }

        return forEachElem;
    }

    public void build(OMElement elem) {

        processAuditStatus(this, elem);
        this.getList().clear();
        sequenceRef = null;

        OMAttribute expression = elem.getAttribute(ATT_EXPRN);
        if (expression != null) {
            try {
                this.expression = SynapseXPathFactory.getSynapseXPath(elem, ATT_EXPRN);
            } catch (JaxenException e) {
                throw new MediatorException("Unable to build the ForEach Mediator. " +
                        "Invalid XPath " +
                        expression.getAttributeValue());
            }
        } else {
            throw new MediatorException(
                    "XPath expression is required "
                            + "for a ForEach Mediator under the \"expression\" attribute");
        }

        OMAttribute idAttr = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "id"));
        if (idAttr != null) {
            this.id = idAttr.getAttributeValue();
        }

        OMAttribute sequenceAttr = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "sequence"));
        OMElement sequence;

        if (sequenceAttr != null && sequenceAttr.getAttributeValue() != null) {
            sequenceRef = sequenceAttr.getAttributeValue();
        } else if ((sequence = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "sequence"))) != null) {
            org.wso2.carbon.mediator.service.builtin.SequenceMediator
                    seq = new org.wso2.carbon.mediator.service.builtin.SequenceMediator();
            seq.setAnonymous(true);
            seq.build(sequence);

            if (seq.getList() != null && seq.getList().size() > 0) {
                for (Mediator m : seq.getList()) {
                    this.addChild(m);
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
