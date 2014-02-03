/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.statistics;

import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.aspects.statistics.view.InOutStatisticsView;
import org.apache.synapse.aspects.statistics.ErrorLog;
import org.apache.synapse.SynapseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * The in-memory data store which keeps all the mediation statistics generated at
 * runtime. This statistics store uses multiple hash maps to store various types of
 * data. It also maintains cumulative data indexed by category types. The implementation
 * attempts to ensure fast lookups and minimum memory usage. It is maintained as a
 * singleton instance and allows registering custom statistics consumers if required.
 */
public class MediationStatisticsStore {

    private static final Log log = LogFactory.getLog(MediationStatisticsStore.class);    

    // Maps to store instance data (for individual sequences, endpoints etc)
    private Map<ComponentType, Map<String, StatisticsRecord>> inDataStore =
            new HashMap<ComponentType, Map<String, StatisticsRecord>>();
    private Map<ComponentType, Map<String, StatisticsRecord>> outDataStore =
            new HashMap<ComponentType, Map<String, StatisticsRecord>>();

    // Maps to store category wide data (for all sequences, all endpoints etc)    
    private Map<ComponentType, StatisticsRecord> categoryInDataStore =
            new HashMap<ComponentType, StatisticsRecord>();
    private Map<ComponentType, StatisticsRecord> categoryOutDataStore =
            new HashMap<ComponentType, StatisticsRecord>();

    private Set<MediationStatisticsObserver> observers =
            new HashSet<MediationStatisticsObserver>();

    /**
     * Register a custom statistics consumer to receive updates from this
     * statistics store
     *
     * @param o The MediationStatisticsObserver instance to be notified of data updates
     */
    public void registerObserver(MediationStatisticsObserver o) {
        observers.add(o);
    }

    /**
     * Unregister the custom statistics consumer from the mediation statistics store
     *
     * @param o The MediationStatisticsObserver instance to be removed
     */
    public void unregisterObserver(MediationStatisticsObserver o) {
        if (observers.contains(o)) {
            observers.remove(o);
            o.destroy();
        }
    }

    void unregisterObservers() {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering mediation statistics observers");
        }

        for (MediationStatisticsObserver o : observers) {
            o.destroy();
        }
        observers.clear();
    }

    void updateStatistics(InOutStatisticsView view) {

        if (log.isDebugEnabled()) {
            log.debug("Received statistics update event");
        }

        if (view.getInStatistics() != null) {
            StatisticsRecord inRecord = new StatisticsRecord(view.getResourceId(),
                    view.getComponentType(), true, view.getInStatistics());
            updateRecord(inRecord, view.getInStatistics().getErrorLogs());
        }

        if (view.getOutStatistics() != null) {
            StatisticsRecord outRecord = new StatisticsRecord(view.getResourceId(),
                        view.getComponentType(), false, view.getOutStatistics());
            updateRecord(outRecord, view.getOutStatistics().getErrorLogs());
        }
    }

    private void updateRecord(StatisticsRecord record, List<ErrorLog> errorLogs) {

        if (record.getTotalCount() == 0) {
            // Nothing to update
            return;
        }


        Map<ComponentType, Map<String, StatisticsRecord>> dataStore;
        Map<ComponentType, StatisticsRecord> categoryDataStore;

        StatisticsRecord entitySnapshot = null, categorySnapshot = null;

        if (record.isInStatistic()) {
            dataStore = inDataStore;
            categoryDataStore = categoryInDataStore;
        } else {
            dataStore = outDataStore;
            categoryDataStore= categoryOutDataStore;
        }

        Map<String, StatisticsRecord> allRecords = dataStore.get(record.getType());
        if (allRecords == null) {
            // If the map for this particular component type is not already created
            // create it now and add it to the data stores
            allRecords = new HashMap<String, StatisticsRecord>();
            dataStore.put(record.getType(), allRecords);
        }

        // Update instance data
        StatisticsRecord oldRecord = allRecords.get(record.getResourceId());
        if (oldRecord == null) {
            allRecords.put(record.getResourceId(), record);
        } else {
            entitySnapshot =new StatisticsRecord(oldRecord);
            oldRecord.updateRecord(record);
        }

        // Update cumulative category data
        StatisticsRecord categoryRecord = categoryDataStore.get(record.getType());
        if (categoryRecord == null) {
            categoryRecord = new StatisticsRecord("CATEGORY-DATA", record.getType(),
                    record.isInStatistic(), null);
            categoryRecord.setTotalCount(record.getTotalCount());
            categoryRecord.setFaultCount(record.getFaultCount());
            categoryRecord.setMaxTime(record.getMaxTime());
            categoryRecord.setMinTime(record.getMinTime());
            categoryRecord.setAvgTime(record.getAvgTime());

            categoryDataStore.put(categoryRecord.getType(), categoryRecord);
        } else {
            categorySnapshot = new StatisticsRecord(categoryRecord);
            categoryRecord.updateRecord(record);
        }

        MediationStatisticsSnapshot snapshot = new MediationStatisticsSnapshot();
        snapshot.setUpdate(record);
        snapshot.setEntitySnapshot(entitySnapshot);
        snapshot.setCategorySnapshot(categorySnapshot);
        snapshot.setErrorLogs(errorLogs);
        notifyObservers(snapshot);
    }

    private void notifyObservers(MediationStatisticsSnapshot snapshot) {

        for (MediationStatisticsObserver o : observers) {
            try {
                o.updateStatistics(snapshot);
            } catch (Throwable t) {
                log.error("Error occured while notifying the statistics observer", t);
            }
        }
    }

    public void notifyTraceLogs(MessageTraceLog[] logs) {
        for (MediationStatisticsObserver o : observers) {
            try {
                o.notifyTraceLogs(logs);
            } catch (Throwable t) {
                log.error("Error occured while notifying the statistics observer", t);
            }
        }
    }

    /**
     * Get the list of resource ID values for a given component type
     *
     * @param type the component type
     * @return an array of resource IDs or an empty array
     */
    public String[] getResourceNames(ComponentType type) {
        Set<String> resourceNames = new HashSet<String>();
        Map<String, StatisticsRecord> inDataMap = inDataStore.get(type);
        if (inDataMap != null) {
            resourceNames.addAll(inDataMap.keySet());
        }

        Map<String, StatisticsRecord> outDataMap = outDataStore.get(type);
        if (outDataMap != null) {
            resourceNames.addAll(outDataMap.keySet());
        }

        return resourceNames.toArray(new String[resourceNames.size()]);
    }

    /**
     * Get the statistics record for a specified resource
     *
     * @param resourceId the resource ID (name)
     * @param type type of the resource (Endpoint, Proxy, Sequence)
     * @param inStatistic true for in-flow statistics and false for out-flow data
     * @return a StatisticsRecord instance or null
     */
    public StatisticsRecord getRecordByResource(String resourceId, ComponentType type,
                                                boolean inStatistic) {

        Map<String, StatisticsRecord> dataMap = inStatistic ?
                inDataStore.get(type) : outDataStore.get(type);
        if (dataMap != null) {
            return dataMap.get(resourceId);
        }
        return null;
    }

    /**
     * Get the cumulative data record for a given type
     *
     * @param type Category type
     * @param inStatistic true for in-flow data and false for out-flow data
     * @return a StatisticsRecord instance or null
     */
    public StatisticsRecord getRecordByCategory(ComponentType type, boolean inStatistic) {
        return inStatistic ? categoryInDataStore.get(type) : categoryOutDataStore.get(type);
    }

    /**
     * Get the total count values for a given component types as a map. By default, only in-flow
     * counts are included. For sequences, out-flow counts are included as well.
     * Map keys are comprised of resource ID values.
     *
     * @param type the component type
     * @return a map of resource ID values and total count values
     */
    public Map<String, Integer> getTotalCounts(ComponentType type) {
        Map<String, Integer> totalCountsMap = new HashMap<String, Integer>();

        Map<String, StatisticsRecord> inDataMap = inDataStore.get(type);
        populateCountsMap(totalCountsMap, inDataMap);

        if (type == ComponentType.SEQUENCE) {
            // for sequences, include out-flow counts as well
            Map<String, StatisticsRecord> outDataMap = outDataStore.get(type);
            populateCountsMap(totalCountsMap, outDataMap);
        }

        return totalCountsMap;
    }

	/**
	 * A private utility method that populates a counts map
	 * 
	 * @param totalCountsMap totalCountsMap
	 * @param dataMap dataMap
	 */
	private void populateCountsMap(Map<String, Integer> totalCountsMap,
                                   Map<String, StatisticsRecord> dataMap) {
		if (dataMap != null) {
            for (Map.Entry<String, StatisticsRecord> entry : dataMap.entrySet()) {
                String key = entry.getKey();
                if (key.contains(SynapseConstants.STATISTICS_KEY_SEPARATOR)) {
                    key = key.substring(0, key.lastIndexOf(
                            SynapseConstants.STATISTICS_KEY_SEPARATOR));
                }

                if (totalCountsMap.containsKey(key)) {
                    int currentVal = totalCountsMap.get(key);
                    totalCountsMap.put(key, currentVal + entry.getValue().getTotalCount());
                } else {
                    totalCountsMap.put(key, entry.getValue().getTotalCount());
                }
            }
        }
	}
}
