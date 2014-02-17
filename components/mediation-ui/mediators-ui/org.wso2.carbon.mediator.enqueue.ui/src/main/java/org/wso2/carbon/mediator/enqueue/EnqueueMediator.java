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
package org.wso2.carbon.mediator.enqueue;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;

import javax.xml.namespace.QName;


public class EnqueueMediator extends AbstractMediator {
    private String executor;

    private int priority = 0;

    private String sequence;

    public String getExecutor() {
        return executor;
    }

    public int getPriority() {
        return priority;
    }

    public String getSequence() {
        return sequence;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public OMElement serialize(OMElement parent) {
        OMElement enqueue = fac.createOMElement("enqueue", synNS);
        saveTracingState(enqueue, this);

        if (executor != null) {
            enqueue.addAttribute(fac.createOMAttribute(
                    "executor", nullNS, executor));
        } else {
            throw new MediatorException("executor not specified");
        }


        enqueue.addAttribute(fac.createOMAttribute(
                "priority", nullNS, Integer.toString(priority)));


        if (sequence != null) {
            enqueue.addAttribute(fac.createOMAttribute("sequence", nullNS, sequence));
        } else {
            throw new MediatorException("sequence not specified");
        }

        if (parent != null) {
            parent.addChild(enqueue);
        }
        return enqueue;
    }

    public void build(OMElement elem) {
        OMAttribute sequence = elem.getAttribute(new QName("sequence"));
        OMAttribute priority = elem.getAttribute(new QName("priority"));
        OMAttribute executor = elem.getAttribute(new QName("executor"));

        if (sequence == null) {
            String msg = "The 'sequence' attribute is required for the " +
                    "configuration of a enqueue mediator";
            throw new MediatorException(msg);
        }
        this.sequence = sequence.getAttributeValue();

        if (priority == null) {
            String msg = "The 'priority' attribute is required for the " +
                    "configuration of a enqueue mediator";
            throw new MediatorException(msg);
        }
        this.priority = Integer.parseInt(priority.getAttributeValue());

        if (executor == null) {
            String msg = "The 'executor' attribute is required for the " +
                    "configuration of a enqueue mediator";
            throw new MediatorException(msg);
        }
        this.executor = executor.getAttributeValue();

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
    }

    public String getTagLocalName() {
        return "enqueue";
    }
}
