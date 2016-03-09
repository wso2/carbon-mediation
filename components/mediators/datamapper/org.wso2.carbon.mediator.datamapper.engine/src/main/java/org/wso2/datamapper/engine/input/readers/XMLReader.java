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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.Stack;

/**
 *
 */
public class XMLReader implements org.wso2.datamapper.engine.input.Readable {

    private static final Log log = LogFactory.getLog(XMLReader.class);
    private InputModelBuilder modelBuilder;
    private Schema inputSchema;
    private Stack<XMLEvent> saxEventStack;
    private Schema currentSchema;

    public XMLReader() {
        saxEventStack = new Stack<>();
    }

    @Override
    public void read(InputStream input,InputModelBuilder inputModelBuilder,Schema inputSchema) {
        modelBuilder = inputModelBuilder;
        this.inputSchema = inputSchema;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLEventReader eventReader = factory.createXMLEventReader(input);
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        saxEventStack.add(event);
                        break;
                    case XMLStreamConstants.START_DOCUMENT:
                        saxEventStack.add(event);
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        saxEventStack.add(event);
                        break;
                    case XMLStreamConstants.NAMESPACE:
                        saxEventStack.add(event);
                        break;
                    case XMLStreamConstants.ATTRIBUTE:
                        saxEventStack.add(event);
                        break;
                    case XMLStreamConstants.CDATA:
                        saxEventStack.add(event);
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        if (!(XMLStreamConstants.START_DOCUMENT == saxEventStack.pop().getEventType())) {
                            throw new IllegalArgumentException("END_DOCUMENT event received middle of a document");
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (!(XMLStreamConstants.START_ELEMENT == saxEventStack.pop().getEventType())) {
                            throw new IllegalArgumentException("END_ELEMENT event received with out a START_ELEMENT");
                        }
                        break;
                    case XMLStreamConstants.DTD:
                        saxEventStack.add(event);
                        break;
                }
            }
        } catch (XMLStreamException e) {
            log.error("Error while parsing xml input stream.", e);
        }
    }

    private void sendFieldEvent(Characters characters) {
        getModelBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD, getFieldName(saxEventStack.peek().asStartElement()), characters.getData()));
    }

    public InputModelBuilder getModelBuilder() {
        return modelBuilder;
    }

    private String getFieldName(StartElement startElement) {
        String fieldName = startElement.getName().getLocalPart();
        return fieldName;
    }
}
