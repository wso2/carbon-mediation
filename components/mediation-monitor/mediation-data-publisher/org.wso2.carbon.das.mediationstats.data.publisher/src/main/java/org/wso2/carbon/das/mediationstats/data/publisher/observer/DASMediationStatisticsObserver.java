/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.das.mediationstats.data.publisher.observer;


import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.messageflowtracer.data.MessageFlowComponentEntry;
import org.apache.synapse.messageflowtracer.data.MessageFlowTraceEntry;
import org.wso2.carbon.das.mediationstats.data.publisher.conf.MediationStatConfig;
import org.wso2.carbon.das.mediationstats.data.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.das.mediationstats.data.publisher.data.MessageFlowTraceEntryData;
import org.wso2.carbon.das.mediationstats.data.publisher.data.TraceComponentData;
import org.wso2.carbon.das.mediationstats.data.publisher.publish.Publisher;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.statistics.*;
import org.wso2.carbon.message.flow.tracer.data.MessageFlowTracingObserver;


public class DASMediationStatisticsObserver implements MessageFlowTracingObserver,
                                                       TenantInformation {

    private static final Log log = LogFactory.getLog(DASMediationStatisticsObserver.class);
    private AxisConfiguration axisConfiguration;
    private int tenantId = -1234;

    public DASMediationStatisticsObserver() {
    }

    @Override
    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Shutting down the mediation statistics observer of BAM");
        }
    }

    @Override
    public void updateStatistics(Object traceEntry) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId,true);
            updateStatisticsInternal(traceEntry);
        } catch (Exception e) {
            log.error("failed to update statics from BAM publisher", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    private void updateStatisticsInternal(Object traceEntry)
            throws Exception {
        int tenantID = getTenantId();
        MediationStatConfig mediationStatConfig = new RegistryPersistenceManager().getEventingConfigData(tenantID);

        if (mediationStatConfig == null) {
            return;
        }

        if("MessageFlowTraceEntry".equals(traceEntry.getClass().getSimpleName())) {
            MessageFlowTraceEntry messageFlowTraceEntry = (MessageFlowTraceEntry) traceEntry;
            processMessageFlowTraceEntry(messageFlowTraceEntry, mediationStatConfig);
        } else {
            MessageFlowComponentEntry messageFlowComponentEntry = (MessageFlowComponentEntry) traceEntry;
            processMessageFlowComponentEntry(messageFlowComponentEntry,mediationStatConfig);
        }

    }

    private void processMessageFlowComponentEntry(MessageFlowComponentEntry messageFlowComponentEntry, MediationStatConfig mediationStatConfig) {
        if (messageFlowComponentEntry == null) {
            return;
        }
        TraceComponentData traceComponentData = new TraceComponentData();
        traceComponentData.setMessageId(messageFlowComponentEntry.getMessageId());
        traceComponentData.setComponentId(messageFlowComponentEntry.getComponentId());
        traceComponentData.setComponentName(messageFlowComponentEntry.getComponentName());
        traceComponentData.setPayload(messageFlowComponentEntry.getPayload());
        traceComponentData.setTimestamp(messageFlowComponentEntry.getTimestamp());
        traceComponentData.setResponse(messageFlowComponentEntry.isResponse());
        traceComponentData.setStart(messageFlowComponentEntry.isStart());
        traceComponentData.setPropertyMap(messageFlowComponentEntry.getPropertyMap());
        traceComponentData.setTransportPropertyMap(messageFlowComponentEntry.getTransportPropertyMap());
        Publisher.process(traceComponentData, mediationStatConfig);
    }

    private void processMessageFlowTraceEntry(MessageFlowTraceEntry messageFlowTraceEntry, MediationStatConfig mediationStatConfig) {
        if (messageFlowTraceEntry == null) {
            return;
        }
        MessageFlowTraceEntryData messageFlowTraceEntryData = new MessageFlowTraceEntryData();
        messageFlowTraceEntryData.setMessageId(messageFlowTraceEntry.getMessageId());
        messageFlowTraceEntryData.setEntryType(messageFlowTraceEntry.getEntryType());
        messageFlowTraceEntryData.setMessageFlow(messageFlowTraceEntry.getMessageFlow());
        messageFlowTraceEntryData.setTimeStamp(messageFlowTraceEntry.getTimeStamp());
        Publisher.process(messageFlowTraceEntryData,mediationStatConfig);
    }

    private void process(Object traceEntry,
                         MediationStatConfig mediationStatConfig) {
        TraceComponentData traceComponentData = new TraceComponentData();
        java.util.Date currentDate = new java.util.Date();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(currentDate.getTime());
        Publisher.process(traceComponentData, mediationStatConfig);
    }



    @Override
    public int getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(int i) {
        tenantId = i;
    }


    public AxisConfiguration getTenantAxisConfiguration() {
        return axisConfiguration;
    }

    public void setTenantAxisConfiguration(AxisConfiguration axisConfiguration) {
        this.axisConfiguration = axisConfiguration;
    }
}
