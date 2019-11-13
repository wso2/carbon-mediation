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
package org.wso2.carbon.mediator.transform.xml;

import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.Mediator;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.wso2.carbon.mediator.transform.Input;
import org.wso2.carbon.mediator.transform.Output;
import org.wso2.carbon.mediator.transform.SmooksMediator;

public class SmooksMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator mediator) {
        assert mediator instanceof SmooksMediator : "Smooks mediator is expected";

        SmooksMediator smooksMediator = (SmooksMediator) mediator;
        OMElement smooks = fac.createOMElement("smooks", synNS);

        smooks.addAttribute(fac.createOMAttribute("config-key", nullNS, smooksMediator.getConfigKey()));
        
        if (smooksMediator.getPersistenceUnitName() != null) {
        	smooks.addAttribute(fac.createOMAttribute("persistence-unit", nullNS, smooksMediator.getPersistenceUnitName()));
        }
    
        smooks.addChild(createInput(smooksMediator.getInput()));
        smooks.addChild(createOutput(smooksMediator.getOutput()));
        serializeComments(smooks, smooksMediator.getCommentsList());
        return smooks;
    }

    private OMElement createInput(Input input) {
        OMElement inputElement = fac.createOMElement("input", synNS);

        if (input.getType() == SmooksMediator.TYPES.TEXT) {
            inputElement.addAttribute(fac.createOMAttribute("type", nullNS, "text"));
        } else if (input.getType() == SmooksMediator.TYPES.XML) {
            inputElement.addAttribute(fac.createOMAttribute("type", nullNS, "xml"));
        }

        if (input.getExpression() != null) {
            SynapseXPathSerializer.serializeXPath(input.getExpression(), inputElement, "expression");
        }

        return inputElement;
    }

    private OMElement createOutput(Output output) {
        OMElement outputElement = fac.createOMElement("output", synNS);

        if (output.getType() == SmooksMediator.TYPES.TEXT) {
            outputElement.addAttribute(fac.createOMAttribute("type", nullNS, "text"));
        } else if (output.getType() == SmooksMediator.TYPES.XML) {
            outputElement.addAttribute(fac.createOMAttribute("type", nullNS, "xml"));
        } else if (output.getType() == SmooksMediator.TYPES.JAVA) {
        	outputElement.addAttribute(fac.createOMAttribute("type", nullNS, "java"));
        }

        if (output.getExpression() != null) {
            SynapseXPathSerializer.serializeXPath(output.getExpression(), outputElement, "expression");
        }

        if (output.getProperty() != null) {
            outputElement.addAttribute(fac.createOMAttribute("property", nullNS, output.getProperty()));
        }

        if (output.getAction() != null) {
            outputElement.addAttribute(fac.createOMAttribute("action", nullNS, output.getAction()));
        }

        return outputElement;
    }

    public String getMediatorClassName() {
        return SmooksMediator.class.getName();
    }
}