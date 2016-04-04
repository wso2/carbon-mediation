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
package org.wso2.datamapper.engine.output.writers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.datamapper.engine.core.Schema;
import org.wso2.datamapper.engine.core.exceptions.InvalidPayloadException;
import org.wso2.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.datamapper.engine.core.exceptions.WriterException;
import org.wso2.datamapper.engine.core.schemas.SchemaElement;
import org.wso2.datamapper.engine.output.Writable;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.OBJECT_ELEMENT_TYPE;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.STRING_ELEMENT_TYPE;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.ARRAY_ELEMENT_TYPE;

/**
 * This class implements {@link Writable} interface and json writer for data mapper engine using Jackson
 */
public class JacksonJSONWriter implements Writable {

    private static final Log log = LogFactory.getLog(XMLWriter.class);
    private Schema outputSchema;
    private JsonGenerator jsonGenerator;
    private StringWriter writer;
    private List<SchemaElement> elementStack;

    public JacksonJSONWriter(Schema outputSchema) throws SchemaException {
        this.outputSchema = outputSchema;
        JsonFactory jsonFactory = new JsonFactory();
        writer = new StringWriter();
        elementStack = new ArrayList<>();
        elementStack.add(new SchemaElement(outputSchema.getName()));
        try {
            jsonGenerator = jsonFactory.createGenerator(writer);
            writeStartAnonymousObject();
        } catch (IOException e) {
            log.error("Error while creating json generator" + e);
        }
    }

    @Override
    public void writeStartObject(String name) throws WriterException {
        try {
            String schemaName = name;
            if (name.endsWith(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)) {
                schemaName = name.substring(0, name.lastIndexOf(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX));
            }
            elementStack.add(new SchemaElement(schemaName));
            String type = null;
            try {
                type = outputSchema.getElementTypeByName(elementStack);
            } catch (InvalidPayloadException | SchemaException e) {
                throw new WriterException(e.getMessage());
            }
            if (OBJECT_ELEMENT_TYPE.equals(type)) {
                jsonGenerator.writeObjectFieldStart(name);
            } else if (STRING_ELEMENT_TYPE.equals(type)) {
                jsonGenerator.writeObjectFieldStart(name);
            } else {
                jsonGenerator.writeArrayFieldStart(name);
                jsonGenerator.writeStartObject();
            }
        } catch (IOException e) {
            log.error("Error while creating starting object" + e);
        }
    }

    @Override
    public void writeField(String name, Object value) {
        try {
            if (value instanceof String) {
                jsonGenerator.writeStringField(name, (String) value);
            } else if (value instanceof Integer) {
                jsonGenerator.writeNumberField(name, (Integer) value);
            } else if (value instanceof Double) {
                jsonGenerator.writeNumberField(name, (Double) value);
            } else if (value instanceof Boolean) {
                jsonGenerator.writeBooleanField(name, (Boolean) value);
            }
        } catch (IOException e) {
            log.error("Error while creating writing field" + e);
        }
    }

    @Override
    public void writeEndObject(String objectName) {
        try {
            if (elementStack.get(elementStack.size() - 1).getElementName().equals(objectName)) {
                if ((!ARRAY_ELEMENT_TYPE.equals(outputSchema.getElementTypeByName(elementStack)) && !elementStack.isEmpty())) {
                    elementStack.remove(elementStack.size() - 1);
                    jsonGenerator.writeEndObject();
                } else {
                    jsonGenerator.writeEndObject();
                }
            }
        } catch (IOException e) {
            log.error("Error while creating ending object" + e);
        } catch (SchemaException e) {
            log.error(e.getMessage(), e);
        } catch (InvalidPayloadException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String terminateMessageBuilding() {
        String inputJSVariable = null;
        try {
            writeEndObject(null);
            jsonGenerator.close();
            inputJSVariable = writer.toString();
            writer.close();
            return inputJSVariable;
        } catch (IOException e) {
            log.error("Error while creating terminating message building" + e);
        }
        return inputJSVariable;
    }

    @Override
    public void writeStartArray() {
        //no implementation
    }

    @Override
    public void writeEndArray() {
        try {
            elementStack.remove(elementStack.size() - 1);
            jsonGenerator.writeEndArray();
        } catch (IOException e) {
            try {
                jsonGenerator.writeEndObject();
                jsonGenerator.writeEndArray();
            } catch (IOException e1) {
                log.error(e.getMessage(), e);
            }
            log.error("Error while creating end array" + e);
        }
    }

    @Override
    public void writeStartAnonymousObject() {
        try {
            jsonGenerator.writeStartObject();
        } catch (IOException e) {
            log.error("Error while creating anonymous object " + e);
        }
    }
}
