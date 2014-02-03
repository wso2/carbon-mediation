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
package org.wso2.carbon.business.messaging.salesforce.mediator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.business.messaging.salesforce.mediator.handler.PropertyHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores Operation type information
 * handles input parameter processing
 */
public class OperationType extends Type {


    /**
     * <p>
     * List of inputs to be accepted by this operation on arrival of a message.
     * </p>
     *
     * @see org.wso2.carbon.business.messaging.salesforce.mediator.ui.salesforce.mediator.Input
     * @see java.util.ArrayList
     */
    private List<InputType> inputs = new ArrayList<InputType>();

    /**
     * <p>
     * List of inputs to be accepted by this operation on arrival of a message.
     * </p>
     *
     * @see org.wso2.carbon.business.messaging.salesforce.mediator.ui.salesforce.mediator.Input
     * @see java.util.ArrayList
     */
    private List<OutputType> outputs = new ArrayList<OutputType>();

    /**
     * Name of the element that wraps the inputs, if required by the operation.
     */
    private String inputWrapperName;

    /**
     * Namespace of the input wrapper element.
     */
    private String inputWrapperNameNS;

    /**
     * The prefix of the input wrapper's namespace.
     */
    private String inputWrapperNameNSPrefix;


    public OperationType() {
    }


    /**
     * Creates an <code>OMElement</code> to represent operation
     *
     * @return
     */
    public OMElement getInputWrapperOMElement() {

        return OMAbstractFactory.getOMFactory().createOMElement(
                inputWrapperName,
                OMAbstractFactory.getOMFactory().createOMNamespace(inputWrapperNameNS,
                                                                   inputWrapperNameNSPrefix));
    }

    /**
     * adds an input to the current input collection
     *
     * @param input
     */
    public void addInput(InputType input) {
        inputs.add(input);
    }

    /**
     * adds an input to the current input collection
     *
     * @param output
     */
    public void addOutput(OutputType output) {
        outputs.add(output);
    }

    /**
     * process this operation
     *
     * @param synCtx
     * @return return corresponding salesforce Operation type
     */
    public Object evaluate(MessageContext synCtx) {

        Object newInstace = PropertyHandler.newInstance(type);

        for (InputType input : inputs) {
            Object value = input.evaluate(synCtx);
            PropertyHandler.setInstanceProperty(input.getName(), value, newInstace);
        }

        return newInstace;
    }


    /**
     * @return the inputs
     */
    public List<InputType> getInputs() {
        return inputs;
    }

    /**
     * @param inputs the inputs to set
     */
    public void setInputs(List<InputType> inputs) {
        this.inputs = inputs;
    }

    /**
     * @return the outputs
     */
    public List<OutputType> getOutputs() {
        return outputs;
    }

    /**
     * @param outputs the outputs to set
     */
    public void setOutputs(List<OutputType> outputs) {
        this.outputs = outputs;
    }

    /**
     * @return the inputWrapperName
     */
    public String getInputWrapperName() {
        return inputWrapperName;
    }

    /**
     * @param inputWrapperName the inputWrapperName to set
     */
    public void setInputWrapperName(String inputWrapperName) {
        this.inputWrapperName = inputWrapperName;
    }

    /**
     * @return the inputWrapperNameNS
     */
    public String getInputWrapperNameNS() {
        return inputWrapperNameNS;
    }

    /**
     * @param inputWrapperNameNS the inputWrapperNameNS to set
     */
    public void setInputWrapperNameNS(String inputWrapperNameNS) {
        this.inputWrapperNameNS = inputWrapperNameNS;
    }

    /**
     * @return the inputWrapperNameNSPrefix
     */
    public String getInputWrapperNameNSPrefix() {
        return inputWrapperNameNSPrefix;
    }

    /**
     * @param inputWrapperNameNSPrefix the inputWrapperNameNSPrefix to set
     */
    public void setInputWrapperNameNSPrefix(String inputWrapperNameNSPrefix) {
        this.inputWrapperNameNSPrefix = inputWrapperNameNSPrefix;
    }

}
