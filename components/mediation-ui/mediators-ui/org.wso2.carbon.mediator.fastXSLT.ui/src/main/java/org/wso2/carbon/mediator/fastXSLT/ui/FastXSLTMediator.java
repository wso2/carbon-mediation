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
package org.wso2.carbon.mediator.fastXSLT.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.*;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.util.MediatorProperty;

import javax.xml.namespace.QName;
import java.util.*;


public class FastXSLTMediator extends AbstractMediator {
    private static final QName ATTRIBUTE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attribute");

    private Value xsltKey = null;

    public String getTagLocalName() {
        return "fastXSLT";
    }

    public OMElement serialize(OMElement parent) {
        OMElement fastXSLT = fac.createOMElement("fastXSLT", synNS);

        if (xsltKey != null) {
            // Use KeySerializer to serialize Key
            ValueSerializer keySerializer = new ValueSerializer();
            keySerializer.serializeValue(xsltKey, XMLConfigConstants.KEY, fastXSLT);

        } else {
            throw new MediatorException("Invalid FastXSLT mediator. XSLT registry key is required");
        }
        saveTracingState(fastXSLT, this);

        if (parent != null) {
            parent.addChild(fastXSLT);
        }
        return fastXSLT;
    }

    public void build(OMElement elem) {
        OMAttribute attXslt = elem.getAttribute(ATT_KEY);
        if (attXslt != null) {
            //Use KeyFactory to create Key
            ValueFactory keyFactory = new ValueFactory();
            xsltKey = keyFactory.createValue(XMLConfigConstants.KEY, elem);
        } else {
            throw new MediatorException("The 'key' attribute is required for the FastXSLT mediator");
        }
    }

    public Value getXsltKey() {
        return xsltKey;
    }

    public void setXsltKey(Value xsltKey) {
        this.xsltKey = xsltKey;
    }

}
