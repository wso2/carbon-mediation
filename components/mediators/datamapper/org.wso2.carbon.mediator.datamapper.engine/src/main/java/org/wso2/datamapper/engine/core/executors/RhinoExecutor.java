/*
 * Copyright 2016 WSO2, Inc. http://www.wso2.org
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
package org.wso2.datamapper.engine.core.executors;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.datamapper.engine.core.IScriptExecutor;
import org.wso2.datamapper.engine.core.MappingResourceLoader;
import org.wso2.datamapper.engine.core.ScriptableRecord;
import org.wso2.datamapper.engine.core.exceptions.JSException;

/**
 * This class implements script executor for data mapper using rhino
 */
public class RhinoExecutor implements IScriptExecutor{
    @Override
    public GenericRecord executeMapping(MappingResourceLoader resourceModel, GenericRecord inputRecord) throws JSException {
        GenericRecord genericOutRecord = new GenericData.Record(resourceModel.getOutputSchema());
        Function fn = resourceModel.getFunction();
        ScriptableRecord inScriptableRecord = new ScriptableRecord(inputRecord,resourceModel.getScope());
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
