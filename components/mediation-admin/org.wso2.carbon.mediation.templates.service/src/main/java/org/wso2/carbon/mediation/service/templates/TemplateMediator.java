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
package org.wso2.carbon.mediation.service.templates;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.service.builtin.SequenceMediator;
import org.wso2.carbon.mediator.service.ui.Mediator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TemplateMediator extends SequenceMediator {
    private static final QName TEMPLATE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "template");
    private static final QName TEMPLATE_BODY_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "sequence");
    public static final QName PARAMETER_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "parameter");

    private Collection<String> paramNames = new ArrayList<String>() ;

    private String eipPatternName;

    public void setParameters(Collection<String> paramNames) {
        this.paramNames = paramNames;
    }

    public void addParameter(String paramName){
        if (paramName!=null && !"".equals(paramName)) {
            this.paramNames.add(paramName);
        }
    }

    public Collection<String> getParameters() {
        return paramNames;
    }

    public void setName(String name) {
        this.eipPatternName = name;
    }

    public String getName() {
        return eipPatternName;
    }

    public OMElement serialize(OMElement parent) {
        TemplateMediator mediator = this;
        OMElement templateElem = fac.createOMElement("template", synNS);

        if (mediator.getName() != null) {
            templateElem.addAttribute(fac.createOMAttribute(
                    "name", nullNS, mediator.getName()));

            if (mediator.getErrorHandler() != null) {
                templateElem.addAttribute(fac.createOMAttribute(
                        "onError", nullNS, mediator.getErrorHandler()));
            }
            serializeParams(templateElem,mediator);
            serializeBody(templateElem, mediator.getList());
            saveTracingState(templateElem, mediator);
        }

        return templateElem;
    }

    private void serializeParams(OMElement templateElem, TemplateMediator mediator) {
        Collection<String> params = mediator.getParameters();
        Iterator<String> paramIterator = params.iterator();
        while (paramIterator.hasNext()){
            String paramName = paramIterator.next();
            if(!"".equals(paramName)){
                OMElement paramEl = fac.createOMElement("parameter", synNS);
                paramEl.addAttribute(fac.createOMAttribute("name",nullNS,paramName));
                templateElem.addChild(paramEl);
            }
        }
    }

    private void serializeBody(OMElement templateElem, List<Mediator> childMediatorList){
        OMElement seqEl = fac.createOMElement("sequence", synNS);
        templateElem.addChild(seqEl);
        serializeChildren(seqEl, childMediatorList);
    }

    public void build(OMElement elem) {
        OMElement templateElem = elem;
        OMAttribute nameAttr = templateElem.getAttribute(ATT_NAME);
        OMAttribute errorHandlerAttr = templateElem.getAttribute(ATT_ONERROR);
        if (nameAttr != null) {
            setName(nameAttr.getAttributeValue());
            if (errorHandlerAttr != null) {
                setErrorHandler(errorHandlerAttr.getAttributeValue());
            }
            processAuditStatus(this, templateElem);
            initParameters(templateElem);
            OMElement templateBodyElem = templateElem.getFirstChildWithName(TEMPLATE_BODY_Q);
            addChildren(templateBodyElem, this);
        } else {
            String msg = "A EIP template should be a named mediator .";
        }
    }

    private void initParameters(OMElement templateElem) {
        Iterator subElements = templateElem.getChildElements();
        Collection<String> paramNames = new ArrayList<String>();
        while (subElements.hasNext()) {
            OMElement child = (OMElement) subElements.next();
            if (child.getQName().equals(PARAMETER_Q)) {
                OMAttribute paramNameAttr = child.getAttribute(ATT_NAME);
                if (paramNameAttr != null) {
                    paramNames.add(paramNameAttr.getAttributeValue());
                }
                child.detach();
            }
        }
        this.setParameters(paramNames);
    }

}
