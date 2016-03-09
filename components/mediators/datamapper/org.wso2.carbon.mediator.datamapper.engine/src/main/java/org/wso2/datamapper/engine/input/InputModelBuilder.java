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
package org.wso2.datamapper.engine.input;

import org.wso2.datamapper.engine.core.Model;
import org.wso2.datamapper.engine.core.Schema;
import org.wso2.datamapper.engine.input.builders.BuilderFactory;
import org.wso2.datamapper.engine.input.readers.ReaderFactory;
import org.wso2.datamapper.engine.input.readers.events.DMReaderEvent;
import org.wso2.datamapper.engine.types.DMModelTypes;
import org.wso2.datamapper.engine.types.InputOutputDataTypes;

import java.io.InputStream;

/**
 *
 */
public class InputModelBuilder {

    private Readable inputReader;
    private Buildable modelBuilder;
    private Model inputModel;
    private Schema inputSchema;

    public InputModelBuilder(InputOutputDataTypes.DataType inputType, DMModelTypes.ModelType modelType,Schema inputSchema) {
        inputReader = ReaderFactory.getReader(inputType);
        modelBuilder = BuilderFactory.getBuilder(modelType);
        this.inputSchema = inputSchema;
    }

    public Model buildInputModel(InputStream inputStream){
        inputReader.read(inputStream,this,inputSchema);
        return inputModel;
    }

    public void notifyEvent(DMReaderEvent readerEvent) {
        switch (readerEvent.getEventType()) {
            case OBJECT_START:
                System.out.println("Object Started");
                break;
            case OBJECT_END:
                System.out.println("Object Ended");
                break;
            case ARRAY_START:
                System.out.println("Array Started");
                break;
            case FIELD:

                System.out.println("Field Started : "+readerEvent.getName()+" : "+readerEvent.getValue());
                break;
            case ARRAY_END:
                System.out.println("Array Ended");
                break;
            default:
                throw new IllegalArgumentException("Illegal Reader event found : " + readerEvent.getEventType());
        }

    }
}
