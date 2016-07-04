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
import org.wso2.carbon.mediator.datamapper.engine.input.readers.events.ReaderEvent;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.events.ReaderEventType;
import org.wso2.carbon.mediator.datamapper.engine.output.OutputMessageBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_FIELD_PREFIX;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX;

/**
 * This class implements {@link Formatter} interface to read {@link Map} model and trigger events
 * to read
 * by {@link OutputMessageBuilder}
 */
public class MapOutputFormatter implements Formatter {

    private static final String RHINO_NATIVE_ARRAY_FULL_QUALIFIED_CLASS_NAME = "sun.org.mozilla.javascript.internal.NativeArray";
    private OutputMessageBuilder outputMessageBuilder;
    private Schema outputSchema;

    @Override public void format(Model model, OutputMessageBuilder outputMessageBuilder, Schema outputSchema)
            throws SchemaException, WriterException {
        if (model.getModel() instanceof Map) {
            this.outputMessageBuilder = outputMessageBuilder;
            this.outputSchema = outputSchema;
            Map<Object, Object> mapOutputModel = (Map<Object, Object>) model.getModel();
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
    private void traverseMap(Map<Object, Object> outputMap) throws SchemaException, WriterException {
        Set<Object> mapKeys = outputMap.keySet();
        LinkedList<Object> orderedKeyList = new LinkedList<>();
        boolean arrayType = false;
        if (isMapContainArray(mapKeys)) {
            sendArrayStartEvent();
            arrayType = true;
        }
        ArrayList<Object> tempKeys = new ArrayList<>();
        tempKeys.addAll(mapKeys);
        //Attributes should come first than other fields. So attribute should be listed first
        for (Object keyVal : mapKeys) {
            if(keyVal instanceof String) {
                String key= (String) keyVal;
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
            }else if(keyVal instanceof Integer){
                if (tempKeys.contains(keyVal)) {
                        orderedKeyList.addLast(((Integer)keyVal).intValue());
                        tempKeys.remove(keyVal);
                    }
            }
        }
        int mapKeyIndex = 0;
        for (Object keyVal : orderedKeyList) {
            Object value = outputMap.get(keyVal);
            String key = String.valueOf(keyVal);
            // When Data Mapper runs in Java 7 array element is given as a Native Array object.
            // This array object doesn't give values inside. That's why we used reflections in here
            if (value.getClass().toString().contains(RHINO_NATIVE_ARRAY_FULL_QUALIFIED_CLASS_NAME)) {
                try {
                    final Class<?> cls = Class.forName(RHINO_NATIVE_ARRAY_FULL_QUALIFIED_CLASS_NAME);
                    if (cls.isAssignableFrom(value.getClass())) {
                        Map<Object,Object> tempValue = new HashMap();
                        final Method getIds = cls.getMethod("getIds");
                        Method get = null;
                        Method[] allMethods = cls.getDeclaredMethods();
                        for (Method method : allMethods) {
                            String methodName = method.getName();
                            // find the method with name get
                            if ("get".equals(methodName)) {
                                Type[] pType = method.getGenericParameterTypes();
                                //find the get method with two parameters and first one a int
                                if ((pType.length == 2) && ((Class)pType[0]).getName().equals("int")) {
                                    get=method;
                                    break;
                                }
                            }
                        }
                        final Object[] result = (Object[]) getIds.invoke(value);
                        for(Object id:result){
                            Object childValue=get.invoke(value, id, value);
                            tempValue.put(id.toString(),childValue);
                        }
                        value=tempValue;
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                    throw new WriterException("Error while parsing rhino native array values",e);
                }
            }
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
                    }
                    traverseMap((Map<Object, Object>) value);
                    if (mapKeyIndex != mapKeys.size() - 1) {
                        sendObjectEndEvent(key);
                    }
                } else {
                    sendObjectStartEvent(key);
                    traverseMap((Map<Object, Object>) value);
                    if (!key.endsWith(SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)) {
                        sendObjectEndEvent(key);
                    }
                }
            } else {
                // Primitive value received to write
                if(arrayType){
                    // if it is an array of primitive values
                    if (mapKeyIndex != 0) {
                        sendAnonymousObjectStartEvent();
                    }
                    sendPrimitiveEvent(key, value);
                    if (mapKeyIndex != mapKeys.size() - 1) {
                        sendObjectEndEvent(key);
                    }
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

    private void sendPrimitiveEvent(String key, Object value) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.PRIMITIVE, key, value));
    }

    private void sendAnonymousObjectStartEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.ANONYMOUS_OBJECT_START, null, null));
    }

    private void sendArrayEndEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.ARRAY_END, null, null));
    }

    private boolean isMapContainArray(Set<Object> mapKeys) {
        for (Object key : mapKeys) {
            try {
                if(key instanceof String) {
                    Integer.parseInt((String) key);
                    continue;
                }else if(key instanceof Integer){
                    continue;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private void sendArrayStartEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.ARRAY_START, null, null));
    }

    private void sendObjectStartEvent(String elementName) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.OBJECT_START, elementName, null));
    }

    private void sendObjectEndEvent(String objectName) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.OBJECT_END, objectName, null));
    }

    private void sendFieldEvent(String fieldName, Object value) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.FIELD, fieldName, value));
    }

    private void sendTerminateEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.TERMINATE, null, null));
    }

    public OutputMessageBuilder getOutputMessageBuilder() {
        return outputMessageBuilder;
    }

}
