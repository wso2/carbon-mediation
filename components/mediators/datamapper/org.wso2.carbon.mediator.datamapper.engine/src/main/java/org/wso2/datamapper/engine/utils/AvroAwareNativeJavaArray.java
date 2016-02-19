/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
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

package org.wso2.datamapper.engine.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericData.Array;
import org.mozilla.javascript.NativeJavaArray;
import org.mozilla.javascript.Scriptable;
import org.wso2.datamapper.engine.core.ScriptableRecord;

public class  AvroAwareNativeJavaArray extends NativeJavaArray  {
	
	private Array<Object> recordArray;
	private static Scriptable scope;

	private static final long serialVersionUID = 1625850167152425723L;

	/**
	 * Need a native array to iterate using rhino
	 * @param scope
	 * @param recordArray
	 */
	public AvroAwareNativeJavaArray(Scriptable scope, Array<Object> recordArray) {
		super(scope, toArray(recordArray)); //FIXME: we don't need to place data in two places
		this.recordArray = recordArray;
		AvroAwareNativeJavaArray.scope = scope;
	}
	
	/**
	 * filling array on demand
	 */
	@Override
	public void put(int index, Scriptable start, Object value) {
		if((recordArray.size() -1) < index){
			//TODO : resize array
		}
		Schema elementType = recordArray.getSchema().getElementType();
		if(elementType.getType().equals(Type.RECORD)){
		GenericRecord record = new GenericData.Record(elementType);
		recordArray.add((GenericRecord)record);
		} else{
			recordArray.add(value);
		}
	//	super.put(index, start, value);
	}
	
	@Override
	public void put(String id, Scriptable start, Object value) {
		// TODO Auto-generated method stub
		super.put(id, start, value);
	}
	
	
	@Override
	public Object get(int index, Scriptable start) {
		Schema elementType = recordArray.getSchema().getElementType();
		if(elementType.getType().equals(Type.RECORD)){
			if((recordArray.size() -1) < index){
				recordArray.add(new GenericData.Record(elementType));
			}
			return new ScriptableRecord((GenericRecord)recordArray.get(index),scope);
		} else{
			return recordArray.get(index);
		}
		//return super.get(index, start);
	}
	
	@Override
	public boolean has(int index, Scriptable start) {
		// TODO Auto-generated method stub
		return super.has(index, start);
	}
	
	@Override
	public boolean has(String id, Scriptable start) {
		// TODO Auto-generated method stub
		return super.has(id, start);
	}
	
	
	private static Object toArray(Array<Object> recordArray){
		List<Object> list = new ArrayList<Object>();
		Iterator<Object> iterator = recordArray.iterator();
		while(iterator.hasNext()){
			Object next = iterator.next();
			if(next instanceof GenericRecord){
				list.add(new ScriptableRecord((GenericRecord)next,scope));
			} else{
				list.add(next);
			}
		}
		return list.toArray();
	}

}