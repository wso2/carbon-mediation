/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.proxyadmin.observer;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PolicyUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.Rampart;
import org.apache.sandesha2.SandeshaModule;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.util.PolicyInfo;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.persistence.ServicePersistenceManager;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.proxyadmin.ProxyAdminException;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.security.SecurityConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy observer observe the Proxy service in runtime and update the synapse Configuration
 */
public class ProxyObserver implements AxisObserver {

    private SynapseEnvironmentService synapseEnvironmentService;

    private Registry configSystemRegistry;

    private static final Log log = LogFactory.getLog(ProxyObserver.class);

    private static final String[] DEFAULT_MODULES = new String[] {
        "addressing", "ServerAdminModule", "wso2statistics", "POXSecurityModule", "pagination"
    };

    /**
     * Constructs a new ProxyObserver using the given SynapseEnvironmentService. This
     * constructor ensures that all the created proxy observer instances have a
     * non-null SynapseConfiguration. Attempting to create a proxy observer with
     * a null SynapseConfiguration would result in an exception.
     * TODO: because of a bug in current equinox framework version, we no longer get events when new
     * TODO: synapse environment created. So changed proxy observer config to take reference of
     * TODO: synapseEnvService and obtaing synapse config from it See : ESBJAVA-1029
     * @param synapseEnvironmentService the Synapse
     * @param configRegistry the registry
     * @throws ProxyAdminException if the SynapseConfiguration is null
     */
    public ProxyObserver(SynapseEnvironmentService synapseEnvironmentService, Registry configRegistry)
            throws ProxyAdminException {
        if (synapseEnvironmentService.getSynapseEnvironment().getSynapseConfiguration() == null) {
            String msg = "Unable to initialize a ProxyObserver with a null SynapseConfiguration";
            log.error(msg);
            throw new ProxyAdminException(msg);
        }
        this.synapseEnvironmentService = synapseEnvironmentService;
        this.configSystemRegistry = configRegistry;
	}

    public void init(AxisConfiguration axisConfiguration) {
    }

    public void serviceUpdate(AxisEvent event, AxisService axisService) {

        Parameter serviceTypeParam = axisService.getParameter(
                SynapseConstants.SERVICE_TYPE_PARAM_NAME);
        if (serviceTypeParam == null || !SynapseConstants.PROXY_SERVICE_TYPE.equals(
                serviceTypeParam.getValue().toString())) {
            // We are only interested about the proxy services
            return;
        }

        if (getSynapseConfiguration() == null) {
            // Somehow the underlying SynapseConfiguration has become null after
            // creating the proxy observer. May be the user is manipulating bundles
            // through the OSGi console or the system is shutting down.
            if (log.isDebugEnabled()) {
                log.debug("SynapseConfiguration in ProxyObserver is null. The service" +
                        " update event will not be processed further.");
            }
            return;
        }

        if (CarbonConstants.POLICY_ADDED == event.getEventType()) {
            updateProxyServicePolicies(axisService, getSynapseConfiguration());
        }

        if (CarbonConstants.AxisEvent.TRANSPORT_BINDING_ADDED == event.getEventType()) {
            ProxyService proxy = getSynapseConfiguration().getProxyService(axisService.getName());
            if (proxy != null && proxy.getTransports() != null) {
                List<String> transports = axisService.getExposedTransports();
                for (String trp : transports) {
                    if (!proxy.getTransports().contains(trp)) {
                        proxy.getTransports().add(trp);
                    }
                }
            }
        }

        if (AxisEvent.SERVICE_DEPLOY == event.getEventType()) {
            ProxyService proxySvc = getSynapseConfiguration().getProxyService(axisService.getName());
            if (proxySvc != null) {
                try {
                    ServicePersistenceManager spm = new ServicePersistenceManager(
                            getSynapseConfiguration().getAxisConfiguration());
                    for (Parameter p : axisService.getParameters()) {
                        spm.updateServiceParameter(axisService, p);
                    }
                } catch (Exception e) {

                }
            }
        }

        if (AxisEvent.SERVICE_REMOVE == event.getEventType()) {

            Parameter keepServiceHistoryParam = axisService.getParameter(
                    CarbonConstants.KEEP_SERVICE_HISTORY_PARAM);
            Parameter originator = axisService.getParameter("originator");
            boolean keepHistory = keepServiceHistoryParam != null
                    && JavaUtils.isTrue(keepServiceHistoryParam.getValue());
            //Only remove proxy config from storage if service remove request coming from
            // Service listing UI. This check will prevent proxy xml deleting from file system
            //during hot update
            if (originator != null && "ServiceAdmin".equals(originator.getValue().toString())) {
                if (!keepHistory) {
                    ProxyService proxySvc = getSynapseConfiguration().getProxyService(axisService.getName());
                    if (proxySvc != null) {
                        getSynapseConfiguration().removeProxyService(axisService.getName());
                        MediationPersistenceManager pm = getMediationPersistenceManager();
                        pm.deleteItem(proxySvc.getName(), proxySvc.getFileName(),
                                ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);
                        log.info("Deleted the proxy service : " + proxySvc.getName());

                    } else if (log.isDebugEnabled()) {
                        log.debug("Proxy Service representing the service " + axisService.getName()
                                + " of type proxy is not found in the SynapseConfiguration");
                    }
                } else {
                    try {
                        ServicePersistenceManager spm = new ServicePersistenceManager(
                                getSynapseConfiguration().getAxisConfiguration());
                        for (Parameter p : axisService.getParameters()) {
                            spm.removeServiceParameter(axisService, p);
                        }
                    } catch (Exception e) {
                        log.warn("Error while removing service parameter information from registry", e);
                    }
                }
            }
        }
    }

    public void moduleUpdate(AxisEvent event, AxisModule axisModule) {

        if (event != null && (event.getAxisDescription() instanceof AxisService ||
                event.getAxisDescription() instanceof AxisOperation)) {

            AxisService axisService;
            if (event.getAxisDescription() instanceof AxisService) {
                axisService = (AxisService) event.getAxisDescription();
            } else {
                axisService = ((AxisOperation) event.getAxisDescription()).getAxisService();
            }

            Parameter serviceTypeParam = axisService.getParameter(
                    SynapseConstants.SERVICE_TYPE_PARAM_NAME);
            if (serviceTypeParam == null || !SynapseConstants.PROXY_SERVICE_TYPE.equals(
                    serviceTypeParam.getValue().toString())) {
                // We are only interested about the proxy services
                return;
            }

            if (getSynapseConfiguration() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("SynapseConfiguration in ProxyObserver is null. The module" +
                            " update event will not be processed further.");
                }
                return;
            }

            if (CarbonConstants.POLICY_ADDED == event.getEventType()) {
                updateProxyServicePolicies(axisService, getSynapseConfiguration());
            }
            if (AxisEvent.MODULE_ENGAGED == event.getEventType() && !isDefaultModule(axisModule)) {
                onEngageModule(axisService, axisModule, getSynapseConfiguration());
                updateProxyServicePolicies(axisService, getSynapseConfiguration());
            }
            if (AxisEvent.MODULE_DISENGAGED == event.getEventType() && !isDefaultModule(axisModule)) {
                onDisEngageModule(axisService, axisModule, getSynapseConfiguration());
                updateProxyServicePolicies(axisService, getSynapseConfiguration());
            }
        }
    }

    private boolean isDefaultModule(AxisModule module) {
        String name = module.getName();
        for (String defaultModule : DEFAULT_MODULES) {
            if (defaultModule.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void serviceGroupUpdate(AxisEvent event, AxisServiceGroup axisServiceGroup) {}
    public void addParameter(Parameter parameter) throws AxisFault {}
    public void removeParameter(Parameter parameter) throws AxisFault {}
    public void deserializeParameters(OMElement omElement) throws AxisFault {}
    public Parameter getParameter(String s) { return null; }
    public ArrayList<Parameter> getParameters() { return null; }
    public boolean isParameterLocked(String s) { return false; }

    private void onEngageModule(AxisService service, AxisModule module,
                                SynapseConfiguration config) {
        if (config.getProxyService(service.getName()) != null) {
            ProxyService proxy = config.getProxyService(service.getName());
            if (module.getModule() instanceof Rampart) {
                proxy.setWsSecEnabled(true);
            } else if (module.getModule() instanceof SandeshaModule) {
                proxy.setWsRMEnabled(true);
            }
        }
    }

    private void onDisEngageModule(AxisService service, AxisModule module,
                                   SynapseConfiguration config) {
        if (config.getProxyService(service.getName()) != null) {
            ProxyService proxy = config.getProxyService(service.getName());
            if (module.getModule() instanceof Rampart) {
                proxy.setWsSecEnabled(false);
                proxy.getParameterMap().remove(SecurityConstants.SECURITY_POLICY_PATH);
                proxy.getParameterMap().remove("disableREST");
            } else if (module.getModule() instanceof SandeshaModule) {
                proxy.setWsRMEnabled(false);
            }
        }
    }

    private void updateProxyServicePolicies(AxisService axisService, SynapseConfiguration config) {

        if (config.getProxyService(axisService.getName()) == null) {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't retrieve the proxy service with name " + axisService.getName() +
                        " to update policies");
            }
            return;
        }

        ProxyService proxyService = config.getProxyService(axisService.getName());
        try {
            Registry registry = this.configSystemRegistry;
            String servicePath = RegistryResources.ROOT + "axis2" +
                    RegistryConstants.PATH_SEPARATOR + "service-groups" +
                    RegistryConstants.PATH_SEPARATOR +
                    axisService.getAxisServiceGroup().getServiceGroupName() +
                    RegistryConstants.PATH_SEPARATOR + "services" +
                    RegistryConstants.PATH_SEPARATOR + axisService.getName();

            String servicePoliciesPath = servicePath
                    + RegistryConstants.PATH_SEPARATOR + "policies";

            List<PolicyInfo> remainingPolicies = new ArrayList<PolicyInfo>();
            for (PolicyInfo info : proxyService.getPolicies()) {
                if (!info.getPolicyKey().startsWith("conf:" + servicePoliciesPath)) {
                    remainingPolicies.add(info);
                }
            }
            proxyService.setPolicies(remainingPolicies);
            
            if (registry.resourceExists(servicePoliciesPath)) {
                // there are service level policies
                Resource servicePoliciesResource = registry.get(servicePoliciesPath);
                if (servicePoliciesResource instanceof Collection) {
                    Collection servicePoliciesCollection = (Collection) servicePoliciesResource;
                    for (String servicePolicyResourcePath :
                            servicePoliciesCollection.getChildren()) {
                        PolicyInfo pi = handlePolicy(config,
                                proxyService, servicePolicyResourcePath, (UserRegistry) registry);
                        if (pi != null) {
                            pi.setPolicyKey("conf:" + pi.getPolicyKey());
                            proxyService.addPolicyInfo(pi);
                        }
                    }
                }
            }

            // Update exposed transports
            if (axisService.getExposedTransports() != null &&
                    !axisService.getExposedTransports().isEmpty()) {
                proxyService.setTransports(
                        new ArrayList<String>(axisService.getExposedTransports()));
            } else {
                proxyService.setTransports(new ArrayList());
            }

            persistChanges(axisService.getName(), axisService.getAxisConfiguration());
        } catch (RegistryException e) {
            log.error("Error checking the policies from the registry", e);
        }
    }

    private PolicyInfo handlePolicy(SynapseConfiguration config, ProxyService proxy,
                                    String policyPath, UserRegistry registry)
            throws RegistryException {

        if (!registry.resourceExists(policyPath)) {
            return null;
        }

        Resource policyResource = registry.get(policyPath);
        byte[] content = (byte[]) policyResource.getContent();
        if (content == null || content.length == 0) {
            return null;
        }
        policyResource.discard();

        ByteArrayInputStream in = new ByteArrayInputStream(content);
        Policy policy = getPolicy(in);
        if (policy != null && !policy.isEmpty()) {
            for (PolicyInfo pi : proxy.getPolicies()) {
                if (pi.isServicePolicy()) {
                    config.getEntryDefinition(pi.getPolicyKey());
                    Policy proxyPolicy = PolicyEngine.getPolicy(SynapseConfigUtils.getStreamSource(
                            config.getEntry(pi.getPolicyKey())).getInputStream());
                    if (proxyPolicy.equal(policy.normalize(false))) {
                        return null;
                    }
                }
            }
            return new PolicyInfo(policyPath);
        }
        return null;
    }

    private Policy getPolicy(InputStream is) {
        BufferedInputStream inputStream = new BufferedInputStream(is);
        try {
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement elem = builder.getDocumentElement();
            return PolicyUtil.getPolicyFromOMElement(elem);
        } catch (XMLStreamException e) {
            return null;
        } finally {
            try { inputStream.close(); } catch (IOException ignored) {}
        }
    }

    private void persistChanges(String proxyName, AxisConfiguration axisConfiguration) {
        Parameter param = axisConfiguration.getParameter(ServiceBusConstants.SUSPEND_PERSISTENCE);
        if (param != null && Boolean.parseBoolean((String) param.getValue())) {
            return;
        }
        MediationPersistenceManager mpm = getMediationPersistenceManager();
        if (mpm != null) {
            mpm.saveItem(proxyName, ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);
        }
    }

    private MediationPersistenceManager getMediationPersistenceManager() {
        Parameter p = getSynapseConfiguration().getAxisConfiguration().getParameter(
                ServiceBusConstants.PERSISTENCE_MANAGER);
        if (p != null) {
            return (MediationPersistenceManager) p.getValue();
        }

        return null;
    }

    public void setSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {
        this.synapseEnvironmentService = synapseEnvironmentService;
    }

    private SynapseConfiguration getSynapseConfiguration() {
        return this.synapseEnvironmentService.getSynapseEnvironment().getSynapseConfiguration();
    }
}
