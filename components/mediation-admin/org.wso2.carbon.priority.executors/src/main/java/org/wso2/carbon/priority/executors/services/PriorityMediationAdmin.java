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

package org.wso2.carbon.priority.executors.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.commons.executors.config.PriorityExecutorFactory;
import org.apache.synapse.commons.executors.config.PriorityExecutorSerializer;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.SynapseConstants;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;

@SuppressWarnings({"UnusedDeclaration"})
public class PriorityMediationAdmin extends AbstractServiceBusAdmin {

    private static final Log log = LogFactory.getLog(PriorityMediationAdmin.class);

    public void add(String name, OMElement executor) throws AxisFault {
        final Lock lock = getLock();

        try {
            lock.lock();
            SynapseConfiguration config = getSynapseConfiguration();
            PriorityExecutor ex = PriorityExecutorFactory.
                    createExecutor(SynapseConstants.SYNAPSE_NAMESPACE, executor,
                            true, new Properties());
            ex.setFileName(ServiceBusUtils.generateFileName(ex.getName()));
            ex.init();
            config.addPriorityExecutor(name, ex);
            if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                MediationPersistenceManager pm = getMediationPersistenceManager();
                pm.saveItem(name, ServiceBusConstants.ITEM_TYPE_EXECUTOR);
            }
            log.info("Adding priority-executor with name: " + name);
        } catch (AxisFault axisFault) {
            log.error("Error occurred while building a priority executor from " +
                    "the configuration element" + axisFault.getMessage());
            throw axisFault;
        } finally {
            lock.unlock();
        }
    }

    public OMElement getExecutor(String name) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration config = getSynapseConfiguration();

            Map<String, PriorityExecutor> exs = config.getPriorityExecutors();
            if (exs != null) {
                PriorityExecutor ex = exs.get(name);
                if (ex != null) {
                    return PriorityExecutorSerializer.serialize(
                            null, ex, SynapseConstants.SYNAPSE_NAMESPACE);
                } else {
                    String msg = "Cannot find an Priority Executor with the name:" + name;
                    log.error(msg);
                    throw new AxisFault(msg);
                }
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String[] getExecutorList() {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration config = getSynapseConfiguration();

            Map<String, PriorityExecutor> exs = config.getPriorityExecutors();
            if (exs != null && !exs.isEmpty()) {
                return exs.keySet().toArray(new String[exs.keySet().size()]);
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void update(String name, OMElement executor) throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration config = getSynapseConfiguration();

            PriorityExecutor oldExecutor = config.removeExecutor(name);

            if (oldExecutor != null) {
                oldExecutor.destroy();
                log.info("Removed priority executor with name: " + name);
                String oldFileName = oldExecutor.getFileName();

                PriorityExecutor ex = PriorityExecutorFactory.
                    createExecutor(SynapseConstants.SYNAPSE_NAMESPACE, executor,
                            true, new Properties());
                ex.setFileName(oldFileName);
                ex.init();
                config.addPriorityExecutor(name, ex);
                log.info("Updated and restored priority executor with name: " + name);
                if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                    MediationPersistenceManager pm = getMediationPersistenceManager();
                    pm.deleteItem(name, oldFileName, ServiceBusConstants.ITEM_TYPE_EXECUTOR);
                    pm.saveItem(name, ServiceBusConstants.ITEM_TYPE_EXECUTOR);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void remove(String name) {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration config = getSynapseConfiguration();

            PriorityExecutor executor = config.removeExecutor(name);

            if (executor != null) {
                executor.destroy();
                log.info("Removed priority executor with name: " + name);
                if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                    MediationPersistenceManager pm = getMediationPersistenceManager();
                    pm.deleteItem(name, executor.getFileName(),
                            ServiceBusConstants.ITEM_TYPE_EXECUTOR);
                }
            }
        } finally {
            lock.unlock();
        }
    }        
}
