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
package org.wso2.datamapper.engine.core.executors;

import org.wso2.datamapper.engine.core.IScriptExecutor;
import org.wso2.datamapper.engine.core.executors.nashorn.NasHornJava8Executor;
import org.wso2.datamapper.engine.core.executors.rhino.RhinoExecutor;

/**
 * This class act as a factory to get the requested script executor
 */
public class ScriptExecutorFactory {

    /**
     * This private constructor added to hide the implicit public constructor
     */
    private ScriptExecutorFactory() {

    }

    /**
     * This method will return the script executor according to the given {@link ScriptExecutorType}
     *
     * @param executorType
     * @return
     */
    public static IScriptExecutor getScriptExecutor(ScriptExecutorType executorType) {
        switch (executorType) {
        case RHINO:
            return new RhinoExecutor();
        case NASHORN:
            return new NasHornJava8Executor();
        }
        throw new IllegalArgumentException("Unsupported script engine type found : " + executorType);
    }
}
