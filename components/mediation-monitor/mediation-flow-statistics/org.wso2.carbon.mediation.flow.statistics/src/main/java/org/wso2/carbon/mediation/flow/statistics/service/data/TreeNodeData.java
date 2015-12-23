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

package org.wso2.carbon.mediation.flow.statistics.service.data;

import org.wso2.carbon.mediation.flow.statistics.store.tree.data.IndividualStatistic;

public class TreeNodeData {

	/**
	 * statistic owners component Id
	 */
	private final String componentId;

	/**
	 * statistic owners component Id Component Type
	 */
	private final String componentType;

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

	public TreeNodeData(IndividualStatistic individualStatistic) {
		this.maxProcessingTime = individualStatistic.getMaxProcessingTime();
		this.minProcessingTime = individualStatistic.getMinProcessingTime();
		this.avgProcessingTime = individualStatistic.getAvgProcessingTime();
		this.isResponse = individualStatistic.getIsResponse();
		this.count = individualStatistic.getCount();
		this.faultCount = individualStatistic.getFaultCount();
		this.componentType = individualStatistic.getComponentTypeToString();
		this.componentId = individualStatistic.getComponentId();
	}

	public TreeNodeData(String componentId, String componentTypeToString, int count, long maxProcessingTime,
	                    long minProcessingTime, long avgProcessingTime, int faultCount, boolean isResponse) {
		this.maxProcessingTime = maxProcessingTime;
		this.minProcessingTime = minProcessingTime;
		this.avgProcessingTime = avgProcessingTime;
		this.isResponse = isResponse;
		this.count = count;
		this.faultCount = faultCount;
		this.componentType = componentTypeToString;
		this.componentId = componentId;
	}

	public String getComponentId() {
		return componentId;
	}

	public String getComponentType() {
		return componentType;
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

	public boolean isResponse() {
		return isResponse;
	}

	public int getCount() {
		return count;
	}

	public int getFaultCount() {
		return faultCount;
	}
}
