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

import org.apache.avro.generic.GenericRecord;
import org.json.JSONException;
import org.wso2.datamapper.engine.core.exceptions.JSException;
import org.wso2.datamapper.engine.core.executors.ScriptExecutorFactory;
import org.wso2.datamapper.engine.core.executors.ScriptExecutorType;
import org.wso2.datamapper.engine.inputAdapters.InputDataReaderAdapter;

import java.io.IOException;
import java.io.InputStream;


public class MappingHandler {

    public static GenericRecord doMap(InputStream inputMsg, MappingResourceLoader resourceModel, InputDataReaderAdapter inputReader)
            throws IOException, IllegalAccessException, InstantiationException, JSONException, JSException {

        inputReader.setInputMsg(inputMsg);
        GenericRecord inputRecord = inputReader.getInputRecord(resourceModel.getInputSchema());
        IScriptExecutor scriptExecutor = ScriptExecutorFactory.getScriptExecutor(ScriptExecutorType.RHINO);
        GenericRecord outputRecord = scriptExecutor.executeMapping(resourceModel, inputRecord);

        return outputRecord;

    }

}
