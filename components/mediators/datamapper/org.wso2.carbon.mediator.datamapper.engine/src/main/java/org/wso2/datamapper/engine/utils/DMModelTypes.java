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
package org.wso2.datamapper.engine.utils;

/**
 *
 */
public class DMModelTypes {

    private final static String JSON_STRING_CONTENT_TYPE = "JSON_STRING";
    private final static String JAVA_MAP_CONTENT_TYPE = "MAP";


    // Use to define input and output data formats
    public enum ModelType {
        JSON_STRING(JSON_STRING_CONTENT_TYPE),
        JAVA_MAP(JAVA_MAP_CONTENT_TYPE);

        private final String value;

        private ModelType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        // Use to get the DataType from the relevant input and output data type
        public static ModelType fromString(String dataType) {
            if (dataType != null) {
                for (ModelType definedTypes : ModelType.values()) {
                    if (dataType.equalsIgnoreCase(definedTypes.toString())) {
                        return definedTypes;
                    }
                }
            }
            return null;
        }

    }
}
