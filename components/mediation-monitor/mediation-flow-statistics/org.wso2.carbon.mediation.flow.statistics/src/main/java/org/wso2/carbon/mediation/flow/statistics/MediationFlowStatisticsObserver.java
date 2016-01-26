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

package org.wso2.carbon.mediation.flow.statistics;

import org.wso2.carbon.mediation.flow.statistics.store.tree.data.StatisticDataHolder;

/**
 * This interface can be used to register consumers into the StatisticsStore and retrieve statistics updates from the
 * data store.
 */
public interface MediationFlowStatisticsObserver {

	/**
	 * Clean up this observer and prepare for shutdown
	 */
	public void destroy();

	/**
	 * Receive statistics updates from the statistics store which receives stats from synapse
	 *
	 * @param statisticSnapshot statistics for a message flow
	 */
	public void updateStatistics(StatisticDataHolder statisticSnapshot);
}
