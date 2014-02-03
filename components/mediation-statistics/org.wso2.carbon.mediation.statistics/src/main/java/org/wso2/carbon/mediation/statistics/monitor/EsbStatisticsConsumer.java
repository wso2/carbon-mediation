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

package org.wso2.carbon.mediation.statistics.monitor;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.wso2.carbon.mediation.statistics.MediationStatisticsObserver;
import org.wso2.carbon.mediation.statistics.MediationStatisticsSnapshot;
import org.wso2.carbon.mediation.statistics.MessageTraceLog;
import org.wso2.carbon.mediation.statistics.StatisticsRecord;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class gets stats updates from the mediation stats component. All update events are stored
 * for a period of 1 minute. A Timer task runs with an interval of 1 minute and it exposes all
 * stored stats in the MBeans. So the MBeans will we updated once a minute with the stats related
 * to the passed minute.
 */
public class EsbStatisticsConsumer implements MediationStatisticsObserver {

    private static final Log log = LogFactory.getLog(EsbStatisticsConsumer.class);

    private static final String SEQUENCE_MONITOR = "Sequence-Monitor";
    private static final String ENDPOINT_MONITOR = "Endpoint-Monitor";
    private static final String ENDPOINT_OPERATION_MONITOR = "Endpoint-Operation-Monitor";
    private static final String MEMORY_MONITOR = "Memory-Monitor";
    private static final String ESB_MONITOR = "ESB-Monitor";

    private static final String MEMORY_ID = "system-memory";
    private static final String ESB_ID = "entire-esb";
    private static final String STAT_PROP_XML = "stat-properties.xml";

    private static final float MEGA = 1024 * 1024;

    private Map<String, SequenceStatView> sequenceViewMap;
    private Map<String, EndpointStatView> endpointViewMap;
    private Map<String, EndpointOperationStatView> endpointOperationViewMap;
    private MemoryStatView memoryView;
    private EsbStatView esbView;

    private Map<String, String> registeredMbeanIds;
    private Map<String, String> endpointThresholds;
    private volatile List<StatisticsRecord> lastMinRecords;

    private ReentrantLock lock = new ReentrantLock();

    public EsbStatisticsConsumer() {
        sequenceViewMap = new HashMap<String, SequenceStatView>();
        endpointViewMap = new HashMap<String, EndpointStatView>();
        endpointOperationViewMap = new HashMap<String, EndpointOperationStatView>();

        registeredMbeanIds = new HashMap<String, String>();
        endpointThresholds = new HashMap<String, String>();
        lastMinRecords = new ArrayList<StatisticsRecord>();

        readStatProperties();

        // We start the scheduler here. it will call the exposeData method after each 1 min
        new StatScheduler(this).schedule();
    }


    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Destroying all the MBeans in the custom statistics consumer");
        }
        for (String key : registeredMbeanIds.keySet()) {
            MBeanRegistrar.getInstance().unRegisterMBean(key, registeredMbeanIds.get(key));
        }
    }

    public void updateStatistics(MediationStatisticsSnapshot mediationStatisticsSnapshot) {
          // storing the received update. lock the lastMinRecords until this is done.
        lock.lock();
        StatisticsRecord record = mediationStatisticsSnapshot.getUpdate();
        try {
            lastMinRecords.add(new StatisticsRecord(record));
        } finally {
            lock.unlock();
        }
    }

    public void notifyTraceLogs(MessageTraceLog[] messageTraceLogs) {

    }

    /**
     * Mediation stats component calls this method time to time. We store received updates for
     * a period of one minute until the Time task processes them.
     *
     * @param updateRecord     - update
     * @param entitySnap       -
     * @param categorySnapshot -
     */
    public void updateStatistics(StatisticsRecord updateRecord, StatisticsRecord entitySnap,
                                 StatisticsRecord categorySnapshot) {
        // storing the received update. lock the lastMinRecords until this is done.
        lock.lock();
        try {
            lastMinRecords.add(new StatisticsRecord(updateRecord));
        } finally {
            lock.unlock();
        }
    }

    /**
     * This method will be repeatedly called by the timer task with an interval of 1 min. Here, we
     * clear all currently existing data on MBeans first. Then expose the data stored during past
     * 1 min through the MBeans.
     */
    public void exposeData() {
        // We are going to use the stored data in the lastMinRecords. We have to lock the
        // lastMinRecords list until data are used and the list is cleaned up..
        lock.lock();
        try {
            // Fill MBeans with new data
            for (StatisticsRecord record : lastMinRecords) {
                exposeDataOnMBeans(record);
            }

            // Clean the update records collected duing last minute
            lastMinRecords.clear();
        } finally {
            lock.unlock();
        }

        // Check whether the endpoints are active or not using the requests received..
        EndpointStatView endpointView;
        long threshold;
        for (String key : endpointViewMap.keySet()) {
            endpointView = endpointViewMap.get(key);
            if (endpointThresholds.get(key) != null) {
                threshold = Long.parseLong(endpointThresholds.get(key));
                if (endpointView.getTransactionsIn() < threshold) {
                    endpointView.setActive(false);
                } else {
                    endpointView.setActive(true);
                }
            }
        }

    }

    /**
     * This method fills the MBeans with the given update record
     *
     * @param updateRecord - update record
     */
    private void exposeDataOnMBeans(StatisticsRecord updateRecord) {

        // We only consider the In flow stats..
        ComponentType type = updateRecord.getType();
        if (type == ComponentType.SEQUENCE) {
            SequenceStatView view;
            if (!sequenceViewMap.containsKey(updateRecord.getResourceId())) {
                view = new SequenceStatView();
                MBeanRegistrar.getInstance().registerMBean(view,
                        SEQUENCE_MONITOR, (updateRecord.getResourceId()));
                registeredMbeanIds.put(SEQUENCE_MONITOR, (updateRecord.getResourceId()));
                sequenceViewMap.put(updateRecord.getResourceId(), view);
            } else {
                view = sequenceViewMap.get(updateRecord.getResourceId());
            }
            updateCommonView(view, updateRecord);
        }

        if (updateRecord.isInStatistic()) {
            if (type == ComponentType.ENDPOINT) {
                // Handling total endpoint related stats
                EndpointStatView endpointView;

                String endpointName = getEndpointName(updateRecord.getResourceId());
                if (!endpointViewMap.containsKey(endpointName)) {
                    endpointView = new EndpointStatView();
                    MBeanRegistrar.getInstance().registerMBean(endpointView,
                            ENDPOINT_MONITOR, endpointName);
                    registeredMbeanIds.put(ENDPOINT_MONITOR, endpointName);
                    endpointViewMap.put(endpointName, endpointView);
                } else {
                    endpointView = endpointViewMap.get(endpointName);
                }
                updateEndpointView(endpointView, updateRecord);

                // Handling individual operation stats of the endpoint
                EndpointOperationStatView endpointOpView;

                if (!endpointOperationViewMap.containsKey(updateRecord.getResourceId())) {
                    endpointOpView = new EndpointOperationStatView();
                    MBeanRegistrar.getInstance().registerMBean(endpointOpView,
                            ENDPOINT_OPERATION_MONITOR, updateRecord.getResourceId());
                    registeredMbeanIds.put(ENDPOINT_OPERATION_MONITOR,
                            updateRecord.getResourceId());
                    endpointOperationViewMap.put(updateRecord.getResourceId(), endpointOpView);
                } else {
                    endpointOpView = endpointOperationViewMap.get(updateRecord.getResourceId());
                }

                updateEndpointOperationView(endpointOpView, updateRecord);

                // We update the ESB monitor here (stats for all endpoints)
                if (esbView == null) {
                    esbView = new EsbStatView();
                    MBeanRegistrar.getInstance().registerMBean(esbView, ESB_MONITOR, ESB_ID);
                    registeredMbeanIds.put(ESB_MONITOR, ESB_ID);
                }

                updateEsbView(esbView, updateRecord);
            }
        }

        // We update the memory monitor here
        if (memoryView == null) {
            memoryView = new MemoryStatView();
            MBeanRegistrar.getInstance().registerMBean(memoryView,
                    MEMORY_MONITOR, MEMORY_ID);
            registeredMbeanIds.put(MEMORY_MONITOR, MEMORY_ID);
        }

        updateMemoryView(memoryView);
    }

    private void updateCommonView(SequenceStatView view, StatisticsRecord update) {
        long updatedTotalCount = view.getTransactionsIn() + update.getTotalCount();
        long updateFaultCount = view.getNumberOfErrorsIn() + update.getFaultCount();

        if (updatedTotalCount > 0) {
            view.setAvgTimeInLastMin((view.getAvgTimeIn() * view.getTransactionsIn() +
                    update.getAvgTime() * update.getTotalCount()) / updatedTotalCount);

            view.setAvgMediationTimeInLastMin(-1);
            view.setAvgMediationTimeInLastMin(-1);

            view.setErrorPercentageInLastMin((updateFaultCount / updatedTotalCount) * 100);
        }
        view.setNumberOfErrorsInLastMin(updateFaultCount);
        view.setTransactionsInLastMin(updatedTotalCount);
    }

    private void updateEndpointView(EndpointStatView view, StatisticsRecord update) {
        view.setTransactionsInLastMin(view.getTransactionsIn() + update.getTotalCount());
    }

    private void updateEndpointOperationView(EndpointOperationStatView view,
                                             StatisticsRecord update) {

        long updatedTotalCount = view.getTransactionsIn() + update.getTotalCount();
        double totalTime = view.getAverageTransactionDurationIn() *
                view.getTransactionsIn() + update.getAvgTime() * update.getTotalCount();

        if (updatedTotalCount > 0 && totalTime > 0) {
            view.setAvgTransactionDurationInLastMin(totalTime / updatedTotalCount);
        }

        view.setTransactionsInLastMin(updatedTotalCount);
    }

    private void updateEsbView(EsbStatView view, StatisticsRecord update) {
        long totalTransactions = view.getTransactionsIn() + update.getTotalCount();
        long totalErrors = view.getErrorsIn() + update.getFaultCount();
        view.setTransactionsInLastMin(totalTransactions);
        view.setErrorsInLastMin(totalErrors);

        if (totalTransactions > 0) {
            view.setErrorPercentageInLastMin(totalErrors * 100 / totalTransactions);
        }
    }

    private void updateMemoryView(MemoryStatView view) {
        Runtime runtime = Runtime.getRuntime();

        view.setTotalMemory(runtime.totalMemory() / MEGA);
        view.setFreeMemory(runtime.freeMemory() / MEGA);
        view.setUsedMemory((runtime.totalMemory() - runtime.freeMemory()) / MEGA);
        view.setMaxMemory(runtime.maxMemory() / MEGA);
    }

    /**
     * Reads the stat-properties.xml file from the conf folder and loads the properties
     * related to statistics
     */
    private void readStatProperties() {
        String statPropPath = CarbonUtils.getCarbonHome() + "/conf/" + STAT_PROP_XML;
        File f = new File(statPropPath);
        if (!f.exists()) {
            log.error(STAT_PROP_XML + " file not found at : " + statPropPath);
            return;
        }
        InputStream xmlInputStream;
        try {
            xmlInputStream = new FileInputStream(f);
            OMElement documentElement = new StAXOMBuilder(xmlInputStream).getDocumentElement();

            OMElement endpointPropElement = documentElement.
                    getFirstChildWithName(new QName("endpointProperties"));

            Iterator itr = endpointPropElement.getChildrenWithLocalName("endpoint");
            String name, threshold = null;
            while (itr.hasNext()) {
                OMElement endpoint = (OMElement) itr.next();
                name = readAttribute(endpoint, "name");
                OMElement thresholdEle = endpoint.getFirstChildWithName(new QName("threshold"));
                if (thresholdEle != null) {
                    threshold = thresholdEle.getText();
                }
                if (name != null && threshold != null) {
                    endpointThresholds.put(name, threshold);
                }
            }
        } catch (Exception e) {
            log.error("Error while parsing file : " + STAT_PROP_XML, e);
        }
    }

    private String readAttribute(OMElement element, String attName) {
        if (element == null) {
            return null;
        }
        OMAttribute temp = element.getAttribute(new QName(attName));
        if (temp != null) {
            return temp.getAttributeValue();
        }
        return null;
    }

    private String getEndpointName(String resId) {
        String[] temp = resId.split("__");
        return temp[0];
    }

}

