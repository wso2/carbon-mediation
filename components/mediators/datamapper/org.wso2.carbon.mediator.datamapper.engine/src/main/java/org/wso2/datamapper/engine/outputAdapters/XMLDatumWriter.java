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
package org.wso2.datamapper.engine.outputAdapters;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.generic.GenericRecord;
import org.json.XML;
import org.json.JSONException;
import org.json.JSONObject;
 
public class XMLDatumWriter extends GenericDatumWriter<GenericRecord> {
	
	
	@Override
	protected void writeRecord(Schema schema, Object datum, Encoder out)
			throws IOException {
		try {
			GenericRecord record = (GenericRecord) datum;
			String name = record.getSchema().getName();
			JSONObject rootObj = new JSONObject();
			rootObj.putOnce(name, new JSONObject(record.toString()));
			out.writeString(XML.toString(rootObj));
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}

}
