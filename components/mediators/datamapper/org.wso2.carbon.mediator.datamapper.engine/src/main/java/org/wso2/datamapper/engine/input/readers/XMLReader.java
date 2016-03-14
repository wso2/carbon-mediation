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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.datamapper.engine.core.Schema;
import org.wso2.datamapper.engine.core.exceptions.JSException;
import org.wso2.datamapper.engine.input.InputModelBuilder;
import org.wso2.datamapper.engine.input.readers.events.DMReaderEvent;
import org.wso2.datamapper.engine.types.ReaderEventTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.events.StartElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.OBJECT_ELEMENT_TYPE;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.ARRAY_ELEMENT_TYPE;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.STRING_ELEMENT_TYPE;

/**
 *  This class implements {@link Readable} interface and xml reader for data mapper engine using SAX
 */
public class XMLReader extends DefaultHandler implements org.wso2.datamapper.engine.input.Readable {

    private static final Log log = LogFactory.getLog(XMLReader.class);
    private InputModelBuilder modelBuilder;
    private Schema inputSchema;
    private Stack<DMReaderEvent> dmEventStack;
    private String tempFieldValue;
    private boolean arrayElementStarted;

    public XMLReader() {
        dmEventStack = new Stack<>();
    }

    @Override
    public void read(InputStream input, InputModelBuilder inputModelBuilder, Schema inputSchema) {
        modelBuilder = inputModelBuilder;
        this.inputSchema = inputSchema;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(input, this);
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
        // do nothing
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
        // no op
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // no op
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if (!getDmEventStack().isEmpty()) {
                DMReaderEvent stackElement = getDmEventStack().peek();
                if (ReaderEventTypes.EventType.ARRAY_START.equals(stackElement.getEventType()) &&
                        !(getInputSchema().isChildElement(stackElement.getName(), qName) ||
                                qName.equals(stackElement.getName()))) {
                    sendArrayEndEvent(qName);
                }
            }

            if (qName.equals(getInputSchema().getName())) {
                sendAnonymousObjectStartEvent();
            } else if (ARRAY_ELEMENT_TYPE.equals(getInputSchema().getElementTypeByName(qName))) {
                //first element of a array should fire array start element
                if (!getDmEventStack().isEmpty()) {
                    DMReaderEvent stackElement = getDmEventStack().peek();
                    if (!(ReaderEventTypes.EventType.ARRAY_START.equals(stackElement.getEventType()) &&
                            qName.equals(stackElement.getName()))) {
                        sendArrayStartEvent(qName);
                    }
                } else {
                    sendArrayStartEvent(qName);
                }
                sendAnonymousObjectStartEvent();
            } else if (OBJECT_ELEMENT_TYPE.equals(getInputSchema().getElementTypeByName(qName))) {
                sendObjectStartEvent(qName);
            }
        } catch (IOException|JSException e) {
            log.error("Error occurred while processing start element event", e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (qName.equals(getInputSchema().getName())) {
                sendObjectEndEvent(qName);
            } else if (STRING_ELEMENT_TYPE.equals(getInputSchema().getElementTypeByName(qName))) {
                sendFieldEvent(qName, tempFieldValue);
            } else if (ARRAY_ELEMENT_TYPE.equals(getInputSchema().getElementTypeByName(qName))) {
                sendObjectEndEvent(qName);
            } else if (OBJECT_ELEMENT_TYPE.equals(getInputSchema().getElementTypeByName(qName))) {
                sendObjectEndEvent(qName);
            }
        } catch (IOException|JSException e) {
            log.error("Error occurred while processing end element event", e);
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

    private void sendFieldEvent(String fieldName, String value) throws IOException, JSException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                fieldName, value));
    }

    private void sendObjectStartEvent(String fieldName) throws IOException, JSException {
        DMReaderEvent objectStartEvent = new DMReaderEvent(ReaderEventTypes.EventType.OBJECT_START,
                fieldName, null);
        getModelBuilder().notifyEvent(objectStartEvent);
        dmEventStack.push(objectStartEvent);
    }

    private void sendObjectEndEvent(String fieldName) throws IOException, JSException {
        DMReaderEvent objectEndEvent = new DMReaderEvent(ReaderEventTypes.EventType.OBJECT_END,
                fieldName, null);
        getModelBuilder().notifyEvent(objectEndEvent);
        if (!ARRAY_ELEMENT_TYPE.equals(getInputSchema().getElementTypeByName(fieldName))) {
            dmEventStack.pop();
        }
    }

    private void sendArrayStartEvent(String fieldName) throws IOException, JSException {
        DMReaderEvent arrayStartEvent = new DMReaderEvent(ReaderEventTypes.EventType.ARRAY_START,
                fieldName, null);
        getModelBuilder().notifyEvent(arrayStartEvent);
        dmEventStack.push(arrayStartEvent);
    }

    private void sendArrayEndEvent(String fieldName) throws IOException, JSException {
        DMReaderEvent arrayEndEvent = new DMReaderEvent(ReaderEventTypes.EventType.ARRAY_END,
                fieldName, null);
        getModelBuilder().notifyEvent(arrayEndEvent);
        dmEventStack.pop();
    }

    public Stack<DMReaderEvent> getDmEventStack() {
        return dmEventStack;
    }

    private void sendTerminateEvent() throws IOException, JSException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.TERMINATE,

                null, null));
    }

    private void sendAnonymousObjectStartEvent() throws IOException, JSException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.ANONYMOUS_OBJECT_START,
                null, null));
    }


    public InputModelBuilder getModelBuilder() {
        return modelBuilder;
    }

    private String getFieldName(StartElement startElement) {
        String fieldName = startElement.getName().getLocalPart();
        return fieldName;
    }

}
