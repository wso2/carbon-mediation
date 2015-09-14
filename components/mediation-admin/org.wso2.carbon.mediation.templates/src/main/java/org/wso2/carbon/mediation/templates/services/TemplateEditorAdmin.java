/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.mediation.templates.services;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.apache.synapse.config.xml.MediatorSerializerFinder;
import org.apache.synapse.config.xml.SynapseXMLConfigurationFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.template.TemplateMediator;
import org.apache.synapse.registry.Registry;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.templates.common.TemplateInfo;
import org.wso2.carbon.mediation.templates.common.factory.TemplateInfoFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * Takes care of all the administration activities related to <code>Sequences</code>
 * in the initialized synapse environment</p>
 * <p/>
 * <p>The administration activities include;</p>
 * <ul>
 * <li>Adding templates</li>
 * <li>Editing templates</li>
 * <li>Saving templates</li>
 * <li>Deleting templates</li>
 * <li>Enabling/disabling statistics/tracing of templates</li>
 * <li>Editing of dynamic templates</li>
 * <li>and so on...</li>
 * </ul>
 */
@SuppressWarnings({"UnusedDeclaration"})
public class TemplateEditorAdmin extends AbstractServiceBusAdmin {

    private static final Log log = LogFactory.getLog(TemplateEditorAdmin.class);

    //TODO: Move WSO2_TEMPLATE_MEDIA_TYPE to registry
    public static final String WSO2_TEMPLATE_MEDIA_TYPE = "application/vnd.wso2.template";


    public TemplateInfo[] getTemplates(int pageNumber, int templatePerPage)
            throws AxisFault {
        final Lock lock = getLock();
        Collection<TemplateMediator> templates = null;
        try {
            lock.lock();
            templates = getSynapseConfiguration().getSequenceTemplates().values();

            TemplateInfo[] info = TemplateInfoFactory.getSortedTemplateInfoArrayByTemplateMediator(templates);
            TemplateInfo[] ret;

            if (info != null && info.length > 0) {
                for (TemplateInfo templateInfo : info) {
                    TemplateMediator templateMediator = getSynapseConfiguration().getSequenceTemplates()
                            .get(templateInfo.getName());
                    if (templateMediator.getArtifactContainerName() != null) {
                        templateInfo.setArtifactContainerName(templateMediator.getArtifactContainerName());
                    }
                    if (templateMediator.isEdited()) {
                        templateInfo.setIsEdited(true);
                    }
                }
            }

            if (info.length >= (templatePerPage * pageNumber + templatePerPage)) {
                ret = new TemplateInfo[templatePerPage];
            } else {
                ret = new TemplateInfo[info.length - (templatePerPage * pageNumber)];
            }
            for (int i = 0; i < templatePerPage; ++i) {
                if (ret.length > i) {
                    ret[i] = info[templatePerPage * pageNumber + i];
                }
            }
            return ret;
        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to " +
                            "get the available templates", fault);
        } finally {
            lock.unlock();
        }
        return null;
    }


    public int getTemplatesCount() throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            return getSynapseConfiguration().getSequenceTemplates().values().size();
        } catch (Exception e) {
            handleException("Couldn't get the Synapse Configuration to get Sequence count", e);
        } finally {
            lock.unlock();
        }
        return 0;
    }

    @SuppressWarnings({"unchecked"})
    public TemplateInfo[] getDynamicTemplates(int pageNumber, int sequencePerPage)
            throws AxisFault {
        
        org.wso2.carbon.registry.core.Registry registry;
        TemplateInfo[] ret;
        final Lock lock = getLock();
        try {
            lock.lock();
            String[] configInfo = getConfigSystemRegistry() != null?getMimeTypeResult(getConfigSystemRegistry()):new String[0];
            String[] govInfo = getGovernanceRegistry() != null?getMimeTypeResult(getGovernanceRegistry()): new String[0];
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
            if (info.length >= (sequencePerPage * pageNumber + sequencePerPage)) {
                ret = new TemplateInfo[sequencePerPage];
            } else {
                ret = new TemplateInfo[info.length - (sequencePerPage * pageNumber)];
            }
            for (int i = 0; i < sequencePerPage; ++i) {
                if (ret.length > i) {
                    TemplateInfo seq = new TemplateInfo();
                    seq.setName(info[sequencePerPage * pageNumber + i]);
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
    }


    public int getDynamicTemplateCount() throws AxisFault {
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
        parameters.put("1", WSO2_TEMPLATE_MEDIA_TYPE);
        Resource result = targetRegistry.executeQuery(null, parameters);
        return (String[]) result.getContent();
    }

/*
    */

    /**
     * Deletes the sequence with the given name from SynapseConfiguration
     *
     * @param templateName - Name of the sequence to delete
     * @throws AxisFault if the Sequence described by the given name doesnt
     *                                 exists in the Synapse Configuration
     */
    public void deleteTemplate(String templateName) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synCfg = getSynapseConfiguration();
            TemplateMediator sequence = synCfg.getSequenceTemplates().get(templateName);
            if (sequence != null) {
                synCfg.removeSequenceTemplate(templateName);
                MediationPersistenceManager pm = getMediationPersistenceManager();
                pm.deleteItem(templateName, sequence.getFileName(),
                              ServiceBusConstants.ITEM_TYPE_TEMPLATE);
            } else {
                handleException("No defined sequence with name " + templateName
                                + " found to delete in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to delete the sequence", fault);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Returns the OMelement representation of the sequence given by sequence
     * name
     *
     * @param templateName - name of the sequence to get
     * @return OMElement representing the SequenceMediator of the given sequence
     *         name
     * @throws AxisFault if any error occured while getting the data from the
     *                                 SynapseConfiguration
     */
    public OMElement getTemplate(String templateName) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            TemplateMediator template = synapseConfiguration.getSequenceTemplate(templateName);
            if (template != null) {
                return MediatorSerializerFinder.getInstance().getSerializer(
                        template).serializeMediator(null, template);
            } else {
                handleException("Template with the name "
                                + templateName + " does not exist");
            }
        } catch (SynapseException syne) {
            handleException("Unable to get the sequence : " + templateName, syne);
        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to get the Template", fault);
        } finally {
            lock.unlock();
        }
        return null;
    }


    /**
     * Add a sequence into the synapseConfiguration
     *
     * @param templateElement - Sequence object to be added as an OMElement
     * @throws AxisFault if a sequence exists with the same name or if the
     *                                 element provided is not a Sequence element
     */
    public void addTemplate(OMElement templateElement) throws AxisFault {
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
                    SynapseXMLConfigurationFactory.defineTemplate(config, templateElement,
                                                                  getSynapseConfiguration().getProperties());
                    if (log.isDebugEnabled()) {
                        log.debug("Added template : " + templateName + " to the configuration");
                    }

                    TemplateMediator templ = config.getSequenceTemplates().get(templateName);
                    templ.setFileName(ServiceBusUtils.generateFileName(templateName));
                    templ.init(getSynapseEnvironment());

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


    /**
     * Saves the sequence described with the sequence string
     *
     * @param templateElement - String representing the XML describing the
     *                        Sequence
     * @throws AxisFault if the sequence name already exists or if the string
     *                                 doesn't represent a Sequence element
     */
    public void saveTemplate(OMElement templateElement) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            if (templateElement != null && templateElement.getLocalName().equals(
                    XMLConfigConstants.TEMPLATE_ELT.getLocalPart())) {
                String templateName = templateElement.getAttributeValue(new QName("name"));
                SynapseConfiguration config = getSynapseConfiguration();
                log.debug("Saving template : " + templateName);
                TemplateMediator preSeq = config.getSequenceTemplates().get(templateName);
                if (preSeq == null) {
                    handleException("Unable to save template " + templateName + ". Does not exist");
                } else {
                    // we should first try to build the new sequence. if exception we return
                    Mediator mediator = MediatorFactoryFinder.getInstance().getMediator(
                            templateElement, getSynapseConfiguration().getProperties());

                    boolean statisticsEnable = preSeq.isStatisticsEnable();
                    // if everything went successfully we remove the sequence
                    config.removeSequenceTemplate(templateName);
                    if (mediator instanceof TemplateMediator) {
                        if (statisticsEnable) {
                            ((TemplateMediator) mediator).enableStatistics();
                        }
                        ((TemplateMediator) mediator).setFileName(preSeq.getFileName());
                        config.addSequenceTemplate(templateName, (TemplateMediator) mediator);
                    }

                    log.debug("Saved template : " + templateName + " to the configuration");

                    TemplateMediator templ = config.getSequenceTemplates().get(templateName);
                    if (templ != null) {
                        templ.init(getSynapseEnvironment());
                        if (preSeq.getArtifactContainerName() != null) {
                            templ.setArtifactContainerName(preSeq.getArtifactContainerName());
                            templ.setIsEdited(true);
                            // TODO check for the setIsEdited in TemplateInfo
                        }
                        else {
                            persistTemplate(templ);
                        }
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

    public String enableStatistics(String templateName) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            TemplateMediator template;
            template = (TemplateMediator) getSynapseConfiguration().getSequenceTemplate(templateName);
            if (template != null) {
                template.enableStatistics();
                persistTemplate(template);
                return templateName;
            } else {
                handleException("No defined template with name " + templateName
                                + " found to enable statistics in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't enable statistics of the template " + templateName
                            + " : " + fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String disableStatistics(String templateName) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            TemplateMediator template;
            template = getSynapseConfiguration().getSequenceTemplate(templateName);
            if (template != null) {
                template.disableStatistics();
                if (template.getArtifactContainerName() == null) {
                    persistTemplate(template);
                }
                return templateName;
            } else {
                handleException("No defined template with name " + templateName
                                + " found to disable statistics in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't disable statistics of the template " + templateName
                            + " : " + fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String enableTracing(String templateName) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            TemplateMediator template;
            template = (TemplateMediator) getSynapseConfiguration().getSequenceTemplate(templateName);
            if (template != null) {
                template.setTraceState(SynapseConstants.TRACING_ON);
                if (template.getArtifactContainerName() == null) {
                    persistTemplate(template);
                }
                return templateName;
            } else {
                handleException("No defined template with name " + templateName
                                + " found to enable tracing in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't enable tracing of the template " + templateName
                            + " : " + fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String disableTracing(String templateName) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            TemplateMediator template;
            template = (TemplateMediator) getSynapseConfiguration().getSequenceTemplate(templateName);
            if (template != null) {
                template.setTraceState(SynapseConstants.TRACING_OFF);
                if (template.getArtifactContainerName() == null) {
                    persistTemplate(template);
                }
                return templateName;
            } else {
                handleException("No defined template with name " + templateName
                                + " found to disable tracing in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't disable tracing of the template " + templateName
                            + " : " + fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    private void handleException(String message, Throwable cause) throws AxisFault {
        log.error(message, cause);
        throw new AxisFault(message, cause);
    }

    private void handleException(String message) throws AxisFault {
        log.error(message);
        throw new AxisFault(message);
    }

/*
    public String getEntryNamesString() throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Map globalEntriesMap = synapseConfiguration.getLocalRegistry();
            List<String> sequenceList = new ArrayList<String>();
            List<String> endpointList = new ArrayList<String>();
            List<String> entryList = new ArrayList<String>();
            StringBuffer entrySb = new StringBuffer();
            StringBuffer endpointSb = new StringBuffer();
            StringBuffer sequenceSb = new StringBuffer();
            for (Object entryValue : globalEntriesMap.values()) {

                if (entryValue instanceof Endpoint) {
                    Endpoint endpoint = (Endpoint) entryValue;
                    String name = endpoint.getName();
                    if (name != null) {
                        endpointList.add(name);
                    }
                } else if (entryValue instanceof SequenceMediator) {
                    SequenceMediator sequenceMediator = (SequenceMediator) entryValue;
                    String name = sequenceMediator.getName();
                    if (name != null) {
                        sequenceList.add(name);
                    }
                } else if (entryValue instanceof Entry) {
                    Entry entry = (Entry) entryValue;

                    if (!entry.isDynamic() && !entry.isRemote()) { // only care pre-defined local entries
                        String key = entry.getKey();
                        if (SynapseConstants.SERVER_IP.equals(key)
                            || SynapseConstants.SERVER_HOST.equals(key)) {
                            continue;
                        }
                        entryList.add(key);
                    }
                }
            }

            if (!sequenceList.isEmpty()) {

                Collections.sort(sequenceList, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                });
                for (String name : sequenceList) {
                    if (name != null) {
                        sequenceSb.append("[Sequence]-").append(name).append(" ");
                    }
                }
            }

            if (!entryList.isEmpty()) {

                Collections.sort(entryList, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                });
                for (String name : entryList) {
                    if (name != null) {
                        entrySb.append("[Entry]-").append(name).append(" ");
                    }
                }
            }

            if (!endpointList.isEmpty()) {

                Collections.sort(endpointList, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                });
                for (String name : endpointList) {
                    if (name != null) {
                        endpointSb.append("[Endpoint]-").append(name).append(" ");
                    }
                }
            }
            return endpointSb.toString() + entrySb.toString() + sequenceSb.toString();
        } catch (Exception axisFault) {
            handleException("Error during retrieving local registry", axisFault);
        } finally {
            lock.unlock();
        }
        return "";
    }
*/

    public void updateDynamicTemplate(String key, OMElement omElement)
            throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            deleteDynamicTemplate(key);
            addDynamicTemplate(key, omElement);
        } finally {
            lock.unlock();
        }
    }

    public void deleteDynamicTemplate(String key) throws AxisFault {
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

    public void addDynamicTemplate(String key, OMElement sequence) throws AxisFault {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.reset();
        try {
            XMLPrettyPrinter.prettify(sequence, stream);
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
            resource.setMediaType(WSO2_TEMPLATE_MEDIA_TYPE);
            resource.setContent(new String(stream.toByteArray()).trim());
            registry.put(key, resource);
        } catch (RegistryException e) {
            handleException("WSO2 Registry Exception", e);
        }
    }

    public OMElement getDynamicTemplate(String key) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synConfig = getSynapseConfiguration();
            Registry registry = synConfig.getRegistry();
            if (registry != null) {
                if (registry.getRegistryEntry(key).getType() == null) {
                    handleException("No resource is available by the key '" + key + "'");
                }
                return (OMElement) registry.getResource(new Entry(key),
                        getSynapseConfiguration().getProperties());
            } else {
                handleException("Unable to access the registry instance for the ESB");
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void saveDynamicTemplate(String key, OMElement sequence) throws AxisFault {
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
                registry.updateResource(key, sequence);
            } else {
                handleException("Unable to access the registry instance for the ESB");
            }
        } finally {
            lock.unlock();
        }
    }

/*
    public ConfigurationObject[] getDependents(String template) {
        DependencyManagementService dependencyMgr = ConfigHolder.getInstance().
                getDependencyManager();
        if (dependencyMgr != null) {
            org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject[] tempDependents =
                    dependencyMgr.getDependents(org.wso2.carbon.mediation.dependency.mgt
                                                        .ConfigurationObject.TYPE_SEQUENCE,
                                                template);
            if (tempDependents != null && tempDependents.length > 0) {
                List<ConfigurationObject> dependents = new ArrayList<ConfigurationObject>();
                for (int i = 0; i < tempDependents.length; i++) {
                    if (tempDependents[i].getType() !=
                        org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject.TYPE_UNKNOWN) {
                        dependents.add(new ConfigurationObject(tempDependents[i].getType(),
                                                               tempDependents[i].getId()));
                    }
                }

                if (dependents.size() > 0) {
                    return dependents.toArray(new ConfigurationObject[dependents.size()]);
                }
            }
        }
        return null;
    }
*/

    private void persistTemplate(Mediator template) throws AxisFault {
       if (template instanceof TemplateMediator) {
            MediationPersistenceManager pm = getMediationPersistenceManager();
            pm.saveItem(((TemplateMediator) template).getName(), ServiceBusConstants.ITEM_TYPE_TEMPLATE);
        }
    }
}
