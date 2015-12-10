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

	private IndividualStatistic root = null;
	private Integer currentIndex = null;

	//	public StatisticsTree() {
	//		statisticTree = new ArrayList<>();
	//	}

	//	public StatisticsTree(StatisticsLog log) {
	//		root = new IndividualStatistic(log.getComponentId(), log.getComponentType(), log.getMsgId(), "",
	//		                                log.getParentMsgId(), log.getEndTime() - log.getStartTime(),
	//		                                log.getNoOfFaults());
	//		root.setIsResponse(log.isResponse());
	//	}

	public TreeNodeData getInfo() {
		return statisticTree.get(0).getTreeNodeData();
	}

	public IndividualStatistic getRoot() {
		return root;
	}

	//	public StatisticsCompositeObject getStatisticOfTheRoot() {
	//		return getCompositeDataObject(root);
	//	}

	public StatisticTreeWrapper getComponentTree() {
		//currentIndex = 0;
		List<TreeNodeData> statisticsTreeNodes = new ArrayList<>();
		List<EdgeData> statisticsTreeEdges = new ArrayList<>();

		for (int i = 0; i < statisticTree.size(); i++) {
			IndividualStatistic individualStatistic = statisticTree.get(i);
			TreeNodeData treeNodeData = new TreeNodeData(individualStatistic.getComponentId(),
			                                             individualStatistic.getComponentTypeToString(),
			                                             individualStatistic.getCount(),
			                                             individualStatistic.getMaxProcessingTime(),
			                                             individualStatistic.getMinProcessingTime(),
			                                             individualStatistic.getAvgProcessingTime(),
			                                             individualStatistic.getFaultCount(),
			                                             individualStatistic.getIsResponse());

			for (Map.Entry<String, Integer> entry : individualStatistic.getChildrenMap().entrySet()) {
				EdgeData edgeData = new EdgeData(i, entry.getValue());
				statisticsTreeEdges.add(edgeData);
			}

			statisticsTreeNodes.add(treeNodeData);
		}

		//		traverseAndGetTree(root, statisticsTreeNodes, statisticsTreeEdges);
		//		currentIndex = null;

		return new StatisticTreeWrapper(statisticsTreeEdges.toArray(new EdgeData[statisticsTreeEdges.size()]),
		                                statisticsTreeNodes.toArray(new TreeNodeData[statisticsTreeNodes.size()]));
	}

	private void traverseAndGetTree(IndividualStatistic treeNode, List<TreeNodeData> statisticsTreeNodes,
	                                List<EdgeData> statisticsTreeEdges) {
		//		int thisParentIndex = currentIndex;
		//		statisticsTreeNodes.add(treeNode.getTreeNodeData());

		//		for (IndividualStatistic individualStatistic : treeNode.getChildren()) {
		//			currentIndex++;
		//			statisticsTreeEdges.add(new EdgeData(thisParentIndex, currentIndex));
		//			System.out.println("parent: " + thisParentIndex + "|child: " + currentIndex);
		//			traverseAndGetTree(individualStatistic, statisticsTreeNodes, statisticsTreeEdges);
		//		}
	}

	/**
	 * Creates a new statistic log or updates the existing statistics log for the given statistics
	 * logs
	 *
	 * @param statisticsLogs statistics logs relating to a message flow
	 */
	//	public void buildTree(List<StatisticsLog> statisticsLogs) {
	//
	//		IndividualStatistic currentStat = root;
	//		//send root node of the tree as parent to next element and recursively find children
	////		for (int i = 0; i < statisticsLogs.size(); i++) {
	////			if (statisticsLogs.get(i) != null) {
	////				currentStat = buildTree(statisticsLogs, currentStat, statisticsLogs.get(i).getNoOfChildren(),
	////				                        currentStat.getComponentId(), statisticsLogs.get(i).getParentMsgId(), i + 1);
	////			}
	////		}
	//		if (log.isDebugEnabled()) { //this will be removed in actual implementation for testing only
	//			StringBuilder sb = new StringBuilder();
	//			sb.append("\nStatistics Tree\n");
	//			print(root, sb);
	//			log.debug(sb.toString());
	//		}
	//		getComponentTree();
	//	}

	private List<IndividualStatistic> statisticTree;

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

	public void updateTree(List<StatisticsLog> statisticsLogList, int statisticLogIndex, int treeIndex) {
		IndividualStatistic treeNode = statisticTree.get(treeIndex);
		StatisticsLog statisticsLog = statisticsLogList.get(statisticLogIndex);

		if (statisticsLog == null) {
			return;
		}

		if (statisticsLog.getChildren().size() > 0) {

			for (int child : statisticsLog.getChildren()) {
				StatisticsLog childLog = statisticsLogList.get(child);
				if (childLog == null) {
					continue;
				}
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

		} else if (statisticsLog.getImmediateChild() != null) {
			StatisticsLog childLog = statisticsLogList.get(statisticsLog.getImmediateChild());
			if (childLog != null) {
				String childKey = getChildKey(childLog);
				Integer childTreePosition = treeNode.getChild(childKey);
				if (childTreePosition == null) {
					if (childLog.getTreeMapping() == null) {
						IndividualStatistic individualStatistic = new IndividualStatistic(childLog);
						statisticTree.add(individualStatistic);
						treeNode.setChild(childKey, statisticTree.size() - 1);
						updateTree(statisticsLogList, statisticsLog.getImmediateChild(), statisticTree.size() - 1);
					} else {
						treeNode.setChild(childKey, childLog.getTreeMapping());
						statisticTree.get(childLog.getTreeMapping()).update(childLog);
					}
				} else {
					statisticTree.get(childTreePosition).update(childLog);
					if (childLog.getTreeMapping() == null) {
						updateTree(statisticsLogList, statisticsLog.getImmediateChild(), childTreePosition);
					}
				}
			}

		}

		statisticsLog.setTreeMapping(treeIndex);

		//statisticsLogList.set(statisticLogIndex, null);
	}

	public void initializeStatisticTree(List<StatisticsLog> statisticsLogs) {
		for (StatisticsLog statisticsLog : statisticsLogs) {
			IndividualStatistic individualStatistic = new IndividualStatistic(statisticsLog);
			List<Integer> children = statisticsLog.getChildren();

			if (children.size() > 0 && statisticsLog.getImmediateChild() != null) {
				log.error("Something wrong with the statistic data. Component:" + statisticsLog.getComponentId() +
				          "have both Immediate child and child map");
			}
			if (children.size() > 0) {
				for (Integer child : children) {
					StatisticsLog childLog = statisticsLogs.get(child);
					String key = getChildKey(childLog);
					individualStatistic.setChild(key, child);
				}
			} else if (statisticsLog.getImmediateChild() != null) {
				StatisticsLog childLog = statisticsLogs.get(statisticsLog.getImmediateChild());
				String key = getChildKey(childLog);
				individualStatistic.setChild(key, statisticsLog.getImmediateChild());
			}
			statisticTree.add(individualStatistic);
		}
	}

	private String getChildKey(StatisticsLog childLog) {
		return childLog.getComponentId() + childLog.getMsgId();
	}

	public void buildTree(List<StatisticsLog> statisticsLogs, int currentIndex) {

	}

	/**
	 * This method recursively build and update the statistics tree
	 *
	 * @param statisticsLogs statistic logs corresponding to the message flow
	 * @param currentStat    current node of the statistics tree
	 * @param noOfChildren   no of children to be search for this parent
	 * @param parentId       parents name
	 * @param parentMsgId    parent's msg Id
	 * @param offset         from where to start searching for children
	 * @return reference to the child
	 */
	//	private IndividualStatistic buildTree(List<StatisticsLog> statisticsLogs, IndividualStatistic currentStat,
	//	                                       int noOfChildren, String parentId, int parentMsgId, int offset) {
	//		int count = 0;
	//		for (int index = offset; index < statisticsLogs.size(); index++) {
	//			if (!(statisticsLogs.get(index) == null)) {
	//				if (statisticsLogs.get(index).getParent().equals(parentId) &&
	//				    (statisticsLogs.get(index).getParentMsgId() == parentMsgId)) {
	//					count++;
	//					//if parent is equal to current stat log get child corresponding to this log
	//					currentStat = getChild(currentStat.getChildren(), statisticsLogs.get(index));
	//
	//					//if that children have children find them recursively
	//					//					if (statisticsLogs.get(index).isHasChildren()) {
	//					//						currentStat =
	//					//								buildTree(statisticsLogs, currentStat, statisticsLogs.get(index).getNoOfChildren(),
	//					//								          currentStat.getComponentId(), statisticsLogs.get(index).getMsgId(),
	//					//								          index + 1);
	//					//					}
	//					statisticsLogs.set(index, null);
	//					if (count == noOfChildren) {
	//						break;
	//					}
	//				}
	//			}
	//		}
	//		return currentStat; //as next sb child will be placed under above child
	//	}

	/**
	 * find the child in the tree node child list which matched with the statistics log componentId
	 * . If no child matches with the log create a new child in the tree.
	 *
	 * @param childList     child list of the corresponding tree node
	 * @param statisticsLog current statistic log
	 * @return refrence to the child element
	 */
	//	private IndividualStatistic getChild(ArrayList<IndividualStatistic> childList, StatisticsLog statisticsLog) {
	//		//if child present get it otherwise create a child
	//		for (IndividualStatistic statisticsNode : childList) {
	//			if (statisticsLog.getParent().equals(statisticsNode.getParentId()) &&
	//			    statisticsLog.getComponentId().equals(statisticsNode.getComponentId()) &&
	//			    (statisticsNode.getMsgId() == statisticsLog.getMsgId()) &&
	//			    (statisticsNode.getParentMsgId() == statisticsLog.getParentMsgId())) {
	//				statisticsNode.update(statisticsLog.getNoOfFaults(),
	//				                      statisticsLog.getEndTime() - statisticsLog.getStartTime());
	//				return statisticsNode;
	//			}
	//		}
	//IndividualStatistic statisticsNode = createNewNodeForLog(statisticsLog);
	//		childList.add(statisticsNode);
	//		return statisticsNode;
	//	}

	/**
	 * Create a new tree node for the statistic log
	 *
	 * @param statisticsLog current statistics log
	 * @return tree node for the log
	 */
	//	private IndividualStatistic createNewNodeForLog(StatisticsLog statisticsLog) {
	//		IndividualStatistic statisticsNode =
	//				new IndividualStatistic(statisticsLog.getComponentId(), statisticsLog.getComponentType(),
	//				                         statisticsLog.getMsgId(), statisticsLog.getParent(),
	//				                         statisticsLog.getParentMsgId(),
	//				                         statisticsLog.getEndTime() - statisticsLog.getStartTime(),
	//				                         statisticsLog.getNoOfFaults());
	//		statisticsNode.setIsResponse(statisticsLog.isResponse());
	//		return statisticsNode;
	//	}

	//	/**
	//	 * Temporary method to print statistics tree recursively
	//	 *
	//	 * @param treeNode treeNode
	//	 * @param sb       StringBuilder object
	//	 */
	//	private void print(IndividualStatistic treeNode, StringBuilder sb) {
	//		printNode(sb, treeNode);
	//		for (IndividualStatistic individualStatistic : treeNode.getChildren()) {
	//			if (treeNode.getChildren().size() >= 2) {
	//				//Workaround to get logs printed correctly
	//				//				sb.append("\n----------Printing a new Branch From ").append(treeNode.getComponentId())
	//				//				  .append("---------------\n");
	//				if (log.isDebugEnabled()) {
	//					log.debug("----------Printing a new Branch From " + treeNode.getComponentId() + "---------------");
	//				}
	//			}
	//			print(individualStatistic, sb);
	//		}
	//	}

	//	/**
	//	 * Temporary method to print statistics node details
	//	 *
	//	 * @param sb             StringBuilder object
	//	 * @param statisticsNode tree node
	//	 */
	//	private void printNode(StringBuilder sb, IndividualStatistic statisticsNode) {
	//		if (log.isDebugEnabled()) {
	//			//Workaround to get logs printed correctly
	//			//			sb.append(statisticsNode.getComponentId()).append("[Count : ").append(statisticsNode.getCount())
	//			//			  .append("]\n");
	//			//
	//			//			sb.append("\t\t Response Path: ").append(statisticsNode.getIsResponse()).append("\n");
	//			//			sb.append("\t\t Component Id: ").append(statisticsNode.getComponentId()).append("\n");
	//			//			sb.append("\t\t Component Type: ").append(statisticsNode.getComponentType()).append("\n");
	//			//			sb.append("\t\t Parent: ").append(statisticsNode.getParentId()).append("\n");
	//			//			sb.append("\t\t Parent MsgID: ").append(statisticsNode.getParentId()).append("\n");
	//			//			sb.append("\t\t Message Id(if > -1 cloned): ").append(statisticsNode.getMsgId()).append("\n");
	//			//			sb.append("\t\t Minimum Response Time: ").append(statisticsNode.getMinProcessingTime()).append("\n");
	//			//			sb.append("\t\t Maximum Response Time: ").append(statisticsNode.getMaxProcessingTime()).append("\n");
	//			//			sb.append("\t\t Average Response Time: ").append(statisticsNode.getAvgProcessingTime()).append("\n");
	//			//			sb.append("\t\t Number of Faults: ").append(statisticsNode.getFaultCount()).append("\n");
	//
	//			log.debug(statisticsNode.getComponentId() + "[Count : " + statisticsNode.getCount());
	//			log.debug("\t\t Response Path: " + statisticsNode.getIsResponse());
	//			log.debug("\t\t Component Id: " + statisticsNode.getComponentId());
	//			log.debug("\t\t Component Type: " + statisticsNode.getComponentType());
	//			log.debug("\t\t Parent: " + statisticsNode.getParentId());
	//			log.debug("\t\t Parent MsgID: " + statisticsNode.getParentId());
	//			log.debug("\t\t Message Id(if > -1 cloned): " + statisticsNode.getMsgId());
	//			log.debug("\t\t Minimum Response Time: " + statisticsNode.getMinProcessingTime());
	//			log.debug("\t\t Maximum Response Time: " + statisticsNode.getMaxProcessingTime());
	//			log.debug("\t\t Average Response Time: " + statisticsNode.getAvgProcessingTime());
	//			log.debug("\t\t Number of Faults: " + statisticsNode.getFaultCount());
	//		}
	//	}

	//	public StatisticsCompositeObject[] getFullTree() {
	//
	//		ArrayList<StatisticsCompositeObject> statisticSamples = new ArrayList<>();
	//
	//		getStatisticTreeAsArrayList(root, statisticSamples);
	//		StatisticsCompositeObject[] statisticSampleArray = new StatisticsCompositeObject[statisticSamples.size()];
	//		return statisticSamples.toArray(statisticSampleArray);
	//	}

	/**
	 * Returns Composite Data Object relating to a in individualStatistic Object
	 *
	 * @param individualStatistic individualStatistic Object
	 * @return Composite Data Object that contain statistics of individualStatistic Object
	 */
	//	public StatisticsCompositeObject getCompositeDataObject(IndividualStatistic individualStatistic) {
	//		if (individualStatistic != null) {
	//			return new StatisticsCompositeObject(individualStatistic.getComponentId(),
	//			                                     individualStatistic.getComponentType().toString(),
	//			                                     individualStatistic.getParentId(),
	//			                                     individualStatistic.getParentMsgId(), individualStatistic.getMsgId(),
	//			                                     individualStatistic.getMaxProcessingTime(),
	//			                                     individualStatistic.getMinProcessingTime(),
	//			                                     individualStatistic.getAvgProcessingTime(),
	//			                                     individualStatistic.getIsResponse(), individualStatistic.getCount(),
	//			                                     individualStatistic.getFaultCount());
	//		}
	//		return null;
	//	}

	/**
	 * Put all the individualStatistics Objects in a statistic tree to a ArrayList
	 *
	 * @param treeNode         treeNode
	 * @param statisticSamples ArrayList That Contains All Tree Nodes
	 */
	//	private void getStatisticTreeAsArrayList(IndividualStatistic treeNode,
	//	                                         ArrayList<StatisticsCompositeObject> statisticSamples) {
	//		if (treeNode != null) {
	//			statisticSamples.add(getCompositeDataObject(treeNode));
	//			for (IndividualStatistic individualStatistic : treeNode.getChildren()) {
	//				getStatisticTreeAsArrayList(individualStatistic, statisticSamples);
	//			}
	//		}
	//	}
}
