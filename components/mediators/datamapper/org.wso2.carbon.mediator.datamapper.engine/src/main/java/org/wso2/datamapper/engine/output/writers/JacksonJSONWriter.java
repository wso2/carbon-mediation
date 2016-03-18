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
import org.wso2.datamapper.engine.output.Writable;

import java.io.IOException;
import java.io.StringWriter;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.OBJECT_ELEMENT_TYPE;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.STRING_ELEMENT_TYPE;

/**
 * This class implements {@link Writable} interface and json writer for data mapper engine using Jackson
 */
public class JacksonJSONWriter implements Writable {

    private static final Log log = LogFactory.getLog(XMLWriter.class);
    private Schema outputSchema;
    private JsonGenerator jsonGenerator;
    private StringWriter writer;

    public JacksonJSONWriter(Schema outputSchema) {
        this.outputSchema = outputSchema;
        JsonFactory jsonFactory = new JsonFactory();
        writer = new StringWriter();
        try {
            jsonGenerator = jsonFactory.createGenerator(writer);
            jsonGenerator.writeStartObject();
        } catch (IOException e) {
            log.error("Error while creating json generator" + e);
        }
    }

    @Override
    public void writeStartObject(String name) {
        try {
            String schemaName=name;
            if (name.endsWith(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)) {
                schemaName = name.substring(0, name.lastIndexOf(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX));
            }
            String type = outputSchema.getElementTypeByName(schemaName);
            if (OBJECT_ELEMENT_TYPE.equals(type)) {
                jsonGenerator.writeObjectFieldStart(name);
            } else if(STRING_ELEMENT_TYPE.equals(type)){
                jsonGenerator.writeObjectFieldStart(name);
            } else{
                jsonGenerator.writeArrayFieldStart(name);
                jsonGenerator.writeStartObject();
            }
        } catch (IOException e) {
            log.error("Error while creating starting object" + e);
        }
    }

    @Override
    public void writeField(String name, String value) {
        try {
            jsonGenerator.writeStringField(name, value);
        } catch (IOException e) {
            log.error("Error while creating writing field" + e);
        }
    }

    @Override
    public void writeEndObject() {
        try {
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            log.error("Error while creating ending object" + e);
        }
    }

    @Override
    public String terminateMessageBuilding() {
        String inputJSVariable = null;
        try {
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
        System.out.println();
    }

    @Override
    public void writeEndArray() {
        try {
            jsonGenerator.writeEndArray();
        } catch (IOException e) {
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
