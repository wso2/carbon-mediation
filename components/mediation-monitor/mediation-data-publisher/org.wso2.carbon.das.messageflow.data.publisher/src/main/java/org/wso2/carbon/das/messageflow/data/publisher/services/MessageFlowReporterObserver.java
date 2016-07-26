/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.das.messageflow.data.publisher.services;

import org.apache.log4j.Logger;
import org.apache.synapse.aspects.flow.statistics.log.StatisticsReportingEventHolder;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;
import org.apache.synapse.aspects.flow.statistics.store.CompletedStatisticStore;
import org.wso2.carbon.das.messageflow.data.publisher.data.MessageFlowObserverStore;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MessageFlowReporterObserver implements Observer {
    private static Logger log = Logger.getLogger(MessageFlowReporterObserver.class);

    private MessageFlowObserverStore messageFlowObserverStore;

    public MessageFlowReporterObserver(MessageFlowObserverStore messageFlowObserverStore) {
        this.messageFlowObserverStore = messageFlowObserverStore;
    }

    @Override
    public void update(Observable o, Object arg) {
        messageFlowObserverStore.notifyObservers((PublishingFlow)arg);
    }
}
