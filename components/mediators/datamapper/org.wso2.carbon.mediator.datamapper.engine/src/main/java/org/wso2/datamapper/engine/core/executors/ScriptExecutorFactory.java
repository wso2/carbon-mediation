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
package org.wso2.datamapper.engine.core.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.wso2.datamapper.engine.core.Executable;
import org.wso2.datamapper.engine.core.executors.nashorn.NasHornJava8Executor;
import org.wso2.datamapper.engine.utils.DataMapperEngineConstants;

/**
 * This class act as a factory to get the requested script executor
 */
public class ScriptExecutorFactory {

    private static ScriptExecutorPool executorPool = null;
    private static ScriptExecutorType scriptExecutorType = ScriptExecutorType.NASHORN; //default is Nashorn
    private static final Log log = LogFactory.getLog(ScriptExecutorFactory.class);

    /**
     * This private constructor added to hide the implicit public constructor
     */
    private ScriptExecutorFactory() {
    }

    /**
     * This method will return the script executor according to the given {@link ScriptExecutorType}
     *
     * @return script executor
     */
    public static Executable getScriptExecutor() throws InterruptedException {
        if (executorPool == null) {
            initializeExecutorPool();
        }
        return executorPool.take();
    }

    private synchronized static void initializeExecutorPool() {
        if (executorPool == null) {
            String javaVersion = System.getProperty("java.version");
            if (javaVersion.startsWith("1.7") || javaVersion.startsWith("1.6")) {
                //TODO : create Rhino engine
                log.error("Script Engine works only on Java 1.8 and above. Found java version : " + javaVersion);
            }

            String executorPoolSizeStr = SynapsePropertiesLoader.getPropertyValue(DataMapperEngineConstants.ORG_APACHE_SYNAPSE_DATAMAPPER_EXECUTOR_POOL_SIZE, null);
            int executorPoolSize = DataMapperEngineConstants.DEFAULT_DATAMAPPER_ENGINE_POOL_SIZE;
            if (executorPoolSizeStr != null) {
                executorPoolSize = Integer.parseInt(executorPoolSizeStr);
                log.debug("Script executor pool size set to " + executorPoolSize);
            } else {
                log.debug("Using default script executor pool size " + executorPoolSize);
            }
            executorPool = new ScriptExecutorPool(scriptExecutorType, executorPoolSize);
        }
    }

    /**
     * This method will release the script executor to the pool
     */
    public static void releaseScriptExecutor(Executable executor) throws InterruptedException {
        executorPool.put(executor);
    }
}
