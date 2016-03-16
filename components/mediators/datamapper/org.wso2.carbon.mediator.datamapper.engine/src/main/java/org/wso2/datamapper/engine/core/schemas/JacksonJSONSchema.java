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
package org.wso2.datamapper.engine.core.schemas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.datamapper.engine.core.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.OBJECT_ELEMENT_TYPE;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.ARRAY_ELEMENT_TYPE;

/**
 * This class implements {@link Schema} interface using Jackson JSON library to hold JSON schema
 *
 */
public class JacksonJSONSchema implements Schema {

    private static final Log log = LogFactory.getLog(JacksonJSONSchema.class);
    Map<String, Object> jsonSchemaMap;

    private static final String PROPERTIES_KEY = "properties";
    private static final String TYPE_KEY = "type";
    private static final String TITLE_KEY = "title";
    private static final String ITEMS_KEY = "items";

    public JacksonJSONSchema(InputStream inputSchema) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            jsonSchemaMap = objectMapper.readValue(inputSchema, Map.class);
        } catch (IOException e) {
            log.error("Error while reading input stream");
        }
    }

    @Override
    public String getName() {
        String schemaName = (String) jsonSchemaMap.get(TITLE_KEY);
        if (schemaName != null) {
            return schemaName;
        } else {
            throw new SynapseException("Invalid WSO2 Data Mapper JSON input schema, schema name not found.");
        }
    }

    @Override
    public String getElementTypeByName(String elementName) {
        String elementType = null;
        Map<String, Object> properties = getSchemaProperties(jsonSchemaMap);
        if (properties.containsKey(elementName)) {
            return getSchemaType((Map<String, Object>) properties.get(elementName));
        } else {
            Set<String> elementKeys = properties.keySet();
            for (String elementKey : elementKeys) {
                Map<String, Object> subSchema = (Map<String, Object>) properties.get(elementKey);
                String schemaType = getSchemaType(subSchema);
                if (OBJECT_ELEMENT_TYPE.equals(schemaType)) {
                    elementType = getElementTypeByName(elementName, subSchema);
                } else if (ARRAY_ELEMENT_TYPE.equals(schemaType)) {
                    elementType = getElementTypeByName(elementName,  getSchemaItems(subSchema));
                }
                if (elementType != null) {
                    return elementType;
                }
            }
            throw new IllegalArgumentException("Given schema does not contain element under name : " + elementName);
        }
    }

    @Override
    public boolean isChildElement(String elementName, String childElementName) {
        Map<String, Object> elementSchema =getElementSchemaByName(elementName, jsonSchemaMap);
        if(elementSchema.containsKey(PROPERTIES_KEY)) {
            if (getSchemaProperties(elementSchema).containsKey(childElementName)) {
                return true;
            }
        } else{
            if ((( Map<String, Object>)getSchemaItems(elementSchema).get(PROPERTIES_KEY)).containsKey(childElementName)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> getElementSchemaByName(String elementName, Map<String, Object> schema) {
        Map<String, Object> elementType = null;
        Map<String, Object> properties = getSchemaProperties(schema);
        if (properties.containsKey(elementName)) {
            return (Map<String, Object>) properties.get(elementName);
        } else {
            Set<String> elementKeys = properties.keySet();
            for (String elementKey : elementKeys) {
                Map<String, Object> subSchema = (Map<String, Object>) properties.get(elementKey);
                String schemaType = getSchemaType(subSchema);
                if (OBJECT_ELEMENT_TYPE.equals(schemaType)) {
                    elementType = getElementSchemaByName(elementName, subSchema);
                } else if (ARRAY_ELEMENT_TYPE.equals(schemaType)) {
                    elementType = getElementSchemaByName(elementName, getSchemaItems(subSchema));
                }
                if (elementType != null) {
                    return elementType;
                }
            }
            return null;
        }
    }

    private String getElementTypeByName(String elementName, Map<String, Object> schema) {
        String elementType = null;
        Map<String, Object> properties = getSchemaProperties(schema);
        if (properties.containsKey(elementName)) {
            return getSchemaType((Map<String, Object>) properties.get(elementName));
        } else {
            Set<String> elementKeys = properties.keySet();
            for (String elementKey : elementKeys) {
                Map<String, Object> subSchema = (Map<String, Object>) properties.get(elementKey);
                String schemaType = getSchemaType(subSchema);
                if (OBJECT_ELEMENT_TYPE.equals(schemaType)) {
                    elementType = getElementTypeByName(elementName, subSchema);
                } else if (ARRAY_ELEMENT_TYPE.equals(schemaType)) {
                    elementType = getElementTypeByName(elementName, getSchemaItems(subSchema));
                }
                if (elementType != null) {
                    return elementType;
                }
            }
            return null;
        }
    }

    private Map<String, Object> getSchemaProperties(Map<String, Object> schema) {
        if (schema.containsKey(PROPERTIES_KEY)) {
            return (Map<String, Object>) schema.get(PROPERTIES_KEY);
        } else {
            throw new IllegalArgumentException("Given schema does not contain value under key : " + PROPERTIES_KEY);
        }
    }

    private Map<String, Object> getSchemaItems(Map<String, Object> schema) {
        if (schema.containsKey(ITEMS_KEY)) {
            return (Map<String, Object>) schema.get(ITEMS_KEY);
        } else {
            throw new IllegalArgumentException("Given schema does not contain value under key : " + ITEMS_KEY);
        }
    }

    private String getSchemaType(Map<String, Object> schema) {
        if (schema.containsKey(TYPE_KEY)) {
            Object type = schema.get(TYPE_KEY);
            if (type instanceof String) {
                return (String) type;
            } else {
                throw new IllegalArgumentException("Illegal format " + type.getClass() + " value found under key : " + TYPE_KEY);
            }
        } else {
            throw new IllegalArgumentException("Given schema does not contain value under key : " + TYPE_KEY);
        }
    }
}
