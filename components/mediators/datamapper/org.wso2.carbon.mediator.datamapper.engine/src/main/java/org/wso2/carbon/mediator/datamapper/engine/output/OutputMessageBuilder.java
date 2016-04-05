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
package org.wso2.carbon.mediator.datamapper.engine.output;

import org.wso2.carbon.mediator.datamapper.engine.core.notifiers.OutputVariableNotifier;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.WriterException;
import org.wso2.carbon.mediator.datamapper.engine.core.mapper.MappingHandler;
import org.wso2.carbon.mediator.datamapper.engine.core.models.Model;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.mediator.datamapper.engine.input.readers.events.DMReaderEvent;
import org.wso2.carbon.mediator.datamapper.engine.output.formatters.Formatter;
import org.wso2.carbon.mediator.datamapper.engine.output.formatters.FormatterFactory;
import org.wso2.carbon.mediator.datamapper.engine.output.writers.Writer;
import org.wso2.carbon.mediator.datamapper.engine.output.writers.WriterFactory;
import org.wso2.carbon.mediator.datamapper.engine.utils.ModelTypes;
import org.wso2.carbon.mediator.datamapper.engine.utils.InputOutputDataTypes;

/**
 *
 */
public class OutputMessageBuilder {

    private Formatter formatter;
    private Writer outputWriter;
    private Schema outputSchema;
    private OutputVariableNotifier mappingHandler;

    public OutputMessageBuilder(InputOutputDataTypes dataType, ModelTypes dmModelType
            , Schema outputSchema) throws SchemaException, WriterException {
        this.outputSchema = outputSchema;
        this.formatter = FormatterFactory.getFormatter(dmModelType);
        this.outputWriter = WriterFactory.getWriter(dataType, outputSchema);
    }

    public void buildOutputMessage(Model outputModel, OutputVariableNotifier mappingHandler) throws SchemaException, WriterException {
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

    public Formatter getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public Writer getOutputWriter() {
        return outputWriter;
    }

    public void setOutputWriter(Writer outputWriter) {
        this.outputWriter = outputWriter;
    }

    public OutputVariableNotifier getMappingHandler() {
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
