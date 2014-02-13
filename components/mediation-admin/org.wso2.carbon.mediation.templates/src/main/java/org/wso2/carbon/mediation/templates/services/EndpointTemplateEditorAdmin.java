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
package org.wso2.carbon.mediation.templates.services;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.SynapseXMLConfigurationFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.TemplateFactory;
import org.apache.synapse.config.xml.endpoints.TemplateSerializer;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.registry.Registry;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.templates.common.EndpointTemplateInfo;
import org.wso2.carbon.mediation.templates.common.factory.TemplateInfoFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

public class EndpointTemplateEditorAdmin extends AbstractServiceBusAdmin {
    private static final Log log = LogFactory.getLog(TemplateEditorAdmin.class);

    //TODO: Move WSO2_TEMPLATE_MEDIA_TYPE to registry
    public static final String WSO2_ENDPOINT_TEMPLATE_MEDIA_TYPE = "application/vnd.wso2.template.endpoint";

    public int getEndpointTemplatesCount() throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            return getSynapseConfiguration().getEndpointTemplates().values().size();
        } catch (Exception e) {
            handleException("Couldn't get the Synapse Configuration to get Sequence count", e);
        } finally {
            lock.unlock();
        }
        return 0;
    }

    public int getDynamicEndpointTemplatesCount() throws AxisFault{
        try {
            String[] govList = getGovernanceRegistry() !=null?getMimeTypeResult(getGovernanceRegistry()):new String[0];
            String[] confList = getConfigSystemRegistry() != null?getMimeTypeResult(getConfigSystemRegistry()):new String[0];
            return confList.length + govList.length;
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressWarnings({"unchecked"})
    private String[] getMimeTypeResult(org.wso2.carbon.registry.core.Registry targetRegistry)
            throws Exception {
        String sql = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE WHERE REG_MEDIA_TYPE = ?";
        Map parameters = new HashMap();
        parameters.put("query", sql);
        parameters.put("1", WSO2_ENDPOINT_TEMPLATE_MEDIA_TYPE);
        Resource result = targetRegistry.executeQuery(null, parameters);
        return (String[]) result.getContent();
    }


    public EndpointTemplateInfo[] getEndpointTemplates(int pageNumber, int endpointTemplatesPerPage) throws AxisFault {
        final Lock lock = getLock();
        Collection<Template> templates = null;
        try {
            lock.lock();
            templates = getSynapseConfiguration().getEndpointTemplates().values();

            EndpointTemplateInfo[] info = TemplateInfoFactory.getSortedTemplateInfoArray(templates);
            EndpointTemplateInfo[] ret;
            if (info.length >= (endpointTemplatesPerPage * pageNumber + endpointTemplatesPerPage)) {
                ret = new EndpointTemplateInfo[endpointTemplatesPerPage];
            } else {
                ret = new EndpointTemplateInfo[info.length - (endpointTemplatesPerPage * pageNumber)];
            }
            for (int i = 0; i < endpointTemplatesPerPage; ++i) {
                if (ret.length > i) {
                    ret[i] = info[endpointTemplatesPerPage * pageNumber + i];
                }
            }
            return ret;
        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to " +
                            "get the available templates", fault);
        } finally {
            lock.unlock();
        }
        return new EndpointTemplateInfo[0];
    }

    public EndpointTemplateInfo[] getDynamicEndpointTemplates(int pageNumber, int endpointTemplatesPerPage) throws AxisFault {
        org.wso2.carbon.registry.core.Registry registry;
        EndpointTemplateInfo[] ret;
        final Lock lock = getLock();
        try {
            lock.lock();
            String[] configInfo = getConfigSystemRegistry() !=null?getMimeTypeResult(getConfigSystemRegistry()) :new String[0];
            String[] govInfo = getGovernanceRegistry() !=null?getMimeTypeResult(getGovernanceRegistry()): new String[0];
            String[] info = new String[configInfo.length + govInfo.length];

            int ptr = 0;
            for (String aConfigInfo : configInfo) {
                info[ptr] = "conf:" + aConfigInfo;
                ++ptr;
            }
            for (String aGovInfo : govInfo) {
                info[ptr] = "gov:" + aGovInfo;
                ++ptr;
            }
            Arrays.sort(info);
            if (info.length >= (endpointTemplatesPerPage * pageNumber + endpointTemplatesPerPage)) {
                ret = new EndpointTemplateInfo[endpointTemplatesPerPage];
            } else {
                ret = new EndpointTemplateInfo[info.length - (endpointTemplatesPerPage * pageNumber)];
            }
            for (int i = 0; i < endpointTemplatesPerPage; ++i) {
                if (ret.length > i) {
                    EndpointTemplateInfo seq = new EndpointTemplateInfo();
                    seq.setTemplateName(info[endpointTemplatesPerPage * pageNumber + i]);
                    ret[i] = seq;
                }
            }
        } catch (Exception e) {
            handleException("Unable to get Dynamic Template Info",e);
            return null;
        } finally {
            lock.unlock();
        }
        return ret;

//        return new EndpointTemplateInfo[0];
    }

    public OMElement getTemplate(String templateName) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Template template = synapseConfiguration.getEndpointTemplate(templateName);
            if (template != null) {
                OMElement parentElement = null;
//                String dummyParentEl = "<" + "parent xmlns=\"" + SynapseConstants.SYNAPSE_NAMESPACE + "\"" + "/" + ">";
//                parentElement = AXIOMUtil.stringToOM(dummyParentEl);
//                return new TemplateSerializer().serializeEndpointTemplate(template, parentElement).getFirstElement();
                return new TemplateSerializer().serializeEndpointTemplate(template, parentElement);
            } else {
                handleException("Template with the name "
                                + templateName + " does not exist");
            }
        } catch (SynapseException syne) {
            handleException("Unable to get the endpoint template : " + templateName, syne);
        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to get the Endpoint Template", fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void deleteEndpointTemplate(String templateName) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synCfg = getSynapseConfiguration();
            Template sequence = synCfg.getEndpointTemplates().get(templateName);
            if (sequence != null) {
                synCfg.removeEndpointTemplate(templateName);
                MediationPersistenceManager pm = getMediationPersistenceManager();
                pm.deleteItem(templateName, sequence.getFileName(),
                              ServiceBusConstants.ITEM_TYPE_TEMPLATE_ENDPOINTS);
            } else {
                handleException("No defined endpoint template with name " + templateName
                                + " found to delete in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to delete the sequence", fault);
        } finally {
            lock.unlock();
        }
    }

    public void deleteDynamicEndpointTemplate(String key) throws AxisFault {
        SynapseConfiguration synConfig = getSynapseConfiguration();
        Registry registry = synConfig.getRegistry();
        if (registry != null) {
            if (registry.getRegistryEntry(key).getType() == null) {
                handleException("The key '" + key +
                        "' cannot be found within the configuration");
            } else {
                registry.delete(key);
            }
        } else {
            handleException("Unable to access the registry instance for the ESB");
        }
    }

    public void saveEndpointTemplate(String  templateConfig) throws AxisFault{
        try {
            saveEndpointTemplate(AXIOMUtil.stringToOM(templateConfig));
        } catch (XMLStreamException e) {
            handleException("unable to save Endpoint template ...invalid configuration element",e);
        }
    }

    private void saveEndpointTemplate(OMElement templateElement) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            if (templateElement != null && templateElement.getLocalName().equals(
                    XMLConfigConstants.TEMPLATE_ELT.getLocalPart())) {
                String templateName = templateElement.getAttributeValue(new QName("name"));
                SynapseConfiguration config = getSynapseConfiguration();
                log.debug("Saving template : " + templateName);
                Template preSeq = config.getEndpointTemplates().get(templateName);
                if (preSeq == null) {
                    handleException("Unable to save template " + templateName + ". Does not exist");
                } else {
                    // we should first try to build the new sequence. if exception we return
                    Template endpointTemplate = new TemplateFactory().createEndpointTemplate(
                            templateElement, getSynapseConfiguration().getProperties());

                    // if everything went successfully we remove the sequence
                    config.removeEndpointTemplate(templateName);
                    if (endpointTemplate instanceof Template) {
                        ((Template) endpointTemplate).setFileName(preSeq.getFileName());
                        config.addEndpointTemplate(templateName, endpointTemplate);
                    }

                    log.debug("Saved template : " + templateName + " to the configuration");

                    Template templ = config.getEndpointTemplates().get(templateName);
                    if (templ != null) {
//                        templ.init(getSynapseEnvironment());
                        persistTemplate(templ);
                    }
                }
            } else {
                handleException("Unable to save template. Invalid definition");
            }
        } catch (Exception fault) {
            handleException("Unable to save the Template : " + fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
    }

    public void saveDynamicEndpointTemplate(String key, String  sequenceConfig) throws AxisFault{
        try {
            saveDynamicEndpointTemplate(key, AXIOMUtil.stringToOM(sequenceConfig));
        } catch (XMLStreamException e) {
            handleException("unable to save dynamic template Endpoint...invalid configuration element",e);
        }
    }

    private void saveDynamicEndpointTemplate(String key, OMElement endpointTempalteEl) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synConfig = getSynapseConfiguration();
            Registry registry = synConfig.getRegistry();
            if (registry != null) {
                if (registry.getRegistryEntry(key).getType() == null) {
                    handleException("Unable to save the template. No resource is available " +
                            "by the key '" + key + "'");
                }
                registry.updateResource(key, endpointTempalteEl);
            } else {
                handleException("Unable to access the registry instance for the ESB");
            }
        } finally {
            lock.unlock();
        }
    }

    public void addEndpointTemplate(String  templateElementConfig) throws AxisFault{
        try {
            addEndpointTemplate(AXIOMUtil.stringToOM(templateElementConfig));
        } catch (XMLStreamException e) {
            handleException("unable to save template Endpoint...invalid configuration element",e);
        }
    }

    private void addEndpointTemplate(OMElement templateElement) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            if (templateElement.getLocalName().equals(
                    XMLConfigConstants.TEMPLATE_ELT.getLocalPart())) {
                String templateName = templateElement.getAttributeValue(new QName("name"));
                SynapseConfiguration config = getSynapseConfiguration();
                if (log.isDebugEnabled()) {
                    log.debug("Adding template : " + templateName + " to the configuration");
                }
                if (config.getLocalRegistry().get(templateName) != null) {
                    handleException("The name '" + templateName +
                                    "' is already used within the configuration");
                } else {
                    SynapseXMLConfigurationFactory.defineEndpointTemplate(config, templateElement,
                                                                          getSynapseConfiguration().getProperties());
                    if (log.isDebugEnabled()) {
                        log.debug("Added template : " + templateName + " to the configuration");
                    }

                    Template templ = config.getEndpointTemplates().get(templateName);
                    templ.setFileName(ServiceBusUtils.generateFileName(templateName));
//                    templ.init(getSynapseEnvironment());

                    //noinspection ConstantConditions
                    persistTemplate(templ);
                }
            } else {
                handleException("Invalid template definition");
            }
        } catch (Exception fault) {
            handleException("Error adding template : " + fault.getMessage(), fault);
        } catch (Error error) {
            throw new AxisFault("Unexpected error occured while " +
                                              "adding the template : " + error.getMessage(), error);
        } finally {
            lock.unlock();
        }
    }

    public void addDynamicEndpointTemplate(String key, String sequenceConfig) throws AxisFault{
        try {
            addDynamicEndpointTemplate(key, AXIOMUtil.stringToOM(sequenceConfig));
        } catch (XMLStreamException e) {
            handleException("unable to save template Endpoint...invalid configuration element",e);
        }
    }

    private void addDynamicEndpointTemplate(String key, OMElement endpointTemplateEl) throws AxisFault {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.reset();
        try {
            XMLPrettyPrinter.prettify(endpointTemplateEl, stream);
        } catch (Exception e) {
            handleException("Unable to pretty print configuration",e);
        }
        try {
            org.wso2.carbon.registry.core.Registry registry;
            if(key.startsWith("conf:")) {
                registry = getConfigSystemRegistry();
                key = key.replace("conf:","");
            } else {
                registry = getGovernanceRegistry();
                key = key.replace("gov:","");
            }
            if(registry.resourceExists(key)){
                handleException("Resource is already exists");
            }
            Resource resource = registry.newResource();
            resource.setMediaType(WSO2_ENDPOINT_TEMPLATE_MEDIA_TYPE);
            resource.setContent(new String(stream.toByteArray()).trim());
            registry.put(key, resource);
        } catch (RegistryException e) {
            handleException("WSO2 Registry Exception", e);
        }
    }

    private void persistTemplate(Template template) throws AxisFault {
       if (template instanceof Template) {
            MediationPersistenceManager pm = getMediationPersistenceManager();
            pm.saveItem(((Template) template).getName(), ServiceBusConstants.ITEM_TYPE_TEMPLATE_ENDPOINTS);
        }
    }


    private void handleException(String message, Throwable cause) throws AxisFault {
        log.error(message, cause);
        throw new AxisFault(message, cause);
    }

    private void handleException(String message) throws AxisFault {
        log.error(message);
        throw new AxisFault(message);
    }

    public boolean hasDuplicateTempleteEndpoint(String  templateElementConfig) throws AxisFault {
        OMElement templateElement = null;
        try {
            templateElement = AXIOMUtil.stringToOM(templateElementConfig);
        }
         catch (XMLStreamException e) {
            handleException("unable to Checking template Endpoint...invalid configuration element", e);
        }
        if (templateElement!=null) {
            final Lock lock = getLock();
            try {
                lock.lock();
                if (templateElement.getLocalName().equals(
                        XMLConfigConstants.TEMPLATE_ELT.getLocalPart())) {
                    String templateName = templateElement.getAttributeValue(new QName("name"));
                    SynapseConfiguration config = getSynapseConfiguration();
                    if (log.isDebugEnabled()) {
                        log.debug("Checking template : " + templateName + " with existing configuration");
                    }
                    if (config.getLocalRegistry().get(templateName) != null) {
                        return true;
                    }
                }
            } catch (Exception fault) {
                handleException("Error Checking template : " + fault.getMessage(), fault);
            } catch (Error error) {
                throw new AxisFault("Unexpected error occured while " +
                        "Checking the template : " + error.getMessage(), error);
            } finally {
                lock.unlock();
            }
        }
     return false;
    }
}
