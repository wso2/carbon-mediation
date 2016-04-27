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
package org.wso2.carbon.mediator.datamapper.engine.input.optimizedReaders;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.InvalidPayloadException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.SchemaElement;
import org.wso2.carbon.mediator.datamapper.engine.input.InputXMLMessageBuilder;
import org.wso2.carbon.mediator.datamapper.engine.input.builders.JSONBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.*;

/**
 * This class is capable of parsing XML through AXIOMS for the InputStream and build the respective JSON message
 */
public class AxiomXMLReader {

    public static final String HTTP_XML_ORG_SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces";
    public static final String HTTP_XML_ORG_SAX_FEATURES_NAMESPACE_PREFIXES =
            "http://xml" + ".org/sax/features/namespace-prefixes";
    public static final String XMLNS = "xmlns";
    private static final Log log = LogFactory.getLog(AxiomXMLReader.class);
    /**
     * Reference of the InputXMLMessageBuilder object to notify the built JSON message
     */
    private InputXMLMessageBuilder messageBuilder;
    /**
     * JSON schema of the input message
     */
    private Schema inputSchema;
    /**
     * Name and NamespaceURI of the currently processing XML element
     */
    private String nameSpaceLocalName;
    private String localName;
    private String nameSpaceURI;
    /**
     * List of parent schema elements of the currently processing element
     */
    private List<SchemaElement> schemaElementList;
    /**
     * JSON Builder to build the respective JSON message
     */
    private JSONBuilder jsonBuilder;
    /**
     * Keep the state of "is the previous element of the current element, a member of an array"
     */
    private boolean isPrevElementArray;
    /**
     * Iterator for the Attribute elements
     */
    private Iterator<OMAttribute> it_attr;

    /**
     * Constructor
     *
     * @throws IOException
     */
    public AxiomXMLReader() throws IOException {
        this.schemaElementList = new ArrayList();
        this.jsonBuilder = new JSONBuilder();
        this.isPrevElementArray = false;
    }

    /**
     * Read, parse the XML and notify with the output JSON message
     *
     * @param input          XML message InputStream
     * @param inputSchema
     * @param messageBuilder Reference of the InputXMLMessageBuilder
     * @throws ReaderException Exceptions in the parsing stage
     */
    public void read(InputStream input, Schema inputSchema, InputXMLMessageBuilder messageBuilder)
            throws ReaderException {

        this.messageBuilder = messageBuilder;
        this.inputSchema = inputSchema;

        OMXMLParserWrapper parserWrapper = OMXMLBuilderFactory.createOMBuilder(input);
        OMElement root = parserWrapper.getDocumentElement();

        try {
            XMLTraverse(root);
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
     * @param omElement initially the root element will be passed-in
     * @return true it is an array element, otherwise false
     * @throws IOException
     * @throws ReaderException
     * @throws SchemaException
     * @throws JSException
     * @throws InvalidPayloadException
     */
    public boolean XMLTraverse(OMElement omElement)
            throws IOException, ReaderException, SchemaException, JSException, InvalidPayloadException {
        /** isObject becomes true if the current element is an object, therefor object end element can be written at
         * the end */
        boolean isObject = false;
        /** isArrayParent becomes true if it is a parent of an Array element. So that it can close the array before
         * closing itself as object */
        boolean isArrayParent = false;
        /** iterator to hold the child elements of the passed OMElement */
        Iterator<OMElement> it;

        localName = omElement.getLocalName();
        nameSpaceURI = this.getNameSpaceURI(omElement);
        nameSpaceLocalName = getNamespaceAddedFieldName(nameSpaceURI, localName);

        schemaElementList.add(new SchemaElement(localName, nameSpaceURI));
        String elementType = getInputSchema().getElementTypeByName(schemaElementList);

        if (nameSpaceLocalName.equals(getInputSchema().getName())) {
            writeAnonymousObjectStartElement();
        } else if (ARRAY_ELEMENT_TYPE.equals(elementType)) {
            if (!isPrevElementArray) {
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

        /** writing attributes ******************/
        it_attr = omElement.getAllAttributes();
        if(it_attr.hasNext()){
            writeAttributes(elementType);
        }
        /****************************************/

        it = omElement.getChildElements();

        /** Recursively call all the children */
        while (it.hasNext()) {
            isArrayParent = XMLTraverse(it.next());
        }

        schemaElementList.remove(schemaElementList.size() - 1);

        if (isArrayParent) {
            writeArrayEndElement();
        }

        if (isObject) {
            writeObjectEndElement();
        }

        if (ARRAY_ELEMENT_TYPE.equals(elementType)) {
            isPrevElementArray = true;
            return true;
        } else {
            isPrevElementArray = false;
        }
        return false;
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
     * @param elementType type of the parent element
     * @throws JSException
     * @throws SchemaException
     * @throws ReaderException
     * @throws IOException
     * @throws InvalidPayloadException
     */
    private void writeAttributes(String elementType)
            throws JSException, SchemaException, ReaderException, IOException, InvalidPayloadException {
        /** object will be opened if the parent element is field type*/
        boolean hasObjectOpened = false;
        /** currently processing attribute element and its parameters*/
        String attributeType;
        String attributeFieldName;
        String attributeLocalName;
        String attributeNSURI;
        OMAttribute omAttribute=null;

        /** continue beyond this while loop only if there is at least one attribute without "XMLNS" tag */
        while (it_attr.hasNext()){
            omAttribute = it_attr.next();
            if (!omAttribute.getLocalName().contains(XMLNS)) break;
            if (!it_attr.hasNext()) return;
        }

        /** if the main XML element is only a field, open an object for attributes */
        if (!ARRAY_ELEMENT_TYPE.equals(elementType) && !OBJECT_ELEMENT_TYPE.equals(elementType)){
            writeObjectStartElement(nameSpaceLocalName + SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX);
            hasObjectOpened =true;
        }

        /** Write the first attribute to the JSON message */
        //extracting parameters from the attribute element
        attributeLocalName = omAttribute.getLocalName();
        attributeNSURI = this.getNameSpaceURI(omAttribute);
        attributeFieldName = getAttributeFieldName(attributeLocalName, attributeNSURI);
        //get the type of the attribute element
        schemaElementList.add(new SchemaElement(attributeLocalName, attributeNSURI));
        attributeType = getInputSchema().getElementTypeByName(schemaElementList);
        schemaElementList.remove(schemaElementList.size()-1);
        //write the attribute to the JSON message
        writeFieldElement(attributeFieldName, omAttribute.getAttributeValue(), attributeType);

        /** Writing next attributes to the JSON message */
        while (it_attr.hasNext()){
            omAttribute = it_attr.next();
            // skip if the attribute name contains "XMLNS"
            attributeLocalName = omAttribute.getLocalName();
            if (attributeLocalName.contains(XMLNS)) continue;

            attributeNSURI = this.getNameSpaceURI(omAttribute);
            attributeFieldName = getAttributeFieldName(attributeLocalName, attributeNSURI);
            //get the type of the attribute element
            schemaElementList.add(new SchemaElement(attributeLocalName, attributeNSURI));
            attributeType = getInputSchema().getElementTypeByName(schemaElementList);
            schemaElementList.remove(schemaElementList.size()-1);
            //write the attribute to the JSON message
            writeFieldElement(attributeFieldName, omAttribute.getAttributeValue(), attributeType);

        }

        /** if an object element was opened for writing this attributes, close it */
        if (hasObjectOpened) {
            writeObjectEndElement();
        }
    }

    private String getAttributeFieldName(String qName, String uri) {
        String[] qNameOriginalArray = qName.split(SCHEMA_NAMESPACE_NAME_SEPARATOR);
        qName = getNamespaceAddedFieldName(uri, qNameOriginalArray[qNameOriginalArray.length - 1]);
        String[] qNameArray = qName.split(SCHEMA_NAMESPACE_NAME_SEPARATOR);
        if (qNameArray.length > 1) {
            return qNameArray[0] + SCHEMA_NAMESPACE_NAME_SEPARATOR +
                    SCHEMA_ATTRIBUTE_FIELD_PREFIX +
                    qNameArray[qNameArray.length - 1];
        } else {
            return SCHEMA_ATTRIBUTE_FIELD_PREFIX + qName;
        }
    }

    private String getNamespaceAddedFieldName(String uri, String localName) {
        String prefix = getInputSchema().getPrefixForNamespace(uri);
        if (StringUtils.isNotEmpty(prefix)) {
            return prefix + SCHEMA_NAMESPACE_NAME_SEPARATOR + localName;
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

        //        if (!fieldName.contains(SCHEMA_ATTRIBUTE_FIELD_PREFIX)) {
        //            schemaElementList.remove(schemaElementList.size() - 1);
        //        }

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
        return fieldName.replace(SCHEMA_NAMESPACE_NAME_SEPARATOR, "_");
    }

}
