/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediator.datamapper.engine.input.readers;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.InvalidPayloadException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.JacksonJSONSchema;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.mediator.datamapper.engine.input.InputBuilder;
import org.wso2.carbon.mediator.datamapper.engine.input.builders.JSONBuilder;
import org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;

import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.ARRAY_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.ATTRIBUTES_KEY;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.BOOLEAN_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.INTEGER_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.ITEMS_KEY;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.NULL_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.NUMBER_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.OBJECT_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.PROPERTIES_KEY;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_FIELD_PREFIX;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_NAMESPACE_NAME_SEPARATOR;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_XML_ELEMENT_TEXT_VALUE_FIELD;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.STRING_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.TYPE_KEY;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.VALUE_KEY;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.XMLNS;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.XSI_NAMESPACE_URI;

/**
 * This class is capable of parsing XML through AXIOMS for the InputStream and build the respective JSON message
 */
public class XMLInputReader implements InputReader {

    private static final Log log = LogFactory.getLog(XMLInputReader.class);

    /* Reference of the InputXMLMessageBuilder object to send the built JSON message */
    private InputBuilder messageBuilder;

    /* JSON schema of the input message */
    private Schema inputSchema;

    /* Name and NamespaceURI of the currently processing XML element */
    private String localName;
    private String nameSpaceURI;

    /* JSON Builder to build the respective JSON message */
    private JSONBuilder jsonBuilder;

    /* Iterator for the Attribute elements */
    private Iterator<OMAttribute> it_attr;

    /* JSON schema for input message */
    private Map jsonSchema;

    /**
     * Constructor
     *
     * @throws IOException
     */
    public XMLInputReader() throws IOException {
        this.jsonBuilder = new JSONBuilder();
    }

    /**
     * Read, parse the XML and notify with the output JSON message
     *
     * @param input          XML message InputStream
     * @param inputSchema    Schema of the input message
     * @param messageBuilder Reference of the InputXMLMessageBuilder
     * @throws ReaderException Exceptions in the parsing stage
     */
    @Override
    public void read(InputStream input, Schema inputSchema, InputBuilder messageBuilder) throws ReaderException {

        this.messageBuilder = messageBuilder;
        this.inputSchema = inputSchema;

        OMXMLParserWrapper parserWrapper = OMXMLBuilderFactory.createOMBuilder(input);
        OMElement root = parserWrapper.getDocumentElement();
        this.jsonSchema = getInputSchema().getSchemaMap();

        try {
            XMLTraverse(root, null, jsonSchema);
            jsonBuilder.writeEndObject();
            writeTerminateElement();
        } catch (IOException | JSException | SchemaException | InvalidPayloadException e) {
            throw new ReaderException("Error while parsing XML input stream. " + e.getMessage());
        }

    }

    /**
     * This method will perform a Depth First Search on the XML message and build the json message
     *
     * @param omElement       initially the root element will be passed-in
     * @param prevElementName name of the previous element only if the previous element was an array, a null otherwise
     * @param jsonSchemaMap   reduced json input schema map that is applicable to this level
     * @return the name of the previous element if the element was an array element, null otherwise
     * @throws IOException
     * @throws ReaderException
     * @throws SchemaException
     * @throws JSException
     * @throws InvalidPayloadException
     */
    public String XMLTraverse(OMElement omElement, String prevElementName, Map jsonSchemaMap)
            throws IOException, ReaderException, SchemaException, JSException, InvalidPayloadException {

        /** isObject becomes true if the current element is an object, therefor object end element can be written at
         * the end */
        boolean isObject = false;

        boolean isArrayElement = false;

        String prevElementNameSpaceLocalName = null;

        String elementType;

        Map nextJSONSchemaMap;

        /* iterator to hold the child elements of the passed OMElement */
        Iterator<OMElement> it;

        /* Reading parameters of the currently processing OMElement */
        localName = omElement.getLocalName();
        nameSpaceURI = this.getNameSpaceURI(omElement);
        String nameSpaceLocalName = getNamespacesAndIdentifiersAddedFieldName(nameSpaceURI, localName, omElement);

        elementType = getElementType(jsonSchemaMap, nameSpaceLocalName);
        if (NULL_ELEMENT_TYPE.equals(elementType)) {
            /* Check whether the input payload has empty tags. */
            log.warn("Element name not found : " + nameSpaceLocalName);
        }

        nextJSONSchemaMap = buildNextSchema(jsonSchemaMap, elementType, nameSpaceLocalName);
        if (nextJSONSchemaMap == null) {
            throw new ReaderException("Invalid element found in the message payload :" + nameSpaceLocalName);
        }
        /* if this is new object preceding an array, close the array before writing the new element */
        if (prevElementName != null && !nameSpaceLocalName.equals(prevElementName)) {
            writeArrayEndElement();
            prevElementName = null;
        }

        if (ARRAY_ELEMENT_TYPE.equals(elementType)) {
            if (prevElementName == null) {
                writeArrayStartElement(nameSpaceLocalName);
            }
            elementType = getArraySubElementType(jsonSchemaMap, nameSpaceLocalName);
            isArrayElement = true;
        }

        if (nameSpaceLocalName.equals(getInputSchema().getName())) {
            writeAnonymousObjectStartElement();
        } else if (OBJECT_ELEMENT_TYPE.equals(elementType)) {
            isObject = true;
            if (isArrayElement) {
                writeAnonymousObjectStartElement();
                elementType = getArrayObjectTextElementType(jsonSchemaMap, nameSpaceLocalName);
            } else {
                writeObjectStartElement(nameSpaceLocalName);
                elementType = getObjectTextElementType(jsonSchemaMap, nameSpaceLocalName);
            }
        }
        /* If there is text in the OMElement */
        if (DataMapperEngineConstants.STRING_ELEMENT_TYPE.equals(elementType)
                || DataMapperEngineConstants.BOOLEAN_ELEMENT_TYPE.equals(elementType)
                || DataMapperEngineConstants.INTEGER_ELEMENT_TYPE.equals(elementType)
                || DataMapperEngineConstants.NUMBER_ELEMENT_TYPE.equals(elementType)) {
            if (isObject) { // if it is a normal object or an array element object
                writeFieldElement(SCHEMA_XML_ELEMENT_TEXT_VALUE_FIELD, omElement.getText(), elementType);
            } else if (!isArrayElement) { // if it is a normal XML element (not a object or part of an array)
                writeFieldElement(nameSpaceLocalName, omElement.getText(), elementType);
            } else { // primitive array elements
                writePrimitiveElement(omElement.getText(), elementType);
            }
        }

        /* writing attributes to the JSON message */
        it_attr = omElement.getAllAttributes();
        if (it_attr.hasNext()) {
            writeAttributes(nextJSONSchemaMap);
        }

        it = omElement.getChildElements();

        /* Recursively call all the children */
        if (!isXsiNil(omElement)) {
            while (it.hasNext()) {
                prevElementNameSpaceLocalName = XMLTraverse(it.next(), prevElementNameSpaceLocalName,
                        nextJSONSchemaMap);
            }
        }

        /* Closing the opened JSON objects and arrays */
        if (prevElementNameSpaceLocalName != null) {
            writeArrayEndElement();
        }

        if (isObject) {
            writeObjectEndElement();
        }

        if (isArrayElement) {
            return nameSpaceLocalName;
        }
        return null;
    }

    /**
     * This method is used to get the namespace URI of an XML element (OMElement)
     *
     * @param omElement
     * @return Namespace URI of the given OMElement, if there is no Namespace return an empty String
     */
    private String getNameSpaceURI(OMElement omElement) {
        String nameSpaceURI = "";
        if (omElement.getNamespace() != null) {
            nameSpaceURI = omElement.getNamespace().getNamespaceURI();
        }
        return nameSpaceURI;
    }

    /**
     * This method is used to get the namespace URI of an XML attribute (OMAttribute)
     *
     * @param omAttribute
     * @return Namespace URI of the given OMAttribute, if there is no Namespace return an empty String
     */
    private String getNameSpaceURI(OMAttribute omAttribute) {
        String nameSpaceURI = "";
        if (omAttribute.getNamespace() != null) {
            nameSpaceURI = omAttribute.getNamespace().getNamespaceURI();
        }
        return nameSpaceURI;
    }

    /**
     * This method writes attribute elements into the JSON input message
     *
     * @param jsonSchemaMap current level JSON Schema
     * @throws JSException
     * @throws SchemaException
     * @throws ReaderException
     * @throws IOException
     * @throws InvalidPayloadException
     */
    private void writeAttributes(Map jsonSchemaMap)
            throws JSException, SchemaException, ReaderException, IOException, InvalidPayloadException {

        /* currently processing attribute element and its parameters*/
        String attributeType;
        String attributeFieldName;
        String attributeLocalName;
        String attributeNSURI;
        String attributeQName;
        OMAttribute omAttribute;

        /** Writing next attributes to the JSON message */
        while (it_attr.hasNext()) {
            omAttribute = it_attr.next();

            /* skip if the attribute name contains "XMLNS" */
            attributeLocalName = omAttribute.getLocalName();
            if (attributeLocalName.contains(XMLNS))
                continue;

            attributeNSURI = this.getNameSpaceURI(omAttribute);
            attributeFieldName = getAttributeFieldName(attributeLocalName, attributeNSURI);
            attributeQName = getAttributeQName(omAttribute.getNamespace(), attributeLocalName);

            /* get the type of the attribute element */
            attributeType = getElementType(jsonSchemaMap, attributeQName);
            if (NULL_ELEMENT_TYPE.equals(attributeType)) {
                 /* Check whether the input payload has empty tags. */
                log.warn("Attribute name not found : " + attributeQName);
            }

            /* write the attribute to the JSON message */
            writeFieldElement(attributeFieldName, omAttribute.getAttributeValue(), attributeType);
        }
    }

    /**
     * Get the element type by referring to the input schema
     *
     * @param jsonSchemaMap Current level of the schema map
     * @param elementName   Name of the OMElement
     * @return Type of the element
     * @throws SchemaException
     */
    private String getElementType(Map jsonSchemaMap, String elementName) throws SchemaException {

        String elementType = NULL_ELEMENT_TYPE;

        if (elementName.equals(getInputSchema().getName())) {
            elementType = (String) jsonSchemaMap.get(TYPE_KEY);
        } else if (jsonSchemaMap.containsKey(elementName)) {
            elementType = (String) ((Map<String, Object>) jsonSchemaMap.get(elementName)).get(TYPE_KEY);
        }
        return elementType;
    }

    /**
     * Get the next schema level to pass to the next element search
     *
     * @param jsonSchemaMap Current level of the schema map
     * @param elementType   Type of the parent element
     * @param elementName   Name of the parent element
     * @return Next level schema map
     * @throws SchemaException
     */
    private Map buildNextSchema(Map jsonSchemaMap, String elementType, String elementName) throws SchemaException {

        Map nextSchema = null;

        if (elementName.equals(getInputSchema().getName())) {
            nextSchema = (Map<String, Object>) jsonSchemaMap.get(PROPERTIES_KEY);
        } else if (jsonSchemaMap.containsKey(elementName)) {
            if (ARRAY_ELEMENT_TYPE.equals(elementType)) {
                nextSchema = ((JacksonJSONSchema) inputSchema)
                        .getSchemaItems((Map<String, Object>) jsonSchemaMap.get(elementName));
                nextSchema = getSchemaProperties(nextSchema);
            } else {
                nextSchema = getSchemaProperties((Map<String, Object>) jsonSchemaMap.get(elementName));
            }
        }
        return nextSchema;
    }

    /**
     * Go to the next level of the schema
     *
     * @param schema Current level of the schema
     * @return next level schema
     */
    private Map<String, Object> getSchemaProperties(Map<String, Object> schema) {
        Map<String, Object> nextSchema = new HashMap<>();
        if (schema.containsKey(PROPERTIES_KEY)) {
            nextSchema.putAll((Map<? extends String, Object>) schema.get(PROPERTIES_KEY));
        }
        if (schema.containsKey(ATTRIBUTES_KEY)) {
            nextSchema.putAll((Map<? extends String, Object>) schema.get(ATTRIBUTES_KEY));
        }
        return nextSchema;
    }

    /**
     * Get the array elements sub-type. It can either be an object or primitive type
     *
     * @param jsonSchemaMap Current level json schema
     * @param elementName   Name of the element
     * @return sub-type of the array element
     */
    private String getArraySubElementType(Map jsonSchemaMap, String elementName) {
        ArrayList itemsList = (ArrayList) ((Map<String, Object>) jsonSchemaMap.get(elementName)).get(ITEMS_KEY);
        String output = (String) ((Map) itemsList.get(0)).get(TYPE_KEY);
        return output;
    }

    /**
     * Get the primitive type of an element
     * Main type :array
     * Sub-type :object
     *
     * @param jsonSchemaMap Current level json schema
     * @param elementName   Name of the element
     * @return Primitive type or NULL_TYPE if there is not primitive text in the object
     */
    private String getArrayObjectTextElementType(Map jsonSchemaMap, String elementName) {
        ArrayList itemsList = (ArrayList) ((Map<String, Object>) jsonSchemaMap.get(elementName)).get(ITEMS_KEY);
        Map itemsMap = (Map) itemsList.get(0);
        return getTextElementType(itemsMap);
    }

    /**
     * Get the primitive type of an element
     * Main type :object
     *
     * @param jsonSchemaMap Current level json schema
     * @param elementName   Name of the element
     * @return Primitive type or NULL_TYPE if there is not primitive text in the object
     */
    private String getObjectTextElementType(Map jsonSchemaMap, String elementName) {
        Map objectsMap = (Map<String, Object>) jsonSchemaMap.get(elementName);
        return getTextElementType(objectsMap);
    }

    /**
     * Get primitive type (used for getArrayObjectTextElementType, getObjectTextElementType methods)
     *
     * @param objectsMap
     * @return primitive type or NULL_TYPE of there is no primitive text
     */
    private String getTextElementType(Map objectsMap) {
        if (!objectsMap.containsKey(VALUE_KEY)) {
            return NULL_ELEMENT_TYPE;
        }
        String output = (String) ((Map<String, Object>) (objectsMap.get(VALUE_KEY))).get(TYPE_KEY);
        return output;
    }

    /**
     * Elements Local name modifier methods
     */

    private String getAttributeFieldName(String qName, String uri) {
        String[] qNameOriginalArray = qName.split(SCHEMA_NAMESPACE_NAME_SEPARATOR);
        qName = getNamespacesAndIdentifiersAddedFieldName(uri, qNameOriginalArray[qNameOriginalArray.length - 1], null);
        String[] qNameArray = qName.split(SCHEMA_NAMESPACE_NAME_SEPARATOR);
        if (qNameArray.length > 1) {
            return SCHEMA_ATTRIBUTE_FIELD_PREFIX + qNameArray[0] + SCHEMA_NAMESPACE_NAME_SEPARATOR +
                    qNameArray[qNameArray.length - 1];
        } else {
            return SCHEMA_ATTRIBUTE_FIELD_PREFIX + qName;
        }
    }

    private String getNamespacesAndIdentifiersAddedFieldName(String uri, String localName, OMElement omElement) {
        String modifiedLocalName = null;
        String prefix = getInputSchema().getPrefixForNamespace(uri);
        if (StringUtils.isNotEmpty(prefix)) {
            modifiedLocalName = prefix + SCHEMA_NAMESPACE_NAME_SEPARATOR + localName;
        } else {
            modifiedLocalName = localName;
        }
        String prefixInMap = inputSchema.getNamespaceMap().get(XSI_NAMESPACE_URI);
        if (prefixInMap != null && omElement != null) {
            String xsiType = omElement.getAttributeValue(new QName(XSI_NAMESPACE_URI, "type", prefixInMap));
            if (xsiType != null) {
                modifiedLocalName = modifiedLocalName + "," + prefixInMap + ":type=" + xsiType;
            }
        }
        return modifiedLocalName;
    }

    private boolean isXsiNil(OMElement omElement) {
        String prefixInMap = inputSchema.getNamespaceMap().get(XSI_NAMESPACE_URI);
        if (prefixInMap != null && omElement != null) {
            String xsiNilValue = omElement.getAttributeValue(new QName(XSI_NAMESPACE_URI, "nil", prefixInMap));
            if (xsiNilValue != null && "true".equalsIgnoreCase(xsiNilValue)) {
                return true;
            }
        }
        return false;
    }

    public String getAttributeQName(OMNamespace omNamespace, String localName) {
        if (omNamespace != null) {
            return omNamespace.getPrefix() + ":" + localName;
        } else {
            return localName;
        }
    }

    public Schema getInputSchema() {
        return inputSchema;
    }

    /**
     * JSON message building methods
     */

    private void writeFieldElement(String fieldName, String valueString, String fieldType)
            throws IOException, JSException, SchemaException, ReaderException {
        switch (fieldType) {
        case STRING_ELEMENT_TYPE:
            jsonBuilder.writeField(getModifiedFieldName(fieldName), valueString, fieldType);
            break;
        case BOOLEAN_ELEMENT_TYPE:
            jsonBuilder.writeField(getModifiedFieldName(fieldName), Boolean.parseBoolean(valueString), fieldType);
            break;
        case NUMBER_ELEMENT_TYPE:
            jsonBuilder.writeField(getModifiedFieldName(fieldName), Double.parseDouble(valueString), fieldType);
            break;
        case INTEGER_ELEMENT_TYPE:
            jsonBuilder.writeField(getModifiedFieldName(fieldName), Integer.parseInt(valueString), fieldType);
            break;
        default:
            jsonBuilder.writeField(getModifiedFieldName(fieldName), valueString, fieldType);

        }
    }

    private void writePrimitiveElement(String valueString, String fieldType)
            throws IOException, JSException, SchemaException, ReaderException {
        switch (fieldType) {
        case STRING_ELEMENT_TYPE:
            jsonBuilder.writePrimitive(valueString, fieldType);
            break;
        case BOOLEAN_ELEMENT_TYPE:
            jsonBuilder.writePrimitive(Boolean.parseBoolean(valueString), fieldType);
            break;
        case NUMBER_ELEMENT_TYPE:
            jsonBuilder.writePrimitive(Double.parseDouble(valueString), fieldType);
            break;
        case INTEGER_ELEMENT_TYPE:
            jsonBuilder.writePrimitive(Integer.parseInt(valueString), fieldType);
            break;
        default:
            jsonBuilder.writePrimitive(valueString, fieldType);

        }
    }

    private void writeObjectStartElement(String fieldName)
            throws IOException, JSException, SchemaException, ReaderException {
        jsonBuilder.writeObjectFieldStart(getModifiedFieldName(fieldName));
    }

    private void writeObjectEndElement() throws IOException, JSException, SchemaException, ReaderException {
        jsonBuilder.writeEndObject();
    }

    private void writeArrayStartElement(String fieldName)
            throws IOException, JSException, SchemaException, ReaderException {
        jsonBuilder.writeArrayFieldStart(getModifiedFieldName(fieldName));
    }

    private void writeArrayEndElement() throws IOException, JSException, SchemaException, ReaderException {
        jsonBuilder.writeEndArray();
    }

    private void writeTerminateElement() throws IOException, JSException, SchemaException, ReaderException {
        jsonBuilder.close();
        String jsonBuiltMessage = jsonBuilder.getContent();
        messageBuilder.notifyWithResult(jsonBuiltMessage);
    }

    private void writeAnonymousObjectStartElement() throws IOException, JSException, SchemaException, ReaderException {
        jsonBuilder.writeStartObject();
    }

    private String getModifiedFieldName(String fieldName) {
        return fieldName.replace(SCHEMA_NAMESPACE_NAME_SEPARATOR, "_").replace(",", "_").replace("=", "_");
    }

}
