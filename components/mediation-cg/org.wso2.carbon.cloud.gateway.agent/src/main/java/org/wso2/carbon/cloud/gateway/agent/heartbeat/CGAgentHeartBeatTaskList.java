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
package org.wso2.carbon.cloud.gateway.agent.heartbeat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maintain the list of heart beat task list added.
 */
public class CGAgentHeartBeatTaskList {
    /**
     * Keep track of scheduled heart beat tasks in the form of 'host:port', i.e. there will be a
     * deployed heart beat task for each host:port in the list
     */
    private static List<String> scheduledHeartBeatTasksList = 
            Collections.synchronizedList(new ArrayList<String>());
    
    public static void addScheduledHeartBeatTask(String task){
        scheduledHeartBeatTasksList.add(task);
    }
    
    public static void removeScheduledHeartBeatTask(String task){
        scheduledHeartBeatTasksList.remove(task);    
    }
    
    public static boolean isScheduledHeartBeatTaskAvailable(String task){
        return scheduledHeartBeatTasksList.contains(task);
    }
}
