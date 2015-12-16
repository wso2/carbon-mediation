package org.wso2.carbon.message.flow.tracer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.message.flow.tracer.services.MessageFlowTraceService;
import org.wso2.carbon.message.flow.tracer.services.MessageFlowTraceServiceImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @scr.component name="mediation.flow.tracer" immediate="true"
 * @scr.reference name="synapse.env.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..n" policy="dynamic"
 * bind="setSynapseEnvironmentService" unbind="unsetSynapseEnvironmentService"
 * @scr.reference name="synapse.registrations.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService"
 * cardinality="1..n" policy="dynamic" bind="setSynapseRegistrationsService"
 * unbind="unsetSynapseRegistrationsService"
 */
public class MessageFlowTracerServiceComponent {

    private static final Log log = LogFactory.getLog(MessageFlowTracerService.class);

    private Map<Integer, MessageFlowTraceDataStore> stores =
            new HashMap<Integer, MessageFlowTraceDataStore>();

    private Map<Integer, MessageFlowTraceReporterThread> reporterThreads =
            new HashMap<Integer, MessageFlowTraceReporterThread>();

    private Map<Integer, SynapseEnvironmentService> synapseEnvServices =
            new HashMap<Integer, SynapseEnvironmentService>();

    private Map<Integer, MessageFlowTraceService> services =
            new HashMap<Integer, MessageFlowTraceService>();

    private boolean initialized = false;

    private ComponentContext compCtx;

    protected void activate(ComponentContext compCtx) throws Exception {
        this.compCtx = compCtx;
        try {
            SynapseEnvironmentService synapseEnvService = synapseEnvServices.get(MultitenantConstants.SUPER_TENANT_ID);

            if (synapseEnvService != null) {
                createStatisticsStore(synapseEnvService);
                MessageFlowTraceDataStore statisticsStore = stores.get(MultitenantConstants.SUPER_TENANT_ID);
                MessageFlowTraceService service =
                        new MessageFlowTraceServiceImpl(statisticsStore, MultitenantConstants.SUPER_TENANT_ID,
                                                           synapseEnvService.getConfigurationContext());

                services.put(MultitenantConstants.SUPER_TENANT_ID, service);

                compCtx.getBundleContext().registerService(MessageFlowTraceService.class.getName(), service, null);
                initialized = true;
            } else {
                log.error("Couldn't initialize Mediation Flow Tracer");
            }
        } catch (Throwable e) {
            log.fatal("Error while initializing Mediation Flow Tracer : ", e);
        }
    }

    /**
     * Create the statistics store using the synapse environment and configuration context.
     *
     * @param synEnvService information about synapse runtime
     */
    private void createStatisticsStore(SynapseEnvironmentService synEnvService) {

        //TODO Do we need to use old stat reporter enable in xml
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConfigurationContext cfgCtx = synEnvService.getConfigurationContext();

        MessageFlowTraceDataStore statisticsStore = new MessageFlowTraceDataStore( synEnvService.getSynapseEnvironment()
                                                                                       .getMessageFlowDataHolder());
        cfgCtx.setProperty(MessageFlowTraceConstants.MESSAGE_FLOW_TRACE_STORE, statisticsStore);

        MessageFlowTraceReporterThread reporterThread = new MessageFlowTraceReporterThread(synEnvService, statisticsStore);
        reporterThread.setName("mediation-flow-tracer-" + tenantId);

        //TODO Do we need this
        //Set a custom interval value if required
        //TODO Engage the persisting stat observer if required OR Engage custom observer implementations (user
        // written extensions)

        reporterThread.start();
        if (log.isDebugEnabled()) {
            log.debug("Registering the new mediation flow tracer service");
        }
        reporterThreads.put(tenantId, reporterThread);
        stores.put(tenantId, statisticsStore);
    }

    protected void deactivate(ComponentContext compCtx) throws Exception {
        Set<Map.Entry<Integer, MessageFlowTraceReporterThread>> threadEntries = reporterThreads.entrySet();
        for (Map.Entry<Integer, MessageFlowTraceReporterThread> threadEntry : threadEntries) {
            MessageFlowTraceReporterThread reporterThread = threadEntry.getValue();
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

        //TODO if observers are registered unregister them
    }

    protected void setSynapseEnvironmentService(SynapseEnvironmentService synEnvService) {

        if (log.isDebugEnabled()) {
            log.debug("SynapseEnvironmentService bound to the mediation tracer initialization");
        }

        synapseEnvServices.put(synEnvService.getTenantId(), synEnvService);
    }

    protected void unsetSynapseEnvironmentService(SynapseEnvironmentService synEnvService) {
        if (log.isDebugEnabled()) {
            log.debug("SynapseEnvironmentService unbound from the mediation tracer collector");
        }

        synapseEnvServices.remove(synEnvService.getTenantId());
    }

    protected void setSynapseRegistrationsService(SynapseRegistrationsService registrationsService) {
        ServiceRegistration synEnvSvcRegistration = registrationsService.getSynapseEnvironmentServiceRegistration();
        try {
            if (initialized && compCtx != null) {
                SynapseEnvironmentService synEnvSvc = (SynapseEnvironmentService) compCtx.getBundleContext().getService(
                        synEnvSvcRegistration.getReference());
                createStatisticsStore(synEnvSvc);
                int tenantId = registrationsService.getTenantId();
                MessageFlowTraceDataStore statisticsStore = stores.get(tenantId);
                if (statisticsStore != null) {
                    MessageFlowTraceService service =
                            new MessageFlowTraceServiceImpl(statisticsStore, registrationsService.getTenantId(),
                                                               registrationsService.getConfigurationContext());

                    services.put(registrationsService.getTenantId(), service);

                    compCtx.getBundleContext()
                            .registerService(MessageFlowTraceService.class.getName(), service, null);
                } else {
                    log.warn("Couldn't find the mediation trace data store for tenant id: " + tenantId);
                }
            }
        } catch (Throwable t) {
            log.fatal("Error occurred at the osgi service method", t);
        }
    }

    protected void unsetSynapseRegistrationsService(SynapseRegistrationsService registrationsService) {
        try {
            int tenantId = registrationsService.getTenantId();
            MessageFlowTraceReporterThread reporterThread = reporterThreads.get(tenantId);
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
            //TODO unregister observers
        } catch (Throwable t) {
            log.error("Fatal error occured at the osgi service method", t);
        }
    }
}
