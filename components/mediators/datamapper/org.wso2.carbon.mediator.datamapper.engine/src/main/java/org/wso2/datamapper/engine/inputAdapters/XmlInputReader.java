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
package org.wso2.datamapper.engine.inputAdapters;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Array;
import org.apache.avro.generic.GenericRecord;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.llom.OMAttributeImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.AVRO_ATTRIBUTE_FIELD_PREFIX;

/**
 * This class implement the xml reader for WSO2 data mapper
 */
public class XmlInputReader implements InputDataReaderAdapter {

    private OMElement messageBody;
    private static final Log log = LogFactory.getLog(XmlInputReader.class);

    public void setInputMsg(InputStream inputStream) {
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(inputStream);
        OMElement documentElement = builder.getDocumentElement();
        this.messageBody = documentElement;
    }

    public GenericRecord getInputRecord(Schema input) {

        GenericRecord inputRecord = getChild(input, this.messageBody.getChildElements());
        return inputRecord;
    }

    private GenericRecord getChild(Schema schema, Iterator<OMElement> omElementIterator) {

        GenericRecord record = new GenericData.Record(schema);
        ConcurrentMap<String, Collection<Object>> arrMap = new ConcurrentHashMap<String, Collection<Object>>();

        while (omElementIterator.hasNext()) {
            OMElement element = omElementIterator.next();
            String localName = element.getLocalName();
            Field field = schema.getField(localName);
            if (field != null) {
                Type fieldSchemaType = field.schema().getType();
                if (Type.ARRAY.equals(fieldSchemaType)) {
                    Collection<Object> arr = arrMap.get(localName);
                    if (arr == null) {
                        arr = new ArrayList<Object>();
                        arrMap.put(localName, arr);
                    }
                    if (field.schema().getElementType().getType().equals(Type.RECORD)) {
                        Iterator childElements = element.getChildElements();
                        GenericRecord child = getChild(field.schema().getElementType(), childElements);
                        arr.add(child);
                    } else if (field.schema().getElementType().getType().equals(Type.ARRAY)) {
                        log.warn("Array avro schema type is not supported inside another array type");
                    } else {
                        arr.add(element.getText());
                    }
                } else if (Type.RECORD.equals(fieldSchemaType)) {
                    //Add child elements to generic record
                    Iterator childElements = element.getChildElements();
                    GenericRecord child = getChild(field.schema(), childElements);
                    record.put(localName, child);
                    //Add attribute values to generic record
                    Iterator attrElements = element.getAllAttributes();
                    GenericRecord attributes = getChildForAttributes(field.schema(), attrElements, child);
                    record.put(localName, attributes);
                } else if (Type.UNION.equals(fieldSchemaType)) {
                    Iterator childElements = element.getChildElements();
                    if (childElements.hasNext()) {
                        Schema childSchema = field.schema();
                        if (childSchema != null) {
                            List<Schema> childFieldList = childSchema.getTypes();
                            Iterator chilFields = childFieldList.iterator();
                            while (chilFields.hasNext()) {
                                Schema chSchema = (Schema) chilFields.next();
                                String scName = chSchema.getName();
                                if (!scName.equals("null")) {
                                    GenericRecord child = getChild(chSchema, childElements);
                                    record.put(localName, child);
                                } else {
                                    continue;
                                }
                            }
                        }
                    } else {
                        record.put(localName, element.getText());
                    }
                } else if (Type.ENUM.equals(fieldSchemaType) || Type.MAP.equals(fieldSchemaType) || Type.FIXED
                        .equals(fieldSchemaType)) {
                    log.warn("Array avro schema type : " + fieldSchemaType + " is not supported.");
                } else {
                    record.put(localName, element.getText());
                }
            } else {
                log.error("Unrecognized element recieved : " + localName);
            }
        }

        for (Entry<String, Collection<Object>> arrEntry : arrMap.entrySet()) {
            String key = arrEntry.getKey();
            Object object = record.get(key);
            Array<Object> childArray = null;
            Collection<Object> value = arrEntry.getValue();
            if (object == null) {
                childArray = new GenericData.Array<Object>(value.size(), schema.getField(key).schema());
            } else {
                childArray = (Array<Object>) object;
            }
            for (Object obj : value) {
                childArray.add(obj);
            }
            record.put(key, childArray);
        }
        return record;
    }

    private GenericRecord getChildForAttributes(Schema schema, Iterator attrElements, GenericRecord child) {
        while (attrElements.hasNext()) {
            OMAttributeImpl element = (OMAttributeImpl) attrElements.next();
            child.put(AVRO_ATTRIBUTE_FIELD_PREFIX + element.getLocalName(), element.getAttributeValue());
        }
        return child;
    }

    public static String getType() {
        return "application/xml";
    }
}
