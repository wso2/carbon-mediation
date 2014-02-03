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
package org.wso2.carbon.business.messaging.salesforce.mediator.ui.factory;

import java.io.FileNotFoundException;
import java.util.*;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.jaxen.JaxenException;
import org.wso2.carbon.business.messaging.salesforce.mediator.ui.*;
import org.wso2.carbon.business.messaging.salesforce.mediator.ui.config.SalesforceUIHandler;
import org.wso2.carbon.business.messaging.salesforce.mediator.ui.constants.SalesforceMedatorConstants;
import org.wso2.carbon.business.messaging.salesforce.mediator.ui.exception.SalesforceUIException;
import org.wso2.carbon.business.messaging.salesforce.mediator.ui.handler.ConfigHandler;

import javax.xml.stream.XMLStreamException;

/**
 * This class a utility class responsible for building mediator + ui types
 * using schema configuration or operations supported
 */
public class OperationBuilder {

    /**
     * <p>
     * Holds the log4j based log for the login purposes
     * </p>
     */
    private static final Log log = LogFactory.getLog(OperationBuilder.class);

    /**
     * Constant for no of default out puts supported
     */
    private static final int DEFAULT_KEYS = 1;

    private SalesforceUIHandler uiHandler ;

    private String opName;

    private OperationType operation;

    private OMElement operationOMElement;


    public OperationBuilder(String operation,SalesforceUIHandler handler) throws SalesforceUIException {
        opName = operation;
        uiHandler = handler;
        parseConfiguration(opName);

    }

    public OperationBuilder(OperationType operation,SalesforceUIHandler handler) throws SalesforceUIException {
        this.operation = operation;
        this.opName = operation.getName();
        uiHandler = handler;
        parseConfiguration(opName);
    }

    private void parseConfiguration(String opName) throws SalesforceUIException{
        try {
            //this will parse configuration only if Config Handler has not been intialized before
            operationOMElement = ConfigHandler.getInstance().parse(opName);
        } catch (FileNotFoundException e) {
            throw new SalesforceUIException("Unable to find UI Configuration File",e);
        } catch (XMLStreamException e) {
            throw new SalesforceUIException("Error parsing UI Configuration File",e);
        } catch (JaxenException e) {
            throw new SalesforceUIException("Unable to parse UI Config for operation artifact  " + opName,e);
        }
    }


    /**
     * Creates an Operation instance that represent a supported operation.
     *
     * @return the Operation instance.
     */
    public OperationType createUIMappedOperation() {

        OperationType operation = new OperationType();
        OperationTypeUI uiOperationType = new OperationTypeUI(operation);
        try {
            OMAttribute typeAttr = operationOMElement.getAttribute(SalesforceMedatorConstants.QNAME_ATTR_TYPE_UI);
            OMAttribute displayAttr = operationOMElement.getAttribute(SalesforceMedatorConstants.QNAME_ATTR_DISP_NAME_UI);

            if (displayAttr != null && displayAttr.getAttributeValue() != null) {
                uiOperationType.setDisplayName(displayAttr.getAttributeValue());
            } else if (log.isDebugEnabled()) {
                log.debug("Display Name not found in the ui descriptor file for operation : " +
                          opName);
            }
            //set operation parameters
            operation.setName(opName);

            if (typeAttr != null && typeAttr.getAttributeValue() != null) {
                operation.setType(typeAttr.getAttributeValue());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Type Name not found in the ui descriptor file for operation : " +
                             opName);
                }
            }
            //populate parameters
            operation.setInputs(createInputs());
            operation.setOutputs(createOutputs());
            //register ui mapping
            uiHandler.setUIMapping(operation, uiOperationType);

        } catch (Exception e) {
            handleException(
                    "An error occured when parsing the configuration file", e);
        }
        return operation;
    }

    /**
     * Create UI options for an existing an Operation instance .
     *
     */
    public void createUIMappedOperationFromExistingModel() {

        OperationTypeUI uiOperationType = new OperationTypeUI(operation);
        try {
            OMAttribute displayAttr = operationOMElement.getAttribute(SalesforceMedatorConstants.QNAME_ATTR_DISP_NAME_UI);

            if (displayAttr != null && displayAttr.getAttributeValue() != null) {
                uiOperationType.setDisplayName(displayAttr.getAttributeValue());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Display Name not found in the ui descriptor file for operation : " +
                             opName);
                }
            }
            //populate input/output ui params
            createUIInputsFrom(operation.getInputs());
            createUIOutputsFrom(operation.getOutputs());
            //register ui mappings
            uiHandler.setUIMapping(operation, uiOperationType);

        } catch (Exception e) {
            handleException(
                    "An error occured when parsing the configuration file", e);
        }
    }


    /**
     * creates input options using descriptor information
     *
     * @return a list of input parameters
     */
    @SuppressWarnings("unchecked")
    private List<InputType> createInputs() {
        List<InputType> inputs = new ArrayList<InputType>();
        OMElement inputElement = operationOMElement.getFirstChildWithName(SalesforceMedatorConstants.QNAME_ELEM_INPUT_UI);
        //for each child element of an operation
        for (Iterator<OMElement> itr = inputElement.getChildrenWithName(SalesforceMedatorConstants.QNAME_ELEM_PROPERTY_UI);
             itr.hasNext();) {
            inputs.add(createTypeFrom(itr.next()));
        }
        return inputs;
    }

    /**
     * creates and maps input ui options for an existing set of input components
     *
     * @param inputs
     */
    private void createUIInputsFrom(List<InputType> inputs) {
        OMElement inputElement = operationOMElement.getFirstChildWithName(SalesforceMedatorConstants.QNAME_ELEM_INPUT_UI);
        Iterator inputsIterator = inputs.iterator();
        //for each input type object try to populate ui options
        while (inputsIterator.hasNext()) {
            InputType input = (InputType) inputsIterator.next();
            for (Iterator<OMElement> itr = inputElement.getChildrenWithName(SalesforceMedatorConstants.QNAME_ELEM_PROPERTY_UI);
                 itr.hasNext();) {
                createUITypeFrom(input, itr.next());
            }
        }
    }


    /**
     * builds an Input Type from descriptor data
     *
     * @param propertyElement
     * @return
     */
    private InputType createTypeFrom(OMElement propertyElement) {

        InputType type = new InputType();
        InputTypeUI uiType = new InputTypeUI(type);
        //check and set parameters for a collection type
        //set ui Only to false since we want to populate all properties
        setCollectionAttributes(propertyElement, type, uiType, false);
        OMAttribute nameAttr = propertyElement.getAttribute(SalesforceMedatorConstants.QNAME_ATTR_NAME_UI);
        if (nameAttr!=null && nameAttr.getAttributeValue()!=null) {
            type.setName(nameAttr.getAttributeValue());
        }
        else{
            handleException("name not found in ui descriptor for input parameter");
        }
        OMAttribute typeAttr = propertyElement.getAttribute(SalesforceMedatorConstants.QNAME_ATTR_TYPE_UI);
        if (typeAttr != null && typeAttr.getAttributeValue() != null) {
            type.setType(typeAttr.getAttributeValue());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Type Name not found in the ui descriptor file for input parameter");
            }
        }

        //set ui attributes for current input type
        setRenderingAttributes(propertyElement, type, uiType);
        return type;
    }

    /**
     * builds an Input UI Type from descriptor data
     *
     * @param input
     * @param propertyElement
     */
    private void createUITypeFrom(InputType input, OMElement propertyElement) {
        OMAttribute nameAttr = propertyElement.getAttribute(SalesforceMedatorConstants.QNAME_ATTR_NAME_UI);
        if (nameAttr!=null && input.getName().equals(nameAttr.getAttributeValue())) {
            InputTypeUI uiType = new InputTypeUI(input);
            //check and set parameters for a collection type
            //set ui Only to true since we want to populate only ui properties
            setCollectionAttributes(propertyElement, input, uiType, true);
            setRenderingAttributes(propertyElement, input, uiType);
        }
    }

    /**
     * @param propertyElement
     * @param type
     * @param uiType
     */
    private void setRenderingAttributes(OMElement propertyElement,InputType type, InputTypeUI uiType) {
        uiType.setRequired(Boolean.parseBoolean(null != propertyElement
                .getAttribute(SalesforceMedatorConstants.QNAME_ATTR_REQUIRED_UI) ? propertyElement.getAttribute(
                SalesforceMedatorConstants.QNAME_ATTR_REQUIRED_UI).getAttributeValue() : "false"));
        OMAttribute displayAttr = propertyElement.getAttribute(SalesforceMedatorConstants.QNAME_ATTR_DISP_NAME_UI);
        if (displayAttr!=null && displayAttr.getAttributeValue()!=null) {
            uiType.setDisplayName(displayAttr.getAttributeValue());
        }else{
            if (log.isDebugEnabled()) {
                log.debug("Display Name no available for input parameter " + type.getName());
            }
        }
        //register ui mappings
        uiHandler.setUIMapping(type, uiType);
    }

    /**
     * This method checks descriptor element for a collection type and assign necessary properties as required
     *
     * @param propertyElement
     * @param type
     * @param uiType
     * @param isUIOnly        use this true if we only want to set
     */
    private void setCollectionAttributes(OMElement propertyElement, InputType type, InputTypeUI uiType
            , boolean isUIOnly) {
        OMAttribute collectionAttr = propertyElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_COLLECTION_UI);
        OMAttribute complexAttr = propertyElement.getAttribute(SalesforceMedatorConstants.QNAME_ATT_COMPLEX_UI);
        if (checkCollectionCondition(type, collectionAttr, isUIOnly)) {
            if (!isUIOnly) {
                type.setCollection(true);
                //handle embedded inputs for collection types
                createInlineTypes(type, propertyElement);
            } else {
                //handle embedded inputs for collection types
                createInlineUITypes(type, propertyElement);
            }
            uiType.setExpandable(true);
        } else if (checkComplexTypeCondition(type, complexAttr, isUIOnly)) {
            if (!isUIOnly) {
                type.setComplex(true);
                //handle embedded inputs for complex types
                createInlineTypes(type, propertyElement);
            } else {
                //handle embedded inputs for complex types
                createInlineUITypes(type, propertyElement);
            }
            uiType.setComplexType(true);
        }
    }

    /**
     * @param collectionAttr
     * @return true if this type is a collection
     *         false otherwise
     */
    private boolean checkCollectionCondition(OMAttribute collectionAttr) {
        boolean flag = null != collectionAttr && "true".equals(collectionAttr.getAttributeValue());

        return flag;
    }

    /**
     * checks condition for a collection type with regard to the current input
     *
     * @param input
     * @param collectionAttr
     * @param isUIOnly       for processing of ui attributes only
     * @return
     */
    private boolean checkCollectionCondition(InputType input, OMAttribute collectionAttr, boolean isUIOnly) {
        if (isUIOnly) {
            return checkCollectionCondition(collectionAttr) && input.isCollection();
        } else {
            return checkCollectionCondition(collectionAttr);
        }

    }

    /**
     * checks condition for a complex type with regard to the current  input
     *
     * @param input
     * @param complexAttr
     * @param isUIOnly
     * @return
     */
    private boolean checkComplexTypeCondition(InputType input, OMAttribute complexAttr, boolean isUIOnly) {
        if (isUIOnly) {
            return checkCollectionCondition(complexAttr) && input.isComplex();
        } else {
            return checkCollectionCondition(complexAttr);
        }

    }

    /**
     * @param type
     * @param propertyElement
     */
    private void createInlineTypes(InputType type, OMElement propertyElement) {
        Iterator<OMElement> inlineInputs = propertyElement.getChildElements();

        while (inlineInputs.hasNext()) {
            type.getCollectionInputs().add(createTypeFrom(inlineInputs.next()));
        }
    }

    /**
     * @param type
     * @param propertyElement
     */
    private void createInlineUITypes(InputType type, OMElement propertyElement) {
        Iterator<OMElement> inlineInputElems = propertyElement.getChildElements();
        Iterator<InputType> inlineInputs = type.getCollectionInputs().iterator();
        while (inlineInputs.hasNext()) {
            while (inlineInputElems.hasNext()) {
                createUITypeFrom(inlineInputs.next(), inlineInputElems.next());
            }
        }
    }

    /**
     * Creates collection of outputs.
     *
     * @return the collection of Outputs.
     */
    private List<OutputType> createOutputs() {

        List<OutputType> outputs = new ArrayList<OutputType>();

        OMElement outputElement = operationOMElement.getFirstChildWithName(SalesforceMedatorConstants.QNAME_ELEM_OUTPUT_UI);
        OMAttribute attrKey = outputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATTR_KEYS_UI);
        int noKeys = attrKey == null ? DEFAULT_KEYS : Integer.parseInt(attrKey.getAttributeValue());

        if (null != outputElement) {
            for (int i = 0; i < noKeys; i++) {
                OutputType output = new OutputType();

                OMAttribute typeAttr = outputElement.getAttribute(SalesforceMedatorConstants.QNAME_ATTR_TYPE_UI);
                if (null != typeAttr && null != typeAttr.getAttributeValue()) {
                    output.setType(typeAttr.getAttributeValue());
                }
                setUIOutputAttributes(i, output);
                outputs.add(output);

            }
        }
        return outputs;
    }

    /**
     * @param index
     * @param output
     */
    private void setUIOutputAttributes(int index, OutputType output) {
        OutputTypeUI uiOutputType = new OutputTypeUI(output);
        uiOutputType.setDisplayName("[ KEY|" + index + "| ]");
        uiHandler.setUIMapping(output, uiOutputType);
    }


    /**
     * Create UI properties for a collection of output types.
     *
     * @param outputs
     */
    private void createUIOutputsFrom(List<OutputType> outputs) {

        OMElement outputElement = operationOMElement.getFirstChildWithName(SalesforceMedatorConstants.QNAME_ELEM_OUTPUT_UI);
        if (null != outputElement) {
            for (int i = 0; i < outputs.size(); i++) {
                OutputType output = outputs.get(i);
                setUIOutputAttributes(i, output);
            }
        }
    }

    /**
     * Logs the exception and wraps the source message into a
     * <code>SynapseException</code> exception.
     *
     * @param msg the source message
     * @param e   the exception
     */
    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SalesforceUIException(msg, e);
    }

    /**
     * wraps the source error message into <code>SynapseException</code> exception.
     *
     * @param msg the source message
     */
    private void handleException(String msg) {
        log.error(msg);
        throw new SalesforceUIException(msg);
    }

    /**
     * Populate a given operation Type with a set of Input types provided
     *
     * @param operation
     * @param typeList
     */
    public void populateInputs(OperationType operation, List<Type> typeList) {

        Map<String, Type> typesMap = registerTypes(typeList);
        populateInputTypes(operation.getInputs(), typesMap);
    }

    /**
     * Populate a given operation Type with a set of Output types provided
     *
     * @param operation
     * @param typeList
     */
    public void populateOutputs(OperationType operation, List<Type> typeList) {

        Map<String, Type> typesMap = registerTypes(typeList);
        populateOutputTypes(operation.getOutputs(), typesMap);
    }


    /**
     * Check and Populate a given input type according to the mapping given
     *
     * @param inputList
     * @param typesMap  mapping involving name to registered types
     */
    private void populateInputTypes(List<InputType> inputList, Map<String, Type> typesMap) {
        for (InputType type : inputList) {
            if (null != type.getName()) {
                String key = getKeyForType(type);

                if (typesMap.containsKey(key)) {
                    InputType fetchedInput = (InputType) typesMap.get(key);
                    type.setSourceValue(fetchedInput.getSourceValue());
                    type.setSourceXPath(fetchedInput.getSourceXPath());
                }
            }
        }
    }

    /**
     * Check and Populate a given output type according to the mapping given
     *
     * @param outputList
     * @param typesMap
     */
    private void populateOutputTypes(List<OutputType> outputList, Map<String, Type> typesMap) {
        int i = 0;
        for (OutputType type : outputList) {
            if (null != type.getName()) {
                String key = getKeyForOutputType(type, i++);

                if (typesMap.containsKey(key)) {
                    OutputType fetchedOutput = (OutputType) typesMap.get(key);
                    type.setSourceXpath(fetchedOutput.getSourceXPath());
                    type.setTargetKey(fetchedOutput.getTargetKey());
                    type.setTargetXpath(fetchedOutput.getTargetXPath());
                }
            }
        }
    }

    /**
     * @param typeList
     * @return
     */
    private Map<String, Type> registerTypes(List<Type> typeList) {
        Map<String, Type> typesMap = new HashMap<String, Type>();
        int i = 0;
        for (Type type : typeList) {
            if (null != type.getName()) {
                String key;
                if (type instanceof OutputType) {
                    typesMap.put(getKeyForOutputType((OutputType) type, i++), type);
                } else {
                    typesMap.put(getKeyForType(type), type);
                }
            }
        }
        return typesMap;
    }

    private String getKeyForOutputType(OutputType output, int index) {
        return output.getName() + index;
    }

    private String getKeyForType(Type input) {
        return input.getName();
    }


}
