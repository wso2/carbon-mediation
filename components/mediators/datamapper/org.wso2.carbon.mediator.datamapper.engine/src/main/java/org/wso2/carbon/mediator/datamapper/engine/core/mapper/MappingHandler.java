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

import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.WriterException;
import org.wso2.carbon.mediator.datamapper.engine.core.executors.Executor;
import org.wso2.carbon.mediator.datamapper.engine.core.executors.ScriptExecutorFactory;
import org.wso2.carbon.mediator.datamapper.engine.core.models.Model;
import org.wso2.carbon.mediator.datamapper.engine.core.notifiers.InputVariableNotifier;
import org.wso2.carbon.mediator.datamapper.engine.core.notifiers.OutputVariableNotifier;
import org.wso2.carbon.mediator.datamapper.engine.input.InputXMLMessageBuilder;
import org.wso2.carbon.mediator.datamapper.engine.output.OutputMessageBuilder;
import org.wso2.carbon.mediator.datamapper.engine.output.OutputXMLMessageBuilder;
import org.wso2.carbon.mediator.datamapper.engine.utils.InputOutputDataType;
import org.wso2.carbon.mediator.datamapper.engine.utils.ModelType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MappingHandler implements InputVariableNotifier, OutputVariableNotifier {

    private String dmExecutorPoolSize;
    private String inputVariable;
    private String outputVariable;
    private MappingResource mappingResource;
    private OutputMessageBuilder outputMessageBuilder;
    private Executor scriptExecutor;
    private InputXMLMessageBuilder inputXMLMessageBuilder;
    private OutputXMLMessageBuilder outputXMLMessageBuilder;
    private String outputType;

    public MappingHandler(MappingResource mappingResource, String inputType, String outputType,
                          String dmExecutorPoolSize) throws IOException, SchemaException, WriterException {

        this.outputType = outputType;

        if (InputOutputDataType.XML.toString().equals(inputType)) {
            this.inputXMLMessageBuilder = new InputXMLMessageBuilder(mappingResource.getInputSchema());
        }

        if (InputOutputDataType.XML.toString().equals(outputType)) {
            this.outputXMLMessageBuilder = new OutputXMLMessageBuilder(mappingResource.getOutputSchema());
        } else {
            this.outputMessageBuilder =
                    new OutputMessageBuilder(InputOutputDataType.fromString(outputType), ModelType.JAVA_MAP,
                            mappingResource.getOutputSchema());
        }

        this.dmExecutorPoolSize = dmExecutorPoolSize;
        this.mappingResource = mappingResource;
    }

    public String doMap(InputStream inputMsg)
            throws ReaderException, InterruptedException, IOException, SchemaException, JSException {
        this.scriptExecutor = ScriptExecutorFactory.getScriptExecutor(dmExecutorPoolSize);
        notifyInputVariable(readFromInputStream(inputMsg));
        return outputVariable;
    }

    @Override public void notifyInputVariable(Object variable) throws SchemaException, JSException, ReaderException {
        this.inputVariable = (String) variable;
        Model outputModel = scriptExecutor.execute(mappingResource, inputVariable);
        try {
            releaseExecutor();
            buildOutputMessage(outputModel);
        } catch (InterruptedException | WriterException e) {
            throw new ReaderException(e.getMessage());
        }
    }

    private void releaseExecutor() throws InterruptedException {
        ScriptExecutorFactory.releaseScriptExecutor(scriptExecutor);
        this.scriptExecutor = null;
    }

    @Override public void notifyOutputVariable(Object variable) {
        outputVariable = (String) variable;
    }

    /**
     * This will be used to build a JSON message from an input XML message
     *
     * @param inputMsg XML message InputStream
     * @return Output message as a String
     * @throws ReaderException
     * @throws InterruptedException
     */
    public String doMapXML(InputStream inputMsg) throws ReaderException, InterruptedException {
        this.scriptExecutor = ScriptExecutorFactory.getScriptExecutor(dmExecutorPoolSize);
        this.inputXMLMessageBuilder.buildInputModel(inputMsg, this);
        return outputVariable;
    }

    /**
     * This method will decide the suitable output builder based on the request output type
     *
     * @param outputModel Model returned by the script engine
     * @throws SchemaException
     * @throws WriterException
     */
    public void buildOutputMessage (Model outputModel) throws SchemaException, WriterException {
        if (InputOutputDataType.XML.toString().equals(outputType)){
            outputXMLMessageBuilder.buildOutputMessage(outputModel,this);
        } else {
            outputMessageBuilder.buildOutputMessage(outputModel, this);
        }
    }

    /**
     * Method added to convert the input directly into a string and to return
     * This method is used only when the JSON input is present
     *
     * @param inputStream JSON message as a InputStream
     * @return JSON message as a String
     * @throws IOException
     */
    private String readFromInputStream(InputStream inputStream) throws IOException {
        InputStreamReader isr = new InputStreamReader((inputStream));
        BufferedReader br = new BufferedReader(isr);

        StringBuilder out = new StringBuilder("");
        String line;
        while ((line = br.readLine()) != null) {
            out.append(line);
        }
        return out.toString();
    }
}
