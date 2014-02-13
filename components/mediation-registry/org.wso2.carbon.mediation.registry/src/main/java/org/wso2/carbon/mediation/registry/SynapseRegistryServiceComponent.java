/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.mediation.registry.services.SynapseRegistryService;
import org.wso2.carbon.mediation.registry.services.SynapseRegistryServiceImpl;
import org.wso2.carbon.mediation.registry.persistence.PersistenceManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.Properties;


/**
 * @scr.component name="mediation.registry" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */
public class SynapseRegistryServiceComponent {

    private static Log log = LogFactory.getLog(SynapseRegistryServiceComponent.class);

    public SynapseRegistryServiceComponent() {
    }

    protected void activate(ComponentContext ctxt) {
        ctxt.getBundleContext().registerService(SynapseRegistryService.class.getName(),
                new SynapseRegistryServiceImpl(), null);
    }

    protected void deactivate(ComponentContext ctxt) {
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the SynapseRegistry initialization process");
        }
        RegistryServiceHolder.getInstance().setRegistryService(registryService);

        try {
            PersistenceManager.getInstance().setRegistry(registryService.getConfigSystemRegistry());
        } catch (RegistryException e) {
            log.error("Error while accessing the registry", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the SynapseRegistry");
        }
        RegistryServiceHolder.getInstance().setRegistryService(null);
        PersistenceManager.getInstance().setRegistry(null);
    }
}
