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
package org.wso2.carbon.mediator.smooks.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;

import javax.xml.namespace.QName;


public class SmooksMediator extends AbstractMediator {
    private String configKey = null;

    private static final QName ATT_CONFIG_KEY = new QName("config-key");

    private String inputType = "xml";

    private SynapseXPath inputExpression = null;

    private String outputType = "xml";

    private SynapseXPath outputExpression = null;

    private String outputProperty = null;

    private String outputAction = null;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getTagLocalName() {
        return "smooks"; 
    }

    public OMElement serialize(OMElement parent) {
        OMElement smooksEle = fac.createOMElement("smooks", synNS);
        saveTracingState(smooksEle, this);

        if (configKey != null) {
            smooksEle.addAttribute(fac.createOMAttribute(
                    "config-key", nullNS, configKey));
        } else {
            throw new MediatorException("config-key not specified");
        }

        smooksEle.addChild(serializeInput());
        smooksEle.addChild(serializeOutput());

        if (parent != null) {
            parent.addChild(smooksEle);
        }
        return smooksEle;
    }

    public void build(OMElement elem) {
        OMAttribute key = elem.getAttribute(ATT_CONFIG_KEY);

        if (key == null) {
            String msg = "The 'config-key' attribute is required";
            throw new MediatorException(msg);
        }
        this.configKey = key.getAttributeValue();

        OMElement inputElement = elem.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "input"));
        if (inputElement != null) {
            processInput(inputElement);
        }

        OMElement outputElement = elem.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "output"));
        if (inputElement != null) {
            processOutput(outputElement);
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
    }

    private void processInput(OMElement input) {
        OMAttribute typeAttr = input.getAttribute(new QName("type"));

        if (typeAttr != null) {
            String typeValue = typeAttr.getAttributeValue();
            setInputType(typeValue);
        }

        if (input.getAttribute(new QName("expression")) != null) {
            try {
                setInputExpression(SynapseXPathFactory.getSynapseXPath(input, new QName("expression")));
            } catch (JaxenException ignored) {
            }
        }
    }

    private void processOutput(OMElement output) {
        OMAttribute typeAttr = output.getAttribute(new QName("type"));

        if (typeAttr != null) {
            String typeValue = typeAttr.getAttributeValue();
            setOutputType(typeValue);
        }

        if (output.getAttribute(new QName("expression")) != null) {
            try {
                setOutputExpression(SynapseXPathFactory.getSynapseXPath(output, new QName("expression")));
            } catch (JaxenException ignored) {
            }

            OMAttribute actionAttr = output.getAttribute(new QName("action"));
            if (actionAttr != null && actionAttr.getAttributeValue() != null) {
                setOutputAction(actionAttr.getAttributeValue());
            }
        }

        OMAttribute propertyAttr = output.getAttribute(new QName("property"));
        if (propertyAttr != null && propertyAttr.getAttributeValue() != null) {
            setOutputProperty(propertyAttr.getAttributeValue());
        }
    }

    private OMElement serializeInput() {
        OMElement inputElement = fac.createOMElement("input", synNS);

        inputElement.addAttribute(fac.createOMAttribute("type", nullNS, inputType));


        if (getInputExpression() != null) {
            SynapseXPathSerializer.serializeXPath(getInputExpression(), inputElement, "expression");
        }

        return inputElement;
    }

    private OMElement serializeOutput() {
        OMElement outputElement = fac.createOMElement("output", synNS);
        outputElement.addAttribute(fac.createOMAttribute("type", nullNS, outputType));


        if (getOutputExpression() != null) {
            SynapseXPathSerializer.serializeXPath(getOutputExpression(), outputElement, "expression");
            if (getOutputAction() != null) {
                outputElement.addAttribute(fac.createOMAttribute("action", nullNS, getOutputAction()));
            }
        } else if (getOutputProperty() != null) {
            outputElement.addAttribute(fac.createOMAttribute("property", nullNS, getOutputProperty()));
        }

        return outputElement;
    }

    public String getInputType() {
        return inputType;
    }

    public SynapseXPath getInputExpression() {
        return inputExpression;
    }

    public String getOutputType() {
        return outputType;
    }

    public SynapseXPath getOutputExpression() {
        return outputExpression;
    }

    public String getOutputProperty() {
        return outputProperty;
    }

    public String getOutputAction() {
        return outputAction;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public void setInputExpression(SynapseXPath inputExpression) {
        this.inputExpression = inputExpression;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public void setOutputExpression(SynapseXPath outputExpression) {
        this.outputExpression = outputExpression;
    }

    public void setOutputProperty(String outputProperty) {
        this.outputProperty = outputProperty;
    }

    public void setOutputAction(String outputAction) {
        this.outputAction = outputAction;
    }
}