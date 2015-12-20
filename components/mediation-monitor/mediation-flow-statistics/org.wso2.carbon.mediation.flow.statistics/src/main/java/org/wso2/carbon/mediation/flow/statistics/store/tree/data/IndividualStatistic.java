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

import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.aspects.newstatistics.EndpointStatisticLog;
import org.apache.synapse.aspects.newstatistics.StatisticsLog;
import org.wso2.carbon.mediation.flow.statistics.MessageFlowStatisticConstants;
import org.wso2.carbon.mediation.flow.statistics.service.data.TreeNodeData;

import java.util.HashMap;
import java.util.Map;

/**
 * IndividualStatistic represent a node in the statistics tree in the statistics store. It is
 * responsible for maintaining statistics about a component and updating statistics of the
 * component when new data comes
 */
public class IndividualStatistic {

	/**
	 * holds references to the branches that starts from its node
	 */
	private Map<String, Integer> children;

	/**
	 * statistic owners component Id
	 */
	private final String componentId;

	/**
	 * statistic owners component Id Component Type
	 */
	private final ComponentType componentType;

	/**
	 * message identification number in the message flow
	 */
	private int msgId;

	/**
	 * Maximum processing time for the component
	 */
	private long maxProcessingTime = 0;

	/**
	 * Minimum processing time for the component
	 */
	private long minProcessingTime = Long.MAX_VALUE;

	/**
	 * Average processing time for the component
	 */
	private long avgProcessingTime = 0;

	/**
	 * component is in the response path or not
	 */
	private boolean isResponse;

	/**
	 * The number of access count this component is invoked in the message flow
	 */
	private int count = 0;

	/**
	 * The number of fault count for this component. This is a combination its own fault count
	 * and children fault count
	 */
	private int faultCount = 0;

	/**
	 * Overloaded constructor to set variables for the node
	 */
	public IndividualStatistic(StatisticsLog statisticsLog) {
		this.componentType = statisticsLog.getComponentType();
		this.componentId = statisticsLog.getComponentId();
		this.faultCount = statisticsLog.getNoOfFaults();
		this.msgId = statisticsLog.getMsgId();
		setDuration(statisticsLog.getEndTime() - statisticsLog.getStartTime());
		this.children = new HashMap<>();
		this.isResponse = statisticsLog.isResponse();
	}

	public IndividualStatistic(EndpointStatisticLog endpointStatisticLog) {
		this.componentId = endpointStatisticLog.getComponentId();
		if (endpointStatisticLog.isHasFault()) {
			this.faultCount += 1;
		}
		this.componentType = ComponentType.ENDPOINT;
		setDuration(endpointStatisticLog.getEndTime() - endpointStatisticLog.getStartTime());
	}

	/**
	 * Updates statistics record for new statistics information
	 */
	public void update(StatisticsLog statisticsLog) {
		this.faultCount += statisticsLog.getNoOfFaults();
		setDuration(statisticsLog.getEndTime() - statisticsLog.getStartTime());
	}

	public void update(EndpointStatisticLog endpointStatisticLog) {
		if(endpointStatisticLog.isHasFault()) {
			this.faultCount += 1;
		}
		setDuration(endpointStatisticLog.getEndTime() - endpointStatisticLog.getStartTime());
	}

	/**
	 * Updates variables relating to execution time
	 *
	 * @param duration execution time for the component
	 */
	private void setDuration(long duration) {
		avgProcessingTime = (avgProcessingTime * count + duration) / (count + 1);
		if (maxProcessingTime < duration) {
			maxProcessingTime = duration;
		}
		if (minProcessingTime > duration) {
			minProcessingTime = duration;
		}
		count += 1;
	}

	public long getMaxProcessingTime() {
		return maxProcessingTime;
	}

	public long getMinProcessingTime() {
		return minProcessingTime;
	}

	public long getAvgProcessingTime() {
		return avgProcessingTime;
	}

	public int getCount() {
		return count;
	}

	public int getFaultCount() {
		return faultCount;
	}

	public String getComponentId() {
		return componentId;
	}

	public ComponentType getComponentType() {
		return componentType;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setChild(String key, int childIndex) {
		children.put(key, childIndex);
	}

	public Integer getChild(String key) {
		if (children.containsKey(key)) {
			return children.get(key);
		}
		return null;
	}

	public Map<String, Integer> getChildrenMap() {
		return children;
	}

	public boolean getIsResponse() {
		return isResponse;
	}

	public TreeNodeData getTreeNodeData() {
		return new TreeNodeData(componentId, getComponentTypeToString(), count, maxProcessingTime, minProcessingTime,
		                        avgProcessingTime, faultCount, isResponse);
	}

	public String getComponentTypeToString() {
		switch (componentType) {
			case PROXYSERVICE:
				return MessageFlowStatisticConstants.PROXYSERVICE;
			case ENDPOINT:
				return MessageFlowStatisticConstants.ENDPOINT;
			case INBOUNDENDPOINT:
				return MessageFlowStatisticConstants.INBOUNDENDPOINT;
			case SEQUENCE:
				return MessageFlowStatisticConstants.SEQUENCE;
			case MEDIATOR:
				return MessageFlowStatisticConstants.MEDIATOR;
			case FAULTHANDLER:
				return MessageFlowStatisticConstants.FAULTHANDLER;
			case API:
				return MessageFlowStatisticConstants.API;
			case RESOURCE:
				return MessageFlowStatisticConstants.RESOURCE;
			default:
				return MessageFlowStatisticConstants.ANY;
		}

	}
}
