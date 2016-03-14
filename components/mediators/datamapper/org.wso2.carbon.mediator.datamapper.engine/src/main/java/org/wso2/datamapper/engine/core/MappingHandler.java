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

import org.wso2.datamapper.engine.core.exceptions.JSException;
import org.wso2.datamapper.engine.core.executors.ScriptExecutorFactory;
import org.wso2.datamapper.engine.core.executors.ScriptExecutorType;
import org.wso2.datamapper.engine.input.InputModelBuilder;
import org.wso2.datamapper.engine.output.OutputMessageBuilder;

import java.io.InputStream;


public class MappingHandler {

    private String inputVariable;
    private String outputVariable;
    private MappingResourceLoader mappingResourceLoader;
    private OutputMessageBuilder outputMessageBuilder;

    public String doMap(InputStream inputMsg, MappingResourceLoader resourceModel, InputModelBuilder inputModelBuilder, OutputMessageBuilder outputMessageBuilder)
            throws JSException {
        mappingResourceLoader=resourceModel;
        this.outputMessageBuilder=outputMessageBuilder;
        inputModelBuilder.buildInputModel(inputMsg,this);
        while(outputVariable==null){
        }
        return outputVariable;
    }

    public void notifyInputVariable(String inputVariable) throws JSException {
        this.inputVariable=inputVariable;
        Executable scriptExecutor = ScriptExecutorFactory.getScriptExecutor(ScriptExecutorType.NASHORN);
        Model outputModel = scriptExecutor.execute(mappingResourceLoader, inputVariable);
        outputMessageBuilder.buildOutputMessage(outputModel,this);
    }

    public void notifyOutputVariable(String outputVariable){
        this.outputVariable=outputVariable;
    }
}
