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
package org.wso2.carbon.sequences;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.ServerContextInformation;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.sequences.common.SequenceEditorException;
import org.wso2.carbon.sequences.internal.ConfigHolder;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SequenceAdminUtil {

    private static final Log log = LogFactory.getLog(SequenceAdminUtil.class);

    public static SynapseConfiguration getSynapseConfiguration() throws SequenceEditorException {
        return (SynapseConfiguration) ConfigHolder.getInstance().getAxisConfiguration().getParameter(
                SynapseConstants.SYNAPSE_CONFIG).getValue();
    }

    /**
     * Helper method to retrieve the Synapse environment from the relevant axis configuration
     *
     * @return extracted SynapseEnvironment from the relevant AxisConfiguration
     */
    public static SynapseEnvironment getSynapseEnvironment() throws SequenceEditorException {
        return getSynapseEnvironment(ConfigHolder.getInstance().getAxisConfiguration());
    }

    public static SynapseEnvironment getSynapseEnvironment(AxisConfiguration axisCfg) {
        return (SynapseEnvironment) axisCfg.getParameter(
                SynapseConstants.SYNAPSE_ENV).getValue();
    }

    /**
     * Helper method to get the persistence manger
     *
     * @return persistence manager for this configuration context
     */
    public static MediationPersistenceManager getMediationPersistenceManager() throws SequenceEditorException {
        return ServiceBusUtils.getMediationPersistenceManager(ConfigHolder.getInstance().getAxisConfiguration());
    }

    public static ServerConfigurationInformation getServerConfigurationInformation() throws SequenceEditorException {
        Parameter p = ConfigHolder.getInstance().getAxisConfiguration().getParameter(
                SynapseConstants.SYNAPSE_SERVER_CONFIG_INFO);
        if (p != null) {
            return (ServerConfigurationInformation) p.getValue();
        }
        return null;
    }

    public static ServerContextInformation getServerContextInformation() throws SequenceEditorException {
        Parameter p = ConfigHolder.getInstance().getAxisConfiguration().getParameter(
                SynapseConstants.SYNAPSE_SERVER_CTX_INFO);
        if (p != null) {
            return (ServerContextInformation) p.getValue();
        }
        return null;
    }

    public static Lock getLock() throws SequenceEditorException {
        Parameter p = ConfigHolder.getInstance().getAxisConfiguration().getParameter(
                ServiceBusConstants.SYNAPSE_CONFIG_LOCK);
        if (p != null) {
            return (Lock) p.getValue();
        } else {
            log.warn(ServiceBusConstants.SYNAPSE_CONFIG_LOCK + " is null, Recreating a new lock");
            Lock lock = new ReentrantLock();
            try {
                ConfigHolder.getInstance().getAxisConfiguration().addParameter(
                        ServiceBusConstants.SYNAPSE_CONFIG_LOCK, lock);
                return lock;
            } catch (AxisFault axisFault) {
                log.error("Error while setting " + ServiceBusConstants.SYNAPSE_CONFIG_LOCK);
            }
        }

        return null;
    }

}
