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
package org.wso2.datamapper.engine.core;

import org.apache.synapse.SynapseException;
import org.wso2.datamapper.engine.core.callbacks.InputVariableCallback;
import org.wso2.datamapper.engine.core.callbacks.OutputVariableCallback;
import org.wso2.datamapper.engine.core.exceptions.JSException;
import org.wso2.datamapper.engine.core.executors.ScriptExecutorFactory;
import org.wso2.datamapper.engine.core.executors.ScriptExecutorType;
import org.wso2.datamapper.engine.input.InputModelBuilder;
import org.wso2.datamapper.engine.output.OutputMessageBuilder;

import java.io.InputStream;


public class MappingHandler implements InputVariableCallback, OutputVariableCallback {

    private String inputVariable;
    private String outputVariable;
    private MappingResourceLoader mappingResourceLoader;
    private OutputMessageBuilder outputMessageBuilder;
    private Executable scriptExecutor;

    public String doMap(InputStream inputMsg, MappingResourceLoader resourceModel, InputModelBuilder inputModelBuilder,
                        OutputMessageBuilder outputMessageBuilder, Executable scriptExecutor)
            throws JSException {
        this.mappingResourceLoader = resourceModel;
        this.outputMessageBuilder = outputMessageBuilder;
        this.scriptExecutor = scriptExecutor;
        inputModelBuilder.buildInputModel(inputMsg, this);
        return outputVariable;
    }

    @Override
    public void notifyInputVariable(Object variable) {
        this.inputVariable = (String) variable;
        //Executable scriptExecutor = ScriptExecutorFactory.getScriptExecutor(ScriptExecutorType.NASHORN);
        Model outputModel = null;
        try {
            outputModel = scriptExecutor.execute(mappingResourceLoader, inputVariable);
            outputMessageBuilder.buildOutputMessage(outputModel, this);
        } catch (JSException e) {
            throw new SynapseException("Unable to execute the mapping configuration on data mapper engine");
        }
    }

    @Override
    public void notifyOutputVariable(Object variable) {
        outputVariable=(String) variable;
    }
}
