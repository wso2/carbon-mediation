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
package org.wso2.carbon.mediator.transaction;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;

public class TransactionMediator extends AbstractMediator {

    public static final String ACTION_COMMIT = "commit";
    public static final String ACTION_ROLLBACK = "rollback";
    public static final String ACTION_SUSPEND = "suspend";
    public static final String ACTION_RESUME = "resume";
    public static final String ACTION_NEW = "new";
    public static final String ACTION_USE_EXISTING_OR_NEW = "use-existing-or-new";
    public static final String ACTION_FAULT_IF_NO_TX = "fault-if-no-tx";
    public static final String SUSPENDED_TRANSACTION = "suspendedTransaction";

    private String action = "";

    public OMElement serialize(OMElement parent) {
        OMElement transaction = fac.createOMElement("transaction", synNS);
        transaction.addAttribute(fac.createOMAttribute("action", nullNS, action));

        saveTracingState(transaction, this);

        if (parent != null) {
            parent.addChild(transaction);
        }
        return transaction;
    }

    public void build(OMElement elem) {
        OMAttribute action
                = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "action"));

        if (action == null) {
            throw new MediatorException("The 'action' attribute " +
                    "is required for Transaction mediator definition");
        } else {

            // after successfully creating the mediator
            // set its common attributes such as tracing etc
            processAuditStatus(this, elem);
            this.action = action.getAttributeValue();
        }        
    }

    public String getTagLocalName() {
        return "transaction";  
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
