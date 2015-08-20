/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.cloud.gateway.agent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains the list of flags of polling task that has requested shut down
 */
public class CGAgentPollingTaskFlags {
    private static Map<String, Boolean> requestShutDownPollingTaskList =
            Collections.synchronizedMap(new HashMap<String, Boolean>());

    public static boolean isFlaggedForShutDown(String serviceName) {
        return requestShutDownPollingTaskList.get(serviceName);
    }

    public static void flagForShutDown(String serviceName, boolean flag) {
        requestShutDownPollingTaskList.put(serviceName, flag);
    }
}
