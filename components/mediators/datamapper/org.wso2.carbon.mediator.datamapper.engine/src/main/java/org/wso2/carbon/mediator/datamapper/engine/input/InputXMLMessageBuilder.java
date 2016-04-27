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
package org.wso2.carbon.mediator.datamapper.engine.input;

import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.notifiers.InputVariableNotifier;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.mediator.datamapper.engine.input.optimizedReaders.AxiomXMLReader;
import org.wso2.carbon.mediator.datamapper.engine.input.optimizedReaders.AxiomXMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class manage the XML to JSON parsing process
 */
public class InputXMLMessageBuilder {

    private AxiomXMLReader inputReader;
    private Schema inputSchema;
    private InputVariableNotifier inputVariableNotifier;

    /**
     * Constructor
     * @param inputSchema Input message JSON schema
     * @throws IOException
     */
    public InputXMLMessageBuilder( Schema inputSchema )
            throws IOException {
        this.inputReader = AxiomXMLReaderFactory.getReader();
        this.inputSchema = inputSchema;
    }

    /**
     *
     * @param inputStream XML input message
     * @param inputVariableNotifier Reference to the MappingHandler instance
     * @throws ReaderException
     */
    public void buildInputModel(InputStream inputStream, InputVariableNotifier inputVariableNotifier)
            throws ReaderException {
        this.inputVariableNotifier = inputVariableNotifier;
        inputReader.read(inputStream, inputSchema, this);
    }

    /**
     * This method will be called by the AxiomXMLReader instance to notify with the output
     * @param builtMessage Built JSON message
     * @throws JSException
     * @throws ReaderException
     * @throws SchemaException
     */
    public void notifyWithResult(String builtMessage) throws JSException, ReaderException, SchemaException {
        inputVariableNotifier.notifyInputVariable(builtMessage);
    }

}
