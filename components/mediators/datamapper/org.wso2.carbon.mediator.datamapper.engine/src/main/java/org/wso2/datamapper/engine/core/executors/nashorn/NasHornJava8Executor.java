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
package org.wso2.datamapper.engine.core.executors.nashorn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.datamapper.engine.core.Executable;
import org.wso2.datamapper.engine.core.JSFunction;
import org.wso2.datamapper.engine.core.MappingResourceLoader;
import org.wso2.datamapper.engine.core.Model;
import org.wso2.datamapper.engine.core.exceptions.JSException;
import org.wso2.datamapper.engine.core.models.MapModel;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

import static org.wso2.datamapper.engine.utils.DataMapperEngineConstants.NASHORN_ENGINE_NAME;

/**
 * This class implements script executor for data mapper using java 8 NasHorn JS executor
 */
public class NasHornJava8Executor implements Executable {

    private ScriptEngine scriptEngine;
    private static final Log log = LogFactory.getLog(NasHornJava8Executor.class);

    public NasHornJava8Executor() {
        scriptEngine = new ScriptEngineManager().getEngineByName(NASHORN_ENGINE_NAME);
    }

    @Override
    public Model execute(MappingResourceLoader resourceModel, String inputRecord) throws JSException {
        try {
            JSFunction jsFunction = resourceModel.getFunction();
            injectInputVariableToEngine(resourceModel.getInputSchema().getName(), inputRecord);
            scriptEngine.eval(jsFunction.getFunctionBody());
            Invocable invocable = (Invocable) scriptEngine;
            //String value= (String) scriptEngine.eval("inputns2_employees.ns2_employee[0].ns2_firstname");
            Object result = invocable.invokeFunction(jsFunction.getFunctioName());
            if (result instanceof Map) {
                return new MapModel((Map<String, Object>) result);
            }

        } catch (ScriptException e) {
            log.error("Script execution failed", e);
            throw new SynapseException("NasHornJava8Executor unable to execute the script " + e);
        } catch (NoSuchMethodException e) {
            log.error("Undefined method called to execute", e);
            throw new SynapseException("Undefined method called to execute " + e);
        }
        throw new SynapseException("NasHornJava8Executor Undefined method called to execute");
    }

    private void injectInputVariableToEngine(String inputSchemaName, String inputVariable) throws ScriptException {
        scriptEngine.eval("var input" + inputSchemaName.replace(':','_') + "=" + inputVariable);
    }
}
