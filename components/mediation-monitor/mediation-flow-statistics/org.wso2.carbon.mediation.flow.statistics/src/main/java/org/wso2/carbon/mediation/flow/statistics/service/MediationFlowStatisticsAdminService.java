package org.wso2.carbon.mediation.flow.statistics.service;


import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.flow.statistics.StatisticCollectingThread;
import org.wso2.carbon.mediation.flow.statistics.StatisticNotifier;
import org.wso2.carbon.mediation.flow.statistics.store.StatisticsStore;
import org.wso2.carbon.mediation.flow.statistics.store.StatisticsTree;

import java.util.*;

public class MediationFlowStatisticsAdminService {

	StatisticsStore statisticsStore;

	public MediationFlowStatisticsAdminService(StatisticsStore statisticsStore) {
		this.statisticsStore = statisticsStore;
	}

	public List<AdminData> getAllStatistics() {
		Set<Map.Entry<String, StatisticsTree>> statRecords = statisticsStore.getElementsWithValue();

		Iterator iterator = statRecords.iterator();

		List<AdminData> outputList = new LinkedList<>();

		for (Map.Entry<String, StatisticsTree> entry : statRecords) {
			outputList.add(new AdminData(entry.getKey(), entry.getValue().getRoot().getComponentType().toString()));
		}

		return outputList;
	}

	public StatisticsTree getComponentStatitics(String componentID){
		return statisticsStore.getStatisticTree(componentID);

	}

	public void startCollectionManually(){
		int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

		StatisticNotifier statisticNotifier = new StatisticNotifier();

		StatisticCollectingThread reporterThread = new StatisticCollectingThread(statisticNotifier);

		reporterThread.setName("mediation-stat-collector-new-" + tenantId);

		reporterThread.start();
	}


}
