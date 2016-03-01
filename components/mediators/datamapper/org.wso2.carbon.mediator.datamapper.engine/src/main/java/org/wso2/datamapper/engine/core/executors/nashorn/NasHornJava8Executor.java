/*
 * Copyright 2016 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.datamapper.engine.core.executors.nashorn;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.synapse.SynapseException;
import org.wso2.datamapper.engine.core.IScriptExecutor;
import org.wso2.datamapper.engine.core.MappingResourceLoader;
import org.wso2.datamapper.engine.core.exceptions.JSException;

/**
 * This class implements script executor for data mapper using java 8 NasHorn JS executor
 */
public class NasHornJava8Executor implements IScriptExecutor {

    @Override
    public GenericRecord executeMapping(MappingResourceLoader resourceModel, GenericRecord inputRecord)
            throws JSException {
        GenericRecord genericOutRecord = new GenericData.Record(resourceModel.getOutputSchema());

        throw new SynapseException("Invalid output mapped generic record found");
    }
}
