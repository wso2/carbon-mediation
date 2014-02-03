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


package org.wso2.carbon.mediator.filter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.*;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;
import org.wso2.carbon.mediator.service.MediatorException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FilterMediator extends AbstractListMediator {

    private SynapsePath source = null;
    private Pattern regex = null;
    private SynapsePath xpath = null;
    private String thenKey = null;
    private String elseKey = null;

    public FilterMediator() {
        addChild(new ThenMediator());
        addChild(new ElseMediator());        
    }

    public SynapsePath getSource() {
        return source;
    }

    public void setSource(SynapsePath source) {
        this.source = source;
    }

    public Pattern getRegex() {
        return regex;
    }

    public void setRegex(Pattern regex) {
        this.regex = regex;
    }

    public SynapsePath getXpath() {
        return xpath;
    }

    public void setXpath(SynapsePath xpath) {
        this.xpath = xpath;
    }

    public String getThenKey() {
        return thenKey;
    }

    public void setThenKey(String thenKey) {
        this.thenKey = thenKey;
    }

    public String getElseKey() {
        return elseKey;
    }

    public void setElseKey(String elseKey) {
        this.elseKey = elseKey;
    }

    public String getTagLocalName() {
        return "filter";
    }

    public OMElement serialize(OMElement parent) {
        OMElement filter = fac.createOMElement("filter", synNS);

        if (getSource() != null && getRegex() != null) {
            SynapsePathSerializer.serializePath(getSource(), filter, "source");
            filter.addAttribute(fac.createOMAttribute(
                "regex", nullNS, getRegex().pattern()));
        } else if (getXpath() != null) {
            SynapsePathSerializer.serializePath(getXpath(), filter, "xpath");
        } else {
            // TODO exception
        }

        saveTracingState(filter, this);        
        serializeChildren(filter, getList());

        if (parent != null) {
            parent.addChild(filter);
        }
        return filter;
    }

    public void build(OMElement elem) {
        // REMOVE the existing sub elements
        getList().clear();

        OMAttribute attXpath  = elem.getAttribute(ATT_XPATH);
        OMAttribute attSource = elem.getAttribute(ATT_SOURCE);
        OMAttribute attRegex  = elem.getAttribute(ATT_REGEX);

        if (attXpath != null) {
            if (attXpath.getAttributeValue() != null &&
                attXpath.getAttributeValue().trim().length() == 0) {
                //TODO error
            } else {
                try {
                    setXpath(SynapsePathFactory.getSynapsePath(elem, ATT_XPATH));
                } catch (JaxenException e) {
                    // TODO error
                }
            }
        } else if (attSource != null && attRegex != null) {
            if ((attSource.getAttributeValue() != null &&
                attSource.getAttributeValue().trim().length() == 0) || (attRegex.getAttributeValue()
                != null && attRegex.getAttributeValue().trim().length() == 0) ){
                //TODO error
            } else {
                try {
                    setSource(SynapsePathFactory.getSynapsePath(elem, ATT_SOURCE));
                } catch (JaxenException e) {
                    // TODO error
                }
                try {
                    setRegex(Pattern.compile(attRegex.getAttributeValue()));
                } catch (PatternSyntaxException pse) {
                    // TODO error
                }
            }
        } else {
            // TODO error
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);        
        addChildren(elem, this);

        boolean thenPresent = false;
        boolean elsePresent = false;
        for (Mediator m : getList()) {
            if (m instanceof ThenMediator) {
                thenPresent = true;
            } else if (m instanceof ElseMediator) {
                elsePresent = true;
            }
        }
        if (!thenPresent) {
            if (elsePresent) {
                throw new MediatorException("Else without Then, Invalid syntax.");
            }
            ThenMediator thenMediator = new ThenMediator();
            for (Mediator m : getList()) {
                thenMediator.addChild(m);
            }
            getList().clear();
            addChild(thenMediator);
            addChild(new ElseMediator());
        }
    }
}
