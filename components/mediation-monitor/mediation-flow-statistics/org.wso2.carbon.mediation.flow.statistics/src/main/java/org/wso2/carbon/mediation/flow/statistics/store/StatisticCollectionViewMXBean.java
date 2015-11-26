package org.wso2.carbon.mediation.flow.statistics.store;

/**
 * MBean interface to expose collected statistic data using JMX
 */
public interface StatisticCollectionViewMXBean {

	/**
	 * Returns number of statistic tree in StatisticStore
	 *
	 * @return number of statistic trees
	 */
	int getNumberOfStatisticTrees();

	/**
	 * reset in memory statistic collection in StatisticStore
	 */
	void resetStatistics();

	/**
	 * Returns statistics related to a Proxy Service
	 *
	 * @param proxyName name of the proxy service
	 * @return Composite Data Object that contains Proxy Statistics
	 */
	StatisticsCompositeObject getProxyServiceStatistics(String proxyName);

	/**
	 * Returns statistics related to a Sequence
	 *
	 * @param sequenceName name of the Sequence
	 * @return Composite Data Object that contains Sequence Statistics
	 */
	StatisticsCompositeObject getSequenceStatistics(String sequenceName);

	/**
	 * Returns statistics related to a API
	 *
	 * @param APIName name of the API
	 * @return Composite Data Object that contains API Statistics
	 */
	StatisticsCompositeObject getAPIStatistics(String APIName);

	/**
	 * Returns statistics tree related to a Proxy Service
	 *
	 * @param proxyName name of the proxy service
	 * @return Composite Data Data Array that contains API Statistics Tree
	 */
	StatisticsCompositeObject[] getProxyServiceStatisticsTree(String proxyName);

	/**
	 * Returns statistics tree related to a Sequence
	 *
	 * @param sequenceName name of the Sequence
	 * @return Composite Data Data Array that contains API Statistics Tree
	 */
	StatisticsCompositeObject[] getSequenceStatisticsTree(String sequenceName);

	/**
	 * Returns statistics tree related to a API
	 *
	 * @param APIName name of the API
	 * @return Composite Data Array that contains API Statistics Tree
	 */
	StatisticsCompositeObject[] getAPIStatisticsTree(String APIName);
}