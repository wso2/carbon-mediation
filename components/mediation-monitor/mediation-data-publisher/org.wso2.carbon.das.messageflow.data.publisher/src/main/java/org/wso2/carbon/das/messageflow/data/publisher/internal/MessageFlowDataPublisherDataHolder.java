/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.das.messageflow.data.publisher.internal;

import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class MessageFlowDataPublisherDataHolder {

    private EventStreamService publisherService;
    private RegistryService registryService;
    private ConfigurationContextService contextService;

    private static MessageFlowDataPublisherDataHolder serviceHolder = new MessageFlowDataPublisherDataHolder();

    private MessageFlowDataPublisherDataHolder() {

    }

    public static MessageFlowDataPublisherDataHolder getInstance() {
        return serviceHolder;
    }

    public EventStreamService getPublisherService() {
        return publisherService;
    }

    public void setPublisherService(EventStreamService publisherService) {
        this.publisherService = publisherService;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public ConfigurationContextService getContextService() {
        return contextService;
    }

    public void setContextService(ConfigurationContextService contextService) {
        this.contextService = contextService;
    }
}
