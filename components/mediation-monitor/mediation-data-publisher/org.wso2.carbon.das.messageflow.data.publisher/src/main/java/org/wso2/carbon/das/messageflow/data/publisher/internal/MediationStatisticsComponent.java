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
package org.wso2.carbon.das.messageflow.data.publisher.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.collectors.RuntimeStatisticCollector;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.das.messageflow.data.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.das.messageflow.data.publisher.observer.DASMediationFlowObserver;
import org.wso2.carbon.das.messageflow.data.publisher.services.DASMessageFlowPublisherAdmin;
import org.wso2.carbon.das.messageflow.data.publisher.util.PublisherUtils;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import org.wso2.carbon.das.messageflow.data.publisher.data.MessageFlowObserverStore;
import org.wso2.carbon.das.messageflow.data.publisher.services.MessageFlowReporterThread;

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
 */
public class MediationStatisticsComponent {

    private static final Log log = LogFactory.getLog(MediationStatisticsComponent.class);

    private static boolean flowStatisticsEnabled;

    private boolean activated = false;

    private Map<Integer, MessageFlowObserverStore> stores = new HashMap<Integer, MessageFlowObserverStore>();

    private Map<Integer, MessageFlowReporterThread> reporterThreads = new HashMap<Integer, MessageFlowReporterThread>();

    private Map<Integer, SynapseEnvironmentService> synapseEnvServices = new HashMap<Integer, SynapseEnvironmentService>();

    private ComponentContext compCtx;


    protected void activate(ComponentContext ctxt) {

        this.compCtx = ctxt;

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

        createObserversStore(synapseEnvService);
        MessageFlowObserverStore observerStore = stores.get(tenantId);


        DASMessageFlowPublisherAdmin dasMediationStatsPublisherAdmin = new DASMessageFlowPublisherAdmin();
        PublisherUtils.setMediationStatPublisherAdmin(dasMediationStatsPublisherAdmin);

        if (observerStore != null) {
            DASMediationFlowObserver observer = new DASMediationFlowObserver();
            observerStore.registerObserver(observer);
            observer.setTenantId(tenantId);
            // 'MediationStat service' will be deployed per tenant (cardinality="1..n")

            log.debug("Registering  Observer for tenant: " + tenantId);
        } else {
            log.error("Can't register an observer for MessageFlowTraceObserverStore. " );
        }

        //Load previously saved configurations
        new RegistryPersistenceManager().load(tenantId);


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
    private void createObserversStore(SynapseEnvironmentService synEnvService) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConfigurationContext cfgCtx = synEnvService.getConfigurationContext();

        MessageFlowObserverStore observerStore = new MessageFlowObserverStore();

        MessageFlowReporterThread reporterThread = new MessageFlowReporterThread(synEnvService, observerStore);
        reporterThread.setName("mediation-flow-tracer-" + tenantId);
        reporterThread.start();
        if (log.isDebugEnabled()) {
            log.debug("Registering the new mediation flow tracer service");
        }
        reporterThreads.put(tenantId, reporterThread);
        stores.put(tenantId, observerStore);
    }

    protected void deactivate(ComponentContext ctxt) {
        // unregistered DASMediationStatsPublisherAdmin service from the OSGi Service Register.
//        statAdminServiceRegistration.unregister();

        Set<Map.Entry<Integer, MessageFlowReporterThread>> threadEntries = reporterThreads.entrySet();
        for (Map.Entry<Integer, MessageFlowReporterThread> threadEntry : threadEntries) {
            MessageFlowReporterThread reporterThread = threadEntry.getValue();
            if (reporterThread != null && reporterThread.isAlive()) {
                reporterThread.shutdown();
                reporterThread.interrupt(); // This should wake up the thread if it is asleep

                // Wait for the reporting thread to gracefully terminate
                // Observers should not be disengaged before this thread halts
                // Otherwise some of the collected data may not be sent to the observers
                while (reporterThread.isAlive()) {
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

        if (log.isDebugEnabled()) {
            log.debug("DAS service statistics data publisher bundle is deactivated");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        PublisherUtils.setConfigurationContextService(contextService);

    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        PublisherUtils.setConfigurationContextService(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        try {
            RegistryPersistenceManager.setDasRegistryService(registryService);
        } catch (Exception e) {
            log.error("Cannot retrieve System Registry", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        RegistryPersistenceManager.setDasRegistryService(null);
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
                createObserversStore(synEnvSvc);
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
        PublisherUtils.setTraceDataCollectingEnabled(flowStatisticsEnabled);

        if (!flowStatisticsEnabled) {
            log.info("Statistic Reporter is Disabled");
        }

    }
}
