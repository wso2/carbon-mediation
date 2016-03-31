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
package org.wso2.datamapper.engine.core.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.datamapper.engine.core.Executable;
import org.wso2.datamapper.engine.core.JSFunction;
import org.wso2.datamapper.engine.core.MappingResourceLoader;
import org.wso2.datamapper.engine.core.Model;
import org.wso2.datamapper.engine.core.exceptions.JSException;
import org.wso2.datamapper.engine.core.models.MapModel;
import org.wso2.datamapper.engine.utils.DataMapperEngineConstants;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

/**
 * This class implements script executor for data mapper using java 8 NasHorn JS executor
 */
public class ScriptExecutor implements Executable {

    private ScriptEngine scriptEngine;
    private static final Log log = LogFactory.getLog(ScriptExecutor.class);

    public ScriptExecutor(ScriptExecutorType scriptExecutorType) {
        switch (scriptExecutorType) {
            case NASHORN:
                scriptEngine = new ScriptEngineManager().getEngineByName(DataMapperEngineConstants.NASHORN_ENGINE_NAME);
                log.debug("Setting Nashorn as Script Engine");
                break;
            case RHINO:
                scriptEngine = new ScriptEngineManager().getEngineByName(DataMapperEngineConstants.DEFAULT_ENGINE_NAME);
                log.debug("Setting default Rhino as Script Engine");
                break;
            default:
                scriptEngine = new ScriptEngineManager().getEngineByName(DataMapperEngineConstants.DEFAULT_ENGINE_NAME);
                log.debug("Setting default Rhino as Script Engine");
                break;
        }
    }

    @Override
    public Model execute(MappingResourceLoader resourceModel, String inputRecord) throws JSException {
        try {
            JSFunction jsFunction = resourceModel.getFunction();
            injectInputVariableToEngine(resourceModel.getInputSchema().getName(), inputRecord);
            scriptEngine.eval(jsFunction.getFunctionBody());
            Invocable invocable = (Invocable) scriptEngine;
            Object result = invocable.invokeFunction(jsFunction.getFunctioName());
            if (result instanceof Map) {
                return new MapModel((Map<String, Object>) result);
            }

        } catch (ScriptException e) {
            log.error("Script execution failed", e);
            throw new SynapseException("Script engine unable to execute the script " + e);
        } catch (NoSuchMethodException e) {
            log.error("Undefined method called to execute", e);
            throw new SynapseException("Undefined method called to execute " + e);
        }
        throw new SynapseException("Undefined method called to execute");
    }

    private void injectInputVariableToEngine(String inputSchemaName, String inputVariable) throws ScriptException {
        scriptEngine.eval("var input" + inputSchemaName.replace(':', '_') + "=" + inputVariable);
    }
}
