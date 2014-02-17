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
import org.apache.synapse.SynapseException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SwitchCaseMediator extends AbstractListMediator {

    private Pattern regex = null;

    public Pattern getRegex() {
        return regex;
    }

    public void setRegex(Pattern regex) {
        this.regex = regex;
    }

    public String getTagLocalName() {
        return "case";
    }

    public OMElement serialize(OMElement parent) {
        OMElement caseElem = fac.createOMElement("case", synNS);
        saveTracingState(caseElem, this);
        if (regex != null) {
            caseElem.addAttribute(fac.createOMAttribute(
                    "regex", nullNS, regex.pattern()));
        } else {
            throw new MediatorException("Invalid switch case. Regex required");
        }
        serializeChildren(caseElem, getList());

        if (parent != null) {
            parent.addChild(caseElem);
        }

        return caseElem;
    }

    public void build(OMElement elem) {
        this.regex = null;
        OMAttribute regex = elem.getAttribute(ATT_REGEX);
        if (regex == null) {
            String msg = "The 'regex' attribute is required for a switch case definition";
            throw new MediatorException(msg);
        }
        try {
            this.regex = Pattern.compile(regex.getAttributeValue());
        } catch (PatternSyntaxException pse) {
            String msg = "Invalid Regular Expression for attribute 'regex' : " + regex.getAttributeValue();
            throw new MediatorException(msg);
        }
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
        addChildren(elem, this);
    }
}
