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
package org.wso2.datamapper.engine.utils;

import java.io.IOException;
import org.apache.avro.generic.GenericRecord;
import org.json.JSONException;
import org.json.JSONObject;

public class OutputJsonBuilder {

	public JSONObject getOutPut(GenericRecord outputRecord , String rootElementName) throws JSONException, IOException {
		
		JSONObject outputJson = new JSONObject();
		outputJson.put(rootElementName, new JSONObject(outputRecord.toString()));
		return outputJson;
	}
}
