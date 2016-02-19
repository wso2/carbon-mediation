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

package org.wso2.datamapper.engine.inputAdapters;

import java.io.InputStream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 *
 */
public class JsonInputReader implements InputDataReaderAdapter {
	private static final String ANONYMOUS_ROOT_ID = "AnonymousRootNode";
	private InputStream inputStream;

	/* (non-Javadoc)
	 * @see org.wso2.datamapper.engine.inputAdapter.InputDataReaderAdapter#setInputMsg(java.io.InputStream)
	 */
	public void setInputMsg(InputStream msg) {
		this.inputStream = msg;
		
	}

	/* (non-Javadoc)
	 * @see org.wso2.datamapper.engine.inputAdapter.InputDataReaderAdapter#getInputRecord(org.apache.avro.Schema)
	 */
	public GenericRecord getInputRecord(Schema input) {
		GenericRecord result = null;
		
		try {
			JsonNode jsonNode;
			DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(input);
			
			ObjectMapper mapper = new ObjectMapper();
			JsonFactory jsonFactory = mapper.getJsonFactory();
			jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
			jsonFactory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
			JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);

			// FIXME If schema contains anonymous root added by graphical
			// editor, then add the same to payload
			if (ANONYMOUS_ROOT_ID.equals(input.getName())) {
				ObjectNode anonymousNode = mapper.createObjectNode();
				anonymousNode.put(ANONYMOUS_ROOT_ID, jsonParser.readValueAsTree());

				jsonNode = anonymousNode.get(input.getName());
			} else {
				// outer json is not part of data, extract child element only
				jsonNode = jsonParser.readValueAsTree().get(input.getName());
			}

			JsonDecoder jsonDecoder = DecoderFactory.get().jsonDecoder(input, jsonNode.toString());
			result = reader.read(null, jsonDecoder);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}
	
	public static String getType() {
		return "application/json";
	}

}
