/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.business.messaging.salesforce.mediator.ui;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.wso2.carbon.business.messaging.salesforce.mediator.ui.constants.SalesforceMedatorConstants;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.Mediator;

/**
 * <p>
 * Factory for {@link SalesforceMediator} instances.Serializes the
 * <code>SalesforceMediator</code> to a configuration similar to following
 * </p>
 * <p/>
 * <pre>
 * <?xml version="1.0" encoding="UTF-8"?>
 * <sequence xmlns="http://synapse.apache.org/ns/2010/04/configuration" name="sforce_sequence">
 * ..
 * <salesforce>
 * <configuration repository="" axis2xml=""/>?
 * <{login} type="{Login}">
 * <{username} xmlns:ns="http://wso2.services.sample" xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope" xmlns:ns3="http://org.apache.synapse/xsd" source-xpath="//ns:login/ns:username"/>
 * <{password} xmlns:ns="http://wso2.services.sample" xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope" xmlns:ns3="http://org.apache.synapse/xsd" source-xpath="//ns:login/ns:password"/>
 * <key type="{LoginResponse}"/>
 * <key type="{LoginResponse}"/>
 * </{login}>?
 * </salesforce>
 * ...
 * </sequence>
 * </pre>
 * <p/>
 * pal&gt;
 */
public class SalesforceMediatorSerializer {

    private OMFactory fac;
    private OMNamespace synNS, nullNS;
    private Log log = LogFactory.getLog(SalesforceMediatorSerializer.class);

    public SalesforceMediatorSerializer(OMFactory factory, OMNamespace synapseNS,
                                        OMNamespace nullNS) {
        fac = factory;
        synNS = synapseNS;
        this.nullNS = nullNS;
    }

    public OMElement serializeMediator(OMElement parent, Mediator mediator) {

        if (!(mediator instanceof SalesforceMediator)) {
            handleException("Unsupported mediator passed in for serialization : "
                            + mediator.getClass());
        }

        SalesforceMediator salesforceMediator = (SalesforceMediator) mediator;

        OMElement salesforceElem = fac.createOMElement(SalesforceMedatorConstants.TAG_SALESFORCE, synNS);

        if (salesforceMediator.getClientRepository() != null
            || salesforceMediator.getAxis2xml() != null) {
            OMElement config = fac.createOMElement(SalesforceMedatorConstants.TAG_CONFIGURATION, synNS);
            if (salesforceMediator.getClientRepository() != null) {
                config.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_REPOSITORY, nullNS,
                                                          salesforceMediator.getClientRepository()));
            }
            if (salesforceMediator.getAxis2xml() != null) {
                config.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_AXIS2XML, nullNS,
                                                          salesforceMediator.getAxis2xml()));
            }
            salesforceElem.addChild(config);
        }

        salesforceElem.addChild(serializeOperation(salesforceMediator.getOperation()));

        if (parent != null) {
            parent.addChild(salesforceElem);
        }

        return salesforceElem;
    }

    /**
     * serialize a mediator OperationType
     *
     * @param operation
     * @return serialized salesforce config Input Element <{op_name} .. > ...</ {op_name}}>
     */
    private OMElement serializeOperation(OperationType operation) {

        if (operation == null) {
            handleException("SalesforceMediator without an operation has been found, "
                            + "when serializing the SalesforceMediator");
        }

        OMElement operationElem = fac.createOMElement(operation.getName(), synNS);

        if (operation.getName() == null) {
            handleException("Operation without the name attribute has been found, "
                            + "when serializing the SalesforceMediator");
        }
        String type = operation.getType();
        if (null == type || "".equals(type)) {
            handleException("Operation without the type attribute has been found, SalesforceMediator serialization failed");
        }
        operationElem.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_TYPE, nullNS,
                                                         operation.getType()));

        if (operation.getInputWrapperName() != null) {
            if (operation.getInputWrapperNameNS() == null) {
                handleException("Operation with " + SalesforceMedatorConstants.TAG_INPUT_WRAPPER + " element without a namespace attribute has been found, "
                                + "when serializing the SalesforceMediator");
            }
            if (operation.getInputWrapperNameNSPrefix() == null) {
                handleException("Operation with " + SalesforceMedatorConstants.TAG_INPUT_WRAPPER + " element without a ns-prefix attribute has been found, "
                                + "when serializing the SalesforceMediator");
            }

            OMElement inputWrapperElem = fac.createOMElement(SalesforceMedatorConstants.TAG_INPUT_WRAPPER,
                                                             synNS);
            inputWrapperElem.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_NAME, nullNS,
                                                                operation.getInputWrapperName()));
            inputWrapperElem.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_NAMESPACE,
                                                                nullNS, operation.getInputWrapperNameNS()));
            inputWrapperElem.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_NS_PREFIX,
                                                                nullNS, operation.getInputWrapperNameNSPrefix()));
            operationElem.addChild(inputWrapperElem);
        }
        List<InputType> inputs = operation.getInputs();
        List<OutputType> outputs = operation.getOutputs();
        //check for inputs and serialize
        if (inputs != null && !inputs.isEmpty()) {
            for (InputType input : inputs) {
                OMElement childElement = serializeInputType(input);
                if (childElement != null) {
                    operationElem.addChild(childElement);
                }
            }
        }
        //check for outputs and serialize
        if (outputs != null && !outputs.isEmpty()) {
            for (OutputType output : outputs) {
                OMElement childElement = serializeOutput(output);
                if (null != childElement) {
                    operationElem.addChild(childElement);
                }
            }
        }

        return operationElem;
    }


    /**
     * serialize a mediator Input Type
     *
     * @param inputType
     * @return serialized salesforce config Input Element <{input_name}}>...</ {input_name}}>
     */
    private OMElement serializeInputType(InputType inputType) {

        if (inputType.getName() == null) {
            handleException("Property without the name attribute has been found, SalesforceMediator serialization failed");
        }

        OMElement inputParameterElem = fac.createOMElement(inputType.getName(), synNS);

        if (inputType.getType() != null) {
            inputParameterElem.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_TYPE,
                                                                  nullNS, inputType.getType()));
        }

        if (inputType.isCollection()) {
            //if input is an array
            inputParameterElem.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_COLLECTION,
                                                                  nullNS, "true"));
            if (!inputType.getCollectionInputs().isEmpty()) {
                //get each array element and serialize
                for (InputType input : inputType.getCollectionInputs()) {
                    inputParameterElem.addChild(serializeInputType(input));
                }
            }
        } else if (inputType.isComplex()) {
            //if input is a complex parameter
            inputParameterElem.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_COMPLEX,
                                                                  nullNS, "true"));
            if (!inputType.getCollectionInputs().isEmpty()) {
                //get each inline element and serialize
                for (InputType input : inputType.getCollectionInputs()) {
                    inputParameterElem.addChild(serializeInputType(input));
                }
            }
        } else {
            //input is a simple type ie:-String
            if (inputType.getSourceXPath() == null
                && inputType.getSourceValue() == null) {
                handleException(String.format(
                        "Property %s: has no source-xpath or source-value attribute, "
                        + "SalesforceMediator serialization failed",
                        inputType.getName()));
            }

            if (inputType.getSourceXPath() != null) {
                SynapseXPathSerializer.serializeXPath(
                        inputType.getSourceXPath(), inputParameterElem,
                        SalesforceMedatorConstants.ATTR_SOURCE_XPATH);
            } else if (inputType.getSourceValue() != null) {
                inputParameterElem.addAttribute(fac.createOMAttribute(
                        SalesforceMedatorConstants.ATTR_SOURCE_VALUE, nullNS, inputType.getSourceValue()));
            }

        }
        return inputParameterElem;
    }


    /**
     * serialize a given Output type
     *
     * @param output
     * @return serialized salesforce config Output Element <key ... />
     */
    private OMElement serializeOutput(OutputType output) {
        OMElement parameterElem = fac.createOMElement(output.getName(), synNS);
        boolean hasKeyValidationFailed = true;
        if (output.getTargetKey() != null) {
            parameterElem.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_TARGET_KEY, nullNS,
                                                             output.getTargetKey()));
            hasKeyValidationFailed = false;
        }

        if (output.getSourceXPath() != null) {
            SynapseXPathSerializer.serializeXPath(output.getSourceXPath(),
                                                  parameterElem, SalesforceMedatorConstants.ATTR_SOURCE_XPATH);
            hasKeyValidationFailed = false;

        }

        if (output.getTargetXPath() != null) {
            SynapseXPathSerializer.serializeXPath(output.getTargetXPath(),
                                                  parameterElem, SalesforceMedatorConstants.ATTR_TARGET_XPATH);
            hasKeyValidationFailed = false;
        }

        if (hasKeyValidationFailed) {
            handleWarning("Output type without key (target-key | source-xpath | target-xpath ) " +
                          "attribute, but it is required to have key attribute for all outputs");
        }

        if (output.getType() != null) {
            parameterElem.addAttribute(fac.createOMAttribute(SalesforceMedatorConstants.ATTR_TYPE,
                                                             nullNS, output.getType()));
        } else {
            handleWarning("it is required to have type attribute for all outputs");
        }

        return parameterElem;
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * org.apache.synapse.config.xml.MediatorSerializer#getMediatorClassName()
      */

    public String getMediatorClassName() {
        return SalesforceMediator.class.getName();
    }

    /**
     * @param exceptionMsg
     */
    private void handleException(String exceptionMsg) {
        throw new MediatorException(exceptionMsg);
    }

    /**
     * @param warningMsg
     */
    private void handleWarning(String warningMsg) {
        log.warn(warningMsg);
    }
}
