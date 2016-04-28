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

import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.mediator.datamapper.engine.core.exceptions.WriterException;
import org.wso2.carbon.mediator.datamapper.engine.core.models.Model;
import org.wso2.carbon.mediator.datamapper.engine.core.notifiers.OutputVariableNotifier;
import org.wso2.carbon.mediator.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.mediator.datamapper.engine.output.formatters.AxiomXMLMapOutputFormatter;
import org.wso2.carbon.mediator.datamapper.engine.utils.InputOutputDataType;
import org.wso2.carbon.mediator.datamapper.engine.utils.ModelType;

public class OutputXMLMessageBuilder {

    private AxiomXMLMapOutputFormatter formatter;
    private Schema outputSchema;
    private OutputVariableNotifier outputVariableNotifier;

    public OutputXMLMessageBuilder(InputOutputDataType dataType, ModelType modelType, Schema outputSchema)
            throws SchemaException, WriterException {
        this.outputSchema = outputSchema;
        this.formatter = new AxiomXMLMapOutputFormatter();
    }

    public void buildOutputMessage(Model outputModel, OutputVariableNotifier mappingHandler)
            throws SchemaException, WriterException {
        this.outputVariableNotifier = mappingHandler;
        this.formatter.format(outputModel, this, outputSchema);
    }

    public void notifyWithResult(String builtMessage){
        outputVariableNotifier.notifyOutputVariable(builtMessage);
    }
}
