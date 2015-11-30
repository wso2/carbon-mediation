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

package org.wso2.carbon.mediation.flow.statistics.store.jmx;

/**
 * MBean interface to expose collected statistic data using JMX
 */
public interface StatisticCollectionViewMXBean {

	/**
	 * reset in memory API statistic collection in StatisticStore
	 */
	void resetAPIStatistics();

	/**
	 * reset in memory Proxy statistic collection in StatisticStore
	 */
	void resetProxyStatistics();

	/**
	 * reset in memory Sequence statistic collection in StatisticStore
	 */
	void resetSequenceStatistics();

	/**
	 * reset in memory Inbound Endpoint statistic collection in StatisticStore
	 */
	void resetInboundEndpointStatistics();

	/**
	 * reset in memory Inbound Endpoint statistic collection in StatisticStore
	 */
	void resetAllStatistics();

	/**
	 * Returns statistics related to a Proxy Service
	 *
	 * @param proxyName name of the proxy service
	 * @return Composite Data Object that contains Proxy Statistics
	 */
	StatisticsCompositeObject getProxyServiceJmxStatistics(String proxyName);

	/**
	 * Returns statistics related to a Sequence
	 *
	 * @param sequenceName name of the Sequence
	 * @return Composite Data Object that contains Sequence Statistics
	 */
	StatisticsCompositeObject getSequenceJmxStatistics(String sequenceName);

	/**
	 * Returns statistics related to a API
	 *
	 * @param APIName name of the API
	 * @return Composite Data Object that contains API Statistics
	 */
	StatisticsCompositeObject getApiJmxStatistics(String APIName);

	/**
	 * Returns statistics related to a Inbound Endpoint
	 *
	 * @param inboundEndpointName name of the Inbound Endpoint
	 * @return Composite Data Object that contains Inbound Endpoint Statistics
	 */
	StatisticsCompositeObject getInboundEndpointJmxStatistics(String inboundEndpointName);

	/**
	 * Returns statistics tree related to a Proxy Service
	 *
	 * @param proxyName name of the proxy service
	 * @return Composite Data Data Array that contains API Statistics Tree
	 */
	StatisticsCompositeObject[] getProxyServiceJmxStatisticsTree(String proxyName);

	/**
	 * Returns statistics tree related to a Sequence
	 *
	 * @param sequenceName name of the Sequence
	 * @return Composite Data Data Array that contains API Statistics Tree
	 */
	StatisticsCompositeObject[] getSequenceJmxStatisticsTree(String sequenceName);

	/**
	 * Returns statistics tree related to a Inbound Endpoint
	 *
	 * @param inboundEndpointName name of the Inbound Endpoint
	 * @return Composite Data Data Array that contains Inbound Endpoint Statistics Tree
	 */
	StatisticsCompositeObject[] getInboundEndpointJmxStatisticsTree(String inboundEndpointName);

	/**
	 * Returns statistics tree related to a API
	 *
	 * @param APIName name of the API
	 * @return Composite Data Array that contains API Statistics Tree
	 */
	StatisticsCompositeObject[] getApiJmxStatisticsTree(String APIName);
}