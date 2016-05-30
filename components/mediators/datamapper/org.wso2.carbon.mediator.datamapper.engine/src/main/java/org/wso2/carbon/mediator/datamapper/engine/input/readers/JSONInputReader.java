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
package org.wso2.carbon.mediator.datamapper.engine.input.readers;

import org.apache.axiom.om.OMAttribute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.mediator.datamapper.engine.input.InputBuilder;
import org.wso2.carbon.mediator.datamapper.engine.input.builders.JSONBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * This class is capable of parsing XML through AXIOMS for the InputStream and build the respective JSON message
 */
public class JSONInputReader implements InputReader {

    private static final Log log = LogFactory.getLog(JSONInputReader.class);

    /* Reference of the InputXMLMessageBuilder object to send the built JSON message */
    private InputBuilder messageBuilder;

    /**
     * Constructor
     *
     * @throws IOException
     */
    public JSONInputReader() throws IOException {
    }

    /**
     * Read, parse the XML and notify with the output JSON message
     *
     * @param input          XML message InputStream
     * @param inputSchema    Schema of the input message
     * @param messageBuilder Reference of the InputXMLMessageBuilder
     * @throws ReaderException Exceptions in the parsing stage
     */
    @Override public void read(InputStream input, Schema inputSchema, InputBuilder messageBuilder)
            throws ReaderException {
        String inputJSONMessage ;
        try {
            inputJSONMessage = readFromInputStream(input);
            messageBuilder.notifyWithResult(inputJSONMessage);
        } catch (IOException e) {
            throw new ReaderException("IO Error while reading input stream. " + e.getMessage());
        } catch (JSException e) {
            throw new ReaderException("JSException while reading input stream. " + e.getMessage());
        } catch (SchemaException e) {
            throw new ReaderException("SchemaException while reading input stream. " + e.getMessage());
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
