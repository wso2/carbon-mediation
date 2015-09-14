/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediation.throttle.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.xml.XmlPrimtiveAssertion;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.PersistenceException;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.core.persistence.file.ModuleFilePersistenceManager;
import org.wso2.carbon.core.persistence.file.ServiceGroupFilePersistenceManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.mediation.throttle.InternalData;
import org.wso2.carbon.mediation.throttle.ThrottleComponentConstants;
import org.wso2.carbon.mediation.throttle.ThrottleComponentException;
import org.wso2.carbon.mediation.throttle.ThrottlePolicy;
import org.apache.synapse.commons.throttle.core.ThrottleConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This is the class which provides all the main functionalities provided by
 * the throttle component. This includes engaging/disengaging throttling and
 * providing the current policy configuration.
 */

public class ThrottleConfigAdminService extends AbstractAdmin {

    private ServiceGroupFilePersistenceManager sfpm;
    private ModuleFilePersistenceManager mfpm;
    private PersistenceFactory pf;

    private Registry registry;

    protected AxisConfiguration axisConfig = null;

    private Policy policyToUpdate = null;

    private static final String ADMIN_SERICE_PARAM_NAME = "adminService";

    private static final String HIDDEN_SERVICE_PARAM_NAME = "hiddenService";

    private static final String GLOBALLY_ENGAGED_PARAM_NAME = "globallyEngaged";

    private static final String GLOBALLY_ENGAGED_CUSTOM = "globallyEngagedCustom";

    private static final Log log = LogFactory.getLog(ThrottleConfigAdminService.class);

    private String wSO2ServiceThrottlingPolicyId = "WSO2ServiceThrottlingPolicy";
    private String wSO2OperationThrottlingPolicyId = "WSO2OperationThrottlingPolicy";
    private String wSO2ModuleThrottlingPolicyId = "WSO2ModuleThrottlingPolicy";
    private String wSO2MediatorThrottlingPolicyId = "WSO2MediatorThrottlingPolicy";

    /**
     * Constructor. Retrieves the registry from the axis configuration
     */
    public ThrottleConfigAdminService() {
        try {
            this.axisConfig = getAxisConfig();
            pf = PersistenceFactory.getInstance(this.axisConfig);
            sfpm = pf.getServiceGroupFilePM();
            mfpm = pf.getModuleFilePM();

            this.registry = getConfigSystemRegistry();
        } catch (Exception e) {
            log.error("Can't initialize ThrottleAdminService.", e);
        }
    }


    /**
     * Engages Throttling for the given serviceName by generating the policy
     * according to the specified parameters
     *
     * @param serviceName - name of the serviceName to engage throttling
     * @param policy      - object containg policy configurations
     * @throws AxisFault                  - if an error is occured when accessing axisConfig or axisService
     * @throws ThrottleComponentException - throttle component specific errors
     */

    public void enableThrottling(String serviceName, ThrottlePolicy policy)
            throws AxisFault, ThrottleComponentException {
        if (log.isDebugEnabled()) {
            log.debug("Engaging throttling for the serviceName : " + serviceName);
        }

        //get the axis serviceName from the axis configuration instance
        AxisService axisService = this.getAxisService(serviceName);
        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();
        boolean isProxyService = PersistenceUtils.isProxyService(axisService);

        //get the throttle module from the current axis config
        AxisModule module = axisService.getAxisConfiguration().getModule(
                ThrottleComponentConstants.THROTTLE_MODULE);

        String serviceXPath = PersistenceUtils.getResourcePath(axisService);

        // persist
        try {
            boolean transactionStarted = sfpm.isTransactionStarted(serviceGroupId);
            if (!transactionStarted) {
                sfpm.beginTransaction(serviceGroupId);
            }
            boolean registryTransactionStarted = Transaction.isStarted();
            if (!registryTransactionStarted) {
                registry.beginTransaction();
            }

            try {
                // Check if an association exist between servicePath and moduleResourcePath.
                List associations = sfpm.getAll(serviceGroupId, serviceXPath +
                        "/" + Resources.ModuleProperties.MODULE_XML_TAG +
                        PersistenceUtils.getXPathAttrPredicate(
                                Resources.ModuleProperties.TYPE, Resources.Associations.ENGAGED_MODULES));
                boolean associationExist = false;
                String version = module.getVersion().toString();
                if (module.getVersion() == null) {
                    version = Resources.ModuleProperties.UNDEFINED;
                }
                for (Object node : associations) {
                    OMElement association = (OMElement) node;
                    if (association.getAttributeValue(new QName(Resources.NAME)).equals(module.getName()) &&
                            association.getAttributeValue(new QName(Resources.VERSION)).equals(version)) {
                        associationExist = true;
                        break;
                    }
                }

                //if throttling is not found, add a new association
                if (!associationExist) {
                    sfpm.put(serviceGroupId,
                            PersistenceUtils.createModule(module.getName(), version, Resources.Associations.ENGAGED_MODULES),
                            serviceXPath);
                }
            } catch (PersistenceException e) {
                log.error("Error occured in persisting throttling", e);
                throw new ThrottleComponentException("errorEngagingModuleAtRegistry");
            }

            XmlPrimtiveAssertion assertion = this.getThrottlePolicy(axisService
                    .getPolicySubject().getAttachedPolicyComponents());

            //build builtPolicy according to received parameters
            OMElement policyElement = this.buildPolicy(policy,
                    assertion, ThrottleComponentConstants.SERVICE_LEVEL);
            Policy builtPolicy = PolicyEngine.getPolicy(policyElement);

            //if we didn't find an already existing builtPolicy, attach a new one
            Policy policyToPersist = builtPolicy;
            if (assertion == null) {
                axisService.getPolicySubject().attachPolicy(builtPolicy);
            } else {
                axisService.getPolicySubject().updatePolicy(policyToUpdate);
                policyToPersist = policyToUpdate;
            }

            //persist the throttle builtPolicy
            try {
                //to registry
                if (isProxyService) {
                    String policyType = "" + PolicyInclude.AXIS_SERVICE_POLICY;
                    String registryServicePath = PersistenceUtils.getRegistryResourcePath(axisService);
                    pf.getServicePM().persistPolicyToRegistry(policyToPersist, policyType, registryServicePath);
                }

                //to file
                OMFactory omFactory = OMAbstractFactory.getOMFactory();
                OMElement policyWrapperElement = omFactory.createOMElement(Resources.POLICY, null);
                policyWrapperElement.addAttribute(Resources.ServiceProperties.POLICY_TYPE,
                        "" + PolicyInclude.AXIS_SERVICE_POLICY, null);

                OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
                idElement.setText("" + policyToPersist.getId());
                policyWrapperElement.addChild(idElement);

                OMElement policyElementToPersist = PersistenceUtils.createPolicyElement(policyToPersist);
                policyWrapperElement.addChild(policyElementToPersist);

                if (!sfpm.elementExists(serviceGroupId, serviceXPath + "/" + Resources.POLICIES)) {
                    sfpm.put(serviceGroupId,
                            omFactory.createOMElement(Resources.POLICIES, null), serviceXPath);
                } else {
                    //you must manually delete the existing policy before adding new one.
                    String pathToPolicy = serviceXPath + "/" + Resources.POLICIES +
                            "/" + Resources.POLICY +
                            PersistenceUtils.getXPathTextPredicate(
                                    Resources.ServiceProperties.POLICY_UUID, policyToPersist.getId());
                    if (sfpm.elementExists(serviceGroupId, pathToPolicy)) {
                        sfpm.delete(serviceGroupId, pathToPolicy);
                    }
                }
                sfpm.put(serviceGroupId, policyWrapperElement, serviceXPath +
                        "/" + Resources.POLICIES);

                if (!sfpm.elementExists(serviceGroupId, serviceXPath +
                        PersistenceUtils.getXPathTextPredicate(
                                Resources.ServiceProperties.POLICY_UUID, policyToPersist.getId()))) {
                    sfpm.put(serviceGroupId, idElement.cloneOMElement(), serviceXPath);
                }
                //            this.persistPoliciesToRegistry(policyToPersist, servicePath, servicePath, policyResource);
            } catch (Exception e) {
                log.error("Error occurred while persisting", e);
                sfpm.rollbackTransaction(serviceGroupId);
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException re) {
                    log.error(e.getMessage(), re);
                }
                throw new ThrottleComponentException("errorSavingPolicy");
            }

            if (!transactionStarted) {
                sfpm.commitTransaction(serviceGroupId);
            }
            if (!registryTransactionStarted) {
                registry.commitTransaction();
            }
        } catch (Exception e) {
            log.error("Error occurred while saving the builtPolicy in registry", e);
            sfpm.rollbackTransaction(serviceGroupId);
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                log.error(e.getMessage(), re);
            }
            throw new ThrottleComponentException("errorSavingPolicy");
        }

        //engage the module only if it is not already engaged
        axisService.engageModule(module);
    }

    /**
     * Engages throttling globally.
     *
     * @param policy - policy configuration to be used
     * @throws AxisFault                  - if error occured when dealing with axisConfig
     * @throws ThrottleComponentException - throttle component specific errors
     */

    public void globallyEngageThrottling(ThrottlePolicy policy) throws AxisFault,
            ThrottleComponentException {
        if (log.isDebugEnabled()) {
            log.debug("Globally engaging throttling");
        }

        //get the throttle module from the current axis config
        AxisModule module = this.axisConfig.getModule(ThrottleComponentConstants.THROTTLE_MODULE);
//        String resourcePath = getModuleResourcePath(module);
        String globalPath = PersistenceUtils.getResourcePath(module);

        try {
            mfpm.beginTransaction(module.getName());
            try {
                if (mfpm.elementExists(module.getName(), globalPath)) {
                    OMElement element = (OMElement) mfpm.get(module.getName(), globalPath);
                    if (!Boolean.parseBoolean(element
                            .getAttributeValue(new QName(GLOBALLY_ENGAGED_CUSTOM)))) {
                        element.addAttribute(GLOBALLY_ENGAGED_CUSTOM, Boolean.TRUE.toString(), null);
                        mfpm.setMetaFileModification(module.getName());
                    }
                } else {
                    OMFactory omFactory = OMAbstractFactory.getOMFactory();
                    OMElement moduleElement = omFactory.createOMElement(Resources.VERSION, null);
                    if (module.getVersion() != null) {
                        moduleElement.addAttribute(Resources.ModuleProperties.VERSION_ID, module.getVersion().toString(), null);
                    } else {
                        moduleElement.addAttribute(Resources.ModuleProperties.VERSION_ID, Resources.ModuleProperties.UNDEFINED, null);
                    }

                    moduleElement.addAttribute(GLOBALLY_ENGAGED_CUSTOM, Boolean.TRUE.toString(), null);
                    mfpm.put(module.getName(), moduleElement, Resources.ModuleProperties.ROOT_XPATH);
                }
            } catch (PersistenceException e) {
                log.error("Error occurred in globally engaging throttling at registry", e);
                throw new ThrottleComponentException("errorEngagingModuleAtRegistry");
            }

            XmlPrimtiveAssertion assertion = this.getThrottlePolicy(module
                    .getPolicySubject().getAttachedPolicyComponents());

            //build builtPolicy according to received parameters
            OMElement policyElement = this.buildPolicy(policy,
                    assertion, ThrottleComponentConstants.GLOBAL_LEVEL);
            Policy builtPolicy = PolicyEngine.getPolicy(policyElement);

            //if we didn't find an already existing builtPolicy, attach a new one
            Policy policyToPersist = builtPolicy;
            if (assertion == null) {
                module.getPolicySubject().attachPolicy(builtPolicy);
            } else {
                module.getPolicySubject().updatePolicy(policyToUpdate);
                policyToPersist = policyToUpdate;
            }

            //persist the throttle builtPolicy into registry
            try {
                //to file
                OMFactory omFactory = OMAbstractFactory.getOMFactory();
                OMElement policyWrapperElement = omFactory.createOMElement(Resources.POLICY, null);
                policyWrapperElement.addAttribute(Resources.ServiceProperties.POLICY_TYPE,
                        "" + PolicyInclude.AXIS_MODULE_POLICY, null);

                OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
                idElement.setText("" + policyToPersist.getId());
                policyWrapperElement.addChild(idElement);

                policyWrapperElement.addAttribute(Resources.VERSION, module.getVersion().toString(), null);

                OMElement policyElementToPersist = PersistenceUtils.createPolicyElement(policyToPersist);
                policyWrapperElement.addChild(policyElementToPersist);

                if (!mfpm.elementExists(module.getName(), globalPath + "/" + Resources.POLICIES)) {
                    mfpm.put(module.getName(),
                            omFactory.createOMElement(Resources.POLICIES, null), globalPath);
                } else {
                    //you must manually delete the existing policy before adding new one.
                    String pathToPolicy = globalPath + "/" + Resources.POLICIES +
                            "/" + Resources.POLICY +
                            PersistenceUtils.getXPathTextPredicate(
                                    Resources.ServiceProperties.POLICY_UUID, policyToPersist.getId());
                    if (mfpm.elementExists(module.getName(), pathToPolicy)) {
                        mfpm.delete(module.getName(), pathToPolicy);
                    }
                }
                mfpm.put(module.getName(), policyWrapperElement, globalPath +
                        "/" + Resources.POLICIES);
            } catch (Exception e) {
                log.error("Error occured while saving the builtPolicy in registry", e);
                throw new ThrottleComponentException("errorSavingPolicy");
            }
            mfpm.commitTransaction(module.getName());
            module.addParameter(new Parameter(GLOBALLY_ENGAGED_PARAM_NAME, Boolean.TRUE.toString()));

            //engage the module for every service which is not an admin service
            try {
                for (AxisService service : this.axisConfig.getServices().values()) {
                    String adminParamValue =
                            (String) service.getParent().getParameterValue(ADMIN_SERICE_PARAM_NAME);
                    String hiddenParamValue =
                            (String) service.getParent().getParameterValue(HIDDEN_SERVICE_PARAM_NAME);

                    //avoid admin and hidden services
                    if ((adminParamValue != null && adminParamValue.length() != 0 &&
                            Boolean.parseBoolean(adminParamValue.trim())) ||
                            (hiddenParamValue != null && hiddenParamValue.length() != 0 &&
                                    Boolean.parseBoolean(hiddenParamValue.trim())) ||
                            service.isClientSide()) {
                        continue;
                    }
                    this.enableThrottling(service.getName(), policy);
                }
            } catch (Exception e) {
                log.error("Error occurred in globally engaging throttlin at registry", e);
                mfpm.rollbackTransaction(module.getName());
                throw new ThrottleComponentException("errorEngagingModuleAtRegistry");
            }
        } catch (Exception e) {
            log.error("Error occurred in globally beginning/committing the transaction", e);
            mfpm.rollbackTransaction(module.getName());
            throw new ThrottleComponentException("errorSavingPolicy");
        }
    }

    /**
     * Engage throttling for the given operation
     *
     * @param policy        - throttle config
     * @param serviceName   - name of the service which contains the operation
     * @param operationName - operation name
     * @return - true if already engaged throttling at the service level, else false
     * @throws AxisFault                  - on axis error
     * @throws ThrottleComponentException - throttle specific error
     */
    public boolean engageThrottlingForOperation(ThrottlePolicy policy,
                                                String serviceName, String operationName)
            throws AxisFault, ThrottleComponentException {

        if (log.isDebugEnabled()) {
            log.debug("Engaging throttling for the operation : " + operationName
                    + ", in service :" + serviceName);
        }

        //get the axis service from the axis configuration instance
        AxisService axisService = this.getAxisService(serviceName);
        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();
        boolean isProxyService = PersistenceUtils.isProxyService(axisService);

        //get the throttle module from the current axis config
        AxisModule module = axisService.getAxisConfiguration().getModule(
                ThrottleComponentConstants.THROTTLE_MODULE);

        if (axisService.isEngaged(module)) {
            return true;
        }

        AxisOperation operation = axisService.getOperation(new QName(operationName));
        if (operation == null) {
            log.error("No operation found from the name " + operationName
                    + ", in service : " + serviceName);
            throw new ThrottleComponentException("noSuchOperation", new String[]{serviceName});
        }

        String serviceXPath = PersistenceUtils.getResourcePath(axisService);
        String operationXPath = PersistenceUtils.getResourcePath(operation);

        // persist
        try {
            boolean isTransactionStarted = sfpm.isTransactionStarted(serviceGroupId);
            if (!isTransactionStarted) {
                sfpm.beginTransaction(serviceGroupId);
            }

            // Check if an association exist between servicePath and moduleResourcePath.
            List associations = sfpm.getAll(serviceGroupId, operationXPath +
                    "/" + Resources.ModuleProperties.MODULE_XML_TAG +
                    PersistenceUtils.getXPathAttrPredicate(
                            Resources.ModuleProperties.TYPE, Resources.Associations.ENGAGED_MODULES));
            boolean associationExist = false;
            String version = module.getVersion().toString();
            if (module.getVersion() == null) {
                version = Resources.ModuleProperties.UNDEFINED;
            }
            for (Object node : associations) {
                OMElement association = (OMElement) node;
                if (association.getAttributeValue(new QName(Resources.NAME)).equals(module.getName()) &&
                        association.getAttributeValue(new QName(Resources.VERSION)).equals(version)) {
                    associationExist = true;
                    break;
                }
            }

            //if throttling is not found, add a new association
            if (!associationExist) {
                sfpm.put(serviceGroupId,
                        PersistenceUtils.createModule(module.getName(), version, Resources.Associations.ENGAGED_MODULES),
                        operationXPath);
            }
            if (!isTransactionStarted) {
                sfpm.commitTransaction(serviceGroupId);
            }
        } catch (PersistenceException e) {
            log.error("Error occured in engaging throttling for operation : "
                    + operationName, e);
            throw new ThrottleComponentException("errorEngagingModuleAtRegistry");
        }

        XmlPrimtiveAssertion assertion = this.getThrottlePolicy(operation
                .getPolicySubject().getAttachedPolicyComponents());

        //build builtPolicy according to received parameters
        OMElement policyElement = this.buildPolicy(policy,
                assertion, ThrottleComponentConstants.OPERATION_LEVEL);
        Policy builtPolicy = PolicyEngine.getPolicy(policyElement);

        //if we didn't find an already existing builtPolicy, attach a new one
        Policy policyToPersist = builtPolicy;
        if (assertion == null) {
            operation.getPolicySubject().attachPolicy(builtPolicy);
        } else {
            operation.getPolicySubject().updatePolicy(policyToUpdate);
            policyToPersist = policyToUpdate;
        }

        //persist the throttle builtPolicy into registry
        try {
            boolean isTransactionStarted = sfpm.isTransactionStarted(serviceGroupId);
            if (!isTransactionStarted) {
                sfpm.beginTransaction(serviceGroupId);
            }
            boolean registryTransactionStarted = Transaction.isStarted();
            if (!registryTransactionStarted) {
                registry.beginTransaction();
            }

            //to registry
            if (isProxyService) {
                String policyType = "" + PolicyInclude.AXIS_OPERATION_POLICY;
                String registryServicePath = PersistenceUtils.getRegistryResourcePath(axisService);
                pf.getServicePM().persistPolicyToRegistry(policyToPersist, policyType, registryServicePath);
            }

            //to file
            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            OMElement policyWrapperElement = omFactory.createOMElement(Resources.POLICY, null);
            policyWrapperElement.addAttribute(Resources.ServiceProperties.POLICY_TYPE,
                    "" + PolicyInclude.AXIS_OPERATION_POLICY, null);

            OMElement idElement = omFactory.createOMElement(Resources.ServiceProperties.POLICY_UUID, null);
            idElement.setText("" + policyToPersist.getId());
            policyWrapperElement.addChild(idElement);

            OMElement policyElementToPersist = PersistenceUtils.createPolicyElement(policyToPersist);
            policyWrapperElement.addChild(policyElementToPersist);

            if (!sfpm.elementExists(serviceGroupId, serviceXPath + "/" + Resources.POLICIES)) {
                sfpm.put(serviceGroupId,
                        omFactory.createOMElement(Resources.POLICIES, null), serviceXPath);
            } else {
                //you must manually delete the existing policy before adding new one.
                String pathToPolicy = serviceXPath + "/" + Resources.POLICIES +
                        "/" + Resources.POLICY +
                        PersistenceUtils.getXPathTextPredicate(
                                Resources.ServiceProperties.POLICY_UUID, policyToPersist.getId());
                if (sfpm.elementExists(serviceGroupId, pathToPolicy)) {
                    sfpm.delete(serviceGroupId, pathToPolicy);
                }
            }
            sfpm.put(serviceGroupId, policyWrapperElement, serviceXPath +
                    "/" + Resources.POLICIES);

            if (!sfpm.elementExists(serviceGroupId, serviceXPath +
                    PersistenceUtils.getXPathTextPredicate(
                            Resources.ServiceProperties.POLICY_UUID, policyToPersist.getId()))) {
                sfpm.put(serviceGroupId, idElement.cloneOMElement(), operationXPath);
            }
            if (!isTransactionStarted) {
                sfpm.commitTransaction(serviceGroupId);
            }
            if (!registryTransactionStarted) {
                registry.commitTransaction();
            }

        } catch (Exception e) {
            log.error("Error occured while saving the builtPolicy in registry", e);
            sfpm.rollbackTransaction(serviceGroupId);
            throw new ThrottleComponentException("errorSavingPolicy");
        }

        //engage the module at operation
        operation.engageModule(module);
        return false;
    }

    /**
     * Disengages throttling from an operation
     *
     * @param serviceName   - name of the service which contains the operation
     * @param operationName - operation name
     * @return - true if throttling is already engaged at the service level, else false
     * @throws ThrottleComponentException - on error
     */
    public boolean disengageThrottlingForOperation(String serviceName, String operationName)
            throws ThrottleComponentException {

        if (log.isDebugEnabled()) {
            log.debug("Disengaging throttling from the operation : " + operationName
                    + ", in service : " + serviceName);
        }
        try {
            AxisService axisService = this.getAxisService(serviceName);
            String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();

            boolean isProxyService = PersistenceUtils.isProxyService(axisService);
            String registryPolicyPath =
                    PersistenceUtils.getRegistryResourcePath(axisService) + RegistryResources.POLICIES;

            //get the throttle module from the current axis config
            AxisModule module = axisService.getAxisConfiguration().getModule(
                    ThrottleComponentConstants.THROTTLE_MODULE);

            //if throttling is already engaged in service level, don't disengage it in op level
            if (axisService.isEngaged(module)) {
                return true;
            }

            AxisOperation operation = axisService.getOperation(new QName(operationName));
            if (operation == null) {
                log.error("No operation found from the name " + operationName
                        + ", in service : " + serviceName);
                throw new ThrottleComponentException("noSuchOperation", new String[]{serviceName});
            }

            String operationXPath = PersistenceUtils.getResourcePath(operation);
//            String operationPath = Resources.SERVICE_GROUPS
//                    + axisService.getAxisServiceGroup().getServiceGroupName()
//                    + Resources.SERVICES + serviceName + Resources.OPERATIONS
//                    + operationName;

            //disengage the throttling module
            try {
                boolean isTransactionStarted = sfpm.isTransactionStarted(serviceGroupId);
                if (!isTransactionStarted) {
                    sfpm.beginTransaction(serviceGroupId);
                }
                // remove persisted data
                sfpm.delete(serviceGroupId, operationXPath +
                        "/" + Resources.ModuleProperties.MODULE_XML_TAG +
                        PersistenceUtils.getXPathAttrPredicate(Resources.NAME, module.getName()) +
                        PersistenceUtils.getXPathAttrPredicate(Resources.ModuleProperties.TYPE,
                                Resources.Associations.ENGAGED_MODULES));
                if (!isTransactionStarted) {
                    sfpm.commitTransaction(serviceGroupId);
                }

                if (isProxyService) {
                    boolean registryTransactionStarted = Transaction.isStarted();
                    if (!registryTransactionStarted) {
                        registry.beginTransaction();
                    }

                    if (registry.resourceExists(registryPolicyPath + wSO2OperationThrottlingPolicyId)) {
                        registry.delete(registryPolicyPath + wSO2OperationThrottlingPolicyId);
                    } else {
                        log.warn("Could not delete the Operation Throttling policy because it " +
                                "does not exist at " + registryPolicyPath + wSO2OperationThrottlingPolicyId);
                    }

                    if (!registryTransactionStarted) {
                        registry.commitTransaction();
                    }
                }

                // disengage at Axis
                operation.disengageModule(module);

            } catch (PersistenceException e) {
                log.error("Error occured while removing assertion from file system", e);
                sfpm.rollbackTransaction(serviceGroupId);
                throw new ThrottleComponentException("errorDisablingAtRegistry");
            } catch (RegistryException e) {
                log.error("Error while deleting throttling policy from registry path : " +
                        registryPolicyPath + wSO2OperationThrottlingPolicyId, e);
                sfpm.rollbackTransaction(serviceGroupId);
                throw new ThrottleComponentException("errorDisablingAtRegistry");
            }

        } catch (AxisFault e) {
            log.error("Error occured while disengaging module from AxisService", e);
            throw new ThrottleComponentException("errorDisablingThrottling", e);
        }
        return false;
    }

    /**
     * Disengage Throttling module from the specified service
     *
     * @param serviceName - name of the service of which throttling should be desabled
     * @throws ThrottleComponentException - error in disabling
     */

    public void disableThrottling(String serviceName) throws ThrottleComponentException {
        if (log.isDebugEnabled()) {
            log.debug("Disengaging throttling from the service : " + serviceName);
        }
        try {
            AxisService axisService = this.getAxisService(serviceName);
            String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();

            String serviceXPath = PersistenceUtils.getResourcePath(axisService);

            boolean isProxyService = PersistenceUtils.isProxyService(axisService);
            String registryPolicyPath =
                    PersistenceUtils.getRegistryResourcePath(axisService) + RegistryResources.POLICIES;

            //disengage the throttling module
            try {
                //get the throttle module from the current axis config
                AxisModule module = axisService.getAxisConfiguration().getModule(
                        ThrottleComponentConstants.THROTTLE_MODULE);

                // remove persisted data
                boolean isTransactionStarted = sfpm.isTransactionStarted(serviceGroupId);
                if (!isTransactionStarted) {
                    sfpm.beginTransaction(serviceGroupId);
                }
                sfpm.delete(serviceGroupId, serviceXPath +
                        "/" + Resources.ModuleProperties.MODULE_XML_TAG +
                        PersistenceUtils.getXPathAttrPredicate(Resources.NAME, module.getName()) +
                        PersistenceUtils.getXPathAttrPredicate(Resources.ModuleProperties.TYPE,
                                Resources.Associations.ENGAGED_MODULES));
                if (!isTransactionStarted) {
                    sfpm.commitTransaction(serviceGroupId);
                }

                if (isProxyService) {
                    boolean registryTransactionStarted = Transaction.isStarted();
                    if (!registryTransactionStarted) {
                        registry.beginTransaction();
                    }

                    if (registry.resourceExists(registryPolicyPath + wSO2ServiceThrottlingPolicyId)) {
                        registry.delete(registryPolicyPath + wSO2ServiceThrottlingPolicyId);
                    } else {
                        log.warn("Could not delete the Service Throttling policy because it " +
                                "does not exist at " + registryPolicyPath + wSO2ServiceThrottlingPolicyId);
                    }

                    if (!registryTransactionStarted) {
                        registry.commitTransaction();
                    }
                }

                // disengage at Axis
                axisService.disengageModule(module);

            } catch (PersistenceException e) {
                log.error("Error occured while removing assertion from registry", e);
                sfpm.rollbackTransaction(serviceGroupId);
                throw new ThrottleComponentException("errorDisablingAtRegistry");
            } catch (RegistryException e) {
                log.error("Error while deleting throttling policy from registry path : " +
                        registryPolicyPath + wSO2ServiceThrottlingPolicyId, e);
                sfpm.rollbackTransaction(serviceGroupId);
                throw new ThrottleComponentException("errorDisablingAtRegistry");
            }

        } catch (AxisFault e) {
            log.error("Error occured while disengaging module from AxisService", e);
            throw new ThrottleComponentException("errorDisablingThrottling", e);
        }
    }

    /**
     * Disengage throttling globally
     *
     * @throws ThrottleComponentException - component specific error
     */

    public void disengageGlobalThrottling() throws ThrottleComponentException {
        if (log.isDebugEnabled()) {
            log.debug("Disengaging globally engaged throttling");
        }
        //get the throttle module from the current axis config
        AxisModule module = this.axisConfig
                .getModule(ThrottleComponentConstants.THROTTLE_MODULE);
        //disengage the throttling module
        try {

            boolean isTransactionStarted = mfpm.isTransactionStarted(module.getName());
            if (!isTransactionStarted) {
                mfpm.beginTransaction(module.getName());
            }

            String modulePath = PersistenceUtils.getResourcePath(module);
            if (mfpm.elementExists(module.getName(), modulePath)) {
                OMElement element = (OMElement) mfpm.get(module.getName(), modulePath);
                if (!Boolean.parseBoolean(element
                        .getAttributeValue(new QName(GLOBALLY_ENGAGED_CUSTOM)))) {
                    element.addAttribute(GLOBALLY_ENGAGED_CUSTOM, Boolean.FALSE.toString(), null);
                    mfpm.setMetaFileModification(module.getName());
                }
            }

            if (!isTransactionStarted) {
                mfpm.commitTransaction(module.getName());
            }

            Parameter param = module.getParameter(GLOBALLY_ENGAGED_PARAM_NAME);
            if (param != null) {
                module.removeParameter(module.getParameter(GLOBALLY_ENGAGED_PARAM_NAME));
            }

            //disengage throttling from all the services which are not admin services
            for (Iterator serviceIter = this.axisConfig.getServices().values().iterator();
                 serviceIter.hasNext(); ) {
                AxisService service = (AxisService) serviceIter.next();
                String adminParamValue =
                        (String) service.getParent().getParameterValue(ADMIN_SERICE_PARAM_NAME);
                String hiddenParamValue =
                        (String) service.getParent().getParameterValue(HIDDEN_SERVICE_PARAM_NAME);

                if ((adminParamValue != null && adminParamValue.length() != 0 &&
                        Boolean.parseBoolean(adminParamValue.trim())) ||
                        (hiddenParamValue != null && hiddenParamValue.length() != 0 &&
                                Boolean.parseBoolean(hiddenParamValue.trim()))) {
                    continue;
                }
                this.disableThrottling(service.getName());
            }

        } catch (PersistenceException e) {
            log.error("Error occurred while removing global throttle from file system", e);
            mfpm.rollbackTransaction(module.getName());
            throw new ThrottleComponentException("errorDisablingAtRegistry");
        } catch (AxisFault axisFault) {
            log.error("Error occurred while disengaging module from AxisService", axisFault);
            mfpm.rollbackTransaction(module.getName());
            throw new ThrottleComponentException("errorDisablingThrottling", axisFault);
        }
    }

    /**
     * Gives the current policy config as a ThrottlePolicy
     *
     * @param serviceName - name of the service of which configs are needed
     * @return - ThrottlePolicy object containing configs
     * @throws AxisFault                  - error in accessing axisConfig or axis service
     * @throws ThrottleComponentException - policy config retrieving error
     */

    public ThrottlePolicy getPolicyConfigs(String serviceName) throws AxisFault,
            ThrottleComponentException {
        if (log.isDebugEnabled()) {
            log.debug("Extracting current policy " +
                    "configurations for the service : " + serviceName);
        }
        //get the axis service
        AxisService service = this.getAxisService(serviceName);

        //object to be returned
        ThrottlePolicy currentConfig = new ThrottlePolicy();

        //Set whether module is currently engaged or not
        AxisModule module = service.getAxisConfiguration()
                .getModule(ThrottleComponentConstants.THROTTLE_MODULE);
        currentConfig.setEngaged(service.isEngaged(module));

        XmlPrimtiveAssertion assertion = this.getThrottlePolicy(service
                .getPolicySubject().getAttachedPolicyComponents());

        return preparePolicyConfigs(assertion, currentConfig);
    }


    public ThrottlePolicy getGlobalPolicyConfigs() throws AxisFault,
            ThrottleComponentException {
        if (log.isDebugEnabled()) {
            log.debug("Extracting current global policy configurations");
        }

        //object to be returned
        ThrottlePolicy currentConfig = new ThrottlePolicy();

        //Set whether module is currently engaged or not
        AxisModule module = this.axisConfig
                .getModule(ThrottleComponentConstants.THROTTLE_MODULE);

        Parameter param = module.getParameter(GLOBALLY_ENGAGED_PARAM_NAME);
        if (param != null) {
            String globallyEngaged = (String) param.getValue();
            if (globallyEngaged != null && globallyEngaged.length() != 0) {
                currentConfig.setEngaged(Boolean.parseBoolean(globallyEngaged.trim()));
            }
        }

        XmlPrimtiveAssertion assertion = this.getThrottlePolicy(module
                .getPolicySubject().getAttachedPolicyComponents());

        return preparePolicyConfigs(assertion, currentConfig);
    }

    public ThrottlePolicy getOperationPolicyConfigs(String serviceName, String operationName)
            throws AxisFault, ThrottleComponentException {
        if (log.isDebugEnabled()) {
            log.debug("Extracting current policy configurations for the operation : "
                    + operationName + " in service : " + serviceName);
        }
        //get the axis service
        AxisService service = this.getAxisService(serviceName);

        AxisOperation operation = service.getOperation(new QName(operationName));
        if (operation == null) {
            log.error("No operation found from the name " + operationName
                    + ", in service : " + serviceName);
            throw new ThrottleComponentException("noSuchOperation", new String[]{serviceName});
        }

        //object to be returned
        ThrottlePolicy currentConfig = new ThrottlePolicy();

        //Set whether module is currently engaged or not
        AxisModule module = service.getAxisConfiguration()
                .getModule(ThrottleComponentConstants.THROTTLE_MODULE);
        currentConfig.setEngaged((operation.isEngaged(module) || service.isEngaged(module)));

        XmlPrimtiveAssertion assertion = null;
        if (service.isEngaged(module)) {
            assertion = this.getThrottlePolicy(service
                    .getPolicySubject().getAttachedPolicyComponents());
        }
        if (assertion == null) {
            assertion = this.getThrottlePolicy(operation
                    .getPolicySubject().getAttachedPolicyComponents());
        }

        return preparePolicyConfigs(assertion, currentConfig);

    }

    public ThrottlePolicy toThrottlePolicy(String policyXML) throws ThrottleComponentException {
        try {
            OMElement policyOM = createOMElement(policyXML);
            Policy policy = PolicyEngine.getPolicy(policyOM);
            List<Policy> list = new ArrayList<Policy>();
            list.add(policy);
            XmlPrimtiveAssertion assertion = this.getThrottlePolicy(list);

            return preparePolicyConfigs(assertion, new ThrottlePolicy());
        } catch (Exception e) {
            throw new ThrottleComponentException("Invalid policy XML ", e);
        }
    }

    public String throttlePolicyToString(ThrottlePolicy policy) throws ThrottleComponentException {
        OMElement policyElement = this.buildPolicy(policy,
                null, ThrottleComponentConstants.MEDIATION_LEVEL);
        if (policyElement != null) {
            return policyElement.toString();
        }
        return "";
    }

    private OMElement createOMElement(String xml) {
        try {
            XMLStreamReader reader = XMLInputFactory
                    .newInstance().createXMLStreamReader(new StringReader(xml));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private ThrottlePolicy preparePolicyConfigs(XmlPrimtiveAssertion assertion, ThrottlePolicy currentConfig) {
        ArrayList<InternalData> internalConfigs = new ArrayList<InternalData>();
        Policy throttlePolicy;

        if (assertion == null) {
            //if no policy exists, just return an empty config
            return new ThrottlePolicy();
        } else {
            throttlePolicy = PolicyEngine.getPolicy(assertion.getValue());
        }

        //Fill data into the ThrottlePolicy object by going through existing policy
        if (throttlePolicy != null) {
            for (Object inThrottle : throttlePolicy.getPolicyComponents()) {
                //top level elements can be an 'All' or MaximumConcurrentAccess
                if (inThrottle instanceof Policy) {
                    InternalData data = new InternalData();
                    for (Object inSecondLevelPolicy : ((Policy) inThrottle).getAssertions()) {
                        //In this level it can be ID or 'ExactlyOne'
                        if (inSecondLevelPolicy instanceof XmlPrimtiveAssertion) {
                            OMElement range = ((XmlPrimtiveAssertion) inSecondLevelPolicy).getValue();
                            data.setRange(range.getText());
                            if (range.getAttributeValue(ThrottleConstants
                                    .THROTTLE_TYPE_ATTRIBUTE_QNAME).equals(
                                    ThrottleComponentConstants.DOMIN_ATT_VALUE)) {
                                data.setRangeType(ThrottleComponentConstants.DOMIN_ATT_VALUE);
                            }
                        } else if (inSecondLevelPolicy instanceof Policy) {
                            for (Object inThirdLevelPolicy :
                                    ((Policy) inSecondLevelPolicy).getPolicyComponents()) {
                                if (inThirdLevelPolicy instanceof XmlPrimtiveAssertion) {
                                    OMElement accessLevel = ((XmlPrimtiveAssertion)
                                            inThirdLevelPolicy).getValue();
                                    if (accessLevel.getLocalName()
                                            .equals(ThrottleConstants.ALLOW_PARAMETER_NAME)) {
                                        data.setAccessLevel(ThrottleConstants.ACCESS_ALLOWED);
                                    } else if (accessLevel.getLocalName()
                                            .equals(ThrottleConstants.DENY_PARAMETER_NAME)) {
                                        data.setAccessLevel(ThrottleConstants.ACCESS_DENIED);
                                    } else {
                                        data.setAccessLevel(ThrottleConstants.ACCESS_CONTROLLED);
                                        OMElement policy = accessLevel
                                                .getFirstChildWithName(Constants.Q_ELEM_POLICY);
                                        Policy fourthLevelPolicy = PolicyEngine.getPolicy(policy);
                                        for (Object inFourthLevelPolicy :
                                                fourthLevelPolicy.getPolicyComponents()) {
                                            OMElement temp = ((XmlPrimtiveAssertion)
                                                    inFourthLevelPolicy).getValue();
                                            String localname = temp.getLocalName();
                                            //there can be three params here
                                            if (localname.equals(ThrottleConstants
                                                    .MAXIMUM_COUNT_PARAMETER_NAME)) {
                                                data.setMaxRequestCount(validateAndToInt(temp.getText()));
                                            } else if (localname.equals(ThrottleConstants
                                                    .UNIT_TIME_PARAMETER_NAME)) {
                                                data.setUnitTime(validateAndToInt(temp.getText()));
                                            } else if (localname.equals(ThrottleConstants
                                                    .PROHIBIT_TIME_PERIOD_PARAMETER_NAME)) {
                                                data.setProhibitTimePeriod(validateAndToInt(temp.getText()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    internalConfigs.add(data);
                } else if (inThrottle instanceof XmlPrimtiveAssertion) {
                    OMElement maxConc = ((XmlPrimtiveAssertion) inThrottle).getValue();
                    currentConfig.setMaxConcurrentAccesses(validateAndToInt(maxConc.getText()));
                }
            }

            InternalData[] data = new InternalData[internalConfigs.size()];
            for (int p = 0; p < internalConfigs.size(); p++) {
                data[p] = internalConfigs.get(p);
            }
            currentConfig.setInternalConfigs(data);
        }
        return currentConfig;
    }

    /**
     * Check whether there exists a policy for throttling within current policies
     * for services
     *
     * @param components - all policy components
     * @return policy assertion if found, else null
     * @throws AxisFault - error accessing axisConfig
     */
    private XmlPrimtiveAssertion getThrottlePolicy(Collection components) throws AxisFault {
        //get all policy components
        QName assertionName;

        //Finds the policy for throttling
        for (Object comp : components) {
            if (comp instanceof Policy) {
                Policy policy = (Policy) comp;
                for (Iterator iterator = policy.getAlternatives();
                     iterator.hasNext(); ) {
                    Object object = iterator.next();
                    if (object instanceof List) {
                        List list = (List) object;
                        for (Object assertObj : list) {
                            if (assertObj instanceof XmlPrimtiveAssertion) {
                                XmlPrimtiveAssertion primitiveAssertion = (XmlPrimtiveAssertion)
                                        assertObj;
                                assertionName = primitiveAssertion.getName();
                                if (assertionName.equals(
                                        ThrottleConstants.SERVICE_THROTTLE_ASSERTION_QNAME)
                                        || assertionName.equals(
                                        ThrottleConstants.MODULE_THROTTLE_ASSERTION_QNAME) ||
                                        assertionName.equals(ThrottleConstants
                                                .OPERATION_THROTTLE_ASSERTION_QNAME) ||
                                        assertionName.equals(ThrottleConstants
                                                .MEDIATOR_THROTTLE_ASSERTION_QNAME) ||
                                        assertionName.equals(ThrottleConstants
                                                .THROTTLE_ASSERTION_QNAME)) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Existing ThrottleAssertion found");
                                    }
                                    this.policyToUpdate = policy;
                                    return primitiveAssertion;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    /**
     * Builds the policy according to the parameters specified by the user. A
     * template policy is used and it is modified to inclued necessary parameters.
     *
     * @param policyConfigs - Throttle policy configurations
     * @param assertion     - existing ThrottleAssertion
     * @param level         - global, service or operation
     * @return - created policy element
     * @throws ThrottleComponentException - error in building policy
     */

    private OMElement buildPolicy(ThrottlePolicy policyConfigs,
                                  XmlPrimtiveAssertion assertion, String level)
            throws ThrottleComponentException {
        if (log.isDebugEnabled()) {
            log.debug("Building the policy using received configurations");
        }
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace throttleNamespace = factory.createOMNamespace(ThrottleConstants.THROTTLE_NS,
                ThrottleConstants.THROTTLE_NS_PREFIX);

        //Get the template policy according to level
        OMElement template = this.getTemplatePolicy(level);
        template.build();

        //Get the ThrottleAssertion element from the policy
        OMElement ta = this.getThrottleAssertion(level, template);

        //Get the All element inside the ThrottleAssertion to be used later
        OMElement secondLevelPolicy = ta.getFirstChildWithName(Constants.Q_ELEM_POLICY);

        /**
         * If the existing ThrottleAssertion element is not null, we can edit
         * it. otherwise we use the template as the new policy element
         */
        OMElement policyElement;
        OMElement throttleAssertion;
        if (assertion != null) {
            //a tempory element to wrap the existing ThrottleAssertion
            policyElement = getPolicyElement();
            OMElement existingElement = assertion.getValue();
            //detach secondLevelPolicy 'All' assertions within the existing element
            OMElement existingSecondLevelPolicy = existingElement
                    .getFirstChildWithName(Constants.Q_ELEM_POLICY);
            while (existingSecondLevelPolicy != null) {
                existingSecondLevelPolicy.detach();
                existingSecondLevelPolicy = existingElement
                        .getFirstChildWithName(Constants.Q_ELEM_POLICY);
            }
            policyElement.addChild(existingElement);
            throttleAssertion = this.getThrottleAssertion(level, policyElement);
        } else {
            policyElement = template;
            throttleAssertion = ta;
        }

        //Set the Maximum Concurrent Accesses value
        QName maxName = new QName(ThrottleConstants.THROTTLE_NS,
                ThrottleConstants.MAXIMUM_CONCURRENT_ACCESS_PARAMETER_NAME);
        OMElement tempToTreat = throttleAssertion.getFirstChildWithName(maxName);
        if (tempToTreat == null) {
            tempToTreat = OMAbstractFactory.getOMFactory().createOMElement(maxName);
            throttleAssertion.addChild(tempToTreat);
        }
        treatSubElement(tempToTreat, policyConfigs.getMaxConcurrentAccesses());

        QName controlQName = new QName(ThrottleConstants.THROTTLE_NS,
                ThrottleConstants.CONTROL_PARAMETER_NAME);

        //Modify the parameters in the ThrottleAssertion according to config data
        InternalData[] internalData = policyConfigs.getInternalConfigs();
        OMElement e;
        for (InternalData confData : internalData) {
            //Use the template 'All' element
            OMElement clonedElement = secondLevelPolicy.cloneOMElement();

            //Set the value for the 'ID' parameter
            OMElement temp = clonedElement.getFirstChildWithName(new QName(ThrottleConstants
                    .THROTTLE_NS, ThrottleConstants.ID_PARAMETER_NAME));
            temp.setText(confData.getRange());
            if (confData.getRangeType().equals(ThrottleComponentConstants.DOMIN_ATT_VALUE)) {
                temp.getAttribute(ThrottleConstants.THROTTLE_TYPE_ATTRIBUTE_QNAME)
                        .setAttributeValue(ThrottleComponentConstants.DOMIN_ATT_VALUE);
            }

            temp = clonedElement.getFirstChildWithName(Constants.Q_ELEM_POLICY);

            if (confData.getAccessLevel() == ThrottleConstants.ACCESS_CONTROLLED) {
                temp = temp.getFirstChildWithName(new QName(ThrottleConstants.THROTTLE_NS,
                        ThrottleConstants.CONTROL_PARAMETER_NAME));
                temp = temp.getFirstChildWithName(Constants.Q_ELEM_POLICY);

                //Set values for Maximum Count, Unit Time and Prohibit Time Period
                e = temp.getFirstChildWithName(new QName(ThrottleConstants.THROTTLE_NS,
                        ThrottleConstants.MAXIMUM_COUNT_PARAMETER_NAME));
                treatSubElement(e, confData.getMaxRequestCount());

                tempToTreat = temp.getFirstChildWithName(new QName(ThrottleConstants.THROTTLE_NS,
                        ThrottleConstants.UNIT_TIME_PARAMETER_NAME));
                treatSubElement(tempToTreat, confData.getUnitTime());

                tempToTreat = temp.getFirstChildWithName(new QName(ThrottleConstants.THROTTLE_NS,
                        ThrottleConstants.PROHIBIT_TIME_PERIOD_PARAMETER_NAME));
                treatSubElement(tempToTreat, confData.getProhibitTimePeriod());

            } else if (confData.getAccessLevel() == ThrottleConstants.ACCESS_ALLOWED) {
                temp.getFirstChildWithName(controlQName).detach();
                temp.addChild(factory.createOMElement(ThrottleConstants.ALLOW_PARAMETER_NAME,
                        throttleNamespace));
            } else if (confData.getAccessLevel() == ThrottleConstants.ACCESS_DENIED) {
                temp.getFirstChildWithName(controlQName).detach();
                temp.addChild(factory.createOMElement(ThrottleConstants.DENY_PARAMETER_NAME,
                        throttleNamespace));
            }
            throttleAssertion.addChild(clonedElement);
        }
        //Detach the template 'All'
        secondLevelPolicy.detach();
        return policyElement;
    }


    /**
     * Set value or detach a sub element
     *
     * @param subElement  - input OMElement
     * @param configValue - value to set
     */
    private void treatSubElement(OMElement subElement, int configValue) {
        if (configValue != 0) {
            subElement.setText(String.valueOf(configValue));
        } else {
            subElement.detach();
        }
    }

    /**
     * Creates a new wsu:Policy element with throttle namespace
     *
     * @return policy element
     */
    private OMElement getPolicyElement() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement policyElement = factory.createOMElement(Constants.Q_ELEM_POLICY);
        OMNamespace wsuNs = factory.createOMNamespace(
                Constants.URI_WSU_NS, "wsu");
        policyElement.addAttribute(factory.createOMAttribute("Id", wsuNs,
                "DummyPolicy"));
//        OMNamespace throttleNs = factory.createOMNamespace(
//                ThrottleConstants.THROTTLE_NS, ThrottleConstants.THROTTLE_NS_PREFIX);
//        policyElement.addAttribute(factory.createOMAttribute("Temp", throttleNs,
//                "throttle"));
        return policyElement;
    }


    /**
     * Read the template policy from registry according to level at which
     * throttling is engaged
     *
     * @param level - global, service or operation level..
     * @return template OMElement
     * @throws ThrottleComponentException - error on reading template
     */
    private OMElement getTemplatePolicy(String level) throws ThrottleComponentException {
        XMLStreamReader parser;
        String resourceUri = ThrottleComponentConstants.TEMPLATE_URI + level;
        try {
            //Get the input stream of the template policy from registry
            Resource resource;
            Registry registry = getConfigSystemRegistry();
            if (registry.resourceExists(resourceUri)) {
                resource = registry.get(resourceUri);
            } else {
                throw new ThrottleComponentException("templateNotFound");
            }
            InputStream in = resource.getContentStream();

            //Get the template policy element by parsing the input stream
            parser = XMLInputFactory.newInstance().createXMLStreamReader(in);
        } catch (Exception e) {
            log.error("Error occoured while loading template from registry", e);
            throw new ThrottleComponentException("errorLoadingTemplate");
        }
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        return builder.getDocumentElement();
    }

    /**
     * Returns the throttle assertion relevent to level of engagement
     *
     * @param level  - global,  service or operation level
     * @param parent - policy element
     * @return throttle assertion
     */
    private OMElement getThrottleAssertion(String level, OMElement parent) {
        OMElement throttleAssertion;
        if (level.equals(ThrottleComponentConstants.SERVICE_LEVEL)) {
            throttleAssertion = parent.getFirstChildWithName(
                    ThrottleConstants.SERVICE_THROTTLE_ASSERTION_QNAME);
        } else if (level.equals(ThrottleComponentConstants.OPERATION_LEVEL)) {
            throttleAssertion = parent.getFirstChildWithName(
                    ThrottleConstants.OPERATION_THROTTLE_ASSERTION_QNAME);
        } else if (level.equals(ThrottleComponentConstants.MEDIATION_LEVEL)) {
            throttleAssertion = parent.getFirstChildWithName(
                    ThrottleConstants.MEDIATOR_THROTTLE_ASSERTION_QNAME);
        } else {
            throttleAssertion = parent.getFirstChildWithName(
                    ThrottleConstants.MODULE_THROTTLE_ASSERTION_QNAME);
        }
        return throttleAssertion;
    }

    private AxisService getAxisService(String serviceName) throws ThrottleComponentException {
        AxisService axisService = axisConfig.getServiceForActivation(serviceName);
        if (axisService == null) {
            log.error("No service found from the name " + serviceName);
            throw new ThrottleComponentException("noSuchService", new String[]{serviceName});
        }
        return axisService;
    }

    private int validateAndToInt(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        int intValue = -1;
        try {
            intValue = Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
        }
        if (intValue == -1) {
            throw new IllegalArgumentException(
                    "Invalid parameter value : " + value + ".Expected integer value.");
        }
        return intValue;
    }
}
