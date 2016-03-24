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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.datamapper.engine.core.Schema;
import org.wso2.datamapper.engine.core.exceptions.InvalidPayloadException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.OBJECT_ELEMENT_TYPE;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.ARRAY_ELEMENT_TYPE;

/**
 * This class implements {@link Schema} interface using Jackson JSON library to hold JSON schema
 */
public class JacksonJSONSchema implements Schema {

    private static final Log log = LogFactory.getLog(JacksonJSONSchema.class);
    Map<String, Object> jsonSchemaMap;

    private static final String PROPERTIES_KEY = "properties";
    private static final String NAMESPACE_KEY = "namespaces";
    private static final String PREFIX_KEY = "prefix";
    private static final String URL_KEY = "url";
    private static final String TYPE_KEY = "type";
    private static final String TITLE_KEY = "title";
    private static final String ITEMS_KEY = "items";
    private Map<String, String> namespaceMap;

    public JacksonJSONSchema(InputStream inputSchema) {
        namespaceMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonSchemaMap = objectMapper.readValue(inputSchema, Map.class);
            //populating name-space value map
            ArrayList<Map> namespaceElementArray = (ArrayList) jsonSchemaMap.get(NAMESPACE_KEY);
            if (namespaceElementArray != null) {
                for (Map namespaceObject : namespaceElementArray) {
                    String urlValue = (String) namespaceObject.get(URL_KEY);
                    if (!namespaceMap.containsKey(urlValue)) {
                        namespaceMap.put(urlValue, (String) namespaceObject.get(PREFIX_KEY));
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error while reading input stream");
            throw new SynapseException("Error while reading input stream"+e);
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
    public String getElementTypeByName(List<SchemaElement> elementStack) throws InvalidPayloadException {
        Map<String, Object> schema = jsonSchemaMap;
        String elementType = null;
        boolean elementFound = false;
        for (SchemaElement element : elementStack) {
            elementFound = false;
            String elementName = element.getElementName();
            String elementNamespace = element.getNamespace();
            elementName = getNamespaceAddedFieldName(elementNamespace, elementName);
            if (elementName.equals(getName())) {
                schema = (Map<String, Object>) jsonSchemaMap.get(PROPERTIES_KEY);
                elementType = (String) jsonSchemaMap.get(TYPE_KEY);
                elementFound = true;
            } else if (schema.containsKey(elementName)) {
                elementType = (String) ((Map<String, Object>) schema.get(elementName)).get(TYPE_KEY);
                if (ARRAY_ELEMENT_TYPE.equals(elementType)) {
                    schema = getSchemaItems((Map<String, Object>) schema.get(elementName));
                    schema = getSchemaProperties(schema);
                } else if (OBJECT_ELEMENT_TYPE.equals(elementType)) {
                    schema = getSchemaProperties((Map<String, Object>) schema.get(elementName));
                }
                elementFound = true;
            }
            if (!elementFound) {
                throw new IllegalArgumentException("Element name not found : " + elementName);
            }
        }
        return elementType;

    }

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
                    elementType = getElementTypeByName(elementName, getSchemaItems(subSchema));
                }
                if (elementType != null) {
                    return elementType;
                }
            }
            return null;
        }
    }

    @Override
    public boolean isChildElement(String elementName, String childElementName) {
        Map<String, Object> elementSchema = getElementSchemaByName(elementName, jsonSchemaMap);
        if (elementSchema.containsKey(PROPERTIES_KEY)) {
            if (getSchemaProperties(elementSchema).containsKey(childElementName)) {
                return true;
            }
        } else {
            if (((Map<String, Object>) getSchemaItems(elementSchema).get(PROPERTIES_KEY)).containsKey(childElementName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isChildElement(List<SchemaElement> elementStack, String childElementName) throws InvalidPayloadException {
        Map<String, Object> elementSchema = getElementSchemaByName(elementStack, jsonSchemaMap);
        if (elementSchema.containsKey(PROPERTIES_KEY)) {
            if (getSchemaProperties(elementSchema).containsKey(childElementName)) {
                return true;
            }
        } else if (elementSchema.containsKey(ITEMS_KEY)) {
            if (((Map<String, Object>) getSchemaItems(elementSchema).get(PROPERTIES_KEY)).containsKey(childElementName)) {
                return true;
            }
        } else {
            if (elementSchema.containsKey(childElementName)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, String> getNamespaceMap() {
        return namespaceMap;
    }

    private Map<String, Object> getElementSchemaByName(List<SchemaElement> elementStack, Map<String, Object> schema) throws InvalidPayloadException {
        Map<String, Object> tempSchema = schema;
        String elementType = null;
        for (SchemaElement element : elementStack) {
            String elementName = element.getElementName();
            String elementNamespace = element.getNamespace();
            elementName = getNamespaceAddedFieldName(elementNamespace, elementName);
            if (elementName.equals(getName())) {
                tempSchema = (Map<String, Object>) jsonSchemaMap.get(PROPERTIES_KEY);
                elementType = (String) jsonSchemaMap.get(TYPE_KEY);
            } else if (tempSchema.containsKey(elementName)) {
                elementType = (String) ((Map<String, Object>) tempSchema.get(elementName)).get(TYPE_KEY);
                if (ARRAY_ELEMENT_TYPE.equals(elementType)) {
                    tempSchema = getSchemaItems((Map<String, Object>) tempSchema.get(elementName));
                    tempSchema = getSchemaProperties(tempSchema);
                } else if (OBJECT_ELEMENT_TYPE.equals(elementType)) {
                    tempSchema = getSchemaProperties((Map<String, Object>) tempSchema.get(elementName));
                }
            }
        }
        return tempSchema;
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
            Object propertyList = schema.get(ITEMS_KEY);
            if (propertyList instanceof Map) {
                return (Map<String, Object>) propertyList;
            } else {
                return (Map<String, Object>) ((ArrayList) propertyList).get(0);
            }
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

    @Override
    public String getPrefixForNamespace(String url) {
        if ("".equals(url)) {
            return "";
        } else if (namespaceMap.containsKey(url)) {
            return namespaceMap.get(url);
        } else {
            return null;
        }
    }

    private String getNamespaceAddedFieldName(String uri, String localName) throws InvalidPayloadException {
        String prefix = getPrefixForNamespace(uri);
        if (StringUtils.isNotEmpty(prefix)) {
            return prefix + ":" + localName;
        } else if(prefix!= null){
            return localName;
        }else{
            throw new InvalidPayloadException(uri+ " name-space is not defined in the schema with element "+localName);
        }
    }
}
