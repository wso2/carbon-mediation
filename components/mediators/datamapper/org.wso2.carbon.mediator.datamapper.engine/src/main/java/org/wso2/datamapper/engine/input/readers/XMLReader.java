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

/**
 *
 */
public class XMLReader extends DefaultHandler implements org.wso2.datamapper.engine.input.Readable {

    private static final Log log = LogFactory.getLog(XMLReader.class);
    private InputModelBuilder modelBuilder;
    private Schema inputSchema;
    private Stack<String> saxEventStack;
    private Schema currentSchema;
    private String tempFieldValue;

    public XMLReader() {
        saxEventStack = new Stack<>();
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
        } catch (IOException e) {
            e.printStackTrace();
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
            if (qName.equals("employee")) {

                sendAnonimusObjectStartEvent();
            } else if (qName.equals("employees")) {
                //sendObjectStartEvent(qName);
                sendAnonimusObjectStartEvent();
                sendArrayStartEvent("employee");
            }else if (qName.equals("address")) {
                //sendObjectStartEvent(qName);
                sendObjectStartEvent("address");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (qName.equals("firstname")) {
                sendFieldEvent(qName, tempFieldValue);
            } else if (qName.equals("lastname")) {
                sendFieldEvent(qName, tempFieldValue);
            } else if (qName.equals("no")) {
                sendFieldEvent(qName, tempFieldValue);
            } else if (qName.equals("road")) {
                sendFieldEvent(qName, tempFieldValue);
            } else if (qName.equals("city")) {
                sendFieldEvent(qName, tempFieldValue);
            } else if (qName.equals("employee")) {
                sendObjectEndEvent(qName);
            } else if (qName.equals("employees")) {
                sendArrayEndEvent(qName);
                sendObjectEndEvent(qName);
            }else if (qName.equals("address")) {
                sendObjectEndEvent(qName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        String value = (new String(ch)).substring(start, start + length);
        tempFieldValue = value;
        System.out.println(value);
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
            throws SAXException {
        // no op
    }


    private void sendFieldEvent(String fieldName, String value) throws IOException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD,
                fieldName, value));
    }

    private void sendObjectStartEvent(String fieldName) throws IOException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.OBJECT_START,
                fieldName, null));
    }

    private void sendObjectEndEvent(String fieldName) throws IOException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.OBJECT_END,
                fieldName, null));
    }

    private void sendArrayStartEvent(String fieldName) throws IOException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.ARRAY_START,
                fieldName, null));
    }

    private void sendArrayEndEvent(String fieldName) throws IOException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.ARRAY_END,
                fieldName, null));
    }

    private void sendTerminateEvent() throws IOException {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.TERMINATE,
                null, null));
    }

    private void sendAnonimusObjectStartEvent() throws IOException {
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
