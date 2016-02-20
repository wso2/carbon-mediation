/*
 * Copyright 2014,2016 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.datamapper.engine.inputAdapters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Input reader class for JSON.
 */
public class JsonInputReader implements InputDataReaderAdapter {
    private static final String ANONYMOUS_ROOT_ID = "AnonymousRootNode";
    private static final String NULL = "null";
    private InputStream inputStream;
    private JsonParser jsonParser;
    private ObjectMapper mapper;

    /* (non-Javadoc)
     * @see org.wso2.datamapper.engine.inputAdapter.InputDataReaderAdapter#setInputMsg(java.io.InputStream)
     */
    public void setInputMsg(InputStream msg) {
        this.inputStream = msg;
        mapper = new ObjectMapper();
        JsonFactory jsonFactory = mapper.getJsonFactory();
        jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        jsonFactory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        try {
            jsonParser = jsonFactory.createJsonParser(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method will return a GenericRecord from provided JSON
     * @param input
     * @return
     */
    public GenericRecord getInputRecord(Schema input) {
        JsonNode jsonNode = null;
        try {
            // FIXME If schema contains anonymous root added by graphical
            // editor, then add the same to payload
            if (ANONYMOUS_ROOT_ID.equals(input.getName())) {
                ObjectNode anonymousNode = mapper.createObjectNode();
                anonymousNode.put(ANONYMOUS_ROOT_ID, jsonParser.readValueAsTree());
                jsonNode = anonymousNode.get(input.getName());
            } else {
                // outer json is not part of data, extract child element only
                jsonNode = jsonParser.readValueAsTree().get(input.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return getChild(input, jsonNode.getFields());
    }

    /**
     * Recursive method to traverse JSON tree and create GenericRecord
     * @param input
     * @param jsonNodeIterator
     * @return
     */
    private GenericRecord getChild(Schema input, Iterator<Map.Entry<String, JsonNode>> jsonNodeIterator) {
        GenericRecord result = new GenericData.Record(input);
        while (jsonNodeIterator.hasNext()) {
            try {
                Map.Entry<String, JsonNode> jsonNode = jsonNodeIterator.next();
                String localName = jsonNode.getKey();
                Schema.Field field = input.getField(localName);

                if (field != null) {
                    if (field.schema().getType().equals(Schema.Type.ARRAY)) {
                        //TODO : implementation for Arrays needs to be done
                    } else if (field.schema().getType().equals(Schema.Type.RECORD)) {
                        Iterator childElements = jsonNode.getValue().getFields();
                        GenericRecord child = getChild(field.schema(), childElements);
                        result.put(localName, child);
                    } else if (field.schema().getType().equals(Schema.Type.UNION)) {
                        Iterator childElements = jsonNode.getValue().getFields();
                        Schema childSchema = field.schema();
                        if (childSchema != null) {
                            List<Schema> childFieldList = childSchema.getTypes();
                            Iterator childFields = childFieldList.iterator();
                            while (childFields.hasNext()) {
                                Schema chSchema = (Schema) childFields.next();
                                String scName = chSchema.getName();
                                if (!NULL.equals(scName)) {
                                    GenericRecord child = getChild(chSchema, childElements);
                                    result.put(localName, child);
                                } else {
                                    continue;
                                }
                            }
                        }
                    } else {
                        result.put(localName, jsonNode.getValue().getTextValue());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static String getType() {
        return "application/json";
    }

}
