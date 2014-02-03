/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.business.messaging.hl7.common.data.conf;

import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
/**
 * This class represents data publisher instances
 */
public class EventPublisherConfig {

    private AsyncDataPublisher asyncDataPublisher;
    private LoadBalancingDataPublisher loadBalancingDataPublisher;

    public AsyncDataPublisher getAsyncDataPublisher() {
        return asyncDataPublisher;
    }

    public void setAsyncDataPublisher(AsyncDataPublisher asyncDataPublisher) {
        this.asyncDataPublisher = asyncDataPublisher;
    }

    public LoadBalancingDataPublisher getLoadBalancingDataPublisher() {
        return loadBalancingDataPublisher;
    }

    public void setLoadBalancingDataPublisher(
            LoadBalancingDataPublisher loadBalancingDataPublisher) {
        this.loadBalancingDataPublisher = loadBalancingDataPublisher;
    }
}
