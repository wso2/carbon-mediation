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
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.datamapper.engine.core.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 */
public class JacksonJSONSchema implements Schema<JsonSchema> {

    private static final Log log = LogFactory.getLog(JacksonJSONSchema.class);
    Map<String, Object> jsonSchemaMap;

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
        String schemaName = (String) jsonSchemaMap.get("title");
        if(schemaName!=null) {
            return schemaName;
        }else{
            throw new SynapseException("Invalid JSON input schema, schema name not found.");
        }
    }
}
