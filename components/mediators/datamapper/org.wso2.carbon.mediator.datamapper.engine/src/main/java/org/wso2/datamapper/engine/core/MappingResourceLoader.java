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

import org.wso2.datamapper.engine.core.schemas.JacksonJSONSchema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingResourceLoader {

    private Schema inputSchema;
    private Schema outputSchema;
    private InputStream mappingConfig;
    private String inputRootelement;
    private String outputRootelement;
    private JSFunction function;

    /**
     * @param inputSchema           - Respective output Avro schema as a a stream of bytes
     * @param outPutSchema          -Respective output Avro schema as a a stream of bytes
     * @param mappingConfig-Mapping configuration file as a stream of bytes
     * @throws IOException - when input errors, If there any parser exception occur while passing above schemas method
     *                     will this exception
     */
    public MappingResourceLoader(InputStream inputSchema, InputStream outPutSchema,
                                 InputStream mappingConfig) throws IOException {

        this.inputSchema = getAvroSchema(inputSchema);
        this.outputSchema = getAvroSchema(outPutSchema);
        this.inputRootelement = this.inputSchema.getName();
        this.outputRootelement = outputSchema.getName();
        this.mappingConfig = mappingConfig;
        this.function = createFunction(mappingConfig);

    }

    private Schema getAvroSchema(InputStream inputSchema) throws IOException {
        return new JacksonJSONSchema(inputSchema);
    }

    public Schema getInputSchema() {
        return inputSchema;
    }

    public Schema getOutputSchema() {
        return outputSchema;
    }

    public InputStream getMappingConfig() {
        return mappingConfig;
    }

    public String getInputRootelement() {
        return inputRootelement;
    }

    public String getOutputRootelement() {
        return outputRootelement;
    }

     public JSFunction getFunction() {
        return function;
    }

    /**
     * need to create java script function by passing the configuration file
     * Since this function going to execute every time when message hit the mapping backend
     * so this function save in the resource model
     *
     * @param mappingConfig
     * @return
     * @throws IOException
     */
    private JSFunction createFunction(InputStream mappingConfig) throws IOException {

        BufferedReader configReader = new BufferedReader(new InputStreamReader(mappingConfig));
        //need to identify the main method of the configuration because that method going to execute in engine
        String[] inputRootelementArray = inputRootelement.split(":");
        String inputRootElement = inputRootelementArray[inputRootelementArray.length - 1];
        String[] outputRootelementArray = outputRootelement.split(":");
        String outputRootElement = outputRootelementArray[outputRootelementArray.length - 1];
        Pattern functionIdPattern = Pattern.compile("(function )(map_(L|S)_" + inputRootElement
                + "_(L|S)_" + outputRootElement + ")");
        String fnName = null;
        String configLine = "";
        StringBuilder configScriptbuilder = new StringBuilder();
        while ((configLine = configReader.readLine()) != null) {
            configScriptbuilder.append(configLine);
            Matcher matcher = functionIdPattern.matcher(configLine);
            if (matcher.find()) {
                fnName = matcher.group(2);
            }
        }

        if (fnName != null) {
            JSFunction jsfunction = new JSFunction(fnName, configScriptbuilder.toString());
            return jsfunction;

        }
        return null;
    }

}
