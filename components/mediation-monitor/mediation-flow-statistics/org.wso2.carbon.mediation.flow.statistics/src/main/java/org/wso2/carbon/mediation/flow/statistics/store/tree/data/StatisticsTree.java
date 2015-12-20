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

package org.wso2.carbon.mediation.flow.statistics.store.tree.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.newstatistics.StatisticsLog;
import org.wso2.carbon.mediation.flow.statistics.service.data.EdgeData;
import org.wso2.carbon.mediation.flow.statistics.service.data.StatisticTreeWrapper;
import org.wso2.carbon.mediation.flow.statistics.service.data.TreeNodeData;
import org.wso2.carbon.mediation.flow.statistics.store.jmx.StatisticsCompositeObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * StatisticsTree holds statistics tree node for each trigger point in the ESB.
 */
public class StatisticsTree {

	private static final Log log = LogFactory.getLog(StatisticsTree.class);

	/**
	 * Statistic Tree of the component
	 */
	private List<IndividualStatistic> statisticTree;

	public TreeNodeData getInfo() {
		return statisticTree.get(0).getTreeNodeData();
	}

	/**
	 * Build or update statistics tree based on the data statistic retrieved from synapse.
	 *
	 * @param statisticsLogs Statistic Logs belonging to single flow.
	 */
	public void buildTree(List<StatisticsLog> statisticsLogs) {
		if (statisticTree == null) {
			statisticTree = new ArrayList<>();
			IndividualStatistic individualStatistic = new IndividualStatistic(statisticsLogs.get(0));
			statisticTree.add(individualStatistic);
			updateTree(statisticsLogs, 0, 0);
		} else {
			statisticTree.get(0).update(statisticsLogs.get(0));
			updateTree(statisticsLogs, 0, 0);
		}
	}

	/**
	 * Populates statistic tree to be shown in UI.
	 *
	 * @return Returns statistic tree containing nodes and their edge relationship.
	 */
	public StatisticTreeWrapper getComponentTree() {
		List<TreeNodeData> statisticsTreeNodes = new ArrayList<>();
		List<EdgeData> statisticsTreeEdges = new ArrayList<>();

		for (int i = 0; i < statisticTree.size(); i++) {
			IndividualStatistic individualStatistic = statisticTree.get(i);
			TreeNodeData treeNodeData = new TreeNodeData(individualStatistic);
			for (Map.Entry<String, Integer> entry : individualStatistic.getChildrenMap().entrySet()) {
				EdgeData edgeData = new EdgeData(i, entry.getValue());
				statisticsTreeEdges.add(edgeData);
			}
			statisticsTreeNodes.add(treeNodeData);
		}
		return new StatisticTreeWrapper(statisticsTreeEdges.toArray(new EdgeData[statisticsTreeEdges.size()]),
		                                statisticsTreeNodes.toArray(new TreeNodeData[statisticsTreeNodes.size()]));
	}

	/**
	 * Sends composite data object of the root of the tree to expose data through JMX.
	 *
	 * @return Composite Data object of the root.
	 */
	public StatisticsCompositeObject getStatisticOfTheRoot() {
		if (statisticTree != null) {
			return getCompositeDataObject(statisticTree.get(0));
		} else {
			return null;
		}
	}

	/**
	 * Combine All the statistics available in the statistic tree and send it to JMX.
	 *
	 * @return JMX statistic tree details.
	 */
	public StatisticsCompositeObject[] getFullTree() {
		if (statisticTree != null) {
			List<StatisticsCompositeObject> statisticSamples = getStatisticTreeAsArrayList();
			StatisticsCompositeObject[] statisticSampleArray = new StatisticsCompositeObject[statisticSamples.size()];
			return statisticSamples.toArray(statisticSampleArray);
		} else {
			return null;
		}

	}

	private void updateTree(List<StatisticsLog> statisticsLogList, int statisticLogIndex, int treeIndex) {
		IndividualStatistic treeNode = statisticTree.get(treeIndex);
		StatisticsLog statisticsLog = statisticsLogList.get(statisticLogIndex);
		if (statisticsLog == null) {
			return;
		}
		if (statisticsLog.getChildren().size() > 0) {
			for (int child : statisticsLog.getChildren()) {
				StatisticsLog childLog = statisticsLogList.get(child);
				if (childLog != null) {
					setChildNode(statisticsLogList, treeNode, child, childLog);
				}
			}
		} else if (statisticsLog.getImmediateChild() != null) {
			StatisticsLog childLog = statisticsLogList.get(statisticsLog.getImmediateChild());
			if (childLog != null) {
				setChildNode(statisticsLogList, treeNode, statisticsLog.getImmediateChild(), childLog);
			}

		}
		statisticsLog.setTreeMapping(treeIndex);
	}

	private void setChildNode(List<StatisticsLog> statisticsLogList, IndividualStatistic treeNode, int child,
	                          StatisticsLog childLog) {
		String childKey = getChildKey(childLog);
		Integer childTreePosition = treeNode.getChild(childKey);
		if (childTreePosition == null) {
			if (childLog.getTreeMapping() == null) {
				IndividualStatistic individualStatistic = new IndividualStatistic(childLog);
				statisticTree.add(individualStatistic);
				treeNode.setChild(childKey, statisticTree.size() - 1);
				updateTree(statisticsLogList, child, statisticTree.size() - 1);
			} else {
				treeNode.setChild(childKey, childLog.getTreeMapping());
				statisticTree.get(childLog.getTreeMapping()).update(childLog);
			}
		} else {
			statisticTree.get(childTreePosition).update(childLog);
			if (childLog.getTreeMapping() == null) {
				updateTree(statisticsLogList, child, childTreePosition);
			}
		}
	}

	private String getChildKey(StatisticsLog childLog) {
		return childLog.getComponentId() + childLog.getMsgId();
	}

	/**
	 * Returns Composite Data Object relating to a in individualStatistic Object.
	 *
	 * @param individualStatistic IndividualStatistic Object.
	 * @return Composite Data Object that contain statistics of individualStatistic Object.
	 */
	private StatisticsCompositeObject getCompositeDataObject(IndividualStatistic individualStatistic) {
		if (individualStatistic != null) {
			return new StatisticsCompositeObject(individualStatistic.getComponentId(),
			                                     individualStatistic.getComponentType().toString(),
			                                     individualStatistic.getMsgId(),
			                                     individualStatistic.getMaxProcessingTime(),
			                                     individualStatistic.getMinProcessingTime(),
			                                     individualStatistic.getAvgProcessingTime(),
			                                     individualStatistic.getIsResponse(), individualStatistic.getCount(),
			                                     individualStatistic.getFaultCount());
		}
		return null;
	}

	/**
	 * Put all the individualStatistics Objects in a statistic tree to a ArrayList.
	 */
	private List<StatisticsCompositeObject> getStatisticTreeAsArrayList() {
		List<StatisticsCompositeObject> statisticSamples = new ArrayList<>();
		for (IndividualStatistic statisticNode : statisticTree) {
			statisticSamples.add(getCompositeDataObject(statisticNode));
		}
		return statisticSamples;
	}
}
