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
package org.wso2.carbon.mediator.store;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.SynapsePathFactory;
import org.apache.synapse.config.xml.SynapsePathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.util.MediatorProperty;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;


public class MessageStoreMediator extends AbstractMediator {


    private static final QName STORE_Q    = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "store");
    private static final String  ATT_MESSAGE_STORE   = "messageStore";
    private static final String  ATT_SEQUENCE   = "sequence";

    private static final QName ATT_MESSAGE_STORE_Q   = new QName("messageStore");
    private static final QName ATT_SEQUENCE_Q   = new QName("sequence");

    private String messageStoreName;

    private SynapsePath messageStoreExpression;

    private String sequence;

    private String name;

    public String getTagLocalName() {
        return "store";
    }

    public String getMessageStoreName() {
        return messageStoreName;
    }

    public void setMessageStoreName(String messageStoreName) {
        this.messageStoreName = messageStoreName;
    }

    public SynapsePath getMessageStoreExp() {
        return messageStoreExpression;
    }

    public void setMessageStoreExp(SynapsePath messageStoreExp) {
        this.messageStoreExpression = messageStoreExp;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        if ("".equalsIgnoreCase(sequence)) {
            sequence = null;
        } else {
            this.sequence = sequence;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OMElement serialize(OMElement parent) {
       OMElement storeElem = fac.createOMElement(STORE_Q);

        if(name != null) {
            OMAttribute nameAtt = fac.createOMAttribute("name" , nullNS , name);
            storeElem.addAttribute(nameAtt);
        }

       //In normal operations messageStoreName can't be null
        //But we do a null check here since in run time there can be manuel modifications
        if (messageStoreExpression != null) {
            SynapsePathSerializer.serializePathWithBraces(messageStoreExpression, storeElem, "messageStore");
        } else if(messageStoreName != null ) {
            OMAttribute msName = fac.createOMAttribute(ATT_MESSAGE_STORE ,nullNS,messageStoreName);
            storeElem.addAttribute(msName);
        } else {
            handleException("Can't serialize MessageStore Mediator message store is null ");
        }

        // sequence is an optional parameter
        if(sequence != null) {
            OMAttribute sequenceAtt = fac.createOMAttribute(ATT_SEQUENCE , nullNS ,sequence);
            storeElem.addAttribute(sequenceAtt);
        }


        if (parent != null) {
            parent.addChild(storeElem);
        }
        return storeElem;
    }

    public void build(OMElement elem) {
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);

        OMAttribute msName = elem.getAttribute(ATT_MESSAGE_STORE_Q);
        if(msName == null) {
            String msg = "Name of the Message Store name is a required attribute";
            throw new MediatorException(msg);
        }

        if (hasBraces(msName)) {
            String path = removeBraces(msName);
            try {
                this.messageStoreExpression = SynapsePathFactory.getSynapsePath(elem, path);
            } catch (JaxenException e) {
                String msg = "Invalid XPath expression for attribute 'messageStore' : " + msName.getAttributeValue();
                throw new SynapseException(msg, e);
            }
        } else {
            this.messageStoreName = msName.getAttributeValue();
        }

        OMAttribute sqName = elem.getAttribute(ATT_SEQUENCE_Q);
        if(sqName != null) {
            this.sequence = sqName.getAttributeValue();
        }



    }

    private void handleException(String msg) {
        LogFactory.getLog(this.getClass()).error(msg);
        throw new SynapseException(msg);
    }

    public boolean hasBraces(OMAttribute atr) {
        String trimmed = atr.getAttributeValue().trim();
        return ((trimmed.startsWith("{")) && (trimmed.endsWith("}")));
    }

    public String removeBraces(OMAttribute atr) {
        String trimmed = atr.getAttributeValue().trim();
        String path = trimmed.substring(1, trimmed.length() - 1);
        return path;
    }
}
