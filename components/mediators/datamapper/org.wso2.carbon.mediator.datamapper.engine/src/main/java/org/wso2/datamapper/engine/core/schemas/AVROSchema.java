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

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 */
public class AVROSchema implements org.wso2.datamapper.engine.core.Schema {

    private Schema schema;

    public AVROSchema(InputStream inputSchema) throws IOException {
        getAvroSchema(inputSchema);
    }

    private void getAvroSchema(InputStream inputSchema) throws IOException {
        schema = new Parser().parse(inputSchema);
    }

    @Override
    public String getName() {
        return schema.getName();
    }

    @Override
    public String getElementTypeByName(List<SchemaElement> elementStack) {
        return null;
    }

    @Override
    public String getElementTypeByName(String elementName) {
        return null;
    }

    @Override
    public boolean isChildElement(String elementName, String childElementName) {
        return false;
    }

    @Override
    public boolean isChildElement(List<SchemaElement> elementStack, String childElementName) {
        return false;
    }

    @Override
    public String getPrefixForNamespace(String url) {
        return null;
    }

}
