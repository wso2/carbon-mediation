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

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.datamapper.engine.core.MappingResourceLoader.JSException;

public class FunctionExecuter {

	public static GenericRecord execute(MappingResourceLoader resourceModel, GenericRecord inRecord) throws JSException  {
		GenericRecord genericOutRecord = new GenericData.Record(resourceModel.getOutputSchema());
		Function fn = resourceModel.getFunction();
		ScriptableRecord inScriptableRecord = new ScriptableRecord(inRecord,resourceModel.getScope());
		ScriptableRecord outScriptableRecord = new ScriptableRecord(genericOutRecord,
				resourceModel.getScope());
		Object resultOb = fn.call(resourceModel.getContext(), resourceModel.getScope(),
				resourceModel.getScope(), new Object[] { inScriptableRecord, outScriptableRecord });

		if (resultOb != ScriptableObject.NOT_FOUND) {
			return outScriptableRecord.getRecord();
		}
		return null;
	}
}