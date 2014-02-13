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
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.OMElementUtils;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;
import org.wso2.carbon.mediator.target.TargetMediator;

import javax.xml.namespace.QName;

public class IterateMediator extends AbstractListMediator {

    private static final QName ATT_CONTPAR = new QName("continueParent");
    private static final QName ATT_PREPLD = new QName("preservePayload");
    private static final QName ATT_ATTACHPATH = new QName("attachPath");
    private static final QName ATT_ID = new QName("id"); 
    private static final QName ATT_SEQUENTIAL = new QName("sequential"); 
    
    private boolean continueParent = false;

    private boolean preservePayload = false;

    private SynapseXPath expression = null;

    private SynapseXPath attachPath = null;
    
    private String id;
    
    private boolean sequential = false;
    
    public IterateMediator() {
        //addChild(new TargetMediator());
    }

    public boolean isContinueParent() {
        return continueParent;
    }

    public boolean isPreservePayload() {
        return preservePayload;
    }

    public SynapseXPath getExpression() {
        return expression;
    }

    public SynapseXPath getAttachPath() {
        return attachPath;
    }

    public void setContinueParent(boolean continueParent) {
        this.continueParent = continueParent;
    }

    public void setPreservePayload(boolean preservePayload) {
        this.preservePayload = preservePayload;
    }

    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }

    public void setAttachPath(SynapseXPath attachPath) {
        this.attachPath = attachPath;
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

        if (attachPath != null && !".".equals(attachPath.toString())) {
            SynapseXPathSerializer.serializeXPath(attachPath, itrElem, "attachPath");
        }

        if (expression != null) {
            SynapseXPathSerializer.serializeXPath(expression, itrElem, "expression");
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
                this.expression = SynapseXPathFactory.getSynapseXPath(elem, ATT_EXPRN);
            } catch (JaxenException e) {
                throw new MediatorException("Unable to build the IterateMediator. " + "Invalid XPATH " +
                    expression.getAttributeValue());
            }
        } else {
            throw new MediatorException("XPATH expression is required " +
                "for an IterateMediator under the \"expression\" attribute");
        }

        OMAttribute attachPath = elem.getAttribute(ATT_ATTACHPATH);
        String attachPathValue = "";
        if (attachPath != null && !this.preservePayload) {
            throw new MediatorException("Wrong configuration for the iterate mediator " +
                    ":: if the iterator should not preserve payload, " +
                    "then attachPath can not be present");
        } else if (attachPath != null) {
            attachPathValue = attachPath.getAttributeValue();
        }

        if (!attachPathValue.equals("")) {
            try {
                SynapseXPath xp = new SynapseXPath(attachPathValue);
                OMElementUtils.addNameSpaces(xp, elem, null);
                this.attachPath = xp;
            } catch (JaxenException e) {
                throw new MediatorException("Unable to build the IterateMediator. Invalid XPATH " +
                        attachPathValue);
            }
        }
        OMElement targetElement = elem.getFirstChildWithName(TARGET_Q);
        if (targetElement != null) {
            addChildren(elem, this);
        } else {
            throw new MediatorException("Target for an iterate mediator is required :: missing target");
        }
    }


}
