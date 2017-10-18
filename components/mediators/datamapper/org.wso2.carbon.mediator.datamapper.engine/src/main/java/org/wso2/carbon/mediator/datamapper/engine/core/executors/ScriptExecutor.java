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
package org.wso2.carbon.mediator.datamapper.engine.core.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.mapper.JSFunction;
import org.wso2.carbon.mediator.datamapper.engine.core.mapper.MappingResource;
import org.wso2.carbon.mediator.datamapper.engine.core.models.MapModel;
import org.wso2.carbon.mediator.datamapper.engine.core.models.Model;
import org.wso2.carbon.mediator.datamapper.engine.core.models.StringModel;
import org.wso2.carbon.mediator.datamapper.engine.output.formatters.MapOutputFormatter;
import org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants;
import org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.ENCODE_CHAR_HYPHEN;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.EQUALS_SIGN;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.HYPHEN;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.PROPERTIES_OBJECT_NAME;

/**
 * This class implements script executor for data mapper using java script executor (Rhino or
 * Nashorn)
 */
public class ScriptExecutor implements Executor {

    private static final Log log = LogFactory.getLog(ScriptExecutor.class);
    private ScriptEngine scriptEngine;

    /**
     * Create a script executor of the provided script executor type
     *
     * @param scriptExecutorType
     */
    public ScriptExecutor(ScriptExecutorType scriptExecutorType) {
        switch (scriptExecutorType) {
        case NASHORN:
            scriptEngine = new ScriptEngineManager().getEngineByName(DataMapperEngineConstants.NASHORN_ENGINE_NAME);
            log.debug("Setting Nashorn as Script Engine");
            break;
        case RHINO:
            scriptEngine = new ScriptEngineManager().getEngineByName(DataMapperEngineConstants.DEFAULT_ENGINE_NAME);
            log.debug("Setting Rhino as Script Engine");
            break;
        default:
            scriptEngine = new ScriptEngineManager().getEngineByName(DataMapperEngineConstants.DEFAULT_ENGINE_NAME);
            log.debug("Setting default Rhino as Script Engine");
            break;
        }
    }

    @Override
    public Model execute(MappingResource mappingResource, String inputVariable, String properties)
            throws JSException, SchemaException {
        try {
            JSFunction jsFunction = mappingResource.getFunction();
            injectPropertiesToEngine(properties);
            injectInputVariableToEngine(mappingResource.getInputSchema().getName(), inputVariable);
            scriptEngine.eval(jsFunction.getFunctionBody());
            Object result = scriptEngine.eval(jsFunction.getFunctionName());
            if (result instanceof Map) {
                return new MapModel((Map<String, Object>) result);
            } else if (result instanceof String) {
                return new StringModel((String) result);
            } else if (result != null && result.getClass().toString()
                    .contains(MapOutputFormatter.RHINO_NATIVE_ARRAY_FULL_QUALIFIED_CLASS_NAME)) {
                return new MapModel(DataMapperEngineUtils.getMapFromNativeArray(result));
            }
        } catch (ScriptException e) {
            throw new JSException("Script engine unable to execute the script " + e);
        }
        throw new JSException("Failed to execute mapping function");
    }

    private void injectInputVariableToEngine(String inputSchemaName, String inputVariable) throws ScriptException {
        scriptEngine.eval("var input" + inputSchemaName.replace(':', '_').replace('=', '_').replace(',', '_').replace(HYPHEN, ENCODE_CHAR_HYPHEN) + "="
                + inputVariable);
    }

    private void injectPropertiesToEngine(String properties) throws ScriptException {
        scriptEngine.eval("var " + PROPERTIES_OBJECT_NAME + EQUALS_SIGN + properties);
    }
}
