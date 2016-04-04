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
package org.wso2.datamapper.engine.output;

import org.wso2.datamapper.engine.core.MappingHandler;
import org.wso2.datamapper.engine.core.Model;
import org.wso2.datamapper.engine.core.Schema;
import org.wso2.datamapper.engine.core.callbacks.OutputVariableCallback;
import org.wso2.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.datamapper.engine.core.exceptions.WriterException;
import org.wso2.datamapper.engine.input.readers.events.DMReaderEvent;
import org.wso2.datamapper.engine.output.formatters.FormatterFactory;
import org.wso2.datamapper.engine.output.writers.WriterFactory;
import org.wso2.datamapper.engine.types.DMModelTypes;
import org.wso2.datamapper.engine.types.InputOutputDataTypes;

/**
 *
 */
public class OutputMessageBuilder {

    private Formattable formatter;
    private Writable outputWriter;
    private Schema outputSchema;
    private OutputVariableCallback mappingHandler;

    public OutputMessageBuilder(InputOutputDataTypes.DataType dataType, DMModelTypes.ModelType dmModelType
            , Schema outputSchema) throws SchemaException {
        this.outputSchema = outputSchema;
        this.formatter = FormatterFactory.getFormatter(dmModelType);
        this.outputWriter = WriterFactory.getWriter(dataType, outputSchema);
    }

    public void buildOutputMessage(Model outputModel, OutputVariableCallback mappingHandler) throws SchemaException, WriterException {
        this.mappingHandler = mappingHandler;
        formatter.format(outputModel, this, outputSchema);
    }

    public void notifyEvent(DMReaderEvent readerEvent) throws SchemaException, WriterException {
        switch (readerEvent.getEventType()) {
            case OBJECT_START:
                outputWriter.writeStartObject(readerEvent.getName());
                break;
            case FIELD:
                outputWriter.writeField(readerEvent.getName(), readerEvent.getValue());
                break;
            case OBJECT_END:
                outputWriter.writeEndObject(readerEvent.getName());
                break;
            case TERMINATE:
                mappingHandler.notifyOutputVariable(outputWriter.terminateMessageBuilding());
                break;
            case ARRAY_START:
                outputWriter.writeStartArray();
                break;
            case ARRAY_END:
                outputWriter.writeEndArray();
                break;
            case ANONYMOUS_OBJECT_START:
                outputWriter.writeStartAnonymousObject();
                break;
            default:
                throw new IllegalArgumentException("Illegal Reader event found : " + readerEvent.getEventType());
        }
    }

    public Formattable getFormatter() {
        return formatter;
    }

    public void setFormatter(Formattable formatter) {
        this.formatter = formatter;
    }

    public Writable getOutputWriter() {
        return outputWriter;
    }

    public void setOutputWriter(Writable outputWriter) {
        this.outputWriter = outputWriter;
    }

    public OutputVariableCallback getMappingHandler() {
        return mappingHandler;
    }

    public void setMappingHandler(MappingHandler mappingHandler) {
        this.mappingHandler = mappingHandler;
    }

    public Schema getOutputSchema() {
        return outputSchema;
    }

    public void setOutputSchema(Schema outputSchema) {
        this.outputSchema = outputSchema;
    }

}
