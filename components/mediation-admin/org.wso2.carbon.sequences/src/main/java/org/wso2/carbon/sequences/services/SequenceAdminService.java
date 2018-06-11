/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.sequences.services;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MediatorSerializerFinder;
import org.apache.synapse.config.xml.SynapseXMLConfigurationFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.sequences.SequenceAdminUtil;
import org.wso2.carbon.sequences.common.SequenceEditorException;

import javax.xml.namespace.QName;
import java.util.concurrent.locks.Lock;

public class SequenceAdminService {

    private static final Log log = LogFactory.getLog(SequenceAdminService.class);

    /**
     * Deletes the sequence with the given name from SynapseConfiguration
     *
     * @param sequenceName - Name of the sequence to delete
     * @throws org.wso2.carbon.sequences.common.SequenceEditorException
     *          if the Sequence described by the given name doesn't
     *          exist in the Synapse Configuration
     */
    public void deleteSequence(String sequenceName) throws SequenceEditorException {
        final Lock lock = SequenceAdminUtil.getLock();
        try {
            lock.lock();
            SynapseConfiguration synCfg = SequenceAdminUtil.getSynapseConfigFromConfigCtx();
            SequenceMediator sequence = synCfg.getDefinedSequences().get(sequenceName);
            if (sequence != null && sequence.getArtifactContainerName() == null) {
                synCfg.removeSequence(sequenceName);
                if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                    MediationPersistenceManager pm = SequenceAdminUtil.getMediationPersistenceManager();
                    pm.deleteItem(sequenceName, sequence.getFileName(),
                            ServiceBusConstants.ITEM_TYPE_SEQUENCE);
                }
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
     * Add a sequence into the synapseConfiguration
     *
     * @param sequenceElement - Sequence object to be added as an OMElement
     * @throws SequenceEditorException if a sequence exists with the same name or if the
     *                                 element provided is not a Sequence element
     */
    public void addSequence(OMElement sequenceElement) throws SequenceEditorException {
        final Lock lock = SequenceAdminUtil.getLock();
        try {
            lock.lock();
            if (sequenceElement.getLocalName().equals(
                    XMLConfigConstants.SEQUENCE_ELT.getLocalPart())) {
                String sequenceName = sequenceElement.getAttributeValue(new QName("name"));
                if ("".equals(sequenceName) || null == sequenceName) {
                    handleException("sequence name is required.");
                }
                SynapseConfiguration config = SequenceAdminUtil.getSynapseConfigFromConfigCtx();
                if (log.isDebugEnabled()) {
                    log.debug("Adding sequence : " + sequenceName + " to the configuration");
                }
                if (config.getLocalRegistry().get(sequenceName) != null) {
                    handleException("The name '" + sequenceName +
                            "' is already used within the configuration - a sequence or local entry with this " +
                            "name already exists");
                } else {
                    SynapseXMLConfigurationFactory.defineSequence(config, sequenceElement,
                            SequenceAdminUtil.getSynapseConfigFromConfigCtx().getProperties());
                    if (log.isDebugEnabled()) {
                        log.debug("Added sequence : " + sequenceName + " to the configuration");
                    }

                    SequenceMediator seq = config.getDefinedSequences().get(sequenceName);
                    seq.setFileName(ServiceBusUtils.generateFileName(sequenceName));
                    seq.init(SequenceAdminUtil.getSynapseEnvironmentFromConfigCtx());

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

    public boolean isExistingSequence(String sequenceName) throws SequenceEditorException {
        SynapseConfiguration config = SequenceAdminUtil.getSynapseConfiguration();
        return config.getLocalRegistry().get(sequenceName) != null;
    }

    public boolean isExistingSequenceForTenant(String sequenceName,
                                               String tenantDomain) throws SequenceEditorException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
                true);

        return isExistingSequence(sequenceName);
    }

    /**
     * Returns the OMelement representation of the sequence given by sequence
     * name
     *
     * @param sequenceName - name of the sequence to get
     * @return OMElement representing the SequenceMediator of the given sequence
     *         name
     * @throws SequenceEditorException if any error occured while getting the data from the
     *                                 SynapseConfiguration
     */
    public OMElement getSequence(String sequenceName) throws SequenceEditorException {
        final Lock lock = SequenceAdminUtil.getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = SequenceAdminUtil.getSynapseConfigFromConfigCtx();
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

    private void handleException(String message, Throwable cause) throws SequenceEditorException {
        log.error(message, cause);
        throw new SequenceEditorException(message, cause);
    }

    private void handleException(String message) throws SequenceEditorException {
        log.error(message);
        throw new SequenceEditorException(message);
    }

    private void persistSequence(SequenceMediator sequence) throws SequenceEditorException {
        if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
            MediationPersistenceManager pm = SequenceAdminUtil.getMediationPersistenceManager();
            if (pm == null) {
                handleException("Cannot Persist sequence because persistence manager is null, " +
                        "probably persistence is disabled");
            }
            pm.saveItem(sequence.getName(), ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        }
    }

}
