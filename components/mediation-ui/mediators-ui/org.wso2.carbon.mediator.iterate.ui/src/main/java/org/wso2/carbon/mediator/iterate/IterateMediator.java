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
package org.wso2.carbon.mediator.iterate;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.SynapsePathFactory;
import org.apache.synapse.config.xml.SynapsePathSerializer;
import org.apache.synapse.config.xml.OMElementUtils;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;

import javax.xml.namespace.QName;

public class IterateMediator extends AbstractListMediator {

    private static final QName ATT_CONTPAR = new QName("continueParent");
    private static final QName ATT_PREPLD = new QName("preservePayload");
    private static final QName ATT_ATTACHPATH = new QName("attachPath");
    private static final QName ATT_ID = new QName("id"); 
    private static final QName ATT_SEQUENTIAL = new QName("sequential");
    
    private boolean continueParent = false;

    private boolean preservePayload = false;

    private SynapsePath expression = null;

    private SynapsePath attachPath = null;
    
    private String id;
    
    private boolean sequential = false;

    private boolean isAttachPathPresent = false;
    
    public IterateMediator() {
        //addChild(new TargetMediator());
    }

    public boolean isContinueParent() {
        return continueParent;
    }

    public boolean isPreservePayload() {
        return preservePayload;
    }

    public SynapsePath getExpression() {
        return expression;
    }

    public SynapsePath getAttachPath() {
        return attachPath;
    }

    public void setContinueParent(boolean continueParent) {
        this.continueParent = continueParent;
    }

    public void setPreservePayload(boolean preservePayload) {
        this.preservePayload = preservePayload;
    }

    public void setExpression(SynapsePath expression) {
        this.expression = expression;
    }

    public void setAttachPath(SynapsePath attachPath) {
        this.attachPath = attachPath;
    }

    public boolean isAttachPathPresent() {
        return isAttachPathPresent;
    }

    public void setAttachPathPresent(boolean attachPathPresent) {
        isAttachPathPresent = attachPathPresent;
    }

    public String getId() {
    		return id;
    }
    public void setId(String id) {
    		this.id = id;
    }
    
    public void setSequential(boolean sequential) {
    		this.sequential = sequential;
    }

    public boolean isSequential() {
    		return sequential;
    }
    
    public String getTagLocalName() {
        return "iterate";
    }

    public OMElement serialize(OMElement parent) {
        OMElement itrElem = fac.createOMElement("iterate", synNS);
        saveTracingState(itrElem, this);

        if (continueParent) {
            itrElem.addAttribute("continueParent", Boolean.toString(true), nullNS);
        }

        if (preservePayload) {
            itrElem.addAttribute("preservePayload", Boolean.toString(true), nullNS);
        }

        if (isAttachPathPresent) {
            SynapsePathSerializer.serializePath(attachPath, itrElem, "attachPath");
        }

        if (expression != null) {
            SynapsePathSerializer.serializePath(expression, itrElem, "expression");
        } else {
            throw new MediatorException("Missing expression of the IterateMediator which is required.");
        }

        if(id != null) {
        	itrElem.addAttribute("id", id, nullNS);
        }
        
		if (sequential) {
			itrElem.addAttribute("sequential",  Boolean.toString(true), nullNS);
       }
        
        serializeChildren(itrElem, getList());

        // attach the serialized element to the parent if specified
        if (parent != null) {
            parent.addChild(itrElem);
        }

        return itrElem;
    }

    public void build(OMElement elem) {
        if (getList() != null) {
            getList().clear();
        }

        processAuditStatus(this, elem);

        OMAttribute idAttr = elem.getAttribute(ATT_ID);        
        if(idAttr !=null) {
        	this.id = idAttr.getAttributeValue();
        }
        
        OMAttribute sequentialAttr = elem.getAttribute(ATT_SEQUENTIAL);   
        if(sequentialAttr !=null) {
        		this.sequential=Boolean.valueOf(sequentialAttr.getAttributeValue());
        }
        
        OMAttribute continueParent = elem.getAttribute(ATT_CONTPAR);
        if (continueParent != null) {
            this.continueParent = Boolean.valueOf(continueParent.getAttributeValue());
        }

        OMAttribute preservePayload = elem.getAttribute(ATT_PREPLD);
        if (preservePayload != null) {
            this.preservePayload = Boolean.valueOf(preservePayload.getAttributeValue());
        }

        OMAttribute expression = elem.getAttribute(ATT_EXPRN);
        if (expression != null) {
            try {
                this.expression = SynapsePathFactory.getSynapsePath(elem, ATT_EXPRN);
            } catch (JaxenException e) {
                throw new MediatorException("Unable to build the IterateMediator. " + "Invalid XPATH " +
                    expression.getAttributeValue());
            }
        } else {
            throw new MediatorException("XPATH expression is required " +
                "for an IterateMediator under the \"expression\" attribute");
        }

        OMAttribute attachPath = elem.getAttribute(ATT_ATTACHPATH);
        if (attachPath != null && !this.isPreservePayload()) {
            throw new MediatorException("Wrong configuration for the iterate mediator :: if the iterator " +
                    "should not preserve payload, then attachPath can not be present");
        }
        try {
            if (attachPath != null) {
                SynapsePath attachSynapsePath = SynapsePathFactory.getSynapsePath(elem, ATT_ATTACHPATH);
                this.isAttachPathPresent = true;
                if (this.expression.getClass() != attachSynapsePath.getClass()) {
                    throw new MediatorException("Wrong configuraton for the iterate mediator :: both expression and " +
                            "attachPath should be either jsonpath or xpath");
                }
                OMElementUtils.addNameSpaces(attachSynapsePath, elem, null);
                this.attachPath = attachSynapsePath;
            } else {
                this.isAttachPathPresent = false;
            }
        } catch (JaxenException e) {
            throw new MediatorException("Unable to build the IterateMediator. Invalid PATH " +
                    attachPath.getAttributeValue());
        }
        OMElement targetElement = elem.getFirstChildWithName(TARGET_Q);
        if (targetElement != null) {
            addChildren(elem, this);
        } else {
            throw new MediatorException("Target for an iterate mediator is required :: missing target");
        }
    }


}
