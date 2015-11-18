package org.wso2.carbon.mediation.statistics.newstatistics;

import org.apache.synapse.aspects.newstatistics.StatisticsLog;
import org.wso2.carbon.mediation.statistics.newstatistics.store.StatisticsStore;

import java.util.ArrayList;

public class StatisticNotifier {

	StatisticsStore statisticsStore;
	public StatisticNotifier() {
		statisticsStore = new StatisticsStore();
	}


	public void updateStatistics(ArrayList<StatisticsLog> statisticsLogs){
		statisticsStore.update(statisticsLogs);
	}

}
