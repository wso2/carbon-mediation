package org.wso2.carbon.mediation.statistics.newstatistics.store;

import org.apache.synapse.aspects.newstatistics.StatisticsLog;

/**
 * StatisticsTree holds statistics tree node for each trigger point in the ESB.
 */
public class StatisticsTree {

	private IndividualStatistics root = null;

	public StatisticsTree(StatisticsLog log) {
		root = new IndividualStatistics(log.getComponentId(), log.getComponentType(), log.getMsgId(), "",
		                                log.getParentMsgId(), log.getEndTime() - log.getStartTime(),
		                                log.getNoOfFaults());
	}

	public IndividualStatistics getRoot() {
		return root;
	}
}
