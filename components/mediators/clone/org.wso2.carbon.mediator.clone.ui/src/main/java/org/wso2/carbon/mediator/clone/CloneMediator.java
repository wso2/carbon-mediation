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
package org.wso2.carbon.mediator.clone;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import javax.xml.namespace.QName;

public class CloneMediator extends AbstractListMediator {

    private static final QName ATT_CONTPAR = new QName("continueParent");    
    private static final QName ATT_ID = new QName("id");    
    private static final QName ATT_SEQUENTIAL = new QName("sequential"); 
    
    private boolean continueParent = false;
    private String id;
    private boolean sequential = false;
    
    public String getTagLocalName() {
        return "clone";
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
    
    public boolean isContinueParent() {
        return continueParent;
    }

    public void setContinueParent(boolean continueParent) {
        this.continueParent = continueParent;
    }

    public OMElement serialize(OMElement parent) {
        OMElement cloneElem = fac.createOMElement("clone", synNS);
        saveTracingState(cloneElem, this);

        if (continueParent) {
            cloneElem.addAttribute("continueParent", Boolean.toString(true), nullNS);
        }

        if(id !=null) {
        		cloneElem.addAttribute("id", id, nullNS);
        }
        
        cloneElem.addAttribute("sequential", Boolean.toString(sequential), nullNS);
        
        if (getList() != null && getList().size() > 0) {
            serializeChildren(cloneElem, getList());
        }

        // attach the serialized element to the parent if specified
        if (parent != null) {
            parent.addChild(cloneElem);
        }

        return cloneElem;
    }

    public void build(OMElement elem) {
        processAuditStatus(this, elem);

        OMAttribute contParent = elem.getAttribute(ATT_CONTPAR);
        OMAttribute idAttr = elem.getAttribute(ATT_ID);
        
        if (contParent != null) {
            this.continueParent = Boolean.valueOf(contParent.getAttributeValue());
        }
        
        if(idAttr !=null) {
        		this.id =idAttr.getAttributeValue();
        }
        
        OMAttribute sequentialAttr = elem.getAttribute(ATT_SEQUENTIAL);   
        if(sequentialAttr !=null) {
        		this.sequential=Boolean.valueOf(sequentialAttr.getAttributeValue());
        }
        addChildren(elem, this);
    }
}
