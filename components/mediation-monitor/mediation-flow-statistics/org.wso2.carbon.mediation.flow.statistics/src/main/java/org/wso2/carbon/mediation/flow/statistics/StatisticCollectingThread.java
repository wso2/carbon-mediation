package org.wso2.carbon.mediation.flow.statistics;

import org.apache.log4j.Logger;
import org.wso2.carbon.mediation.flow.statistics.store.StatisticsStore;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.apache.synapse.aspects.newstatistics.StatisticsLog;

import java.util.ArrayList;
import java.util.List;

public class StatisticCollectingThread extends Thread {

	private static Logger log = Logger.getLogger(StatisticCollectingThread.class);
	/**
	 * The reference to the synapse environment service
	 */
	private SynapseEnvironmentService synapseEnvironmentService;

	private boolean shutdownRequested = false;
	private long delay = 5 * 1000;

	private StatisticsStore statisticsStore;

	public StatisticCollectingThread(SynapseEnvironmentService synEnvService, StatisticsStore statisticsStore) {
		this.synapseEnvironmentService = synEnvService;
		this.statisticsStore = statisticsStore;
	}

	public void setDelay(long delay) {
		if (log.isDebugEnabled()) {
			log.debug("Mediation statistics reporter delay set to " + delay + " ms");
		}
		this.delay = delay;
	}

	private void reportStatistics() {
		List<ArrayList<StatisticsLog>> statisticEntries =
				synapseEnvironmentService.getSynapseEnvironment().getCompletedStatisticStore()
				                         .getCompletedStatitisticEntries();
		for (ArrayList<StatisticsLog> statisticsLogs : statisticEntries) {
			statisticsStore.update(statisticsLogs);
		}
	}

	public void run() {
		while (!shutdownRequested) {
			try {
				reportStatistics();
				delay();
			} catch (Throwable t) {
				// catch all possible errors to prevent the thread from dying
				log.error("Error while collecting and reporting mediation statistics", t);
			}
		}
	}

	public void shutdown() {
		if (log.isDebugEnabled()) {
			log.debug("Statistics reporter thread is being stopped");
		}
		shutdownRequested = true;
	}

	private void delay() {
		if (delay <= 0) {
			return;
		}
		try {
			sleep(delay);
		} catch (InterruptedException ignore) {

		}
	}

}
