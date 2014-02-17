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
package org.wso2.carbon.mediator.calltemplate;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.calltemplate.util.Value;
import org.wso2.carbon.mediator.calltemplate.util.ValueFactory;
import org.wso2.carbon.mediator.calltemplate.util.ValueSerializer;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class CallTemplateMediator extends AbstractMediator {
    /**
     * refers to the target template this is going to invoke
     * this is a read only attribute of the mediator
     */
    private String targetTemplate;

    /**
     * maps each parameter name to a Expression/Value
     * this is a read only attribute of the mediator
     */
    private Map<String, Value> pName2ExpressionMap;

    public static final QName WITH_PARAM_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "with-param");

    public static final String INVOKE_N = "call-template";

    public CallTemplateMediator() {
        pName2ExpressionMap = new LinkedHashMap<String, Value>();
    }

    public String getTargetTemplate() {
        return targetTemplate;
    }

    public void setTargetTemplate(String targetTemplate) {
        this.targetTemplate = targetTemplate;
    }

    public Map<String, Value> getpName2ExpressionMap() {
        return pName2ExpressionMap;
    }

    public void addExpressionForParamName(String pName, Value expr) {
        pName2ExpressionMap.put(pName, expr);
    }

    public String getTagLocalName() {
        return "call-template";
    }


    public OMElement serialize(OMElement parent) {

        OMElement callTemplateElem = fac.createOMElement(INVOKE_N, synNS);

        if (this.getTargetTemplate() != null) {
            callTemplateElem.addAttribute(fac.createOMAttribute(
                    "target", nullNS, this.getTargetTemplate()));

            serializeParams(callTemplateElem, this);
            saveTracingState(callTemplateElem, this);
        }

        if (parent != null) {
            parent.addChild(callTemplateElem);
        }
        return callTemplateElem;
    }

    private void serializeParams(OMElement invokeElem, CallTemplateMediator mediator) {
        Map<String, Value> paramsMap = mediator.getpName2ExpressionMap();
        Iterator<String> paramIterator = paramsMap.keySet().iterator();
        while (paramIterator.hasNext()) {
            String paramName = paramIterator.next();
            if (!"".equals(paramName)) {
                OMElement paramEl = fac.createOMElement(WITH_PARAM_Q.getLocalPart(),
                                                        synNS);
                paramEl.addAttribute(fac.createOMAttribute("name", nullNS, paramName));
                //serialize value attribute
                Value value = paramsMap.get(paramName);
                new ValueSerializer().serializeValue(value, "value", paramEl);
                invokeElem.addChild(paramEl);
            }
        }

    }


    public void build(OMElement elem) {
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
        OMAttribute targetTemplateAttr = elem.getAttribute(ATT_TARGET);
        if (targetTemplateAttr != null) {
            this.setTargetTemplate(targetTemplateAttr.getAttributeValue());
            buildParameters(elem);
        } else {
            String msg = "CAll template mediator should have a target template specified.";
//            log.error(msg);
            throw new SynapseException(msg);
        }
    }

    private void buildParameters(OMElement elem) {
        Iterator subElements = elem.getChildElements();
        while (subElements.hasNext()) {
            OMElement child = (OMElement) subElements.next();
            if (child.getQName().equals(WITH_PARAM_Q)) {
                OMAttribute paramNameAttr = child.getAttribute(ATT_NAME);
                Value paramValue = new ValueFactory().createValue("value", child);
                if (paramNameAttr != null) {
                    //set parameter value
                    this.addExpressionForParamName(paramNameAttr.getAttributeValue(), paramValue);
                }
            }
        }

    }

}
