/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.das.messageflow.data.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.collectors.RuntimeStatisticCollector;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.das.data.publisher.util.DASDataPublisherConstants;
import org.wso2.carbon.das.messageflow.data.publisher.observer.DASMediationFlowObserver;
import org.wso2.carbon.das.messageflow.data.publisher.observer.MessageFlowObserver;
import org.wso2.carbon.das.messageflow.data.publisher.observer.TenantInformation;
import org.wso2.carbon.das.messageflow.data.publisher.observer.jmx.JMXMediationFlowObserver;
import org.wso2.carbon.das.messageflow.data.publisher.services.MediationConfigReporterThread;
import org.wso2.carbon.das.messageflow.data.publisher.services.MessageFlowReporterThread;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.event.stream.core.EventStreamService;

import org.wso2.carbon.das.messageflow.data.publisher.data.MessageFlowObserverStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @scr.component name="org.wso2.carbon.das.messageflow.data.publisher" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="org.wso2.carbon.registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="synapse.env.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..1" policy="dynamic" bind="setSynapseEnvironmentService"
 * unbind="unsetSynapseEnvironmentService"
 * @scr.reference name="synapse.registrations.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService"
 * cardinality="1..n" policy="dynamic" bind="setSynapseRegistrationsService"
 * unbind="unsetSynapseRegistrationsService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * unbind="unsetEventStreamService"
 */
public class MediationStatisticsComponent {

    private static final Log log = LogFactory.getLog(MediationStatisticsComponent.class);

    private static boolean flowStatisticsEnabled;

    private boolean activated = false;

    private Map<Integer, MessageFlowObserverStore> stores = new HashMap<Integer, MessageFlowObserverStore>();

    private Map<Integer, MessageFlowReporterThread> reporterThreads = new HashMap<Integer, MessageFlowReporterThread>();

    private Map<Integer, MediationConfigReporterThread> configReporterThreads = new HashMap<Integer, MediationConfigReporterThread>();

    private Map<Integer, SynapseEnvironmentService> synapseEnvServices = new HashMap<Integer, SynapseEnvironmentService>();

    private ComponentContext compCtx;


    protected void activate(ComponentContext ctxt) {

        this.compCtx = ctxt;

        // Check whether statistic collecting is globally enabled
        checkPublishingEnabled();

        if (!flowStatisticsEnabled){
            activated = false;
            if (log.isDebugEnabled()) {
                log.debug("DAS Message Flow Publishing Component not-activated");
            }
            return;
        }

        int tenantId = MultitenantConstants.SUPER_TENANT_ID;

        SynapseEnvironmentService synapseEnvService = synapseEnvServices.get(tenantId);

        // Create observer store for super-tenant
        createStores(synapseEnvService);

        activated = true;

        if (log.isDebugEnabled()) {
            log.debug("DAS Message Flow Publishing Component activate");
        }
    }

    /**
     * Create the observers store using the synapse environment and configuration context.
     *
     * @param synEnvService information about synapse runtime
     */
    private void createStores(SynapseEnvironmentService synEnvService) {
        int tenantId = resolveTenantId();

        MessageFlowObserverStore observerStore = new MessageFlowObserverStore();

        MessageFlowReporterThread reporterThread = null;

        ServerConfiguration serverConf = ServerConfiguration.getInstance();

        // Set a custom interval value if required
        String interval = serverConf.getFirstProperty(DASDataPublisherConstants.FLOW_STATISTIC_WORKER_IDLE_INTERVAL);
        long delay = DASDataPublisherConstants.FLOW_STATISTIC_WORKER_IDLE_INTERVAL_DEFAULT;
        if (interval != null) {
            try {
                delay = Long.parseLong(interval);
            } catch (NumberFormatException ignored) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid delay time for mediation-flow-tracer thread. It will use default value - "
                              + DASDataPublisherConstants.FLOW_STATISTIC_WORKER_IDLE_INTERVAL_DEFAULT);
                }
                delay = DASDataPublisherConstants.FLOW_STATISTIC_WORKER_IDLE_INTERVAL_DEFAULT;
            }
        }

        String workerCountString = serverConf.getFirstProperty(DASDataPublisherConstants.FLOW_STATISTIC_WORKER_COUNT);
        int workerCount = DASDataPublisherConstants.FLOW_STATISTIC_WORKER_COUNT_DEFAULT;
        if (workerCountString != null) {
            try {
                workerCount = Integer.parseInt(workerCountString);
            } catch (NumberFormatException ignored) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid StatisticWorkerCount. It will use default value - "
                              + DASDataPublisherConstants.FLOW_STATISTIC_WORKER_COUNT_DEFAULT);
                }
                workerCount = DASDataPublisherConstants.FLOW_STATISTIC_WORKER_COUNT_DEFAULT;
            }
        }
        for (int i = 0; i < workerCount; i++) {
            reporterThread = new MessageFlowReporterThread(synEnvService, observerStore);
            reporterThread.setName("message-flow-reporter-" + i + "-tenant-" + tenantId);
            reporterThread.setDelay(delay);
            reporterThread.start();
            reporterThreads.put(tenantId, reporterThread);
        }


        String disableJmxStr = serverConf.getFirstProperty(DASDataPublisherConstants.FLOW_STATISTIC_JMX_PUBLISHING);
        boolean enableJmxPublishing = !Boolean.parseBoolean(disableJmxStr);
        if (enableJmxPublishing) {
            JMXMediationFlowObserver jmxObserver = new JMXMediationFlowObserver(tenantId);
            observerStore.registerObserver(jmxObserver);
            log.info("JMX mediation statistic publishing enabled for tenant: " + tenantId);
        }
        String disableAnalyticStr = serverConf.getFirstProperty(DASDataPublisherConstants.FLOW_STATISTIC_ANALYTICS_PUBLISHING);
        boolean enableAnalyticsPublishing = !Boolean.parseBoolean(disableAnalyticStr);
        if (enableAnalyticsPublishing) {
            DASMediationFlowObserver dasObserver = new DASMediationFlowObserver();
            observerStore.registerObserver(dasObserver);
            dasObserver.setTenantId(tenantId);
            log.info("DAS mediation statistic publishing enabled for tenant: " + tenantId);
        }
        // Engage custom observer implementations (user written extensions)
        String observers = serverConf.getFirstProperty(DASDataPublisherConstants.STAT_OBSERVERS);
        if (observers != null && !"".equals(observers)) {
            String[] classNames = observers.split(",");
            for (String className : classNames) {
                try {
                    Class clazz = this.getClass().getClassLoader().loadClass(className.trim());
                    MessageFlowObserver o = (MessageFlowObserver) clazz.newInstance();
                    observerStore.registerObserver(o);
                    if (o instanceof TenantInformation) {
                        TenantInformation tenantInformation = (TenantInformation) o;
                        tenantInformation.setTenantId(synEnvService.getTenantId());
                    }
                } catch (Exception e) {
                    log.error("Error while initializing the mediation statistics observer : " + className, e);
                }
            }
        }
        // 'MediationStat service' will be deployed per tenant (cardinality="1..n")

        if (log.isDebugEnabled()) {
            log.debug("Registering  Observer for tenant: " + tenantId);
        }
        stores.put(tenantId, observerStore);

        // Adding configuration reporting thread
        MediationConfigReporterThread configReporterThread = new MediationConfigReporterThread(synEnvService);
        configReporterThread.setName("mediation-config-reporter-" + tenantId);
        configReporterThread.setTenantId(tenantId);
        configReporterThread.setPublishingAnalyticESB(enableAnalyticsPublishing);

        configReporterThread.start();
        if (log.isDebugEnabled()) {
            log.debug("Registering the new mediation configuration reporter thread");
        }
        configReporterThreads.put(tenantId, configReporterThread);
    }

    /**
     * If CARBON_HOME contains tenantIdFile, take tenant-id from there (cloud installations), else take from carbon context
     *
     * @return tenant-id
     */
    private int resolveTenantId() {

        int tenantId = -1;
        String TENANT_ID_FILE_NAME = "tenantIdFile";
        File tenantIdFile = new File(CarbonBaseUtils.getCarbonHome(), TENANT_ID_FILE_NAME);

        if (tenantIdFile.exists()) {
            try {
                // Read the file first line
                BufferedReader br = null;
                br = new BufferedReader(new FileReader(tenantIdFile));

                String line = null;

                while ((line = br.readLine()) != null) {
                    tenantId = Integer.parseInt(line);
                    log.info("Message publisher detected tenant id: " + tenantId + " from file");
                }
                br.close();
            } catch (IOException | NumberFormatException e) {
                log.error("Failed to read tenant-id from file", e);
            }

            // Need to delete file after use
            tenantIdFile.delete();
        }

        if(tenantId == -1)
            return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        else
            return tenantId;
    }

    protected void deactivate(ComponentContext ctxt) {
        Set<Map.Entry<Integer, MessageFlowReporterThread>> threadEntries = reporterThreads.entrySet();
        for (Map.Entry<Integer, MessageFlowReporterThread> threadEntry : threadEntries) {
            MessageFlowReporterThread reporterThread = threadEntry.getValue();
            if (reporterThread != null && reporterThread.isAlive()) {
                reporterThread.shutdown();
                reporterThread.interrupt(); // This should wake up the thread if it is asleep

                // Wait for the reporting thread to gracefully terminate
                // Observers should not be disengaged before this thread halts
                // Otherwise some of the collected data may not be sent to the observers
                for (int i = 0; i < 50; i++) {
                    if (!reporterThread.isAlive()) {
                        break;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Waiting for the mediation tracer reporter thread to terminate");
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignore) {

                    }
                }
            }
        }

        // Stops config reporting threads
        for(MediationConfigReporterThread configReporterThread : configReporterThreads.values()) {
            if (configReporterThread != null && configReporterThread.isAlive()) {
                configReporterThread.shutdown();
                configReporterThread.interrupt();

                // Wait until the thread is gracefully terminates
//                for (int i = 0; i < 50; i++) {
//                    if (!configReporterThread.isAlive()) {
//                        break;
//                    }
//                    if (log.isDebugEnabled()) {
//                        log.debug("Waiting for the mediation config reporter thread to terminate");
//                    }
//
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException ignore) {
//
//                    }
//                }
            }
        }


        if (log.isDebugEnabled()) {
            log.debug("DAS service statistics data publisher bundle is deactivated");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        MessageFlowDataPublisherDataHolder.getInstance().setContextService(contextService);

    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        MessageFlowDataPublisherDataHolder.getInstance().setContextService(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        MessageFlowDataPublisherDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        MessageFlowDataPublisherDataHolder.getInstance().setRegistryService(null);
    }

    protected void setEventStreamService(EventStreamService publisherService) {
        MessageFlowDataPublisherDataHolder.getInstance().setPublisherService(publisherService);
    }

    protected void unsetEventStreamService(EventStreamService publisherService) {
        MessageFlowDataPublisherDataHolder.getInstance().setPublisherService(null);
    }

    protected void setSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {
        if (log.isDebugEnabled()) {
            log.debug("SynapseEnvironmentService bound to the mediation tracer initialization");
        }

        synapseEnvServices.put(synapseEnvironmentService.getTenantId(), synapseEnvironmentService);
    }

    protected void unsetSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {
        if (log.isDebugEnabled()) {
            log.debug("SynapseEnvironmentService unbound from the mediation tracer collector");
        }

        synapseEnvServices.remove(synapseEnvironmentService.getTenantId());
    }

    protected void setSynapseRegistrationsService(SynapseRegistrationsService registrationsService) {
        ServiceRegistration synEnvSvcRegistration = registrationsService.getSynapseEnvironmentServiceRegistration();
        try {
            if (activated && compCtx != null) {
                SynapseEnvironmentService synEnvSvc = (SynapseEnvironmentService) compCtx.getBundleContext().getService(
                        synEnvSvcRegistration.getReference());
                createStores(synEnvSvc);
            }
        } catch (Throwable t) {
            log.fatal("Error occurred at the osgi service method", t);
        }
    }

    protected void unsetSynapseRegistrationsService(SynapseRegistrationsService registrationsService) {
        try {
            int tenantId = registrationsService.getTenantId();
            MessageFlowReporterThread reporterThread = reporterThreads.get(tenantId);
            if (reporterThread != null && reporterThread.isAlive()) {
                reporterThread.shutdown();
                reporterThread.interrupt(); // This should wake up the thread if it is asleep

                // Wait for the reporting thread to gracefully terminate
                // Observers should not be disengaged before this thread halts
                // Otherwise some of the collected data may not be sent to the observers
                while (reporterThread.isAlive()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Waiting for the trace reporter thread to terminate");
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignore) {

                    }
                }
            }
        } catch (Throwable t) {
            log.error("Fatal error occurred at the osgi service method", t);
        }
    }

    private void checkPublishingEnabled() {
        flowStatisticsEnabled = RuntimeStatisticCollector.isStatisticsEnabled();
        MessageFlowDataPublisherDataHolder.getInstance().setGlobalStatisticsEnabled(flowStatisticsEnabled);

        if (!flowStatisticsEnabled) {
            log.info("Global Message-Flow Statistic Reporting is Disabled");
        }

    }
}
