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
package org.wso2.datamapper.engine.core.executors.nashorn;

import org.wso2.datamapper.engine.core.Executable;
import org.wso2.datamapper.engine.core.MappingResourceLoader;
import org.wso2.datamapper.engine.core.Model;
import org.wso2.datamapper.engine.core.exceptions.JSException;

/**
 * This class implements script executor for data mapper using java 8 NasHorn JS executor
 */
public class NasHornJava8Executor implements Executable {

    @Override
    public Model execute(MappingResourceLoader resourceModel, Model inputRecord, Model outputRecord) throws JSException {
        return null;
    }
}
