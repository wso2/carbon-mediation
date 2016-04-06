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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.InvalidPayloadException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SimpleJSONParserException;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.SchemaElement;
import org.wso2.carbon.mediator.datamapper.engine.input.InputModelBuilder;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.events.ReaderEvent;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.events.ReaderEventType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.ARRAY_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.BOOLEAN_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.INTEGER_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.NULL_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.NUMBER_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.OBJECT_ELEMENT_TYPE;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.STRING_ELEMENT_TYPE;

/**
 * This is a JSON reader implementation based on JSON Simple library.
 */
public class JSONReader implements Reader, ContentHandler {
    private static final Log log = LogFactory.getLog(JSONReader.class);
    private InputModelBuilder modelBuilder;
    private Schema inputSchema;
    private Stack<ReaderEvent> readerEventStack;
    private List<SchemaElement> schemaElementList;

    public JSONReader() {
        this.readerEventStack = new Stack<>();
        this.schemaElementList = new ArrayList<>();
    }

    public Schema getInputSchema() {
        return inputSchema;
    }

    @Override
    public void read(InputStream input, InputModelBuilder inputModelBuilder, Schema inputSchema) throws ReaderException {

        this.modelBuilder = inputModelBuilder;
        this.inputSchema = inputSchema;
        java.io.Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();
        try {
            parser.parse(reader, this);
        } catch (IOException e) {
            throw new ReaderException("IO Error while parsing JSON input stream. " + e.getMessage());
        } catch (ParseException e) {
            throw new ReaderException("Error while parsing JSON input stream. " + e.getMessage());
        }
    }

    @Override
    public void startJSON() throws ParseException, IOException {
    }

    @Override
    public void endJSON() throws ParseException, IOException {
        try {
            sendTerminateEvent();
        } catch (IOException | JSException | SchemaException | ReaderException e) {
            throw new SimpleJSONParserException("Error occurred while sending termination event. " + e.getMessage());
        }
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        try {
            if (!getReaderEventStack().isEmpty()) {
                ReaderEvent stackElement = getReaderEventStack().peek();
                String type = getInputSchema().getElementTypeByName(schemaElementList);
                if (ReaderEventType.OBJECT_START.equals(stackElement.getEventType())) {
                    if (ARRAY_ELEMENT_TYPE.equals(type)) {
                        throw new SimpleJSONParserException("Schema specifies an array of type " + type + ". But payload doesn't contain an array.");
                    }
                    sendObjectStartEvent(stackElement.getName());
                    return true;
                }
            } else {
                schemaElementList.add(new SchemaElement(getInputSchema().getName()));
            }
            sendAnonymousObjectStartEvent();
        } catch (JSException | InvalidPayloadException | SchemaException | ReaderException e) {
            throw new SimpleJSONParserException("Error occurred while processing start object event. " + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        if (!getReaderEventStack().isEmpty()) {
            ReaderEvent stackElement = getReaderEventStack().peek();
            try {
                sendObjectEndEvent(stackElement.getName());
            } catch (JSException | InvalidPayloadException | SchemaException | ReaderException e) {
                throw new SimpleJSONParserException("Error occurred while processing end object event. " + e.getMessage());
            }
        }
        return true;
    }

    @Override
    public boolean startObjectEntry(String s) throws ParseException, IOException {
        try {
            schemaElementList.add(new SchemaElement(s));
            String type = getInputSchema().getElementTypeByName(schemaElementList);
            if (ARRAY_ELEMENT_TYPE.equals(type)) {
                pushObjectStartEvent(s);
            } else if (OBJECT_ELEMENT_TYPE.equals(type)) {
                pushObjectStartEvent(s);
            } else if (STRING_ELEMENT_TYPE.equals(type) || BOOLEAN_ELEMENT_TYPE.equals(type)
                    || NUMBER_ELEMENT_TYPE.equals(type) || INTEGER_ELEMENT_TYPE.equals(type)
                    || NULL_ELEMENT_TYPE.equals(type)) {
                pushObjectStartEvent(s);
            }
        } catch (JSException | InvalidPayloadException | SchemaException e) {
            throw new SimpleJSONParserException("Error occurred while processing start element event. " + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        if (!getReaderEventStack().isEmpty()) {
            ReaderEvent stackElement = getReaderEventStack().peek();
            try {
                popObjectEndEvent(stackElement.getName());
                schemaElementList.remove(schemaElementList.size() - 1);
            } catch (JSException | InvalidPayloadException | SchemaException e) {
                throw new SimpleJSONParserException("Error while sending end object entry. " + e.getMessage());
            }
        }
        return true;
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        try {
            if (!getReaderEventStack().isEmpty()) {
                ReaderEvent stackElement = getReaderEventStack().peek();
                String type = getInputSchema().getElementTypeByName(schemaElementList);

                if (ARRAY_ELEMENT_TYPE.equals(type)) {
                    try {
                        sendArrayStartEvent(stackElement.getName());
                    } catch (JSException | SchemaException | ReaderException e) {
                        throw new SimpleJSONParserException("Error occurred while processing start array event. " + e.getMessage());
                    }
                } else {
                    throw new SimpleJSONParserException("Found an array in the payload but schema doesn't specify any array of type " + type);
                }
            }
        } catch (InvalidPayloadException | SchemaException e) {
            throw new SimpleJSONParserException("Error while sending array start entry. " + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        if (!getReaderEventStack().isEmpty()) {
            ReaderEvent stackElement = getReaderEventStack().peek();
            String type = null;
            try {
                type = getInputSchema().getElementTypeByName(schemaElementList);
            } catch (InvalidPayloadException | SchemaException e) {
                throw new SimpleJSONParserException(e.getMessage());
            }
            if (ARRAY_ELEMENT_TYPE.equals(type)) {
                try {
                    sendArrayEndEvent(stackElement.getName());
                } catch (JSException | SchemaException | ReaderException e) {
                    throw new SimpleJSONParserException("Error occurred while processing end array event. " + e.getMessage());
                }
            } else {
                throw new SimpleJSONParserException("Array element not found " + type);
            }
        }
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        if (!getReaderEventStack().isEmpty()) {
            ReaderEvent stackElement = getReaderEventStack().peek();
            try {
                String fieldType = getFieldType(value);
                sendFieldEvent(stackElement.getName(), value, fieldType);
            } catch (JSException | SchemaException | ReaderException e) {
                throw new SimpleJSONParserException("Error while sending field value. " + e.getMessage());
            }
        }
        return true;
    }

    private String getFieldType(Object value) {
        if (value instanceof String) {
            return STRING_ELEMENT_TYPE;
        } else if (value instanceof Integer || value instanceof Long) {
            return INTEGER_ELEMENT_TYPE;
        } else if (value instanceof Double || value instanceof Float) {
            return NUMBER_ELEMENT_TYPE;
        } else if (value instanceof Boolean) {
            return BOOLEAN_ELEMENT_TYPE;
        }
        throw new IllegalArgumentException("Unsupported value type found" + value.toString());
    }

    public InputModelBuilder getModelBuilder() {
        return modelBuilder;
    }

    private void sendFieldEvent(String fieldName, Object value, String type) throws IOException, JSException,
            SchemaException, ReaderException {
        ReaderEvent fieldEvent = new ReaderEvent(ReaderEventType.FIELD,
                fieldName, value, type);
        getModelBuilder().notifyEvent(fieldEvent);
    }

    private void pushObjectStartEvent(String fieldName) throws IOException, JSException {
        ReaderEvent objectStartEvent = new ReaderEvent(ReaderEventType.OBJECT_START, fieldName, null);
        readerEventStack.push(objectStartEvent);
    }

    private void sendObjectStartEvent(String fieldName) throws IOException, JSException, SchemaException, ReaderException {
        ReaderEvent objectStartEvent = new ReaderEvent(ReaderEventType.OBJECT_START, fieldName, null);
        getModelBuilder().notifyEvent(objectStartEvent);
        readerEventStack.push(objectStartEvent);
    }

    private void sendObjectEndEvent(String fieldName) throws IOException, JSException, InvalidPayloadException, SchemaException, ReaderException {
        ReaderEvent objectEndEvent = new ReaderEvent(ReaderEventType.OBJECT_END, fieldName, null);
        getModelBuilder().notifyEvent(objectEndEvent);
        if (fieldName != null) {
            if (!ARRAY_ELEMENT_TYPE.equals(getInputSchema().getElementTypeByName(schemaElementList))) {
                readerEventStack.pop();
            }
        } else {
            readerEventStack.pop();
        }
    }

    private void popObjectEndEvent(String fieldName) throws IOException, JSException, InvalidPayloadException,
            SchemaException {
        ReaderEvent objectEndEvent = new ReaderEvent(ReaderEventType.OBJECT_END, fieldName, null);
        if (!ARRAY_ELEMENT_TYPE.equals(getInputSchema().getElementTypeByName(schemaElementList))
                || fieldName.equals(objectEndEvent.getName())) {
            readerEventStack.pop();
        }
    }

    private void sendArrayStartEvent(String fieldName) throws IOException, JSException, SchemaException, ReaderException {
        ReaderEvent arrayStartEvent = new ReaderEvent(ReaderEventType.ARRAY_START,
                fieldName, null);
        getModelBuilder().notifyEvent(arrayStartEvent);
        readerEventStack.push(arrayStartEvent);
    }

    private void sendArrayEndEvent(String fieldName) throws IOException, JSException, SchemaException, ReaderException {
        ReaderEvent arrayEndEvent = new ReaderEvent(ReaderEventType.ARRAY_END, fieldName, null);
        getModelBuilder().notifyEvent(arrayEndEvent);
        readerEventStack.pop();
    }

    public Stack<ReaderEvent> getReaderEventStack() {
        return readerEventStack;
    }

    private void sendTerminateEvent() throws IOException, JSException, SchemaException, ReaderException {
        getModelBuilder().notifyEvent(new ReaderEvent(ReaderEventType.TERMINATE, null, null));
        if (schemaElementList.size() != 1) {
            throw new ReaderException("schemaElementList contain more than one value in the end : " + schemaElementList.size());
        } else {
            schemaElementList.remove(0);
        }
    }

    private void sendAnonymousObjectStartEvent() throws IOException, JSException, SchemaException, ReaderException {
        ReaderEvent anonymousObjectStartEvent = new ReaderEvent(ReaderEventType.ANONYMOUS_OBJECT_START, null, null);
        getModelBuilder().notifyEvent(anonymousObjectStartEvent);
        readerEventStack.push(anonymousObjectStartEvent);
    }
}