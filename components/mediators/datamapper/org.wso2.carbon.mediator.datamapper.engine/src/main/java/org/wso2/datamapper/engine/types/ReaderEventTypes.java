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
package org.wso2.datamapper.engine.types;

/**
 *
 */
public class ReaderEventTypes {

    private final static String OBJECT_START_EVENT = "ObjectStart";
    private final static String OBJECT_END_EVENT  = "ObjectEnd";
    private final static String ARRAY_START_EVENT  = "ArrayStart";
    private final static String ARRAY_END_EVENT  = "ArrayEnd";
    private final static String FIELD_EVENT = "Field";

    // Use to define input and output data formats
    public enum EventType {
        OBJECT_START(OBJECT_START_EVENT), OBJECT_END(OBJECT_END_EVENT), ARRAY_START(ARRAY_START_EVENT),ARRAY_END(ARRAY_END_EVENT),FIELD(FIELD_EVENT);
        private final String value;

        private EventType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        // Use to get the DataType from the relevant input and output data type
        public static EventType fromString(String dataType) {
            if (dataType != null) {
                for (EventType definedTypes : EventType.values()) {
                    if (dataType.equalsIgnoreCase(definedTypes.toString())) {
                        return definedTypes;
                    }
                }
            }
            return null;
        }

    }
}
