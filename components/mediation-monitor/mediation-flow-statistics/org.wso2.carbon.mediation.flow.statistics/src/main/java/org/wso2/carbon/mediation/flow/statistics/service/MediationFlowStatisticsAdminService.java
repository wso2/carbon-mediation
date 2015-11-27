package org.wso2.carbon.mediation.flow.statistics.service;

import org.wso2.carbon.mediation.flow.statistics.MessageFlowStatisticConstants;
import org.wso2.carbon.mediation.flow.statistics.store.StatisticsStore;
import org.wso2.carbon.mediation.flow.statistics.store.StatisticsTree;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;

import java.util.*;

public class MediationFlowStatisticsAdminService extends AbstractServiceBusAdmin {

	public List<AdminData> getAllStatistics() {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));

		Set<Map.Entry<String, StatisticsTree>> statRecords = statisticsStore.getElementsWithValue();

		Iterator iterator = statRecords.iterator();

		List<AdminData> outputList = new LinkedList<>();

		for (Map.Entry<String, StatisticsTree> entry : statRecords) {
			outputList.add(new AdminData(entry.getKey(), entry.getValue().getRoot().getComponentType().toString()));
		}

		return outputList;
	}

	public StatisticsTree getComponentStatitics(String componentID) {
		StatisticsStore statisticsStore = ((StatisticsStore) getConfigContext()
				.getProperty(MessageFlowStatisticConstants.MESSAGE_FLOW_STATISTIC_STORE));
		return statisticsStore.getStatisticTree(componentID);

	}

	public void startCollectionManually() {

	}

}
