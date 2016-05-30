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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;

import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.ARRAY_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.BOOLEAN_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.INTEGER_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.NULL_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.NUMBER_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.OBJECT_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_FIELD_PREFIX;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_NAMESPACE_NAME_SEPARATOR;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.STRING_ELEMENT_TYPE;

/**
 * This class is capable of parsing XML through AXIOMS for the InputStream and build the respective JSON message
 */
public class XMLInputReader implements InputReader {

    public static final String HTTP_XML_ORG_SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces";
    public static final String HTTP_XML_ORG_SAX_FEATURES_NAMESPACE_PREFIXES =
            "http://xml" + ".org/sax/features/namespace-prefixes";
    public static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XMLNS = "xmlns";
    private static final String PROPERTIES_KEY = "properties";
    private static final String ATTRIBUTES_KEY = "attributes";
    private static final String TYPE_KEY = "type";

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
        } catch (IOException e) {
            throw new ReaderException("IO Error while parsing xml input stream. " + e.getMessage());
        } catch (JSException e) {
            throw new ReaderException("JSException while parsing xml input stream. " + e.getMessage());
        } catch (SchemaException e) {
            throw new ReaderException("SchemaException while parsing xml input stream. " + e.getMessage());
        } catch (InvalidPayloadException e) {
            throw new ReaderException("InvalidPayLoad while parsing xml input stream. " + e.getMessage());
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

        String prevElementNameSpaceLocalName = null;

        String elementType;

        Map nextJSONMap;

        /* iterator to hold the child elements of the passed OMElement */
        Iterator<OMElement> it;

        /* Reading parameters of the currently processing OMElement */
        localName = omElement.getLocalName();
        nameSpaceURI = this.getNameSpaceURI(omElement);
        String nameSpaceLocalName = getNamespacesAndIdentifiersAddedFieldName(nameSpaceURI, localName, omElement);

        elementType = getElementType(jsonSchemaMap, nameSpaceLocalName);
        if (NULL_ELEMENT_TYPE.equals(elementType) && !localName.equals("Header")) {
            log.warn("Element name not found : " + nameSpaceLocalName);
        }

        nextJSONMap = buildNextSchema(jsonSchemaMap, elementType, nameSpaceLocalName);

        /* if this is new object preceding an array, close the array before writing the new element */
        if (prevElementName != null && !nameSpaceLocalName.equals(prevElementName)) {
            writeArrayEndElement();
            prevElementName = null;
        }

        if (nameSpaceLocalName.equals(getInputSchema().getName())) {
            writeAnonymousObjectStartElement();
        } else if (ARRAY_ELEMENT_TYPE.equals(elementType)) {
            if (prevElementName == null) {
                writeArrayStartElement(nameSpaceLocalName);
            }
            writeAnonymousObjectStartElement();
            isObject = true;
        } else if (OBJECT_ELEMENT_TYPE.equals(elementType)) {
            writeObjectStartElement(nameSpaceLocalName);
            isObject = true;
        } else {
            writeFieldElement(nameSpaceLocalName, omElement.getText(), elementType);
        }

        /* writing attributes to the JSON message */
        it_attr = omElement.getAllAttributes();
        if (it_attr.hasNext()) {
            writeAttributes(elementType, nameSpaceLocalName, nextJSONMap);
        }

        it = omElement.getChildElements();

        /* Recursively call all the children */
        while (it.hasNext()) {
            prevElementNameSpaceLocalName = XMLTraverse(it.next(), prevElementNameSpaceLocalName, nextJSONMap);
        }

        /* Closing the opened JSON objects and arrays */
        if (prevElementNameSpaceLocalName != null) {
            writeArrayEndElement();
        }

        if (isObject) {
            writeObjectEndElement();
        }

        if (ARRAY_ELEMENT_TYPE.equals(elementType)) {
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
     * @param elementType        type of the parent element
     * @param nameSpaceLocalName name of the parent element
     * @throws JSException
     * @throws SchemaException
     * @throws ReaderException
     * @throws IOException
     * @throws InvalidPayloadException
     */
    private void writeAttributes(String elementType, String nameSpaceLocalName, Map jsonSchemaMap)
            throws JSException, SchemaException, ReaderException, IOException, InvalidPayloadException {

        /* object will be opened if the parent element is field type*/
        boolean hasObjectOpened = false;

        /* currently processing attribute element and its parameters*/
        String attributeType;
        String attributeFieldName;
        String attributeLocalName;
        String attributeNSURI;
        String attributeQName;
        OMAttribute omAttribute = null;

        /* continue beyond this while loop only if there is at least one attribute without "XMLNS" tag */
        while (it_attr.hasNext()) {
            omAttribute = it_attr.next();
            if (!omAttribute.getLocalName().contains(XMLNS))
                break;
            if (!it_attr.hasNext())
                return;
        }

        /* if the main XML element is only a field, open an object to include the attributes */
        if (!ARRAY_ELEMENT_TYPE.equals(elementType) && !OBJECT_ELEMENT_TYPE.equals(elementType)) {
            writeObjectStartElement(nameSpaceLocalName + SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX);
            hasObjectOpened = true;
        }

        /** Write the first attribute to the JSON message */

        /* extracting parameters from the attribute element */
        attributeLocalName = omAttribute.getLocalName();
        attributeNSURI = this.getNameSpaceURI(omAttribute);
        attributeFieldName = getAttributeFieldName(attributeLocalName, attributeNSURI);
        attributeQName = getAttributeQName(omAttribute.getNamespace(), attributeLocalName);

        /* get the type of the attribute element */
        attributeType = getElementType(jsonSchemaMap, attributeQName);

        /* write the attribute to the JSON message */
        writeFieldElement(attributeFieldName, omAttribute.getAttributeValue(), attributeType);

        /** Writing next attributes to the JSON message */
        while (it_attr.hasNext()) {
            omAttribute = it_attr.next();

            /* skip if the attribute name contains "XMLNS" */
            attributeLocalName = omAttribute.getLocalName();
            if (attributeLocalName.contains(XMLNS))
                continue;

            attributeNSURI = this.getNameSpaceURI(omAttribute);
            attributeFieldName = getAttributeFieldName(attributeLocalName, attributeNSURI);
            attributeQName = getAttributeQName(omAttribute.getNamespace(), localName);

            /* get the type of the attribute element */
            attributeType = getElementType(jsonSchemaMap, attributeQName);

            /* write the attribute to the JSON message */
            writeFieldElement(attributeFieldName, omAttribute.getAttributeValue(), attributeType);

        }

        /* if an object element was opened for writing this attributes, close it */
        if (hasObjectOpened) {
            writeObjectEndElement();
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
     * Elements Local name modifier methods
     */

    private String getAttributeFieldName(String qName, String uri) {
        String[] qNameOriginalArray = qName.split(SCHEMA_NAMESPACE_NAME_SEPARATOR);
        qName = getNamespacesAndIdentifiersAddedFieldName(uri, qNameOriginalArray[qNameOriginalArray.length - 1], null);
        String[] qNameArray = qName.split(SCHEMA_NAMESPACE_NAME_SEPARATOR);
        if (qNameArray.length > 1) {
            return qNameArray[0] + SCHEMA_NAMESPACE_NAME_SEPARATOR +
                    SCHEMA_ATTRIBUTE_FIELD_PREFIX +
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
