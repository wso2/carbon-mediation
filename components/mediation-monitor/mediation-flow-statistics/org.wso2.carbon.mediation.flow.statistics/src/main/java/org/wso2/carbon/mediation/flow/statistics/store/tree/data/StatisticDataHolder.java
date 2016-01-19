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
