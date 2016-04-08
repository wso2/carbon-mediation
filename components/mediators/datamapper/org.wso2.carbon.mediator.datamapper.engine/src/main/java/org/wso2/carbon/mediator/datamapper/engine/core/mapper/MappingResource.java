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
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.JacksonJSONSchema;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingResource {

    public static final String NAMESPACE_DELIMETER = ":";
    private Schema inputSchema;
    private Schema outputSchema;
    private String inputRootelement;
    private String outputRootelement;
    private JSFunction function;

    /**
     * @param inputSchema   respective output json schema as a a stream of bytes
     * @param outputSchema  respective output json schema as a a stream of bytes
     * @param mappingConfig mapping configuration file as a stream of bytes
     * @throws IOException when input errors, If there any parser exception occur while passing
     *                     above schemas method
     *                     will this exception
     */
    public MappingResource(InputStream inputSchema, InputStream outputSchema, InputStream mappingConfig)
            throws SchemaException, JSException {
        this.inputSchema = getJSONSchema(inputSchema);
        this.outputSchema = getJSONSchema(outputSchema);
        this.inputRootelement = this.inputSchema.getName();
        this.outputRootelement = this.outputSchema.getName();
        this.function = createFunction(mappingConfig);
    }

    private Schema getJSONSchema(InputStream inputSchema) throws SchemaException {
        return new JacksonJSONSchema(inputSchema);
    }

    public Schema getInputSchema() {
        return inputSchema;
    }

    public Schema getOutputSchema() {
        return outputSchema;
    }

    public JSFunction getFunction() {
        return function;
    }

    /**
     * need to create java script function by passing the configuration file
     * Since this function going to execute every time when message hit the mapping backend
     * so this function save in the resource model
     *
     * @param mappingConfig mapping configuration
     * @return java script function
     */
    private JSFunction createFunction(InputStream mappingConfig) throws JSException {
        BufferedReader configReader = new BufferedReader(new InputStreamReader(mappingConfig, StandardCharsets.UTF_8));
        //need to identify the main method of the configuration because that method going to
        // execute in engine
        String[] inputRootElementArray = inputRootelement.split(NAMESPACE_DELIMETER);
        String inputRootElement = inputRootElementArray[inputRootElementArray.length - 1];
        String[] outputRootElementArray = outputRootelement.split(NAMESPACE_DELIMETER);
        String outputRootElement = outputRootElementArray[outputRootElementArray.length - 1];

        Pattern functionIdPattern = Pattern.compile("(function )(map_(L|S)_" + inputRootElement + "_(L|S)_" +
                                                    outputRootElement + ")");
        String fnName = null;
        String configLine;
        StringBuilder configScriptBuilder = new StringBuilder();
        try {
            while ((configLine = configReader.readLine()) != null) {
                configScriptBuilder.append(configLine);
                Matcher matcher = functionIdPattern.matcher(configLine);
                if (matcher.find()) {
                    //get the second matching group for the function name
                    fnName = matcher.group(2);
                }
            }
        } catch (IOException e) {
            throw new JSException(e.getMessage());
        }

        if (fnName != null) {
            return new JSFunction(fnName, configScriptBuilder.toString());
        } else {
            throw new JSException("Could not find mapping JavaScript function. Expecting function name pattern" +
                                  " " +
                                  "is " +
                                  functionIdPattern.toString());
        }
    }

}
