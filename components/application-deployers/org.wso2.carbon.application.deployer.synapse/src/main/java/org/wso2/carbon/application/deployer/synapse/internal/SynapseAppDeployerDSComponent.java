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
package org.wso2.carbon.application.deployer.synapse.internal;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.Feature;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.application.deployer.synapse.SynapseAppDeployer;
import org.wso2.carbon.application.deployer.synapse.SynapseAppDeployerConstants;
import org.wso2.carbon.application.deployer.synapse.service.SynapseAppDeployerService;
import org.wso2.carbon.application.deployer.synapse.service.SynapseAppDeployerServiceImpl;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @scr.component name="application.deployer.synapse" immediate="true"
 * @scr.reference name="synapse.env.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..n" policy="dynamic" bind="setSynapseEnvironmentService"
 * unbind="unsetSynapseEnvironmentService"
 */
public class SynapseAppDeployerDSComponent implements ServiceListener {

    private static Log log = LogFactory.getLog(SynapseAppDeployerDSComponent.class);

    private static Map<String, List<Feature>> requiredFeatures;

    private static ServiceRegistration appHandlerRegistration;

    private static BundleContext bndCtx;

    private List<String> requiredServices = new ArrayList<String>();

    private Timer pendingServicesObservationTimer = new Timer();

    protected void activate(ComponentContext ctxt) {
        try {
            // Register synapse deployer as an OSGi service
            SynapseAppDeployer synapseDeployer = new SynapseAppDeployer();

            bndCtx = ctxt.getBundleContext();

            appHandlerRegistration = bndCtx.registerService(
                    AppDeploymentHandler.class.getName(), synapseDeployer, null);

            URL reqFeaturesResource = ctxt.getBundleContext().getBundle()
                    .getResource(AppDeployerConstants.REQ_FEATURES_XML);
            if (reqFeaturesResource != null) {
                InputStream xmlStream = reqFeaturesResource.openStream();
                requiredFeatures = AppDeployerUtils
                        .readRequiredFeaturs(new StAXOMBuilder(xmlStream).getDocumentElement());
            }

            populateRequiredServices();

            if (requiredServices.isEmpty()) {
                completeInitialization();
            } else {

                StringBuffer ldapFilter = new StringBuffer("(|");
                for (String service : requiredServices) {
                    ldapFilter.append("(").append(Constants.OBJECTCLASS).append("=").append(service).append(")");
                }
                ldapFilter.append(")");

                bndCtx.addServiceListener(this, ldapFilter.toString());
                ServiceReference[] serviceReferences =
                        bndCtx.getServiceReferences((String) null, ldapFilter.toString());
                if (serviceReferences != null) {
                    for (ServiceReference reference : serviceReferences) {
                        String service = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
                        requiredServices.remove(service);
                        if (log.isDebugEnabled()) {
                            log.debug("Removed pending service " + service);
                        }
                    }
                }
                if (requiredServices.isEmpty()) {
                    completeInitialization();
                } else {
                    schedulePendingServicesObservationTimer();
                }
            }
        } catch (Throwable e) {
            log.error("Failed to activate Synapse Application Deployer", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        // Unregister the OSGi service
        if (appHandlerRegistration != null) {
            appHandlerRegistration.unregister();
        }
    }

    /**
     * Here we receive an event about the creation of a SynapseEnvironment. If this is
     * SuperTenant we have to wait until all the other constraints are met and actual
     * initialization is done in the activate method. Otherwise we have to do the activation here.
     *
     * @param synapseEnvironmentService SynapseEnvironmentService which contains information
     *                                  about the new Synapse Instance
     */
    protected void setSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {

        DataHolder.getInstance().addSynapseEnvironmentService(
                synapseEnvironmentService.getTenantId(),
                synapseEnvironmentService);
    }

    /**
     * Here we receive an event about Destroying a SynapseEnvironment. This can be the super tenant
     * destruction or a tenant destruction.
     *
     * @param synapseEnvironmentService synapseEnvironment
     */
    protected void unsetSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        DataHolder.getInstance().removeSynapseEnvironmentService(
                synapseEnvironmentService.getTenantId());
    }


    public static Map<String, List<Feature>> getRequiredFeatures() {
        return requiredFeatures;
    }


    public void serviceChanged(ServiceEvent serviceEvent) {
        if (serviceEvent.getType() == ServiceEvent.REGISTERED) {
            String service =
                    ((String[]) serviceEvent.getServiceReference().getProperty(Constants.OBJECTCLASS))[0];
            requiredServices.remove(service);
            if (log.isDebugEnabled()) {
                log.debug("Removed pending service " + service);
            }
            if (requiredServices.isEmpty()) {
                completeInitialization();
            }
        }
    }

    private void populateRequiredServices() {
        Bundle[] bundles = bndCtx.getBundles();
        for (Bundle bundle : bundles) {
            String requiredServiceList =
                    (String) bundle.getHeaders().
                            get(SynapseAppDeployerConstants.SYNAPSE_DEPLOYER_REQUIRED_SERVICES);
            if (requiredServiceList != null) {
                String[] values = requiredServiceList.split(",");
                for (String value : values) {
                    requiredServices.add(value);
                }
            }
        }
    }

    private void schedulePendingServicesObservationTimer() {
        pendingServicesObservationTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!requiredServices.isEmpty()) {
                    StringBuffer services = new StringBuffer();
                    for (String service : requiredServices) {
                        services.append(service).append(",");
                    }
                    log.warn("Waiting for required OSGi services: " + services.toString());
                }
            }
        }, 60000, 60000);
    }

    private void completeInitialization() {
        bndCtx.registerService(SynapseAppDeployerService.class.getName(),
                                                       new SynapseAppDeployerServiceImpl(), null);
        if (log.isDebugEnabled()) {
            log.debug("Synapse Application deployer activated");
        }
    }

}
