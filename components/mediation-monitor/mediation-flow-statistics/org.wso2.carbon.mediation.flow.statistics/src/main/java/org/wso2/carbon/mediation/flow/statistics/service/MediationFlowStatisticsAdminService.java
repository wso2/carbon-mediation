/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.mediation.flow.statistics.service;

import org.wso2.carbon.mediation.flow.statistics.MessageFlowStatisticConstants;
import org.wso2.carbon.mediation.flow.statistics.service.data.AdminData;
import org.wso2.carbon.mediation.flow.statistics.service.data.StatisticTreeWrapper;
import org.wso2.carbon.mediation.flow.statistics.store.StatisticsStore;
import org.wso2.carbon.mediation.flow.statistics.store.tree.data.EndpointDataHolder;
import org.wso2.carbon.mediation.flow.statistics.store.tree.data.StatisticsTree;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the admin service class that will expose to the UI.
 */
public class MediationFlowStatisticsAdminService extends AbstractServiceBusAdmin {

	public AdminData[] getAllSequenceStatistics() {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));
		return getAdminData(statisticsStore.getSequencesWithValues());
	}

	public AdminData[] getAllApiStatistics() {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));
		return getAdminData(statisticsStore.getApisWithValues());
	}

	public AdminData[] getAllProxyStatistics() {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));
		return getAdminData(statisticsStore.getProxiesWithValues());
	}

	public AdminData[] getAllInboundEndpointStatistics() {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));
		return getAdminData(statisticsStore.getInboundEndpointsWithValues());
	}

	public AdminData[] getAllEndpointStatistics() {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));
		return getAdminDataForEndpoint(statisticsStore.getEndpoint());
	}

	public StatisticTreeWrapper getProxyStatistics(String componentID) {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));
		return statisticsStore.getProxyStatistics(componentID);
	}

	public StatisticTreeWrapper getInboundEndpointStatistics(String componentID) {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));
		return statisticsStore.getInboundEndpointStatistics(componentID);
	}

	public StatisticTreeWrapper getSequenceStatistics(String componentID) {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));
		return statisticsStore.getSequenceStatistics(componentID);
	}

	public StatisticTreeWrapper getApiStatistics(String componentID) {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));
		return statisticsStore.getApiStatistics(componentID);
	}

	private AdminData[] getAdminData(Set<Map.Entry<String, StatisticsTree>> statRecords) {
		List<AdminData> outputList = new LinkedList<>();
		for (Map.Entry<String, StatisticsTree> entry : statRecords) {
			outputList.add(new AdminData(entry.getKey(), entry.getValue().getInfo()));
		}
		return outputList.toArray(new AdminData[outputList.size()]);
	}

	private AdminData[] getAdminDataForEndpoint(Set<Map.Entry<String, EndpointDataHolder>> statRecords) {
		List<AdminData> outputList = new LinkedList<>();
		for (Map.Entry<String, EndpointDataHolder> entry : statRecords) {
			outputList.add(new AdminData(entry.getKey(), entry.getValue().getInfo()));
		}
		return outputList.toArray(new AdminData[outputList.size()]);
	}

}
