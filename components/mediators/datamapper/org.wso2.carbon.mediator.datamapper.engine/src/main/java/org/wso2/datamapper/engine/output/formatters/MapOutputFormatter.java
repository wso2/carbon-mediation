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
package org.wso2.datamapper.engine.output.formatters;

import org.wso2.datamapper.engine.core.Model;
import org.wso2.datamapper.engine.core.Schema;
import org.wso2.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.datamapper.engine.core.exceptions.WriterException;
import org.wso2.datamapper.engine.input.readers.events.DMReaderEvent;
import org.wso2.datamapper.engine.output.Formattable;
import org.wso2.datamapper.engine.output.OutputMessageBuilder;
import org.wso2.datamapper.engine.types.ReaderEventTypes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.ARRAY_ELEMENT_FIRST_NAME;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_FIELD_PREFIX;
import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX;

/**
 * This class implements {@link Formattable} interface to read {@link Map} model and trigger events to read
 * by {@link OutputMessageBuilder}
 */
public class MapOutputFormatter implements Formattable {

    private OutputMessageBuilder outputMessageBuilder;

    @Override
    public void format(Model model, OutputMessageBuilder outputMessageBuilder, Schema outputSchema)
            throws SchemaException, WriterException {
        if (model.getModel() instanceof Map) {
            this.outputMessageBuilder = outputMessageBuilder;
            Map<String, Object> mapOutputModel = (Map<String, Object>) model.getModel();
            traverseMap(mapOutputModel);
            sendTerminateEvent();
        } else {
            throw new IllegalArgumentException("Illegal model passed to MapOutputFormatter : " + model.getModel());
        }
    }

    /**
     * This method traverse output variable represented as a map in a depth first traverse recursively to trigger events
     * to build output message in {@link OutputMessageBuilder}
     *
     * @param outputMap
     */
    private void traverseMap(Map<String, Object> outputMap) throws SchemaException, WriterException {
        Set<String> mapKeys = outputMap.keySet();
        LinkedList<String> orderedKeyList = new LinkedList<>();
        boolean arrayType = false;
        if (isMapContainArray(mapKeys)) {
            sendArrayStartEvent();
            arrayType = true;
        }
        ArrayList<String> tempKeys = new ArrayList<>();
        tempKeys.addAll(mapKeys);
        //Attributes should come first than other fields. So attribute should be listed first
        for (String key : mapKeys) {
            if (key.contains(SCHEMA_ATTRIBUTE_FIELD_PREFIX) && tempKeys.contains(key)) {
                orderedKeyList.addFirst(key);
                tempKeys.remove(key);
            } else {
                if (key.endsWith(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX) && tempKeys.contains(key)) {
                    String elementName = key.substring(0, key.lastIndexOf(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX));
                    orderedKeyList.addLast(key);
                    orderedKeyList.addLast(elementName);
                    tempKeys.remove(key);
                    tempKeys.remove(elementName);
                } else if (tempKeys.contains(key)) {
                    if (tempKeys.contains(key + SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)) {
                        orderedKeyList.addLast(key + SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX);
                        orderedKeyList.addLast(key);
                        tempKeys.remove(key);
                        tempKeys.remove(key + SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX);
                    } else {
                        orderedKeyList.addLast(key);
                        tempKeys.remove(key);
                    }
                }
            }
        }
        int mapKeyIndex = 0;
        for (String key : orderedKeyList) {
            Object value = outputMap.get(key);
            if (value instanceof Map) {
                if (arrayType) {
                    /*If it is array type we need to compensate the already created object start element.
                    So avoid create another start element in first array element and endElement in the last
                    */
                    if (mapKeyIndex != 0) {
                        sendAnonymousObjectStartEvent();
                    }
                    traverseMap((Map<String, Object>) value);
                    if (mapKeyIndex != mapKeys.size() - 1) {
                        sendObjectEndEvent(key);
                    }
                } else {
                    sendObjectStartEvent(key);
                    traverseMap((Map<String, Object>) value);
                    if (!key.endsWith(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)) {
                        sendObjectEndEvent(key);
                    }
                }
            } else {
                sendFieldEvent(key, value);
            }
            mapKeyIndex++;
        }
        if (arrayType) {
            sendArrayEndEvent();
        }
    }

    private void sendAnonymousObjectStartEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.ANONYMOUS_OBJECT_START,
                null, null));
    }

    private void sendArrayEndEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.ARRAY_END, null, null));
    }

    private boolean isMapContainArray(Set<String> mapKeys) {
        for (String key : mapKeys) {
            if (ARRAY_ELEMENT_FIRST_NAME.equals(key)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private void sendArrayStartEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.ARRAY_START, null, null));
    }

    private void sendObjectStartEvent(String elementName) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.OBJECT_START,
                elementName, null));
    }

    private void sendObjectEndEvent(String objectName) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.OBJECT_END, objectName,
                null));
    }

    private void sendFieldEvent(String fieldName, Object value) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.FIELD, fieldName, value));
    }

    private void sendTerminateEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new DMReaderEvent(ReaderEventTypes.EventType.TERMINATE, null, null));
    }


    public OutputMessageBuilder getOutputMessageBuilder() {
        return outputMessageBuilder;
    }

    public void setOutputMessageBuilder(OutputMessageBuilder outputMessageBuilder) {
        this.outputMessageBuilder = outputMessageBuilder;
    }
}
