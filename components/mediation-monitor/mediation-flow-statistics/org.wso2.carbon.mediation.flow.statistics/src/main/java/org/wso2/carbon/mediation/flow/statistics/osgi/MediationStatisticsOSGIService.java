package org.wso2.carbon.mediation.flow.statistics.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.mediation.flow.statistics.StatisticCollectingThread;
import org.wso2.carbon.mediation.flow.statistics.StatisticNotifier;
import org.wso2.carbon.mediation.flow.statistics.service.MediationStatisticsService;
import org.wso2.carbon.mediation.flow.statistics.service.MediationStatisticsServiceImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @scr.component name="new.mediation.statistics" immediate="true"
 * @scr.reference name="synapse.env.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..n" policy="dynamic"
 * bind="setSynapseEnvironmentService" unbind="unsetSynapseEnvironmentService"
 * @scr.reference name="synapse.registrations.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService"
 * cardinality="1..n" policy="dynamic" bind="setSynapseRegistrationsService"
 * unbind="unsetSynapseRegistrationsService"
 */
//NO registry service like previous one
public class MediationStatisticsOSGIService {

	private static final Log log = LogFactory.getLog(MediationStatisticsOSGIService.class);

	private Map<Integer, StatisticNotifier> notifierHashMap = new HashMap<>();

	private Map<Integer, StatisticCollectingThread> reporterThreads = new HashMap<>();

	private Map<Integer, SynapseEnvironmentService> synapseEnvServices = new HashMap<>();

	private boolean initialized = false;

	private Map<Integer, MediationStatisticsService> services = new HashMap<>();

	private ComponentContext compCtx;

	protected void activate(ComponentContext compCtx) throws Exception {
		this.compCtx = compCtx;
		try {
			SynapseEnvironmentService synapseEnvService = synapseEnvServices.get(MultitenantConstants.SUPER_TENANT_ID);

			if (synapseEnvService != null) {
				createStatisticsStore(synapseEnvService);
				StatisticNotifier statisticNotifier = notifierHashMap.get(MultitenantConstants.SUPER_TENANT_ID);
				MediationStatisticsService service =
						new MediationStatisticsServiceImpl(statisticNotifier, MultitenantConstants.SUPER_TENANT_ID,
						                                   synapseEnvService.getConfigurationContext());

				services.put(MultitenantConstants.SUPER_TENANT_ID, service);

				compCtx.getBundleContext().registerService(MediationStatisticsService.class.getName(), service, null);
				initialized = true;
			} else {
				log.error("Couldn't initialize Mediation Statistics");
			}
		} catch (Throwable e) {
			log.fatal("Error while initializing Mediation Statistics : ", e);
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
		StatisticNotifier statisticNotifier = new StatisticNotifier();
		StatisticCollectingThread reporterThread = new StatisticCollectingThread(synEnvService, statisticNotifier);
		reporterThread.setName("mediation-stat-collector-new-" + tenantId);

		//TODO Do we need this
		//Set a custom interval value if required
		//TODO Engage the persisting stat observer if required OR Engage custom observer implementations (user
		// written extensions)

		reporterThread.start();
		if (log.isDebugEnabled()) {
			log.debug("Registering the new mediation statistics service");
		}
		reporterThreads.put(tenantId, reporterThread);
		notifierHashMap.put(tenantId, statisticNotifier);
	}

	protected void deactivate(ComponentContext compCtx) throws Exception {
		Set<Map.Entry<Integer, StatisticCollectingThread>> threadEntries = reporterThreads.entrySet();
		for (Map.Entry<Integer, StatisticCollectingThread> threadEntry : threadEntries) {
			StatisticCollectingThread reporterThread = threadEntry.getValue();
			if (reporterThread != null && reporterThread.isAlive()) {
				reporterThread.shutdown();
				reporterThread.interrupt(); // This should wake up the thread if it is asleep

				// Wait for the reporting thread to gracefully terminate
				// Observers should not be disengaged before this thread halts
				// Otherwise some of the collected data may not be sent to the observers
				while (reporterThread.isAlive()) {
					if (log.isDebugEnabled()) {
						log.debug("Waiting for the statistics reporter thread to terminate");
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
			log.debug("SynapseEnvironmentService bound to the mediation statistics initialization");
		}

		synapseEnvServices.put(synEnvService.getTenantId(), synEnvService);
	}

	protected void unsetSynapseEnvironmentService(SynapseEnvironmentService synEnvService) {
		if (log.isDebugEnabled()) {
			log.debug("SynapseEnvironmentService unbound from the mediation statistics collector");
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
				StatisticNotifier store = notifierHashMap.get(tenantId);
				if (store != null) {
					MediationStatisticsService service =
							new MediationStatisticsServiceImpl(store, registrationsService.getTenantId(),
							                                   registrationsService.getConfigurationContext());

					services.put(registrationsService.getTenantId(), service);

					compCtx.getBundleContext()
					       .registerService(MediationStatisticsService.class.getName(), service, null);
				} else {
					log.warn("Couldn't find the mediation statistics store for tenant id: " + tenantId);
				}
			}
		} catch (Throwable t) {
			log.fatal("Error occurred at the osgi service method", t);
		}
	}

	protected void unsetSynapseRegistrationsService(SynapseRegistrationsService registrationsService) {
		try {
			int tenantId = registrationsService.getTenantId();
			StatisticCollectingThread reporterThread = reporterThreads.get(tenantId);
			if (reporterThread != null && reporterThread.isAlive()) {
				reporterThread.shutdown();
				reporterThread.interrupt(); // This should wake up the thread if it is asleep

				// Wait for the reporting thread to gracefully terminate
				// Observers should not be disengaged before this thread halts
				// Otherwise some of the collected data may not be sent to the observers
				while (reporterThread.isAlive()) {
					if (log.isDebugEnabled()) {
						log.debug("Waiting for the statistics reporter thread to terminate");
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
