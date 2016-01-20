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
import org.apache.synapse.aspects.flow.statistics.data.raw.EndpointStatisticLog;
import org.apache.synapse.aspects.flow.statistics.data.raw.StatisticsLog;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.wso2.carbon.mediation.flow.statistics.MediationFlowStatisticsObserver;
import org.wso2.carbon.mediation.flow.statistics.service.data.StatisticTreeWrapper;
import org.wso2.carbon.mediation.flow.statistics.store.jmx.StatisticCollectionViewMXBean;
import org.wso2.carbon.mediation.flow.statistics.store.jmx.StatisticsCompositeObject;
import org.wso2.carbon.mediation.flow.statistics.store.tree.data.EndpointDataHolder;
import org.wso2.carbon.mediation.flow.statistics.store.tree.StatisticsTree;
import org.wso2.carbon.mediation.flow.statistics.store.tree.data.StatisticDataHolder;

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

	private final Map<String, EndpointDataHolder> endpointStatistics = new HashMap<>();

	private Set<MediationFlowStatisticsObserver> observers = new HashSet<MediationFlowStatisticsObserver>();

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

	public void updateEndpoint(EndpointStatisticLog endpointStatisticLog) {
		if (endpointStatistics.containsKey(endpointStatisticLog.getComponentId())) {
			endpointStatistics.get(endpointStatisticLog.getComponentId()).update(endpointStatisticLog);
		} else {
			EndpointDataHolder endpointDataHolder = new EndpointDataHolder(endpointStatisticLog);
			endpointStatistics.put(endpointStatisticLog.getComponentId(), endpointDataHolder);
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
			StatisticDataHolder statisticDataHolder = tree.buildTree(statisticsLogs); //build tree with these
			notifyObservers(statisticDataHolder);
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

	public Set<Map.Entry<String, EndpointDataHolder>> getEndpoint() {
		return endpointStatistics.entrySet();
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

	public StatisticDataHolder[] getAllMessageFlows(String request) {
		String[] requestData = request.split(":");
		if (requestData.length == 2) {
			switch (Integer.parseInt(requestData[0])) {
				case 1:
					return proxyStatistics.get(requestData[1]).getAllMessageFlows();
				case 2:
					return apiStatistics.get(requestData[1]).getAllMessageFlows();
				case 3:
					return inboundEndpointStatistics.get(requestData[1]).getAllMessageFlows();
				case 4:
					return sequenceStatistics.get(requestData[1]).getAllMessageFlows();
				case 5:
					return null;//endpointStatistics.get(requestData[1]).getAllMessageFlows();
				default:
					log.error("Requested message flow statistics incorrect type");
			}
		}
		return null;
	}

	public String getMessageFlowTree(String request) {
		String[] requestData = request.split(":");
		if (requestData.length == 3) {
			switch (Integer.parseInt(requestData[0])) {
				case 1:
					return proxyStatistics.get(requestData[1]).getMessageFlowStatisticTree(requestData[2]);
				case 2:
					return apiStatistics.get(requestData[1]).getMessageFlowStatisticTree(requestData[2]);
				case 3:
					return inboundEndpointStatistics.get(requestData[1]).getMessageFlowStatisticTree(requestData[2]);
				case 4:
					return sequenceStatistics.get(requestData[1]).getMessageFlowStatisticTree(requestData[2]);
				case 5:
					return null;//endpointStatistics.get(requestData[1]).getMessageFlowStatisticTree(requestData[2]);
				default:
					log.error("Requested message flow statistics incorrect type");
			}
		}
		return null;

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

	/**
	 * Unregister the custom statistics consumer from the mediation statistics store
	 *
	 * @param o The MediationStatisticsObserver instance to be removed
	 */
	public void unregisterObserver(MediationFlowStatisticsObserver o) {
		if (observers.contains(o)) {
			observers.remove(o);
			o.destroy();
		}
	}

	void unregisterObservers() {
		if (log.isDebugEnabled()) {
			log.debug("Unregistering mediation statistics observers");
		}

		for (MediationFlowStatisticsObserver o : observers) {
			o.destroy();
		}
		observers.clear();
	}

	private void notifyObservers(StatisticDataHolder snapshot) {

		for (MediationFlowStatisticsObserver o : observers) {
			try {
				o.updateStatistics(snapshot);
			} catch (Throwable t) {
				log.error("Error occured while notifying the statistics observer", t);
			}
		}
	}

}