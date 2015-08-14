/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.proxyadmin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.initializer.services.CAppArtifactDataService;
import org.wso2.carbon.proxyadmin.ProxyAdminException;
import org.wso2.carbon.proxyadmin.observer.ProxyObserver;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import java.util.Map;
import java.util.HashMap;

public class ConfigHolder {

    private static ConfigHolder instance;
    private static final Log log = LogFactory.getLog(ConfigHolder.class);
    private CAppArtifactDataService cAppArtifactDataService;
    private RegistryService registryService;

    private Map<Integer, ProxyObserver> proxyObservers = new HashMap<Integer, ProxyObserver>();

    private Map<Integer, SynapseEnvironmentService> synapseEnvironmentServices =
            new HashMap<Integer, SynapseEnvironmentService>();

    private ConfigHolder() {}

    public static ConfigHolder getInstance() {
        if(instance == null) {
            instance = new ConfigHolder();
        }
        return instance;
    }

    public CAppArtifactDataService getcAppArtifactDataService() {
        return cAppArtifactDataService;
    }

    public void setcAppArtifactDataService(CAppArtifactDataService cAppArtifactDataService) {
        this.cAppArtifactDataService = cAppArtifactDataService;
    }

    public RegistryService getRegistryService() throws ProxyAdminException {
        assertNull("Registry", registryService);
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    private void assertNull(String name, Object object) throws ProxyAdminException {
        if (object == null) {
            String message = name + " reference in the proxy admin config holder is null";
            log.error(message);
            throw new ProxyAdminException(message);
        }
    }

    public ProxyObserver getProxyObsever(int id) {
        return proxyObservers.get(id);
    }

    public void addProxyObserver(int id, ProxyObserver observer) {
        proxyObservers.put(id, observer);
    }

    public void removeProxyObserver(int id) {
        proxyObservers.remove(id);
    }

    public SynapseEnvironmentService getSynapseEnvironmentService(int id) {
        return synapseEnvironmentServices.get(id);
    }

    public void addSynapseEnvironmentService(int id,
                                             SynapseEnvironmentService synapseEnvironmentService) {
        synapseEnvironmentServices.put(id, synapseEnvironmentService);
    }

    public void removeSynapseEnvironmentService(int id) {
        synapseEnvironmentServices.remove(id);
    }

    public Map<Integer, SynapseEnvironmentService> getSynapseEnvironmentServices() {
        return synapseEnvironmentServices;
    }

    public Map<Integer, ProxyObserver> getProxyObservers() {
        return proxyObservers;
    }
}

