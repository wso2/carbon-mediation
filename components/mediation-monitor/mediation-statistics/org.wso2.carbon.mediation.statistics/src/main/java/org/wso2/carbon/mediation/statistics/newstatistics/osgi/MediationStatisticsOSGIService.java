package org.wso2.carbon.mediation.statistics.newstatistics.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.mediation.statistics.newstatistics.StatisticCollectingThread;
import org.wso2.carbon.mediation.statistics.newstatistics.StatisticNotifier;
import org.wso2.carbon.mediation.statistics.newstatistics.service.MediationStatisticsServiceImpl;
import org.wso2.carbon.mediation.statistics.newstatistics.service.MediationStatisticsService;
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

///**
// * @scr.component name="mediation.statistics" immediate="true"
// * @scr.reference name="synapse.env.service"
// * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
// * cardinality="1..n" policy="dynamic"
// * bind="setSynapseEnvironmentService" unbind="unsetSynapseEnvironmentService"
// * @scr.reference name="registry.service"
// * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
// * policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
// * @scr.reference name="synapse.registrations.service"
// * interface="org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService"
// * cardinality="1..n" policy="dynamic" bind="setSynapseRegistrationsService"
// * unbind="unsetSynapseRegistrationsService"
// */
public class MediationStatisticsOSGIService {

	private static final Log log = LogFactory.getLog(MediationStatisticsOSGIService.class);

	private Map<Integer, StatisticNotifier> notifierHashMap = new HashMap<Integer, StatisticNotifier>();

	private Map<Integer, StatisticCollectingThread> reporterThreads = new HashMap<Integer, StatisticCollectingThread>();

	private Map<Integer, SynapseEnvironmentService> synapseEnvServices =
			new HashMap<Integer, SynapseEnvironmentService>();

	private boolean initialized = false;

	private Map<Integer, MediationStatisticsService> services = new HashMap<Integer, MediationStatisticsService>();

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

				compCtx.getBundleContext().registerService(
						org.wso2.carbon.mediation.statistics.newstatistics.service.MediationStatisticsService.class
								.getName(), service, null);
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

		//Ignoring statistics report observers registration if {StatisticsReporterDisabled =true}
		//		ServerConfiguration config = ServerConfiguration.getInstance();
		//		String confstatisticsReporterDisabled = config.getFirstProperty("StatisticsReporterDisabled");
		//		if (!"".equals(confstatisticsReporterDisabled)) {
		//			boolean disabled = new Boolean(confstatisticsReporterDisabled);
		//			if (disabled) {
		//				return;
		//			}
		//
		//		}

		int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

		StatisticNotifier statisticNotifier = new StatisticNotifier();
		//This is where all the proxies and endpoints takes the store reference
		//ConfigurationContext cfgCtx = synEnvService.getConfigurationContext();
		//cfgCtx.setProperty(StatisticsConstants.STAT_PROPERTY, mediationStatStore);

		StatisticCollectingThread reporterThread = new StatisticCollectingThread(synEnvService, statisticNotifier);
		reporterThread.setName("mediation-stat-collector-new-" + tenantId);

		// Set a custom interval value if required
		//		ServerConfiguration serverConf = ServerConfiguration.getInstance();
		//		String interval = serverConf.getFirstProperty(StatisticsConstants.STAT_REPORTING_INTERVAL);
		//		if (interval != null) {
		//			reporterThread.setDelay(Long.parseLong(interval));
		//		}
		//
		//		String tracing = serverConf.getFirstProperty(StatisticsConstants.STAT_TRACING);
		//		if ("enabled".equals(tracing)) {
		//			reporterThread.setTracingEnabled(true);
		//		}

		//		// Engage the persisting stat observer if required
		//		String persistence = serverConf.getFirstProperty(StatisticsConstants.STAT_PERSISTENCE);
		//		if ("enabled".equals(persistence)) {
		//			String root = serverConf.getFirstProperty(StatisticsConstants.STAT_PERSISTENCE_ROOT);
		//			if (root != null) {
		//				log.info("Enabling mediation statistics persistence. Statistics will be " +
		//				         "stored at: " + root);
		//				mediationStatStore.registerObserver(new PersistingStatisticsObserver(root));
		//			} else {
		//				log.warn(StatisticsConstants.STAT_PERSISTENCE_ROOT + " parameter has not " +
		//				         "been specified in the server configuration to activate " +
		//				         "statistics persistence");
		//			}
		//		}

		//		// Engage custom observer implementations (user written extensions)
		//		String observers = serverConf.getFirstProperty(StatisticsConstants.STAT_OBSERVERS);
		//		if (observers != null && !"".equals(observers)) {
		//			String[] classNames = observers.split(",");
		//			for (String className : classNames) {
		//				try {
		//					Class clazz = this.getClass().getClassLoader().loadClass(className.trim());
		//					MediationStatisticsObserver o = (MediationStatisticsObserver) clazz.newInstance();
		//					mediationStatStore.registerObserver(o);
		//					if (o instanceof TenantInformation) {
		//						TenantInformation tenantInformation = (TenantInformation) o;
		//						tenantInformation.setTenantId(synEnvService.getTenantId());
		//					}
		//				} catch (Exception e) {
		//					log.error("Error while initializing the mediation statistics " +
		//					          "observer : " + className, e);
		//				}
		//			}
		//		}

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
		//		Set<Map.Entry<Integer, MediationStatisticsStore>> storeEntries = notifierHashMap.entrySet();
		//		for (Map.Entry<Integer, MediationStatisticsStore> storeEntry : storeEntries) {
		//			// Now we can disengage the observers
		//			//storeEntry.getValue().unregisterObservers();
		//		}
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
	//
	//	protected void setRegistryService(RegistryService registryService) {
	//		if (log.isDebugEnabled()) {
	//			log.debug("RegistryService bound to the mediation statistics initialization");
	//		}
	//		ServiceReferenceHolder.getInstance().setRegistrySvc(registryService);
	//	}
	//
	//	protected void unsetRegistryService(RegistryService registryService) {
	//		if (log.isDebugEnabled()) {
	//			log.debug("RegistryService unbound from the mediation statistics collector");
	//		}
	//		ServiceReferenceHolder.getInstance().setRegistrySvc(null);
	//	}

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

					compCtx.getBundleContext().registerService(
							org.wso2.carbon.mediation.statistics.newstatistics.service.MediationStatisticsService.class
									.getName(), service, null);
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

			//			MediationStatisticsStore store = notifierHashMap.get(tenantId);
			//			if (store != null) {
			//				//store.unregisterObservers();
			//			}
		} catch (Throwable t) {
			log.error("Fatal error occured at the osgi service method", t);
		}
	}
}
