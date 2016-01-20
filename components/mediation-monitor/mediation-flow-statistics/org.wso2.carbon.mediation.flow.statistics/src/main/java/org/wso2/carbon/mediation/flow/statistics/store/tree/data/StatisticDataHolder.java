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

import org.apache.synapse.aspects.flow.statistics.data.raw.StatisticsLog;

/**
 * This holds statistics for component per message flow
 **/
public class StatisticDataHolder {

	private String messageFlowId;

	private String componentType;

	private String componentId;

	private long processingTime;

	private int faultCount;

	private String statisticTree;

	private String timeStamp;

	public StatisticDataHolder(StatisticsLog statisticsLog, String statisticTree) {
		this.messageFlowId = statisticsLog.getMessageFlowId().replace(':', '_');
		this.componentType = statisticsLog.getComponentTypeToString();
		this.componentId = statisticsLog.getComponentId();
		this.processingTime = statisticsLog.getEndTime() - statisticsLog.getStartTime();
		this.faultCount = statisticsLog.getNoOfFaults();
		this.statisticTree = statisticTree;
		this.timeStamp = statisticsLog.getTimeStamp();
	}

	public String getMessageFlowId() {
		return messageFlowId;
	}

	public String getComponentType() {
		return componentType;
	}

	public String getComponentId() {
		return componentId;
	}

	public long getProcessingTime() {
		return processingTime;
	}

	public int getFaultCount() {
		return faultCount;
	}

	public String getStatisticTree() {
		return statisticTree;
	}

	public String getTimeStamp() {
		return timeStamp;
	}
}
