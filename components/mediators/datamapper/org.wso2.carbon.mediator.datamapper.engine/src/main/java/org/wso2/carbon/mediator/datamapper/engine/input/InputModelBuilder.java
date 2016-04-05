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

import org.wso2.carbon.mediator.datamapper.engine.core.callbacks.InputVariableCallback;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.mediator.datamapper.engine.input.builders.Builder;
import org.wso2.carbon.mediator.datamapper.engine.input.builders.BuilderFactory;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.Reader;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.ReaderFactory;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.events.DMReaderEvent;
import org.wso2.carbon.mediator.datamapper.engine.utils.DMModelTypes;
import org.wso2.carbon.mediator.datamapper.engine.utils.InputOutputDataTypes;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class InputModelBuilder {

    private Reader inputReader;
    private Builder modelBuilder;
    private Schema inputSchema;
    private InputVariableCallback mappingHandler;

    public InputModelBuilder(InputOutputDataTypes.DataType inputType,
                             DMModelTypes.ModelType modelType, Schema inputSchema) throws IOException {
        inputReader = ReaderFactory.getReader(inputType);
        modelBuilder = BuilderFactory.getBuilder(modelType);
        this.inputSchema = inputSchema;
    }

    public void buildInputModel(InputStream inputStream, InputVariableCallback mappingHandler) throws ReaderException {
        this.mappingHandler = mappingHandler;
        inputReader.read(inputStream, this, inputSchema);
    }

    public void notifyEvent(DMReaderEvent readerEvent) throws IOException, JSException, SchemaException, ReaderException {
        switch (readerEvent.getEventType()) {
            case OBJECT_START:
                modelBuilder.writeObjectFieldStart(readerEvent.getName());
                break;
            case OBJECT_END:
                modelBuilder.writeEndObject();
                break;
            case ARRAY_START:
                modelBuilder.writeArrayFieldStart(readerEvent.getName());
                break;
            case FIELD:
                modelBuilder.writeField(readerEvent.getName(), readerEvent.getValue(), readerEvent.getFieldType());
                break;
            case ARRAY_END:
                modelBuilder.writeEndArray();
                break;
            case TERMINATE:
                modelBuilder.close();
                //TODO : move this to an observer
                mappingHandler.notifyInputVariable(modelBuilder.getContent());
                break;
            case ANONYMOUS_OBJECT_START:
                modelBuilder.writeStartObject();
                break;
            default:
                throw new IllegalArgumentException("Illegal Reader event found : " + readerEvent.getEventType());
        }

    }
}
