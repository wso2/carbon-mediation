package org.wso2.carbon.mediation.flow.statistics;

import org.apache.log4j.Logger;
import org.apache.synapse.aspects.newstatistics.RuntimeStatisticCollector;
import org.apache.synapse.aspects.newstatistics.StatisticsLog;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import java.util.ArrayList;
import java.util.List;

public class StatisticCollectingThread extends Thread {

	private static Logger log = Logger.getLogger(StatisticCollectingThread.class);
	/**
	 * The reference to the synapse environment service
	 */
	//private SynapseEnvironmentService synapseEnvironmentService;

	private boolean shutdownRequested = false;
	private long delay = 5 * 1000;

	private StatisticNotifier statisticNotifier;

	public StatisticCollectingThread(StatisticNotifier statisticNotifier) {
		//this.synapseEnvironmentService = synEnvSvc;
		this.statisticNotifier = statisticNotifier;
	}

	public void setDelay(long delay) {
		if (log.isDebugEnabled()) {
			log.debug("Mediation statistics reporter delay set to " + delay + " ms");
		}
		this.delay = delay;
	}

	private void reportStatistics() {
		List<ArrayList<StatisticsLog>> statitisticEntries = RuntimeStatisticCollector.getCompletedStatisticStore()
		                                                                             .getCompletedStatitisticEntries();
		for (ArrayList<StatisticsLog> statisticsLogs : statitisticEntries) {
			statisticNotifier.updateStatistics(statisticsLogs);
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
