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

import java.io.InputStream;


public class MappingHandler {

    private String inputVariable;

    public Model doMap(InputStream inputMsg, MappingResourceLoader resourceModel, InputModelBuilder inputModelBuilder)
            throws JSException {

        inputModelBuilder.buildInputModel(inputMsg,this);
        Executable scriptExecutor = ScriptExecutorFactory.getScriptExecutor(ScriptExecutorType.NASHORN);
        while(inputVariable==null){
        }
        Model outputModel = scriptExecutor.execute(resourceModel, inputVariable);
        return outputModel;
    }

    public void notifyInputVariable(String inputVariable){
        this.inputVariable=inputVariable;
    }

}
