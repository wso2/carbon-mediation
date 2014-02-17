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
package org.wso2.carbon.mediator.switchm;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.synapse.config.xml.*;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;



public class SwitchMediator extends AbstractListMediator {
    private static final QName CASE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "case");
    private static final QName DEFAULT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "default");

    /** The XPath expression specifying the source element to apply the switch case expressions against   */
    private SynapsePath source = null;

    public SynapsePath getSource() {
        return source;
    }

    public void setSource(SynapsePath source) {
        this.source = source;
    }

    public String getTagLocalName() {
        return "switch";
    }

    public OMElement serialize(OMElement parent) {
        OMElement switchMed = fac.createOMElement("switch", synNS);
        saveTracingState(switchMed, this);

        if (getSource() != null) {
            SynapsePathSerializer.serializePath(getSource(), switchMed, "source");

        } else {
            throw new MediatorException("Invalid switch mediator. Source required");
        }


        serializeChildren(switchMed, getList());

        if (parent != null) {
            parent.addChild(switchMed);
        }
        return switchMed;
    }

    public void build(OMElement elem) {
        if (getList() != null) {
            getList().clear();
        }

        OMAttribute source = elem.getAttribute(ATT_SOURCE);
        if (source == null) {
            String msg = "A 'source' XPath attribute is required for a switch mediator";
            throw new MediatorException(msg);
        } else {
            try {

                setSource(SynapsePathFactory.getSynapsePath(elem, ATT_SOURCE));

            } catch (JaxenException e) {
                String msg = "Invalid XPath for attribute 'source' : " + source.getAttributeValue();
                throw new MediatorException(msg);
            }
        }
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
        addChildren(elem, this);          
    }
}
