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
package org.wso2.carbon.business.messaging.salesforce.mediator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.business.messaging.salesforce.mediator.constants.SalesforceMedatorConstants;
import org.apache.synapse.Mediator;

/**
 * <p>
 * Factory for {@link SalesforceMediator} instances.Builds the
 * <code>SalesforceMediator</code> using a configuration similar to following
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
 */
public class SalesforceMediatorBuilder {

    private static final Log log = LogFactory.getLog(SalesforceMediatorBuilder.class);

    /*
      * (non-Javadoc)
      *
      * @see
      * org.apache.synapse.config.xml.MediatorFactory#createMediator(org.apache
      * .axiom.om.OMElement)
      */

    /**
     * populate a given mediator with the configuration provided
     *
     * @param elem     Salesforce configuration Element
     * @param mediator
     */
    public void buildMediator(OMElement elem, Mediator mediator) {

        if (!SalesforceMedatorConstants.QNAME_SALESFORCE.equals(elem.getQName())) {
            handleException("Unable to create the Salesforce mediator. "
                            + "Unexpected element as the Salesforce mediator configuration");
        }


        if (!(mediator instanceof SalesforceMediator)) {
            handleException("Unsupported mediator passed in for serialization : "
                            + mediator.getClass());
        }

        SalesforceMediator salesforceMediator = (SalesforceMediator) mediator;

        OMElement configElt = elem.getFirstChildWithName(SalesforceMedatorConstants.QNAME_CONFIG);
        if (configElt != null) {

            OMAttribute axis2xmlAttr = configElt.getAttribute(SalesforceMedatorConstants.QNAME_ATT_AXIS2XML);
            OMAttribute repoAttr = configElt.getAttribute(SalesforceMedatorConstants.QNAME_ATT_REPOSITORY);

            if (axis2xmlAttr != null
                && axis2xmlAttr.getAttributeValue() != null) {
                salesforceMediator
                        .setAxis2xml(axis2xmlAttr.getAttributeValue());
            }

            if (repoAttr != null && repoAttr.getAttributeValue() != null) {
                salesforceMediator.setClientRepository(repoAttr
                        .getAttributeValue());
            }
        }
        salesforceMediator.setOperation(createOperation(getOperationElement(elem)));
    }

    /**
     * @param parent
     * @return
     */
    private OMElement getOperationElement(OMElement parent) {
        Iterator children = parent.getChildElements();
        while (children.hasNext()) {
            OMElement child = (OMElement) children.next();
            QName qName = child.getQName();
            OMAttribute attrType = child.getAttribute(SalesforceMedatorConstants.QNAME_ATT_TYPE);
            if (!qName.equals(SalesforceMedatorConstants.QNAME_CONFIG) && attrType != null) {
                return child;
            }
        }
        return null;
    }

    /**
     * @param parent
     * @param inputElems
     * @param outputElems
     */
    private void registerParamElements(OMElement parent, List<OMElement> inputElems,
                                       List<OMElement> outputElems) {
        Iterator children = parent.getChildElements();
        while (children.hasNext()) {
            OMElement child = (OMElement) children.next();
            QName qName = child.getQName();
            OMAttribute attrSource = child.getAttribute(SalesforceMedatorConstants.QNAME_ATT_SOURCE_VALUE);
            OMAttribute attrSourceXpath = child.getAttribute(SalesforceMedatorConstants.QNAME_ATT_SOURCE_XPATH);
            OMAttribute attrCollection = child.getAttribute(SalesforceMedatorConstants.QNAME_ATT_COLLECTION);
            OMAttribute attrComplex = child.getAttribute(SalesforceMedatorConstants.QNAME_ATT_COMPLEX);

            if (outputElems != null && qName.equals(SalesforceMedatorConstants.QNAME_ELEM_KEY)) {
                outputElems.add(child);
            } else if (inputElems != null && (attrSource != null || attrSourceXpath != null ||
                                              attrComplex != null || attrCollection != null)) {
                inputElems.add(child);
            }
        }
    }

    /**
     * This method parses the operation Element and builds
     * salesforce operation type.
     *
     * @param operationElement
     * @return
     */
    private OperationType createOperation(OMElement operationElement) {
        if (null == operationElement) {
            handleException("SalesforceMediator without an operation element has been found, "
                            + "but it is required to have an operation element for SalesforceMediator");
        }
        String opName = operationElement.getLocalName();
        OperationType operation = new OperationType();
        operation.setName(opName);
        OMAttribute type = operationElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_TYPE);
        operation.setType(type.getAttributeValue());
        OMElement inputWrapperElement = operationElement
                .getFirstChildWithName(SalesforceMedatorConstants.QNAME_INPUT_WRAPPER);

        if (inputWrapperElement != null) {
            if (inputWrapperElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_NAME) == null
                || inputWrapperElement.getAttributeValue(SalesforceMedatorConstants.QNAME_ATT_NAME) == null) {
                handleException("Operton with an input-wrapper element that has no name attribute, "
                                + "but it is required to have the name attribute for input-wrapper in an operation");
            }

            if (inputWrapperElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_NAMESPACE) == null
                || inputWrapperElement
                    .getAttributeValue(SalesforceMedatorConstants.QNAME_ATT_NAMESPACE) == null) {
                handleException("Operton with an input-wrapper element that has no namespace attribute, "
                                + "but it is required to have the namespace attribute for input-wrapper in an operation");
            }
            if (inputWrapperElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_NAMESPACE_PREFIX) == null
                || inputWrapperElement
                    .getAttributeValue(SalesforceMedatorConstants.QNAME_ATT_NAMESPACE_PREFIX) == null) {
                handleException("Operton with an input-wrapper element that has no ns-prifix attribute, "
                                + "but it is required to have the ns-prifix attribute for input-wrapper in an operation");
            }

            operation.setInputWrapperName(inputWrapperElement
                    .getAttributeValue(SalesforceMedatorConstants.QNAME_ATT_NAME));
            operation.setInputWrapperNameNS(inputWrapperElement
                    .getAttributeValue(SalesforceMedatorConstants.QNAME_ATT_NAMESPACE));
            operation.setInputWrapperNameNSPrefix(inputWrapperElement
                    .getAttributeValue(SalesforceMedatorConstants.QNAME_ATT_NAMESPACE_PREFIX));
        }

        List<OMElement> inputList = new ArrayList<OMElement>();
        List<OMElement> outputList = new ArrayList<OMElement>();
        registerParamElements(operationElement, inputList, outputList);

        if (inputList.size() > 0) {
            operation.setInputs(createInputs(inputList));
        }
        if (outputList.size() > 0) {
            operation.setOutputs(createOutputs(outputList));
        }

        return operation;
    }

    /**
     * This method parses the operation inputs.
     *
     * @param inputElements
     */
    @SuppressWarnings("unchecked")
    public List<InputType> createInputs(List<OMElement> inputElements) {

        List<InputType> inputs = new ArrayList<InputType>();
        // Handling the inputs
        Iterator<OMElement> inputElems = inputElements.iterator();

        while (inputElems.hasNext()) {
            inputs.add(createInputType(inputElems.next()));
        }

        return inputs;
    }


    /**
     * This method builds the following from an <code>OMElement</code>.
     * <p/>
     * <pre>
     * &lt;input source-xpath=&quot;expression&quot; type=[xml|string] mode=[single|list]| name=&quot;string&quot; source-value=&quot;value&quot; /&gt;
     * </pre>
     *
     * @param inputElement
     * @return
     */
    @SuppressWarnings("unchecked")
    public InputType createInputType(OMElement inputElement) {

        InputType input = new InputType();
        String parameterName = inputElement.getLocalName();
        OMAttribute srcXPathAttr = inputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_SOURCE_XPATH);
        OMAttribute srcValueAttr = inputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_SOURCE_VALUE);
        // Handling the collection properties
        OMAttribute collectionAttr = inputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_COLLECTION);
        OMAttribute complexAttr = inputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_COMPLEX);
        OMAttribute type = inputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_TYPE);
        if (type != null) {
            input.setType(type.getAttributeValue());
        }

        if (parameterName == null
            || "".equals(parameterName.trim())) {
            handleException("Property without the name attribute has been found, but it is required to have the name attribute for all properties");
        }

        input.setName(parameterName);

        if (collectionAttr != null
            && "true".equals(collectionAttr.getAttributeValue())) {
            input.setCollection(true);
            createInlineInputs(input, inputElement);
        } else if (complexAttr != null
                   && "true".equals(complexAttr.getAttributeValue())) {
            //TODO create a complex type setter
            input.setComplex(true);
            createInlineInputs(input, inputElement);
        } else {
            if ((srcXPathAttr == null || srcXPathAttr.getAttributeValue() == null)
                && (srcValueAttr == null || srcValueAttr.getAttributeValue() == null)) {
                handleException(String.format("Property %s: has no source-xpath or source-value " +
                                              "attribute,but required to have source-xpath or source-value attribute for all inputs",
                                              input.getName()));
            }
            if (srcXPathAttr != null && srcXPathAttr.getAttributeValue() != null) {
                try {
                    input.setSourceXPath(SynapseXPathFactory
                            .getSynapseXPath(inputElement, SalesforceMedatorConstants.QNAME_ATT_SOURCE_XPATH));
                } catch (JaxenException e) {
                    handleException(String.format("Input parameter %s: couldn't build the source-xpath " +
                                                  "from the expression: %s", input.getName(), srcXPathAttr
                            .getAttributeValue()));
                }
            } else {
                input.setSourceValue(srcValueAttr.getAttributeValue());
            }

        }
        return input;
    }


    private void createInlineInputs(InputType input, OMElement inputElement) {
        List<OMElement> inputList = new ArrayList<OMElement>();
        registerParamElements(inputElement, inputList, null);
        Iterator<OMElement> inlineInputs = inputList.iterator();

        while (inlineInputs.hasNext()) {
            input.getCollectionInputs().add(createInputType(inlineInputs.next()));
        }
    }

    /**
     * This method parses the operation outputs.
     *
     * @param outputsElements
     */
    @SuppressWarnings("unchecked")
    private List<OutputType> createOutputs(List<OMElement> outputsElements) {

        List<OutputType> outputs = new ArrayList<OutputType>();
        // Handling the outputs
        Iterator<OMElement> outputElems = outputsElements.iterator();

        while (outputElems.hasNext()) {
            outputs.add(createOutputType(outputElems.next()));
        }
        return outputs;
    }

    /**
     * This method builds the following from an <code>OMElement</code>.
     * <p/>
     * <pre>
     * &lt;output source-xpath=&quot;expression&quot; type=[xml|string] mode=[single|list]| name=&quot;string&quot; source-value=&quot;value&quot; /&gt;
     * </pre>
     *
     * @param outputElement
     * @return
     */
    private OutputType createOutputType(OMElement outputElement) {

        OutputType output = new OutputType();
        output.setName(outputElement.getLocalName());
        OMAttribute typeAttr = outputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_TYPE);
        OMAttribute targetKeyAttr = outputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_TARGET_KEY);
        OMAttribute targetXPathAttr = outputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_TARGET_XPATH);
        OMAttribute sourceXPathAttr = outputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_SOURCE_XPATH);
        boolean hasValidationFailed = true;

        if (targetKeyAttr != null && targetKeyAttr.getAttributeValue() != null) {
            output.setTargetKey(targetKeyAttr.getAttributeValue());
            hasValidationFailed = false;
        }
        if (targetXPathAttr != null && targetXPathAttr.getAttributeValue() != null) {
            try {
                output.setTargetXpath(SynapseXPathFactory.getSynapseXPath(outputElement, SalesforceMedatorConstants.QNAME_ATT_TARGET_XPATH));
                hasValidationFailed = false;
            } catch (JaxenException e) {
                handleException(String.format("Input parameter %s: couldn't build the target-xpath " +
                                              "from the expression: %s", output.getName(), targetXPathAttr
                        .getAttributeValue()));
            }
        }
        if (sourceXPathAttr != null && sourceXPathAttr.getAttributeValue() != null) {
            try {
                output.setSourceXpath(SynapseXPathFactory.getSynapseXPath(outputElement, SalesforceMedatorConstants.QNAME_ATT_SOURCE_XPATH));
                hasValidationFailed = false;
            } catch (JaxenException e) {
                handleException(String.format("Input parameter %s: couldn't build the source-xpath " +
                                              "from the expression: %s", output.getName(), targetXPathAttr
                        .getAttributeValue()));
            }
        }
        if (hasValidationFailed) {
            handleWarning("Output without key attribute, "
                          + "but it is required to have key attribute for all inputs");
        }

        if (typeAttr != null && typeAttr.getAttributeValue() != null) {
            output.setType(typeAttr.getAttributeValue());
        }

        return output;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.apache.synapse.config.xml.MediatorFactory#getTagQName()
      */

    public QName getTagQName() {
        return SalesforceMedatorConstants.QNAME_SALESFORCE;
    }

    /**
     * @param exceptionMsg
     */
    private void handleException(String exceptionMsg) {
        throw new SynapseException(exceptionMsg);
    }

    /**
     * @param warningMsg
     */
    private void handleWarning(String warningMsg) {
        log.warn(warningMsg);
    }
}
