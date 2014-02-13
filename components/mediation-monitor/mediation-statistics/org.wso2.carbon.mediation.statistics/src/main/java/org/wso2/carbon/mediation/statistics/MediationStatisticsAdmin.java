/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediation.statistics;

import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;

import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MediationStatisticsAdmin extends AbstractServiceBusAdmin {

    public String[] listServers() {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "127.0.0.1";
        }
        return new String[] { host };
    }

    public String[] listSequence() {
        return ((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).getResourceNames(ComponentType.SEQUENCE);
    }

    public String[] listProxyServices() {
        return ((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).getResourceNames(ComponentType.PROXYSERVICE);
    }

    public String[] listEndPoint() {
        return ((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).getResourceNames(ComponentType.ENDPOINT);
    }

    public InOutStatisticsRecord getCategoryStatistics(int category) {
        ComponentType type = StatisticsUtil.getComponentType(category);
        return getCategoryStatistics(type);
    }

    private InOutStatisticsRecord getCategoryStatistics(ComponentType type) {
        InOutStatisticsRecord record = new InOutStatisticsRecord();
        record.setInRecord(((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).getRecordByCategory(type, true));
        record.setOutRecord(((MediationStatisticsStore) getConfigContext().
                getProperty("MediationStatisticsStore")).getRecordByCategory(type, false));
        return record;
    }

    public GraphData getDataForGraph() throws MediationStatisticsException {

        GraphData graphData = new GraphData();
        Map<String, Integer> serverData = new HashMap<String, Integer>();
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "127.0.0.1";
        }

        StatisticsRecord serverRecord = getServerStatistics().getInRecord();
        if (serverRecord != null) {
            serverData.put(host, serverRecord.getTotalCount());
        }
        Map<String, Integer> psData = ((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).
                getTotalCounts(ComponentType.PROXYSERVICE);
        Map<String, Integer> epData = ((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).
                getTotalCounts(ComponentType.ENDPOINT);
        Map<String, Integer> sequenceData = ((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).
                getTotalCounts(ComponentType.SEQUENCE);

        StringBuffer serverDataStr = new StringBuffer();
        StringBuffer psDataStr = new StringBuffer();
        StringBuffer epDataStr = new StringBuffer();
        StringBuffer sequenceDataStr = new StringBuffer();

        for (Map.Entry<String, Integer> entry : serverData.entrySet()) {
            serverDataStr.append(entry.getKey() + "-[" + entry.getValue() + "]-," +
                    entry.getValue() + ";");
        }
        for (Map.Entry<String, Integer> entry : psData.entrySet()) {
            psDataStr.append(entry.getKey() + "-[" + entry.getValue() + "]-," +
                    entry.getValue() + ";");
        }
        for (Map.Entry<String, Integer> entry : epData.entrySet()) {
            epDataStr.append(entry.getKey() + "-[" + entry.getValue() + "]-," +
                    entry.getValue() + ";");
        }
        for (Map.Entry<String, Integer> entry : sequenceData.entrySet()) {
            sequenceDataStr.append(entry.getKey() + "-[" + entry.getValue() + "]-," +
                    entry.getValue() + ";");
        }

        graphData.setEndPointData(epDataStr.toString());
        graphData.setProxyServiceData(psDataStr.toString());
        graphData.setSequenceData(sequenceDataStr.toString());
        graphData.setServerData(serverDataStr.toString());
        return graphData;
    }

    public InOutStatisticsRecord getProxyServiceStatistics(String proxyName) {
        InOutStatisticsRecord record = new InOutStatisticsRecord();
        record.setInRecord(((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).
                getRecordByResource(proxyName, ComponentType.PROXYSERVICE, true));
        record.setOutRecord(((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).
                getRecordByResource(proxyName, ComponentType.PROXYSERVICE, false));
        return record;
    }

    public InOutStatisticsRecord getServerStatistics() {
        List<StatisticsRecord> inRecords = new ArrayList<StatisticsRecord>();
        List<StatisticsRecord> outRecords = new ArrayList<StatisticsRecord>();

        StatisticsRecord proxyInStats = getCategoryStatistics(ComponentType.PROXYSERVICE).
                getInRecord();
        if (proxyInStats != null) {
            inRecords.add(proxyInStats);
        }

        StatisticsRecord seqInStats = getSequenceStatistics(SynapseConstants.MAIN_SEQUENCE_KEY).
                getInRecord();
        if (seqInStats != null) {
            inRecords.add(seqInStats);
        }

        StatisticsRecord proxyOutStats = getCategoryStatistics(ComponentType.PROXYSERVICE).
                getOutRecord();
        if (proxyOutStats != null) {
            outRecords.add(proxyOutStats);
        }

        StatisticsRecord seqOutStats = getSequenceStatistics(SynapseConstants.MAIN_SEQUENCE_KEY).
                getOutRecord();
        if (seqOutStats != null) {
            outRecords.add(seqOutStats);
        }

        InOutStatisticsRecord inOutRecord = new InOutStatisticsRecord();
        inOutRecord.setInRecord(StatisticsUtil.getCombinedRecord(inRecords));
        inOutRecord.setOutRecord(StatisticsUtil.getCombinedRecord(outRecords));
        return inOutRecord;
    }

    public InOutStatisticsRecord getSequenceStatistics(String sequenceName) {
        InOutStatisticsRecord record = new InOutStatisticsRecord();
        record.setInRecord(((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).
                getRecordByResource(sequenceName, ComponentType.SEQUENCE, true));
        record.setOutRecord(((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).
                getRecordByResource(sequenceName, ComponentType.SEQUENCE, false));
        return record;
    }

    public InOutStatisticsRecord getEndPointStatistics(String endpointName) {
        InOutStatisticsRecord record = new InOutStatisticsRecord();
        if (endpointName.contains(SynapseConstants.STATISTICS_KEY_SEPARATOR)) {
            record.setInRecord(((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).
                getRecordByResource(endpointName, ComponentType.ENDPOINT, true));
        } else {
            String[] endpoints = listEndPoint();
            StatisticsRecord fullRecord = null;
            for (String endpoint : endpoints) {
                if (endpoint.equals(endpointName) ||
                        endpoint.startsWith(endpointName + SynapseConstants.STATISTICS_KEY_SEPARATOR)) {

                    StatisticsRecord currentRecord = ((MediationStatisticsStore) getConfigContext().
                getProperty(StatisticsConstants.STAT_PROPERTY)).
                            getRecordByResource(endpoint, ComponentType.ENDPOINT, true);
                    if (fullRecord == null) {
                        fullRecord = new StatisticsRecord(currentRecord);
                    } else {
                        fullRecord.updateRecord(currentRecord);
                    }
                }
            }
            record.setInRecord(fullRecord);
        }
        return record;
    }
}