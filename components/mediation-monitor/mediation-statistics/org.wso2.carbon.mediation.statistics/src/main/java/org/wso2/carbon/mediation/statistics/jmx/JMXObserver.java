/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.statistics.jmx;

import org.wso2.carbon.mediation.statistics.MediationStatisticsObserver;
import org.wso2.carbon.mediation.statistics.MessageTraceLog;
import org.wso2.carbon.mediation.statistics.StatisticsRecord;
import org.wso2.carbon.mediation.statistics.MediationStatisticsSnapshot;
import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.commons.jmx.MBeanRegistrar;

import java.util.Map;
import java.util.HashMap;

public class JMXObserver implements MediationStatisticsObserver {
    // Maps to store mbeans (for individual sequences, endpoints etc)
    private Map<ComponentType, Map<String, StatisticsView>> inDataStore =
            new HashMap<ComponentType, Map<String,StatisticsView>>();

    private Map<ComponentType, Map<String, StatisticsView>> outDataStore =
            new HashMap<ComponentType, Map<String, StatisticsView>>();

    private Map<String, String> registeredMbeanIds = new HashMap<String, String>();

    public JMXObserver() {
        inDataStore = new HashMap<ComponentType, Map<String, StatisticsView>>();

        inDataStore.put(ComponentType.ENDPOINT, new HashMap<String, StatisticsView>());
        inDataStore.put(ComponentType.SEQUENCE, new HashMap<String, StatisticsView>());
        inDataStore.put(ComponentType.PROXYSERVICE, new HashMap<String, StatisticsView>());
        inDataStore.put(ComponentType.ANY, new HashMap<String, StatisticsView>());

        outDataStore = new HashMap<ComponentType, Map<String, StatisticsView>>();

        outDataStore.put(ComponentType.ENDPOINT, new HashMap<String, StatisticsView>());
        outDataStore.put(ComponentType.SEQUENCE, new HashMap<String, StatisticsView>());
        outDataStore.put(ComponentType.PROXYSERVICE, new HashMap<String, StatisticsView>());
        outDataStore.put(ComponentType.ANY, new HashMap<String, StatisticsView>());
    }

    public void destroy() {
        for (String key : registeredMbeanIds.keySet()) {
            MBeanRegistrar.getInstance().unRegisterMBean(key, registeredMbeanIds.get(key));
        }
    }

    public void updateStatistics(MediationStatisticsSnapshot snapshot) {
        StatisticsRecord update = snapshot.getUpdate();
                
        if (update.isInStatistic()) {
            Map<String, StatisticsView> map = inDataStore.get(update.getType());
            StatisticsView view;
            if (!map.containsKey(update.getResourceId())) {
                view = new StatisticsView();
                MBeanRegistrar.getInstance().registerMBean(view,
                        getCatagoryForType(update.getType()),
                        update.getResourceId() + "-in");
                // store this information to unregister the MBeans later
                registeredMbeanIds.put(getCatagoryForType(update.getType()),
                        update.getResourceId() + "-in");

                map.put(update.getResourceId(), view);
            } else {
                view = map.get(update.getResourceId());
            }

            updateView(view, update);
        } else {
            Map<String, StatisticsView> map = outDataStore.get(update.getType());
            StatisticsView view;
            if (!map.containsKey(update.getResourceId())) {
                view = new StatisticsView();
                MBeanRegistrar.getInstance().registerMBean(view,
                        getCatagoryForType(update.getType()),
                        update.getResourceId() + "-out");
                // store this information to unregister the MBeans later
                registeredMbeanIds.put(getCatagoryForType(update.getType()),
                        update.getResourceId() + "-out");

                map.put(update.getResourceId(), view);
            } else {
                view = map.get(update.getResourceId());
            }

            updateView(view, update);
        }
    }

    private void updateView(StatisticsView view, StatisticsRecord update) {
        long updatedTotalCount = view.getTotalCount() + update.getTotalCount();

        if (updatedTotalCount > 0) {
            view.setFaultCount(update.getFaultCount() + view.getFaultCount());
            view.setAvgTime((view.getAvgTime() * view.getTotalCount() +
                    update.getAvgTime() * update.getTotalCount()) / updatedTotalCount);

            if (update.getMaxTime() > view.getMaxTime()) {
                view.setMaxTime(update.getMaxTime());
            }

            if (view.getMinTime() == StatisticsRecord.DEFAULT_MIN_TIME ||
                    update.getMinTime() < view.getMinTime()) {
                view.setMinTime(update.getMinTime());
            }

            view.setTotalCount(updatedTotalCount);
        }
    }

    private String getCatagoryForType(ComponentType type) {
        if (type == ComponentType.ENDPOINT) {
            return "Endpoint-Statistics";
        } else if (type == ComponentType.PROXYSERVICE) {
            return "ProxyService-Statistics";
        } else if (type == ComponentType.SEQUENCE) {
            return "Sequence-Statistics";
        } else if (type == ComponentType.ANY) {
            return "Any-Statistics";
        }

        return "";
    }

    public void notifyTraceLogs(MessageTraceLog[] logs) {
        // TODO Auto-generated method stub

    }
}
