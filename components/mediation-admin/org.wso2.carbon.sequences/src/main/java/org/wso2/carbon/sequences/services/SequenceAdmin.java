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

package org.wso2.carbon.sequences.services;

import org.apache.axiom.om.OMElement;
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
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.registry.Registry;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.CarbonConfigurationContextFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.mediation.dependency.mgt.services.DependencyManagementService;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.sequences.common.SequenceEditorException;
import org.wso2.carbon.sequences.common.factory.SequenceInfoFactory;
import org.wso2.carbon.sequences.common.to.ConfigurationObject;
import org.wso2.carbon.sequences.common.to.SequenceInfo;
import org.wso2.carbon.sequences.internal.ConfigHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes care of all the administration activities related to <code>Sequences</code>
 * in the initialized synapse environment</p>
 *
 * <p>The administration activities include;</p>
 *  <ul>
 *      <li>Adding sequences</li>
 *      <li>Editing sequences</li>
 *      <li>Saving sequence</li>
 *      <li>Deleting sequences</li>
 *      <li>Enabling/disabling statistics/tracing of sequences</li>
 *      <li>Editing of dynamic sequences</li>
 *      <li>and so on...</li>
 *  </ul>
 */
@SuppressWarnings({"UnusedDeclaration"})
public class SequenceAdmin extends AbstractServiceBusAdmin {

    private static final Log log = LogFactory.getLog(SequenceAdmin.class);

    //TODO: Move WSO2_SEQUENCE_MEDIA_TYPE to registry
    public static final String WSO2_SEQUENCE_MEDIA_TYPE ="application/vnd.wso2.sequence";

    public SequenceInfo[] getSequences(int pageNumber,int sequencePerPage)
            throws SequenceEditorException {
        final Lock lock = getLock();
        Collection<SequenceMediator> sequences = null;
        try {
            lock.lock();
            sequences = getSynapseConfiguration().getDefinedSequences().values();

            SequenceInfo[] info = SequenceInfoFactory.getSortedSequenceInfoArray(sequences);
            SequenceInfo[] ret;
            if (info.length >= (sequencePerPage * pageNumber + sequencePerPage)) {
                ret = new SequenceInfo[sequencePerPage];
            } else {
                ret = new SequenceInfo[info.length - (sequencePerPage * pageNumber)];
            }
            for (int i = 0; i < sequencePerPage; ++i) {
                if (ret.length > i)
                    ret[i] = info[sequencePerPage * pageNumber + i];
            }
            return ret;
        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to " +
                    "get the available sequences", fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public int getSequencesCount() throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            return getSynapseConfiguration().getDefinedSequences().values().size();
        } catch (Exception e) {
            handleException("Couldn't get the Synapse Configuration to get Sequence count", e);
        } finally {
            lock.unlock();
        }
        return 0;
    }
    
    @SuppressWarnings({"unchecked"})
    public SequenceInfo[] getDynamicSequences(int pageNumber, int sequencePerPage)
            throws SequenceEditorException {
        
        org.wso2.carbon.registry.core.Registry registry;
        SequenceInfo[] ret;
        final Lock lock = getLock();
        try {
            lock.lock();
            String[] configInfo = getConfigSystemRegistry() !=null?getMimeTypeResult(getConfigSystemRegistry()):new String[0];
            String[] govInfo = getGovernanceRegistry() != null ?getMimeTypeResult(getGovernanceRegistry()):new String[0];
            String[] info = new String[(configInfo != null?configInfo.length:0) + (govInfo !=null?govInfo.length:0)];
            
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
                ret = new SequenceInfo[sequencePerPage];
            } else {
                ret = new SequenceInfo[info.length - (sequencePerPage * pageNumber)];
            }
            for (int i = 0; i < sequencePerPage; ++i) {
                if (ret.length > i) {
                    SequenceInfo seq = new SequenceInfo();
                    seq.setName(info[sequencePerPage * pageNumber + i]);
                    ret[i] = seq;
                }
            }
        } catch (Exception e) {
            handleException("Unable to get Dynamic Sequence Info",e);
            return null;
        } finally {
            lock.unlock();
        }
        return ret;
    }


    public int getDynamicSequenceCount() throws SequenceEditorException {
        try {
            String[] govList = getGovernanceRegistry() !=null ?getMimeTypeResult(getGovernanceRegistry()) : new String[0];
            String[] confList = getConfigSystemRegistry() !=null ? getMimeTypeResult(getConfigSystemRegistry()) : new String[0];
            return (confList  != null ?confList.length:0) + (govList != null ?govList.length:0);
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
        parameters.put("1", WSO2_SEQUENCE_MEDIA_TYPE);
        Resource result = targetRegistry.executeQuery(null, parameters);
        return (String[]) result.getContent();
    }

    /**
     * Deletes the sequence with the given name from SynapseConfiguration
     *
     * @param sequenceName - Name of the sequence to delete
     * @throws SequenceEditorException if the Sequence described by the given name doesn't
     *                   exist in the Synapse Configuration
     */
    public void deleteSequence(String sequenceName) throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synCfg = getSynapseConfiguration();
            SequenceMediator sequence = synCfg.getDefinedSequences().get(sequenceName);
            if (sequence != null) {
                synCfg.removeSequence(sequenceName);
                MediationPersistenceManager pm = getMediationPersistenceManager();
                pm.deleteItem(sequenceName, sequence.getFileName(),
                        ServiceBusConstants.ITEM_TYPE_SEQUENCE);
            } else {
                handleException("No defined sequence with name " + sequenceName
                        + " found to delete in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to delete the sequence", fault);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Set the tenant domain when a publisher deletes custom sequences in MT mode. When publisher 
     * deletes the sequence, we login the gateway as supertenant. But we need to delete in the particular 
	 * tenant domain.
	 *
     * @param sequenceName
     * @param tenantDomain
     * @throws SequenceEditorException 
     */
    public void deleteSequenceForTenant(String sequenceName, String tenantDomain) throws SequenceEditorException{

		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
			                                                                      true);
			deleteSequence(sequenceName);
		} catch (Exception e) {
			  handleException("Issue is in deleting the sequence definition");
        } finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
    }
    /**
     * Delete Selected Sequence in the synapse configuration
     *
     * @param sequenceNames
     * @throws SequenceEditorException
     */

    public void deleteSelectedSequence(String[] sequenceNames) throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synCfg = getSynapseConfiguration();
            List<String> list = new ArrayList<String>();
            Collections.addAll(list, sequenceNames);
            list.remove("main");
            list.remove("fault");
            sequenceNames = list.toArray(new String[list.size()]);
            for (String sequenceName : sequenceNames) {
                SequenceMediator sequence = synCfg.getDefinedSequences().get(sequenceName);
                if (sequence != null) {
                    synCfg.removeSequence(sequenceName);
                    MediationPersistenceManager pm = getMediationPersistenceManager();
                    pm.deleteItem(sequenceName, sequence.getFileName(),
                            ServiceBusConstants.ITEM_TYPE_SEQUENCE);
                }
            }
        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to delete the sequence", fault);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Delete all the sequences in the synapse configuration
     *
     * @throws SequenceEditorException
     */
    public void deleteAllSequence() throws SequenceEditorException {
        final Lock lock = getLock();
        Collection<SequenceMediator> sequences = null;
        try {
            lock.lock();
            sequences = getSynapseConfiguration().getDefinedSequences().values();

            SynapseConfiguration synCfg = getSynapseConfiguration();
            for (SequenceMediator sequence : sequences) {
                if (sequence != null) {
                    if ((!sequence.getName().equals("main")) && (!sequence.getName().equals("fault"))) {
                        synCfg.removeSequence(sequence.getName());
                        MediationPersistenceManager pm = getMediationPersistenceManager();
                        pm.deleteItem(sequence.getName(), sequence.getFileName(),
                                ServiceBusConstants.ITEM_TYPE_SEQUENCE);
                    }
                }
            }
        } catch (Exception fault) {
            handleException("Unable to delete entire sequences in the synapse configuration", fault);
        } finally {
            lock.unlock();
        }
    }
    /**
     * Returns the OMelement representation of the sequence given by sequence
     * name
     *
     * @param sequenceName - name of the sequence to get
     * @return OMElement representing the SequenceMediator of the given sequence
     *         name
     * @throws SequenceEditorException if any error occured while getting the data from the
     *                   SynapseConfiguration
     */
    public OMElement getSequence(String sequenceName) throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            if (synapseConfiguration.getSequence(sequenceName) != null) {
                return MediatorSerializerFinder.getInstance().getSerializer(
                        synapseConfiguration.getSequence(sequenceName))
                        .serializeMediator(null, synapseConfiguration
                                .getSequence(sequenceName));
            } else {
                handleException("Sequence with the name "
                        + sequenceName + " does not exist");
            }
        } catch (SynapseException syne) {
            handleException("Unable to get the sequence : " + sequenceName, syne);
        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to get the sequence", fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * Set the tenant domain when a publisher tries to get custom sequences in MT mode. When publisher 
     * tries to get the sequence, we login the gateway as supertenant. But we need to get in the particular 
	 * tenant domain.
	 *
     * @param sequenceName
     * @param tenantDomain
     * @throws SequenceEditorException 
     * 
     */
    public OMElement getSequenceForTenant(String sequenceName, String tenantDomain) throws SequenceEditorException{

		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
			                                                                      true);
			OMElement seq = getSequence(sequenceName);
			return seq;
		} catch (Exception e) {
			  handleException("Issue is in getting the sequence definition");
        } finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
		return null;
    }
    /**
     * Add a sequence into the synapseConfiguration
     *
     * @param sequenceElement - Sequence object to be added as an OMElement
     * @throws SequenceEditorException if a sequence exists with the same name or if the
     *                   element provided is not a Sequence element
     */
    public void addSequence(OMElement sequenceElement) throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            if (sequenceElement.getLocalName().equals(
                    XMLConfigConstants.SEQUENCE_ELT.getLocalPart())) {
                String sequenceName = sequenceElement.getAttributeValue(new QName("name"));
                if("".equals(sequenceName) || null == sequenceName) {
                    handleException("sequence name is required.");   
                }                 
                SynapseConfiguration config = getSynapseConfiguration();
                if(log.isDebugEnabled()) {
                    log.debug("Adding sequence : " + sequenceName + " to the configuration");
                }
                if (config.getLocalRegistry().get(sequenceName) != null) {
                    handleException("The name '" + sequenceName +
                        "' is already used within the configuration - a sequence or local entry with this name already exists");
                } else {
                    SynapseXMLConfigurationFactory.defineSequence(config, sequenceElement,
                            getSynapseConfiguration().getProperties());
                    if(log.isDebugEnabled()) {
                        log.debug("Added sequence : " + sequenceName + " to the configuration");
                    }
                    
                    SequenceMediator seq = config.getDefinedSequences().get(sequenceName);
                    seq.setFileName(ServiceBusUtils.generateFileName(sequenceName));
                    seq.init(getSynapseEnvironment());

                    //noinspection ConstantConditions
                    persistSequence(seq);
                }
            } else {
                handleException("Invalid sequence definition");
            }
        } catch (Exception fault) {
            handleException("Error adding sequence : " + fault.getMessage(), fault);
        } catch (Error error) {
            throw new SequenceEditorException("Unexpected error occured while " +
                    "adding the sequence : " + error.getMessage(), error);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Set the tenant domain when a publisher deploys custom sequences in MT mode. When publisher 
     * deploys the sequence, we login the gateway as supertenant. But we need to deploy in the particular 
	 * tenant domain.
	 *
     * @param sequenceElement
     * @param tenantDomain
     * @throws SequenceEditorException 
     */
    public void addSequenceForTenant(OMElement sequenceElement, String tenantDomain) throws SequenceEditorException{

		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
			                                                                      true);
			addSequence(sequenceElement);
		
		} catch (Exception e) {
			  handleException("Issue in deploying the sequence definition");
        } finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
    }

    public boolean isExistingSequence(String sequenceName){
        SynapseConfiguration config = getSynapseConfiguration();
        if (config.getLocalRegistry().get(sequenceName) != null){
            return true;
        }
        return false;
    }

    public boolean isExistingSequenceForTenant(String sequenceName, String tenantDomain){
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
                true);

        return isExistingSequence(sequenceName);
    }

    /**
     * Saves the sequence described with the sequence string
     *
     * @param sequenceElement - String representing the XML describing the
     *                        Sequence
     * @throws SequenceEditorException if the sequence name already exists or if the string
     *                   doesn't represent a Sequence element
     */
    public void saveSequence(OMElement sequenceElement) throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();

            /**The SynapseXPathFactory adds all namespaces defined in the upper levels of the xpath element to the xpath.
                        *Remove the sequence element from its parent to avoid inheritance of unwanted namespaces (ex: namespace of
                        *the admin service operation)*/
            sequenceElement.detach();

            if (sequenceElement != null && sequenceElement.getLocalName().equals(
                    XMLConfigConstants.SEQUENCE_ELT.getLocalPart())) {
                String sequenceName = sequenceElement.getAttributeValue(new QName("name"));
                SynapseConfiguration config = getSynapseConfiguration();
                log.debug("Saving sequence : " + sequenceName);
                SequenceMediator preSeq = config.getDefinedSequences().get(sequenceName);
                if (preSeq == null) {
                    handleException("Unable to save sequence " + sequenceName + ". Does not exist");
                } else {
                    // we should first try to build the new sequence. if exception we return
                    Mediator mediator = MediatorFactoryFinder.getInstance().getMediator(
                            sequenceElement, getSynapseConfiguration().getProperties());

                    boolean statisticsEnable = preSeq.isStatisticsEnable();
                    // if everything went successfully we remove the sequence
                    config.removeSequence(sequenceName);
                    if (mediator instanceof SequenceMediator) {
                        if (statisticsEnable) {
                            ((SequenceMediator) mediator).enableStatistics();
                        }
                        ((SequenceMediator)mediator).setFileName(preSeq.getFileName());
                    }

                    config.addSequence(sequenceName, mediator);
                    log.debug("Saved sequence : " + sequenceName + " to the configuration");

                    SequenceMediator seq = config.getDefinedSequences().get(sequenceName);
                    if (seq != null) {
                        seq.init(getSynapseEnvironment());
                        persistSequence(seq);
                    }
                }
            } else {
                handleException("Unable to save sequence. Invalid definition");
            }
        } catch (Exception fault) {
            handleException("Unable to save the Sequence : " + fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
    }

    public String enableStatistics(String sequenceName) throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SequenceMediator sequence
                    = (SequenceMediator) getSynapseConfiguration().getSequence(sequenceName);
            if (sequence != null) {
                sequence.enableStatistics();
                persistSequence(sequence);
                return sequenceName;
            } else {
                handleException("No defined sequence with name " + sequenceName
                        + " found to enable statistics in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't enable statistics of the sequence " + sequenceName
                    + " : " + fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String disableStatistics(String sequenceName) throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SequenceMediator sequence
                    = (SequenceMediator) getSynapseConfiguration().getSequence(sequenceName);
            if (sequence != null) {
                sequence.disableStatistics();
                persistSequence(sequence);
                return sequenceName;
            } else {
                handleException("No defined sequence with name " + sequenceName
                        + " found to disable statistics in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't disable statistics of the sequence " + sequenceName
                    + " : " + fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String enableTracing(String sequenceName) throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SequenceMediator sequence
                    = (SequenceMediator) getSynapseConfiguration().getSequence(sequenceName);
            if (sequence != null) {
                sequence.setTraceState(SynapseConstants.TRACING_ON);
                persistSequence(sequence);
                return sequenceName;
            } else {
                handleException("No defined sequence with name " + sequenceName
                        + " found to enable tracing in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't enable tracing of the sequence " + sequenceName
                    + " : " + fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String disableTracing(String sequenceName) throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SequenceMediator sequence
                    = (SequenceMediator) getSynapseConfiguration().getSequence(sequenceName);
            if (sequence != null) {
                sequence.setTraceState(SynapseConstants.TRACING_OFF);
                persistSequence(sequence);
                return sequenceName;
            } else {
                handleException("No defined sequence with name " + sequenceName
                        + " found to disable tracing in the Synapse configuration");
            }
        } catch (Exception fault) {
            handleException("Couldn't disable tracing of the sequence " + sequenceName
                    + " : " + fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    private void handleException(String message, Throwable cause) throws SequenceEditorException {
        log.error(message, cause);
        throw new SequenceEditorException(message, cause);
    }

    private void handleException(String message) throws SequenceEditorException {
        log.error(message);
        throw new SequenceEditorException(message);
    }

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

    public void updateDynamicSequence(String key, OMElement omElement)
            throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            deleteDynamicSequence(key);
            addDynamicSequence(key, omElement);
        } finally {
            lock.unlock();
        }
    }

    public void deleteDynamicSequence(String key) throws SequenceEditorException {
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

    public void addDynamicSequence(String key, OMElement sequence) throws SequenceEditorException {
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
            resource.setMediaType(WSO2_SEQUENCE_MEDIA_TYPE);
            resource.setContent(new String(stream.toByteArray()).trim());
            registry.put(key, resource);
        } catch (RegistryException e) {
            handleException("WSO2 Registry Exception", e);
        }
    }

    public OMElement getDynamicSequence(String key) throws SequenceEditorException {
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

    public void saveDynamicSequence(String key, OMElement sequence) throws SequenceEditorException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synConfig = getSynapseConfiguration();
            Registry registry = synConfig.getRegistry();
            if (registry != null) {
                if (registry.getRegistryEntry(key).getType() == null) {
                    handleException("Unable to save the sequence. No resource is available " +
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

    public ConfigurationObject[] getDependents(String sequence) {
        DependencyManagementService dependencyMgr = ConfigHolder.getInstance().
                getDependencyManager();
        if (dependencyMgr != null) {
            org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject[] tempDependents =
                    dependencyMgr.getDependents(org.wso2.carbon.mediation.dependency.mgt
                            .ConfigurationObject.TYPE_SEQUENCE,
                            sequence);
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

    private void persistSequence(SequenceMediator sequence) throws SequenceEditorException {
        MediationPersistenceManager pm = getMediationPersistenceManager();
        if (pm == null){
            handleException("Cannot Persist sequence because persistence manager is null, " +
                    "probably persistence is disabled");
        }
        pm.saveItem(sequence.getName(), ServiceBusConstants.ITEM_TYPE_SEQUENCE);
    }
    
    /**
 	 * Override the AbstarctAdmin.java's getAxisConfig() to create the CarbonContext from ThreadLoaclContext.
 	 * We do this to support, publishing APIs as a supertenant but want to deploy that in tenant space.
 	 * (This model is needed for APIManager)
 	 */
 	
 	protected AxisConfiguration getAxisConfig() {
 		return (axisConfig != null) ? axisConfig : getConfigContext().getAxisConfiguration();
 	}

 	protected ConfigurationContext getConfigContext() {
 		if (configurationContext != null) {
 			return configurationContext;
 		}
 		
 		MessageContext msgContext = MessageContext.getCurrentMessageContext();
 		if (msgContext != null) {
 			ConfigurationContext mainConfigContext = msgContext.getConfigurationContext();

 			// If a tenant has been set, then try to get the
 			// ConfigurationContext of that tenant
 			PrivilegedCarbonContext carbonContext =
 			                                        PrivilegedCarbonContext.getThreadLocalCarbonContext();
 			String domain = carbonContext.getTenantDomain();
 			if (domain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(domain)) {
 				return TenantAxisUtils.getTenantConfigurationContext(domain, mainConfigContext);
 			} else if (carbonContext.getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
 				return mainConfigContext;
 			} else {
 				throw new UnsupportedOperationException(
 				                                        "Tenant domain unidentified. "
 				                                                + "Upstream code needs to identify & set the tenant domain & tenant ID. "
 				                                                + " The TenantDomain SOAP header could be set by the clients or "
 				                                                + "tenant authentication should be carried out.");
 			}
 		} else {
 			return CarbonConfigurationContextFactory.getConfigurationContext();
 		}
 	}
    private boolean isServiceSatisfySearchString(String searchString, String sequenceName) {
        if (searchString != null) {
            String regex = searchString.toLowerCase().
                    replace("..?", ".?").replace("..*", ".*").
                    replaceAll("\\?", ".?").replaceAll("\\*", ".*?");

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sequenceName.toLowerCase());

            return regex.trim().length() == 0 || matcher.find();
        }
        return false;
    }

    public SequenceInfo[] getSequencesSearch(String searchText)
            throws SequenceEditorException {

        final Lock lock = getLock();
        Collection<SequenceMediator> sequences = null;
        List<SequenceInfo> searchedSequenceInfo = null;

        try {
            lock.lock();
            sequences = getSynapseConfiguration().getDefinedSequences().values();
            SequenceInfo[] info = SequenceInfoFactory.getSortedSequenceInfoArray(sequences);

            if (info != null && info.length > 0) {
                searchedSequenceInfo = new ArrayList<SequenceInfo>();
                for (SequenceInfo infoTemp : info) {
                    SequenceInfo seqInfo = new SequenceInfo();
                    seqInfo.setEnableStatistics(infoTemp.isEnableTracing());
                    seqInfo.setEnableTracing(infoTemp.isEnableTracing());
                    seqInfo.setName(infoTemp.getName());
                    seqInfo.setDescription(infoTemp.getDescription());
                    if (this.isServiceSatisfySearchString(searchText, seqInfo.getName())) {
                        searchedSequenceInfo.add(seqInfo);
                    }
                }
            }

            if (searchedSequenceInfo != null && searchedSequenceInfo.size() > 0) {
                return searchedSequenceInfo.toArray(new SequenceInfo[searchedSequenceInfo.size()]);
            }

        } catch (Exception fault) {
            handleException("Couldn't get the Synapse Configuration to " +
                    "search sequences", fault);
        } finally {
            lock.unlock();
        }
        return null;
    }
}
