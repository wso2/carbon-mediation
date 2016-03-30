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
package org.wso2.datamapper.engine.input.readers;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.datamapper.engine.core.Schema;
import org.wso2.datamapper.engine.core.exceptions.InvalidPayloadException;
import org.wso2.datamapper.engine.core.exceptions.JSException;
import org.wso2.datamapper.engine.core.schemas.SchemaElement;
import org.wso2.datamapper.engine.input.InputModelBuilder;
import org.wso2.datamapper.engine.input.readers.events.DMReaderEvent;
import org.wso2.datamapper.engine.types.ReaderEventTypes;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.events.StartElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.*;

/**
 * This class implements {@link Readable} interface and xml reader for data mapper engine using SAX
 */
public class XMLReader extends DefaultHandler implements org.wso2.datamapper.engine.input.Readable {

    private static final Log log = LogFactory.getLog(XMLReader.class);
    private InputModelBuilder modelBuilder;
    private Schema inputSchema;
    private Stack<DMReaderEvent> dmEventStack;
    private String tempFieldValue;
    private List<SchemaElement> elementStack;

    @Override
    public void read(InputStream input, InputModelBuilder inputModelBuilder, Schema inputSchema) {
        dmEventStack = new Stack<>();
        elementStack = new ArrayList();
        modelBuilder = inputModelBuilder;
        this.inputSchema = inputSchema;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            org.xml.sax.XMLReader xmlReader = parser.getXMLReader();
            xmlReader.setContentHandler(this);
            xmlReader.setDTDHandler(this);
            xmlReader.setEntityResolver(this);
            xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
            xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            xmlReader.parse(new InputSource(input));
        } catch (ParserConfigurationException e) {
            log.error("ParserConfig error", e);
        } catch (SAXException e) {
            log.error("Xml not well formed.", e);
        } catch (IOException e) {
            log.error("IO Error while parsing xml input stream.", e);
        }
    }

    @Override
    public void startDocument() throws SAXException {

    }

    @Override
    public void endDocument() throws SAXException {
        try {
            sendTerminateEvent();
        } catch (IOException | JSException e) {
            log.error("Error occurred while sending termination event", e);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {

    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            elementStack.add(new SchemaElement(localName, uri));
            String tempLocalName = getNamespaceAddedFieldName(uri, localName).replace(":", "_");
            if (!getDmEventStack().isEmpty()) {
                DMReaderEvent stackElement = getDmEventStack().peek();
                if (ReaderEventTypes.EventType.ARRAY_START.equals(stackElement.getEventType()) &&
                        !(getInputSchema().isChildElement(elementStack.subList(0, elementStack.size() - 2), localName) ||
                                tempLocalName.equals(stackElement.getName()))) {
                    sendArrayEndEvent(localName);
                    elementStack.add(new SchemaElement(localName, uri));
                }
            }
            localName = getNamespaceAddedFieldName(uri, localName);
            String elementType = getInputSchema().getElementTypeByName(elementStack);
            String schemaTitle = getInputSchema().getName();
            if (localName.equals(schemaTitle)) {
                sendAnonymousObjectStartEvent(schemaTitle);
                for (int attributeCount = 0; attributeCount < attributes.getLength(); attributeCount++) {
                    if (!attributes.getQName(attributeCount).contains("xmlns")) {
                        String attributeFieldName = getAttributeFieldName(attributes.getQName(attributeCount), attributes.getURI(attributeCount));
                        sendFieldEvent(attributeFieldName,attributes.getValue(attributeCount),null);
                    }
                }
            } else if (ARRAY_ELEMENT_TYPE.equals(elementType)) {
                //first element of a array should fire array start element
                if (!getDmEventStack().isEmpty()) {
                    DMReaderEvent stackElement = getDmEventStack().peek();
                    if (!(ReaderEventTypes.EventType.ARRAY_START.equals(stackElement.getEventType()) &&
                            tempLocalName.equals(stackElement.getName()))) {
                        sendArrayStartEvent(localName);
                    }
                } else {
                    sendArrayStartEvent(localName);
                }
                sendAnonymousObjectStartEvent(localName);
                for (int attributeCount = 0; attributeCount < attributes.getLength(); attributeCount++) {
                    if (!attributes.getQName(attributeCount).contains("xmlns")) {
                        String attributeFieldName = getAttributeFieldName(attributes.getQName(attributeCount),
                                attributes.getURI(attributeCount));
                        sendFieldEvent(attributeFieldName,attributes.getValue(attributeCount),null);
                    }
                }
            } else if (OBJECT_ELEMENT_TYPE.equals(elementType)) {
                sendObjectStartEvent(localName);
                for (int attributeCount = 0; attributeCount < attributes.getLength(); attributeCount++) {
                    if (!attributes.getQName(attributeCount).contains("xmlns")) {
                        String attributeFieldName = getAttributeFieldName(attributes.getQName(attributeCount),
                                attributes.getURI(attributeCount));
                        sendFieldEvent(attributeFieldName,attributes.getValue(attributeCount),null);
                    }
                }
            } else if ((STRING_ELEMENT_TYPE.equals(elementType) || BOOLEAN_ELEMENT_TYPE.equals(elementType) ||
                    NUMBER_ELEMENT_TYPE.equals(elementType) || INTEGER_ELEMENT_TYPE.equals(elementType))
                    && attributes.getLength() > 0) {
                sendObjectStartEvent(localName + SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX);
                for (int attributeCount = 0; attributeCount < attributes.getLength(); attributeCount++) {
                    if (!attributes.getQName(attributeCount).contains("xmlns")) {
                        String attributeFieldName = getAttributeFieldName(attributes.getQName(attributeCount)
                                ,attributes.getURI(attributeCount));
                        sendFieldEvent(attributeFieldName,attributes.getValue(attributeCount),null);
                    }
                }
                sendObjectEndEvent(localName + SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX);
            }
        } catch (IOException | JSException e) {
            log.error("Error occurred while processing start element event", e);
        } catch (InvalidPayloadException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getAttributeFieldName(String qName, String uri) {
        String[] qNameOriginalArray=qName.split(":");
        qName=getNamespaceAddedFieldName(uri, qNameOriginalArray[qNameOriginalArray.length-1]);
        String[] qNameArray = qName.split(":");
        if (qNameArray.length > 1) {
            return qNameArray[0] + ":" + SCHEMA_ATTRIBUTE_FIELD_PREFIX + qNameArray[qNameArray.length - 1];
        } else {
            return SCHEMA_ATTRIBUTE_FIELD_PREFIX + qName;
        }
    }

    private String getNamespaceAddedFieldName(String uri, String localName) {
        String prefix = getInputSchema().getPrefixForNamespace(uri);
        if (StringUtils.isNotEmpty(prefix)) {
            return prefix + ":" + localName;
        } else {
            return localName;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            String elementType = getInputSchema().getElementTypeByName(elementStack);
            localName = getNamespaceAddedFieldName(uri, localName);
            if (localName.equals(getInputSchema().getName())) {
                sendObjectEndEvent(localName);
            } else if (STRING_ELEMENT_TYPE.equals(elementType)) {
                sendFieldEvent(localName, tempFieldValue,STRING_ELEMENT_TYPE);
            } else if (NUMBER_ELEMENT_TYPE.equals(elementType)) {
                sendFieldEvent(localName, tempFieldValue,NUMBER_ELEMENT_TYPE);
            } else if (BOOLEAN_ELEMENT_TYPE.equals(elementType)) {
                sendFieldEvent(localName, tempFieldValue,BOOLEAN_ELEMENT_TYPE);
            } else if (INTEGER_ELEMENT_TYPE.equals(elementType)) {
                sendFieldEvent(localName, tempFieldValue,INTEGER_ELEMENT_TYPE);
            } else if (ARRAY_ELEMENT_TYPE.equals(elementType)) {
                sendObjectEndEvent(localName);
            } else if (OBJECT_ELEMENT_TYPE.equals(elementType)) {
                while (!getDmEventStack().isEmpty()) {
                    DMReaderEvent stackElement = getDmEventStack().peek();
                    if (ReaderEventTypes.EventType.ARRAY_START.equals(stackElement.getEventType())) {
                        elementStack.add(new SchemaElement(stackElement.getName(), uri));
                        sendArrayEndEvent(stackElement.getName());
                    } else {
                        break;
                    }
                }
                sendObjectEndEvent(localName);
            }

        } catch (IOException | JSException e) {
            log.error("Error occurred while processing end element event", e);
        } catch (InvalidPayloadException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        String value = (new String(ch)).substring(start, start + length);
        tempFieldValue = value;
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
            throws SAXException {
        // no op
    }

    public Schema getInputSchema() {
        return inputSchema;
    }

    private void sendFieldEvent(String fieldName, String valueString,String fieldType) throws IOException, JSException {
        switch (fieldType){
            case "string":
                getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                        getModifiedFieldName(fieldName), valueString, fieldType));
                break;
            case "boolean":
                getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                        getModifiedFieldName(fieldName), Boolean.parseBoolean(valueString), fieldType));
                break;
            case "number":
                getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                        getModifiedFieldName(fieldName), Double.parseDouble((String) valueString), fieldType));
                break;
            case "integer":
                getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                        getModifiedFieldName(fieldName), Integer.parseInt((String) valueString), fieldType));
                break;
            default:
                getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                        getModifiedFieldName(fieldName), valueString, fieldType));
        }

        if (!fieldName.contains(SCHEMA_ATTRIBUTE_FIELD_PREFIX)) {
            elementStack.remove(elementStack.size() - 1);
        }

    }

    private void sendObjectStartEvent(String fieldName) throws IOException, JSException {
        DMReaderEvent objectStartEvent = new DMReaderEvent(ReaderEventTypes.EventType.OBJECT_START,
                getModifiedFieldName(fieldName));
        getModelBuilder().notifyEvent(objectStartEvent);
        dmEventStack.push(objectStartEvent);
    }

    private void sendObjectEndEvent(String fieldName) throws IOException, JSException {
        DMReaderEvent objectEndEvent = new DMReaderEvent(ReaderEventTypes.EventType.OBJECT_END,
                getModifiedFieldName(fieldName));
        getModelBuilder().notifyEvent(objectEndEvent);
        /*if (!getInputSchema().getName().equals(fieldName)&&
                !ARRAY_ELEMENT_TYPE.equals(getInputSchema().getElementTypeByName(fieldName))) {
            dmEventStack.pop();
        }*/
        dmEventStack.pop();
        if (!fieldName.contains(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)) {
            elementStack.remove(elementStack.size() - 1);
        }
    }

    private void sendArrayStartEvent(String fieldName) throws IOException, JSException {
        DMReaderEvent arrayStartEvent = new DMReaderEvent(ReaderEventTypes.EventType.ARRAY_START,
                getModifiedFieldName(fieldName));
        getModelBuilder().notifyEvent(arrayStartEvent);
        dmEventStack.push(arrayStartEvent);
    }

    private void sendArrayEndEvent(String fieldName) throws IOException, JSException {
        DMReaderEvent arrayEndEvent = new DMReaderEvent(ReaderEventTypes.EventType.ARRAY_END,
                getModifiedFieldName(fieldName));
        getModelBuilder().notifyEvent(arrayEndEvent);
        dmEventStack.pop();
        elementStack.remove(elementStack.size() - 1);
    }

    public Stack<DMReaderEvent> getDmEventStack() {
        return dmEventStack;
    }

    private void sendTerminateEvent() throws IOException, JSException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.TERMINATE));
    }

    private void sendAnonymousObjectStartEvent(String fieldName) throws IOException, JSException {
        DMReaderEvent anonymousObjectStartEvent = new DMReaderEvent(ReaderEventTypes.EventType.ANONYMOUS_OBJECT_START,
                getModifiedFieldName(fieldName));
        getModelBuilder().notifyEvent(anonymousObjectStartEvent);
        dmEventStack.push(anonymousObjectStartEvent);
    }


    public InputModelBuilder getModelBuilder() {
        return modelBuilder;
    }

    private String getFieldName(StartElement startElement) {
        String fieldName = startElement.getName().getLocalPart();
        return fieldName;
    }

    private String getModifiedFieldName(String fieldName) {
        return fieldName.replace(":", "_");
    }

}
