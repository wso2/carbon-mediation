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
package org.wso2.carbon.mediator.datamapper.engine.output.formatters;

import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.WriterException;
import org.wso2.carbon.mediator.datamapper.engine.core.models.Model;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.mediator.datamapper.engine.output.OutputMessageBuilder;
import org.wso2.carbon.mediator.datamapper.engine.output.OutputXMLMessageBuilder;
import org.wso2.carbon.mediator.datamapper.engine.output.writers.XMLWriter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.*;

/**
 * This class responsible for building the output XML message using the generated Map model
 */
public class AxiomXMLMapOutputFormatter {

    private static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";
    private OutputXMLMessageBuilder outputXMLMessageBuilder;
    private Schema outputSchema;
    private XMLWriter outputWriter;

    public void format(Model model, OutputXMLMessageBuilder outputXMLMessageBuilder, Schema outputSchema)
            throws SchemaException, WriterException {
        if (model.getModel() instanceof Map) {
            this.outputXMLMessageBuilder = outputXMLMessageBuilder;
            this.outputSchema = outputSchema;
            this.outputWriter = new XMLWriter(outputSchema);
            Map<String, Object> mapOutputModel = (Map<String, Object>) model.getModel();
            traverseMap(mapOutputModel);
            sendTerminateEvent();
        } else {
            throw new IllegalArgumentException("Illegal model passed to MapOutputFormatter : " + model.getModel());
        }
    }

    /**
     * This method traverse output variable represented as a map in a depth first traverse
     * recursively to trigger events
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
                // key value is a type of object or an array
                if (arrayType) {
                    /*If it is array type we need to compensate the already created object start
                    element.
                    So avoid create another start element in first array element and endElement
                    in the last
                    */
                    if (mapKeyIndex != 0) {
                        sendAnonymousObjectStartEvent();
                        createAndSendIdentifierFieldEvent(key);
                    }
                    traverseMap((Map<String, Object>) value);
                    if (mapKeyIndex != mapKeys.size() - 1) {
                        sendObjectEndEvent(key);
                    }
                } else {
                    sendObjectStartEvent(key);
                    createAndSendIdentifierFieldEvent(key);
                    traverseMap((Map<String, Object>) value);
                    if (!key.endsWith(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)) {
                        sendObjectEndEvent(key);
                    }
                }
            } else {
                // Primitive value recieved to write
                if(arrayType){
                    // if it is an array of primitive values
                    sendPrimitiveEvent(key, value);
                } else {
                    // if field value
                    sendFieldEvent(key, value);
                }
            }
            mapKeyIndex++;
        }
        if (arrayType) {
            sendArrayEndEvent();
        }
    }

    /**
     *  Method for handling xsi parameters
     * @param key param name
     * @throws SchemaException
     * @throws WriterException
     */
    private void createAndSendIdentifierFieldEvent(String key) throws SchemaException, WriterException {
        //sending events to create xsi:type attribute
        Pattern identifierPattern = Pattern.compile("(_.+_type)");
        Matcher matcher = identifierPattern.matcher(key);
        while (matcher.find()) {
            String s = matcher.group(0);
            String stringArray[] = s.split("_");
            String prefix = stringArray[stringArray.length - 2];
            if (prefix.equals(outputSchema.getNamespaceMap().get(XSI_NAMESPACE_URI))) {
                sendFieldEvent("attr_" + prefix + ":type", key.split("_" + prefix + "_type_")[1].replace('_', ':'));
            }
        }
    }

    private void sendPrimitiveEvent(String key, Object value) throws SchemaException, WriterException {
        outputWriter.writePrimitive(value);
    }

    private void sendAnonymousObjectStartEvent() throws SchemaException, WriterException {
        outputWriter.writeStartAnonymousObject();
    }

    private void sendArrayEndEvent() throws SchemaException, WriterException {
        outputWriter.writeEndArray();
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
        outputWriter.writeStartArray();
    }

    private void sendObjectStartEvent(String elementName) throws SchemaException, WriterException {
        outputWriter.writeStartObject(elementName);
    }

    private void sendObjectEndEvent(String objectName) throws SchemaException, WriterException {
        outputWriter.writeEndObject(objectName);
    }

    private void sendFieldEvent(String fieldName, Object value) throws SchemaException, WriterException {
        outputWriter.writeField(fieldName, value);
    }

    private void sendTerminateEvent() throws SchemaException, WriterException {
        outputXMLMessageBuilder.notifyWithResult(outputWriter.terminateMessageBuilding());
    }

}
