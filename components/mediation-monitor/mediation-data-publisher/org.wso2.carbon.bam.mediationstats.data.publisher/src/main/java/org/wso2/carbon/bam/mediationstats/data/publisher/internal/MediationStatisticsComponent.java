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
package org.wso2.carbon.bam.mediationstats.data.publisher.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.mediationstats.data.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.bam.mediationstats.data.publisher.observer.BAMMediationStatisticsObserver;
import org.wso2.carbon.bam.mediationstats.data.publisher.services.BAMMediationStatsPublisherAdmin;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.CommonConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.PublisherUtils;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.statistics.MediationStatisticsStore;
import org.wso2.carbon.mediation.statistics.services.MediationStatisticsService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @scr.component name="org.wso2.carbon.bam.mediationstats.data.publisher" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration" interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic" bind="setServerConfiguration"
 * unbind="unsetServerConfiguration"
 * @scr.reference name="org.wso2.carbon.registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="synapse.env.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..1" policy="dynamic" bind="setSynapseEnvironmentService"
 * unbind="unsetSynapseEnvironmentService"
 * @scr.reference name="mediation.statistics"
 * interface="org.wso2.carbon.mediation.statistics.services.MediationStatisticsService"
 * cardinality="1..n" policy="dynamic" bind="setMediationStatisticsService"
 * unbind="unsetMediationStatisticsService"
 */
public class MediationStatisticsComponent {

    private static final Log log = LogFactory.getLog(MediationStatisticsComponent.class);

    private static boolean StatisticReporterDisabled;

    private boolean activated = false;
    private ServiceRegistration statAdminServiceRegistration;
    private MediationStatisticsService mediationStatisticsService;
    private ConfigurationContext configContext;
    private ServerConfigurationService serverConfigurationService;

    protected void activate(ComponentContext ctxt) {

        checkPublishingEnabled();

        PublisherUtils.setStatisticsReporterDisable(StatisticReporterDisabled);

        if(!StatisticReporterDisabled){
            BAMMediationStatsPublisherAdmin bamMediationStatsPublisherAdmin = new BAMMediationStatsPublisherAdmin();

            PublisherUtils.setMediationStatPublisherAdmin(bamMediationStatsPublisherAdmin);

            MediationStatisticsStore mediationStatisticsStore = mediationStatisticsService.getStatisticsStore();
            int tenantId = mediationStatisticsService.getTenantId();

            if(mediationStatisticsStore!= null){
                BAMMediationStatisticsObserver observer = new BAMMediationStatisticsObserver();
                mediationStatisticsStore.registerObserver(observer);
                observer.setTenantId(tenantId);
                // 'MediationStat service' will be deployed per tenant (cardinality="1..n")
                observer.setTenantAxisConfiguration(mediationStatisticsService.getConfigurationContext().
                        getAxisConfiguration());
                log.debug("Registering  Observer for tenant: " + mediationStatisticsService.getTenantId());
            } else {
                log.error("Can't register an observer for mediationStatisticsStore. " +
                                    "If you have disabled StatisticsReporter, please enable it in the Carbon.xml");
            }

            //Load previously saved configurations
            new RegistryPersistenceManager().load(tenantId);


            activated = true;
            log.debug("BAM MediationStatisticsComponent activate");
            if (log.isDebugEnabled()) {
                log.debug("BAM Mediation statistics data publisher bundle is activated");
            }
        }else{
            log.info("Can't register an observer for mediationStatisticsStore. " +
                                    "If you have disabled StatisticsReporter, please enable it in the Carbon.xml");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        // unregistered BAMMediationStatsPublisherAdmin service from the OSGi Service Register.
        statAdminServiceRegistration.unregister();
        if (log.isDebugEnabled()) {
            log.debug("BAM service statistics data publisher bundle is deactivated");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        this.configContext = contextService.getServerConfigContext();
        PublisherUtils.setConfigurationContextService(contextService);

    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        PublisherUtils.setConfigurationContextService(null);
        this.configContext = null;
    }

    protected void setRegistryService(RegistryService registryService) {
        try {
            RegistryPersistenceManager.setRegistryService(registryService);
        } catch (Exception e) {
            log.error("Cannot retrieve System Registry", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        RegistryPersistenceManager.setRegistryService(null);
    }

    protected void setSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        PublisherUtils.setSynapseEnvironmentService(synapseEnvironmentService);
    }

    protected void unsetSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        PublisherUtils.setSynapseEnvironmentService(null);
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfigService) {
        this.serverConfigurationService = serverConfigService;
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfigService) {
        this.serverConfigurationService = null;
    }

    protected void setMediationStatisticsService(
            MediationStatisticsService mediationStatisticsService) {
        if (log.isDebugEnabled()) {
            log.debug("Mediation statistics service bound to the BAM mediation statistics component");
        }
        if (activated && mediationStatisticsService != null) {
            MediationStatisticsStore mediationStatisticsStore =
                    mediationStatisticsService.getStatisticsStore();
            BAMMediationStatisticsObserver observer = new BAMMediationStatisticsObserver();
            mediationStatisticsStore.registerObserver(observer);
            observer.setTenantId(mediationStatisticsService.getTenantId());
            observer.setTenantAxisConfiguration(
                    mediationStatisticsService.getConfigurationContext().getAxisConfiguration());
            log.info("Registering BamMediationStatistics Observer for tenant: " +
                     mediationStatisticsService.getTenantId());

        } else {
            this.mediationStatisticsService = mediationStatisticsService;
        }
    }

    protected void unsetMediationStatisticsService(MediationStatisticsService statService) {
        if (log.isDebugEnabled()) {
            log.debug("Mediation statistics service unbound from the BAM mediation statistics component");
        }
        // mediationStatisticsService = null;
    }

    private void checkPublishingEnabled() {

                String carbonStatisticsReporter =
                        /*bamConfig.getFirstChildWithName(new QName());*/
                        CarbonUtils.getServerConfiguration().getFirstProperty(CommonConstants.CARBON_STATISTICS_REPORTER);
                if (null != carbonStatisticsReporter) {
                    if (carbonStatisticsReporter.trim()
                            .equalsIgnoreCase(CommonConstants.CARBON_STATISTIC_REPORTER_DISABLED)) {
                        StatisticReporterDisabled = false;
                    } else {
                        log.info("Statistic Reporter is Disabled");
                        StatisticReporterDisabled = true;
                    }
                } else {
                    StatisticReporterDisabled = true;
                }
        }
}
