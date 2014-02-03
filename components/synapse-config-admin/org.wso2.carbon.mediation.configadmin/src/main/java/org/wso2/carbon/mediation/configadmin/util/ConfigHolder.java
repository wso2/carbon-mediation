/*
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

package org.wso2.carbon.mediation.configadmin.util;

import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.mediation.dependency.mgt.services.ConfigurationTrackingService;
import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.HashMap;

public class ConfigHolder {

    private static ConfigHolder instance;

    private BundleContext bundleContext;
    private ConfigurationTrackingService configurationTrackingService;
    private Map<Integer, SynapseRegistrationsService> registrationsServices =
            new HashMap<Integer, SynapseRegistrationsService>();

    private ConfigHolder() {}

    public static ConfigHolder getInstance() {
        if(instance == null) {
            instance = new ConfigHolder();
        }
        return instance;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void addSynapseRegistrationService(int id,
                                              SynapseRegistrationsService registrationsService) {
        registrationsServices.put(id, registrationsService);
    }

    public SynapseRegistrationsService getSynapseRegistrationService(int id) {
        return registrationsServices.get(id);
    }

    public void removeSynapseRegistrationService(int id) {
        registrationsServices.remove(id);
    }

    public void setSynapseConfigTrackingService(ConfigurationTrackingService configTrackingService) {
        this.configurationTrackingService = configTrackingService;
    }

    public ConfigurationTrackingService getSynapseConfigTrackingService() {
        return configurationTrackingService;
    }
}
