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
 *
 */

package org.wso2.carbon.mediation.flow.statistics.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.newstatistics.StatisticsLog;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.wso2.carbon.mediation.flow.statistics.service.data.StatisticTreeWrapper;
import org.wso2.carbon.mediation.flow.statistics.store.jmx.StatisticCollectionViewMXBean;
import org.wso2.carbon.mediation.flow.statistics.store.jmx.StatisticsCompositeObject;
import org.wso2.carbon.mediation.flow.statistics.store.tree.data.StatisticsTree;

import java.util.*;

/**
 * StatisticsStore holds collected statistics in the memory. It stores these statistics in a tree
 * data structure where root of each of these trees belong to the trigger points of the ESB i.e.
 * PROXY, API or SEQUENCES
 */
public class StatisticsStore implements StatisticCollectionViewMXBean {

	private static final Log log = LogFactory.getLog(StatisticsStore.class);

	private final Map<String, StatisticsTree> proxyStatistics = new HashMap<>();

	private final Map<String, StatisticsTree> apiStatistics = new HashMap<>();

	private final Map<String, StatisticsTree> sequenceStatistics = new HashMap<>();

	private final Map<String, StatisticsTree> inboundEndpointStatistics = new HashMap<>();

	public StatisticsStore() {
		MBeanRegistrar.getInstance().registerMBean(this, "MediationFlowStatisticView", "MediationFlowStatisticView");
	}

	/**
	 * Updates the statistics tree corresponding to the received StatisticsLog ArrayList. If
	 * statistics tree is not  present creates the root node of the statistics tree
	 *
	 * @param statisticsLogs Collected statistics logs for a message flow
	 */
	public void update(List<StatisticsLog> statisticsLogs) {

		switch (statisticsLogs.get(0).getComponentType()) {
			case PROXYSERVICE:
				updateTree(statisticsLogs, proxyStatistics);
				break;
			case INBOUNDENDPOINT:
				updateTree(statisticsLogs, inboundEndpointStatistics);
				break;
			case SEQUENCE:
				updateTree(statisticsLogs, sequenceStatistics);
				break;
			case API:
				updateTree(statisticsLogs, apiStatistics);
				break;
			default:
				log.error("Unidentified component type reported statistics : " +
				          statisticsLogs.get(0).getComponentType());
		}

	}

	private void updateTree(List<StatisticsLog> statisticsLogs, Map<String, StatisticsTree> statisticsTreeMap) {
		if (!statisticsLogs.isEmpty()) {
			StatisticsTree tree;
			if (!statisticsTreeMap.containsKey(statisticsLogs.get(0).getComponentId())) {
				tree = new StatisticsTree();
				statisticsTreeMap.put(statisticsLogs.get(0).getComponentId(), tree);
			} else {
				tree = statisticsTreeMap.get(statisticsLogs.get(0).getComponentId());
			}
			tree.buildTree(statisticsLogs); //build tree with these statistic logs
		}
	}

	public Set<Map.Entry<String, StatisticsTree>> getSequencesWithValues() {
		return sequenceStatistics.entrySet();
	}

	public Set<Map.Entry<String, StatisticsTree>> getProxiesWithValues() {
		return proxyStatistics.entrySet();
	}

	public Set<Map.Entry<String, StatisticsTree>> getApisWithValues() {
		return apiStatistics.entrySet();
	}

	public Set<Map.Entry<String, StatisticsTree>> getInboundEndpointsWithValues() {
		return inboundEndpointStatistics.entrySet();
	}

	public StatisticTreeWrapper getApiStatistics(String apiName) {
		return apiStatistics.get(apiName).getComponentTree();
	}

	public StatisticTreeWrapper getProxyStatistics(String proxyName) {
		return proxyStatistics.get(proxyName).getComponentTree();
	}

	public StatisticTreeWrapper getInboundEndpointStatistics(String inboundEndpointName) {
		return inboundEndpointStatistics.get(inboundEndpointName).getComponentTree();
	}

	public StatisticTreeWrapper getSequenceStatistics(String sequenceName) {
		return sequenceStatistics.get(sequenceName).getComponentTree();
	}

	@Override public void resetAPIStatistics() {
		apiStatistics.clear();
	}

	@Override public void resetProxyStatistics() {
		proxyStatistics.clear();
	}

	@Override public void resetSequenceStatistics() {
		sequenceStatistics.clear();
	}

	@Override public void resetInboundEndpointStatistics() {
		inboundEndpointStatistics.clear();
	}

	@Override public void resetAllStatistics() {
		resetProxyStatistics();
		resetAPIStatistics();
		resetInboundEndpointStatistics();
		resetSequenceStatistics();
	}

	@Override public StatisticsCompositeObject getProxyServiceJmxStatistics(String proxyName) {
		if (proxyStatistics.containsKey(proxyName)) {
			return proxyStatistics.get(proxyName).getStatisticOfTheRoot();
		} else {
			return null;
		}
	}

	@Override public StatisticsCompositeObject getSequenceJmxStatistics(String sequenceName) {
		if (proxyStatistics.containsKey(sequenceName)) {
			return proxyStatistics.get(sequenceName).getStatisticOfTheRoot();
		} else {
			return null;
		}
	}

	@Override public StatisticsCompositeObject getApiJmxStatistics(String APIName) {
		if (proxyStatistics.containsKey(APIName)) {
			return proxyStatistics.get(APIName).getStatisticOfTheRoot();
		} else {
			return null;
		}
	}

	@Override public StatisticsCompositeObject getInboundEndpointJmxStatistics(String inboundEndpointName) {
		if (proxyStatistics.containsKey(inboundEndpointName)) {
			return proxyStatistics.get(inboundEndpointName).getStatisticOfTheRoot();
		} else {
			return null;
		}
	}

	@Override public StatisticsCompositeObject[] getProxyServiceJmxStatisticsTree(String proxyName) {
		if (proxyStatistics.containsKey(proxyName)) {
			return proxyStatistics.get(proxyName).getFullTree();
		} else {
			return null;
		}
	}

	@Override public StatisticsCompositeObject[] getSequenceJmxStatisticsTree(String sequenceName) {
		if (proxyStatistics.containsKey(sequenceName)) {
			return proxyStatistics.get(sequenceName).getFullTree();
		} else {
			return null;
		}
	}

	@Override public StatisticsCompositeObject[] getInboundEndpointJmxStatisticsTree(String inboundEndpointName) {
		if (proxyStatistics.containsKey(inboundEndpointName)) {
			return proxyStatistics.get(inboundEndpointName).getFullTree();
		} else {
			return null;
		}
	}

	@Override public StatisticsCompositeObject[] getApiJmxStatisticsTree(String apiName) {
		if (proxyStatistics.containsKey(apiName)) {
			return proxyStatistics.get(apiName).getFullTree();
		} else {
			return null;
		}
	}

}