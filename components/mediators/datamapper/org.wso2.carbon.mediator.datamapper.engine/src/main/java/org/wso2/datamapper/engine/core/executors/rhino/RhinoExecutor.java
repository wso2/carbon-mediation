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
package org.wso2.datamapper.engine.core.executors.rhino;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.synapse.SynapseException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.datamapper.engine.core.IScriptExecutor;
import org.wso2.datamapper.engine.core.JSFunction;
import org.wso2.datamapper.engine.core.MappingResourceLoader;
import org.wso2.datamapper.engine.core.exceptions.JSException;

/**
 * This class implements script executor for data mapper using rhino
 */
public class RhinoExecutor implements IScriptExecutor {

    private Context context;
    private Scriptable scope;

    @Override
    public GenericRecord executeMapping(MappingResourceLoader resourceModel, GenericRecord inputRecord) throws JSException {
        GenericRecord genericOutRecord = new GenericData.Record(resourceModel.getOutputSchema());
        Function fn = getFunction(resourceModel.getFunction());
        ScriptableRecord inScriptableRecord = new ScriptableRecord(inputRecord, getScope());
        ScriptableRecord outScriptableRecord = new ScriptableRecord(genericOutRecord,
                getScope());
        Object resultOb = fn.call(resourceModel.getContext(), getScope(), getScope(),
                new Object[]{inScriptableRecord, outScriptableRecord});

        if (resultOb != ScriptableObject.NOT_FOUND) {
            return outScriptableRecord.getRecord();
        }
        throw new SynapseException("Invalid output mapped generic record found");
    }

    public Scriptable getScope() {
        return scope;
    }

    public Function getFunction(JSFunction function) throws JSException {
        if (function != null) {
            initScriptEnviroment();
            context.evaluateString(scope, function.getFunctionBody(), "	", 1, null);
            return (Function) scope.get(function.getFunctioName(), scope);
        } else {
            throw new JSException("JS function not in a correct format");
        }
    }

    /**
     * Before executing a script, an instance of Context must be created
     * and associated with the thread that will be executing the script
     */
    private void initScriptEnviroment() {
        context = Context.enter();
        context.setOptimizationLevel(-1);
        scope = context.initStandardObjects();
    }
}
