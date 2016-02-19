/*
 * Copyright 2005,2013 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.datamapper.engine.inputAdapters;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Array;
import org.apache.avro.generic.GenericRecord;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;


public class XmlInputReader implements InputDataReaderAdapter {

	private OMElement body; // Soap Message body
 
	/**
	 * @param msg - Soap Envelop
	 * @throws IOException
	 */
	public void setInputMsg(InputStream in) {
		OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(in);
		OMElement documentElement = builder.getDocumentElement(); 
		this.body = documentElement;
	}

	public GenericRecord getInputRecord(Schema input) {

		@SuppressWarnings("unchecked")
		GenericRecord inputRecord = getChild(input, this.body.getChildElements());
		return inputRecord;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private GenericRecord getChild(Schema schema, Iterator<OMElement> iter) {

		GenericRecord record = new GenericData.Record(schema);
		ConcurrentMap<String, Collection<Object>> arrMap = new ConcurrentHashMap<String, Collection<Object>>();

		while (iter.hasNext()) {
			OMElement element = iter.next();
			String localName = element.getLocalName();
			Field field = schema.getField(localName);
			if (field != null) {
				if (field.schema().getType().equals(Type.ARRAY)) {
					Collection<Object> arr = arrMap.get(localName);
					if (arr == null) {
						arr = new ArrayList<Object>();
						arrMap.put(localName, arr);
					}
					if (field.schema().getElementType().getType().equals(Type.RECORD)) {
						Iterator childElements = element.getChildElements();
						GenericRecord child = getChild(field.schema().getElementType(),
								childElements);
						arr.add(child);
					} else if (field.schema().getElementType().getType().equals(Type.ARRAY)) {
						// not supports yet!
					} else { // !(ARRAY||RECORD) != primitive type
						arr.add(element.getText());
					}
				}
				else if (field.schema().getType().equals(Type.RECORD)) {
					Iterator childElements = element.getChildElements();
					GenericRecord child = getChild(field.schema(), childElements);
					record.put(localName, child);
				} else if (field.schema().getType().equals(Type.UNION)) {
					Iterator childElements = element.getChildElements();
					if(childElements.hasNext()){
						Schema childSchema = field.schema();
						if(childSchema != null){	
							List<Schema> childFieldList = childSchema.getTypes();
							Iterator chilFields = childFieldList.iterator();
							while (chilFields.hasNext()) {
								Schema chSchema = (Schema) chilFields.next();
								String scName = chSchema.getName();
								if(!scName.equals("null")){
									GenericRecord child = getChild(chSchema, childElements);
									record.put(localName, child);
								}else{
									continue;
								}	
							}
						}
					}else{
						record.put(localName, element.getText());
					}
				}else {
					record.put(localName, element.getText());
					// TODO: fix for other types too... !(ARRAY||RECORD) !=
					// primitive type
				}
			} else {
				// TODO: log unrecognized element
			}
		}

		for (Entry<String, Collection<Object>> arrEntry : arrMap.entrySet()) {
			String key = arrEntry.getKey();
			Object object = record.get(key);
			Array<Object> childArray = null;
			Collection<Object> value = arrEntry.getValue();
			if (object == null) {
				childArray = new GenericData.Array<Object>(value.size(), schema.getField(key)
						.schema());
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
	
	public static String getType() {
		return "application/xml";
	}
}
