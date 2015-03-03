/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediation.ntask;

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskLocationResolver;
import org.wso2.carbon.ntask.core.TaskServiceContext;

import java.util.Map;

/**
 * Use this {@link org.wso2.carbon.ntask.core.TaskLocationResolver} only if you need to run the same task
 * on multiple worker nodes concurrently. For each worker/member where you need
 * to run the task concurrently, you may have to call this
 * {@link org.wso2.carbon.ntask.core.TaskLocationResolver} and get the member location index value. Then
 * you can schedule that particular task on the selected member node.
 */
public class MultiMemberTaskLocationResolver implements TaskLocationResolver {

    private static int counter = 0;

    @Override
    public int getLocation(TaskServiceContext ctx, TaskInfo taskInfo) throws TaskException {
        return counter++ % ctx.getServerCount();
    }

    @Override
    public void init(Map<String, String> stringStringMap) throws TaskException {

    }
}
