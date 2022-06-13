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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.das.data.publisher.util.AnalyticsDataPublisherConstants;
import org.wso2.carbon.das.messageflow.data.publisher.observer.AnalyticsMediationFlowObserver;
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
import org.wso2.carbon.das.messageflow.data.publisher.data.MessageFlowObserverStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "org.wso2.carbon.das.messageflow.data.publisher",
        immediate = true)
public class MediationStatisticsComponent {

    private static final Log log = LogFactory.getLog(MediationStatisticsComponent.class);

    private static boolean flowStatisticsEnabled;

    private boolean activated = false;

    private Map<Integer, MessageFlowObserverStore> stores = new HashMap<Integer, MessageFlowObserverStore>();

    private Map<Integer, List<MessageFlowReporterThread>> reporterThreads = new HashMap<>();

    private Map<Integer, MediationConfigReporterThread> configReporterThreads = new HashMap<Integer,
            MediationConfigReporterThread>();

    private Map<Integer, SynapseEnvironmentService> synapseEnvServices = new HashMap<Integer,
            SynapseEnvironmentService>();

    private ComponentContext compCtx;

    @Activate
    protected void activate(ComponentContext ctxt) {

        this.compCtx = ctxt;
        // Check whether statistic collecting is globally enabled
        checkPublishingEnabled();
        if (!flowStatisticsEnabled) {
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

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        MessageFlowObserverStore observerStore = new MessageFlowObserverStore();
        MessageFlowReporterThread reporterThread = null;
        ServerConfiguration serverConf = ServerConfiguration.getInstance();
        // Set a custom interval value if required
        String interval = serverConf.getFirstProperty(AnalyticsDataPublisherConstants
                .FLOW_STATISTIC_WORKER_IDLE_INTERVAL);
        long delay = AnalyticsDataPublisherConstants.FLOW_STATISTIC_WORKER_IDLE_INTERVAL_DEFAULT;
        if (interval != null) {
            try {
                delay = Long.parseLong(interval);
            } catch (NumberFormatException ignored) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid delay time for mediation-flow-tracer thread. It will use default value - " +
                            AnalyticsDataPublisherConstants.FLOW_STATISTIC_WORKER_IDLE_INTERVAL_DEFAULT);
                }
                delay = AnalyticsDataPublisherConstants.FLOW_STATISTIC_WORKER_IDLE_INTERVAL_DEFAULT;
            }
        }
        String workerCountString = serverConf.getFirstProperty(AnalyticsDataPublisherConstants
                .FLOW_STATISTIC_WORKER_COUNT);
        int workerCount = AnalyticsDataPublisherConstants.FLOW_STATISTIC_WORKER_COUNT_DEFAULT;
        if (workerCountString != null) {
            try {
                workerCount = Integer.parseInt(workerCountString);
            } catch (NumberFormatException ignored) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid StatisticWorkerCount. It will use default value - " +
                            AnalyticsDataPublisherConstants.FLOW_STATISTIC_WORKER_COUNT_DEFAULT);
                }
                workerCount = AnalyticsDataPublisherConstants.FLOW_STATISTIC_WORKER_COUNT_DEFAULT;
            }
        }
        
        List<MessageFlowReporterThread> messageFlowReporterThreadList = new ArrayList<>();
        for (int i = 0; i < workerCount; i++) {
            reporterThread = new MessageFlowReporterThread(synEnvService, observerStore);
            reporterThread.setName("message-flow-reporter-" + i + "-tenant-" + tenantId);
            reporterThread.setDelay(delay);
            reporterThread.start();
            messageFlowReporterThreadList.add(reporterThread);
        }
        reporterThreads.put(tenantId, messageFlowReporterThreadList);
        
        String disableJmxStr = serverConf.getFirstProperty(AnalyticsDataPublisherConstants
                .FLOW_STATISTIC_JMX_PUBLISHING);
        boolean enableJmxPublishing = !Boolean.parseBoolean(disableJmxStr);
        if (enableJmxPublishing) {
            JMXMediationFlowObserver jmxObserver = new JMXMediationFlowObserver(tenantId);
            observerStore.registerObserver(jmxObserver);
            log.info("JMX mediation statistic publishing enabled for tenant: " + tenantId);
        }
        String disableAnalyticStr = serverConf.getFirstProperty(AnalyticsDataPublisherConstants
                .FLOW_STATISTIC_ANALYTICS_PUBLISHING);
        boolean enableAnalyticsPublishing = !Boolean.parseBoolean(disableAnalyticStr);
        if (enableAnalyticsPublishing) {
            AnalyticsMediationFlowObserver dasObserver = new AnalyticsMediationFlowObserver();
            observerStore.registerObserver(dasObserver);
            dasObserver.setTenantId(tenantId);
            log.info("DAS mediation statistic publishing enabled for tenant: " + tenantId);
        }
        // Engage custom observer implementations (user written extensions)
        String observers = serverConf.getFirstProperty(AnalyticsDataPublisherConstants.STAT_OBSERVERS);
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

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        Set<Map.Entry<Integer, List<MessageFlowReporterThread>>> threadEntriesSet = reporterThreads.entrySet();
        for (Map.Entry<Integer, List<MessageFlowReporterThread>> threadEntryList : threadEntriesSet) {
            List<MessageFlowReporterThread> reporterThreadsList = threadEntryList.getValue();
            for (MessageFlowReporterThread reporterThread: reporterThreadsList) {
                if (reporterThread != null && reporterThread.isAlive()) {
                    reporterThread.shutdown();
                    // This should wake up the thread if it is asleep
                    reporterThread.interrupt();
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
        }
        // Stops config reporting threads
        for (MediationConfigReporterThread configReporterThread : configReporterThreads.values()) {
            if (configReporterThread != null && configReporterThread.isAlive()) {
                configReporterThread.shutdown();
                configReporterThread.interrupt();
                // Wait until the thread is gracefully terminates
                // for (int i = 0; i < 50; i++) {
                // if (!configReporterThread.isAlive()) {
                // break;
                // }
                // if (log.isDebugEnabled()) {
                // log.debug("Waiting for the mediation config reporter thread to terminate");
                // }
                //
                // try {
                // Thread.sleep(100);
                // } catch (InterruptedException ignore) {
                //
                // }
                // }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("DAS service statistics data publisher bundle is deactivated");
        }
    }

    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {

        MessageFlowDataPublisherDataHolder.getInstance().setContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

        MessageFlowDataPublisherDataHolder.getInstance().setContextService(null);
    }

    @Reference(
            name = "org.wso2.carbon.registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        MessageFlowDataPublisherDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

        MessageFlowDataPublisherDataHolder.getInstance().setRegistryService(null);
    }

    @Reference(
            name = "synapse.env.service",
            service = org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSynapseEnvironmentService")
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

    @Reference(
            name = "synapse.registrations.service",
            service = org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSynapseRegistrationsService")
    protected void setSynapseRegistrationsService(SynapseRegistrationsService registrationsService) {

        ServiceRegistration synEnvSvcRegistration = registrationsService.getSynapseEnvironmentServiceRegistration();
        try {
            if (activated && compCtx != null) {
                SynapseEnvironmentService synEnvSvc = (SynapseEnvironmentService) compCtx.getBundleContext()
                        .getService(synEnvSvcRegistration.getReference());
                createStores(synEnvSvc);
            }
        } catch (Throwable t) {
            log.fatal("Error occurred at the osgi service method", t);
        }
    }

    protected void unsetSynapseRegistrationsService(SynapseRegistrationsService registrationsService) {

        try {
            int tenantId = registrationsService.getTenantId();
            shutdownMessageFlowReporterThreads(tenantId);
            shutdownMediationConfigReporterThread(tenantId);

        } catch (Throwable t) {
            log.error("Fatal error occurred at the osgi service method", t);
        }
    }

    private void shutdownMediationConfigReporterThread(int tenantId) {

        MediationConfigReporterThread mediationConfigReporterThread = configReporterThreads.get(tenantId);
        if (mediationConfigReporterThread != null && mediationConfigReporterThread.isAlive()) {
            mediationConfigReporterThread.shutdown();
            mediationConfigReporterThread.interrupt();

            while (mediationConfigReporterThread.isAlive()) {
                if (log.isDebugEnabled()) {
                    log.debug("Waiting for the mediation config reporter thread to terminate");
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    private void shutdownMessageFlowReporterThreads(int tenantId) {

        List<MessageFlowReporterThread> reporterThreadList = reporterThreads.get(tenantId);
        if (reporterThreadList != null) {
            for (MessageFlowReporterThread reporterThread: reporterThreadList) {
                if (reporterThread != null && reporterThread.isAlive()) {
                    reporterThread.shutdown();
                    // This should wake up the thread if it is asleep
                    reporterThread.interrupt();
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
            }
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
