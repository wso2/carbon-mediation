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
package org.wso2.datamapper.engine.core;

import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.wso2.datamapper.engine.utils.AvroAwareNativeJavaArray;
import org.apache.avro.generic.GenericData.Array;

public class ScriptableRecord implements Scriptable{
	
	private GenericRecord record;
	private Scriptable prototype;
	private Scriptable scope;
	
	
	public ScriptableRecord(GenericRecord record,Scriptable scope) {	
		this.record = record;
		this.scope = scope;
	}

	public GenericRecord getRecord() {
		return record;
	}

	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object get(String name, Scriptable start) {
		Object resource = this.record.get(name);
		Field field = record.getSchema().getField(name);
		if(resource instanceof GenericRecord){
			return	new ScriptableRecord((GenericRecord)resource,getScope());
		} else if (resource instanceof Array){
			@SuppressWarnings("unchecked")
			Array<Object> recordArray = (Array<Object>) resource;
			return new AvroAwareNativeJavaArray(getScope(),recordArray);
		} else if (resource != null){
				return resource;
		}	else if(field!=null){
			if(field.schema().getType().equals(Type.ARRAY)){
				Array<Object> recordArray = new GenericData.Array<Object>(32,field.schema());
				record.put(name,recordArray);
				return  new AvroAwareNativeJavaArray(getScope(),recordArray);
			} else if(field.schema().getType().equals(Type.RECORD)){
				GenericRecord subRecord = new GenericData.Record(field.schema());
				record.put(name,subRecord);
				return new ScriptableRecord((GenericRecord)record.get(name),getScope());
			} else{
				return NOT_FOUND;
			}
		} 	
		return NOT_FOUND;
	}

	public Object get(int index, Scriptable start) {
		return null;
	}

	public boolean has(String name, Scriptable start) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean has(int index, Scriptable start) {
		// TODO Auto-generated method stub
		return false;
	}

	public void put(String name, Scriptable start, Object value) {
		
		if (value instanceof NativeJavaObject) {
			this.record.put(name, ((NativeJavaObject) value).getDefaultValue(String.class));
		}else{
			this.record.put(name, value);
		}
	}

	public void put(int index, Scriptable start, Object value) {

	}

	public void delete(String name) {
		// TODO Auto-generated method stub
		
	}

	public void delete(int index) {
		// TODO Auto-generated method stub
		
	}

	public Scriptable getPrototype() {
		return prototype;
	}

	public void setPrototype(Scriptable prototype) {
		this.prototype = prototype;		
	}

	public Scriptable getParentScope() {
		return null;
	}

	public void setParentScope(Scriptable parent) {
		// TODO Auto-generated method stub
		
	}

	public Object getDefaultValue(Class<?> hint) {
		return null;
	}

	public boolean hasInstance(Scriptable instance) {
		return false;
	}

	public Object[] getIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public Scriptable getScope() {
		return scope;
	}

	public void setScope(Scriptable scope) {
		this.scope = scope;
	}

}