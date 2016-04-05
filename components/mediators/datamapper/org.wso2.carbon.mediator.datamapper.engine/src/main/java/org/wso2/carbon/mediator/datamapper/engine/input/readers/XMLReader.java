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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.InvalidPayloadException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.SchemaElement;
import org.wso2.carbon.mediator.datamapper.engine.input.InputModelBuilder;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.events.DMReaderEvent;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.events.ReaderEventTypes;
import org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants;
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

/**
 * This class implements {@link Reader} interface and xml reader for data mapper engine using SAX
 */
public class XMLReader extends DefaultHandler implements Reader {

    private static final Log log = LogFactory.getLog(XMLReader.class);
    public static final String HTTP_XML_ORG_SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces";
    public static final String HTTP_XML_ORG_SAX_FEATURES_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
    private InputModelBuilder modelBuilder;
    private Schema inputSchema;
    private Stack<DMReaderEvent> dmEventStack;
    private String tempFieldValue;
    private List<SchemaElement> elementStack;

    @Override
    public void read(InputStream input, InputModelBuilder inputModelBuilder, Schema inputSchema) throws ReaderException {
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
            xmlReader.setFeature(HTTP_XML_ORG_SAX_FEATURES_NAMESPACES, true);
            xmlReader.setFeature(HTTP_XML_ORG_SAX_FEATURES_NAMESPACE_PREFIXES, true);
            xmlReader.parse(new InputSource(input));
        } catch (ParserConfigurationException e) {
            throw new ReaderException("ParserConfig error. " + e.getMessage());
        } catch (SAXException e) {
            throw new ReaderException("XML not well-formed. " + e.getMessage());
        } catch (IOException e) {
            throw new ReaderException("IO Error while parsing xml input stream. " + e.getMessage());
        }
    }

    @Override
    public void startDocument() throws SAXException {
        System.out.println();
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            sendTerminateEvent();
        } catch (IOException | JSException | SchemaException | ReaderException e) {
            throw new SAXException("Error occurred while sending termination event" + e.getMessage());
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
            String tempLocalName = getNamespaceAddedFieldName(uri, localName).replace(DataMapperEngineConstants.SCHEMA_NAMESPACE_NAME_SEPARATOR, "_");
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
                        elementStack.add(new SchemaElement(attributes.getQName(attributeCount), attributes.getURI(attributeCount)));
                        String attributeType = getInputSchema().getElementTypeByName(elementStack);
                        elementStack.remove(elementStack.size() - 1);
                        String attributeFieldName = getAttributeFieldName(attributes.getQName(attributeCount), attributes.getURI(attributeCount));
                        sendFieldEvent(attributeFieldName, attributes.getValue(attributeCount), attributeType);
                    }
                }
            } else if (DataMapperEngineConstants.ARRAY_ELEMENT_TYPE.equals(elementType)) {
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
                        elementStack.add(new SchemaElement(attributes.getQName(attributeCount), attributes.getURI(attributeCount)));
                        String attributeType = getInputSchema().getElementTypeByName(elementStack);
                        elementStack.remove(elementStack.size() - 1);
                        String attributeFieldName = getAttributeFieldName(attributes.getQName(attributeCount),
                                attributes.getURI(attributeCount));
                        sendFieldEvent(attributeFieldName, attributes.getValue(attributeCount), attributeType);
                    }
                }
            } else if (DataMapperEngineConstants.OBJECT_ELEMENT_TYPE.equals(elementType)) {
                sendObjectStartEvent(localName);
                for (int attributeCount = 0; attributeCount < attributes.getLength(); attributeCount++) {
                    if (!attributes.getQName(attributeCount).contains("xmlns")) {
                        elementStack.add(new SchemaElement(attributes.getQName(attributeCount), attributes.getURI(attributeCount)));
                        String attributeType = getInputSchema().getElementTypeByName(elementStack);
                        elementStack.remove(elementStack.size() - 1);
                        String attributeFieldName = getAttributeFieldName(attributes.getQName(attributeCount),
                                attributes.getURI(attributeCount));
                        sendFieldEvent(attributeFieldName, attributes.getValue(attributeCount), attributeType);
                    }
                }
            } else if ((DataMapperEngineConstants.STRING_ELEMENT_TYPE.equals(elementType) || DataMapperEngineConstants.BOOLEAN_ELEMENT_TYPE.equals(elementType) ||
                    DataMapperEngineConstants.NUMBER_ELEMENT_TYPE.equals(elementType) || DataMapperEngineConstants.INTEGER_ELEMENT_TYPE.equals(elementType))
                    && attributes.getLength() > 0) {
                sendObjectStartEvent(localName + DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX);
                for (int attributeCount = 0; attributeCount < attributes.getLength(); attributeCount++) {
                    if (!attributes.getQName(attributeCount).contains("xmlns")) {
                        elementStack.add(new SchemaElement(attributes.getQName(attributeCount), attributes.getURI(attributeCount)));
                        String attributeType = getInputSchema().getElementTypeByName(elementStack);
                        elementStack.remove(elementStack.size() - 1);
                        String attributeFieldName = getAttributeFieldName(attributes.getQName(attributeCount)
                                , attributes.getURI(attributeCount));
                        sendFieldEvent(attributeFieldName, attributes.getValue(attributeCount), attributeType);
                    }
                }
                sendObjectEndEvent(localName + DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX);
            }
        } catch (IOException | JSException | InvalidPayloadException | SchemaException | ReaderException e) {
            throw new SAXException("Error occurred while processing start element event", e);
        }
    }

    private String getAttributeFieldName(String qName, String uri) {
        String[] qNameOriginalArray = qName.split(DataMapperEngineConstants.SCHEMA_NAMESPACE_NAME_SEPARATOR);
        qName = getNamespaceAddedFieldName(uri, qNameOriginalArray[qNameOriginalArray.length - 1]);
        String[] qNameArray = qName.split(DataMapperEngineConstants.SCHEMA_NAMESPACE_NAME_SEPARATOR);
        if (qNameArray.length > 1) {
            return qNameArray[0] + DataMapperEngineConstants.SCHEMA_NAMESPACE_NAME_SEPARATOR + DataMapperEngineConstants.SCHEMA_ATTRIBUTE_FIELD_PREFIX + qNameArray[qNameArray.length - 1];
        } else {
            return DataMapperEngineConstants.SCHEMA_ATTRIBUTE_FIELD_PREFIX + qName;
        }
    }

    private String getNamespaceAddedFieldName(String uri, String localName) {
        String prefix = getInputSchema().getPrefixForNamespace(uri);
        if (StringUtils.isNotEmpty(prefix)) {
            return prefix + DataMapperEngineConstants.SCHEMA_NAMESPACE_NAME_SEPARATOR + localName;
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
            } else if (DataMapperEngineConstants.STRING_ELEMENT_TYPE.equals(elementType)) {
                sendFieldEvent(localName, tempFieldValue, DataMapperEngineConstants.STRING_ELEMENT_TYPE);
            } else if (DataMapperEngineConstants.NUMBER_ELEMENT_TYPE.equals(elementType)) {
                sendFieldEvent(localName, tempFieldValue, DataMapperEngineConstants.NUMBER_ELEMENT_TYPE);
            } else if (DataMapperEngineConstants.BOOLEAN_ELEMENT_TYPE.equals(elementType)) {
                sendFieldEvent(localName, tempFieldValue, DataMapperEngineConstants.BOOLEAN_ELEMENT_TYPE);
            } else if (DataMapperEngineConstants.INTEGER_ELEMENT_TYPE.equals(elementType)) {
                sendFieldEvent(localName, tempFieldValue, DataMapperEngineConstants.INTEGER_ELEMENT_TYPE);
            } else if (DataMapperEngineConstants.ARRAY_ELEMENT_TYPE.equals(elementType)) {
                sendObjectEndEvent(localName);
            } else if (DataMapperEngineConstants.OBJECT_ELEMENT_TYPE.equals(elementType)) {
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
            throw new SAXException("Error occurred while processing end element event" + e.getMessage());
        } catch (InvalidPayloadException e) {
            throw new SAXException(e.getMessage());
        } catch (SchemaException e) {
            throw new SAXException(e.getMessage());
        } catch (ReaderException e) {
            throw new SAXException(e.getMessage());
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

    private void sendFieldEvent(String fieldName, String valueString, String fieldType) throws IOException, JSException, SchemaException, ReaderException {
        switch (fieldType) {
            case DataMapperEngineConstants.STRING_ELEMENT_TYPE:
                getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                        getModifiedFieldName(fieldName), valueString, fieldType));
                break;
            case DataMapperEngineConstants.BOOLEAN_ELEMENT_TYPE:
                getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                        getModifiedFieldName(fieldName), Boolean.parseBoolean(valueString), fieldType));
                break;
            case DataMapperEngineConstants.NUMBER_ELEMENT_TYPE:
                getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                        getModifiedFieldName(fieldName), Double.parseDouble((String) valueString), fieldType));
                break;
            case DataMapperEngineConstants.INTEGER_ELEMENT_TYPE:
                getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                        getModifiedFieldName(fieldName), Integer.parseInt((String) valueString), fieldType));
                break;
            default:
                getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                        getModifiedFieldName(fieldName), valueString, fieldType));
        }

        if (!fieldName.contains(DataMapperEngineConstants.SCHEMA_ATTRIBUTE_FIELD_PREFIX)) {
            elementStack.remove(elementStack.size() - 1);
        }

    }

    private void sendObjectStartEvent(String fieldName) throws IOException, JSException, SchemaException, ReaderException {
        DMReaderEvent objectStartEvent = new DMReaderEvent(ReaderEventTypes.EventType.OBJECT_START,
                getModifiedFieldName(fieldName));
        getModelBuilder().notifyEvent(objectStartEvent);
        dmEventStack.push(objectStartEvent);
    }

    private void sendObjectEndEvent(String fieldName) throws IOException, JSException, SchemaException, ReaderException {
        DMReaderEvent objectEndEvent = new DMReaderEvent(ReaderEventTypes.EventType.OBJECT_END,
                getModifiedFieldName(fieldName));
        getModelBuilder().notifyEvent(objectEndEvent);
        /*if (!getInputSchema().getName().equals(fieldName)&&
                !ARRAY_ELEMENT_TYPE.equals(getInputSchema().getElementTypeByName(fieldName))) {
            dmEventStack.pop();
        }*/
        dmEventStack.pop();
        if (!fieldName.contains(DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)) {
            elementStack.remove(elementStack.size() - 1);
        }
    }

    private void sendArrayStartEvent(String fieldName) throws IOException, JSException, SchemaException, ReaderException {
        DMReaderEvent arrayStartEvent = new DMReaderEvent(ReaderEventTypes.EventType.ARRAY_START,
                getModifiedFieldName(fieldName));
        getModelBuilder().notifyEvent(arrayStartEvent);
        dmEventStack.push(arrayStartEvent);
    }

    private void sendArrayEndEvent(String fieldName) throws IOException, JSException, SchemaException, ReaderException {
        DMReaderEvent arrayEndEvent = new DMReaderEvent(ReaderEventTypes.EventType.ARRAY_END,
                getModifiedFieldName(fieldName));
        getModelBuilder().notifyEvent(arrayEndEvent);
        dmEventStack.pop();
        elementStack.remove(elementStack.size() - 1);
    }

    public Stack<DMReaderEvent> getDmEventStack() {
        return dmEventStack;
    }

    private void sendTerminateEvent() throws IOException, JSException, SchemaException, ReaderException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.TERMINATE));
    }

    private void sendAnonymousObjectStartEvent(String fieldName) throws IOException, JSException, SchemaException, ReaderException {
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
        return fieldName.replace(DataMapperEngineConstants.SCHEMA_NAMESPACE_NAME_SEPARATOR, "_");
    }

}
