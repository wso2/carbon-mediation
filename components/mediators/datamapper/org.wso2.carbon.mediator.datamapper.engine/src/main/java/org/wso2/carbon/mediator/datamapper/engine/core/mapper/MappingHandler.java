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
package org.wso2.carbon.mediator.datamapper.engine.core.mapper;

import org.wso2.carbon.mediator.datamapper.engine.core.notifiers.InputVariableNotifier;
import org.wso2.carbon.mediator.datamapper.engine.core.notifiers.OutputVariableNotifier;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.WriterException;
import org.wso2.carbon.mediator.datamapper.engine.core.executors.Executor;
import org.wso2.carbon.mediator.datamapper.engine.core.models.Model;
import org.wso2.carbon.mediator.datamapper.engine.input.InputModelBuilder;
import org.wso2.carbon.mediator.datamapper.engine.output.OutputMessageBuilder;

import java.io.InputStream;


public class MappingHandler implements InputVariableNotifier, OutputVariableNotifier {

    private String inputVariable;
    private String outputVariable;
    private MappingResource mappingResource;
    private OutputMessageBuilder outputMessageBuilder;
    private Executor scriptExecutor;
    private InputModelBuilder inputModelBuilder;

    public MappingHandler(MappingResource mappingResource, Executor scriptExecutor,
                          InputModelBuilder inputModelBuilder, OutputMessageBuilder outputMessageBuilder){
        this.mappingResource = mappingResource;
        this.outputMessageBuilder = outputMessageBuilder;
        this.scriptExecutor = scriptExecutor;
        this.inputModelBuilder = inputModelBuilder;
    }

    public String doMap(InputStream inputMsg) throws ReaderException {
        inputModelBuilder.buildInputModel(inputMsg, this);
        return outputVariable;
    }

    @Override
    public void notifyInputVariable(Object variable) throws SchemaException, JSException, ReaderException {
        this.inputVariable = (String) variable;
        Model outputModel = null;
        outputModel = scriptExecutor.execute(mappingResource, inputVariable);
        //TODO : move output message builder to output component
        try {
            outputMessageBuilder.buildOutputMessage(outputModel, this);
        } catch (WriterException e) {
            throw new ReaderException(e.getMessage());//TODO to avoid throwing a writer exception on reader side. remove this
        }
    }

    @Override
    public void notifyOutputVariable(Object variable) {
        outputVariable = (String) variable;
    }
}
