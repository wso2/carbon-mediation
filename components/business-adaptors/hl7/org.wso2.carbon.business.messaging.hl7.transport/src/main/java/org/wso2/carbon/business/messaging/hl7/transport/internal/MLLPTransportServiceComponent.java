/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.business.messaging.hl7.transport.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.business.messaging.hl7.transport.service.HL7TransportService;
import org.wso2.carbon.core.transports.TransportService;
import org.wso2.carbon.core.transports.TransportPersistenceManager;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="mllp.transport.services" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class MLLPTransportServiceComponent {

    private static Log log = LogFactory.getLog(MLLPTransportServiceComponent.class);

    private ConfigurationContextService contextService;

    public MLLPTransportServiceComponent() {
    }

    protected void activate(ComponentContext ctxt) {
        ConfigurationContext configContext;
        HL7TransportService HL7Transport;
        //Properties props;
        if (log.isDebugEnabled()) {
            log.debug("MLLP Transport bundle is activated");
        }

        try {
            if (contextService != null) {
                // Getting server's configContext instance
                configContext = contextService.getServerConfigContext();
            } else {
                throw new Exception(
                        "ConfigurationContext is not found while loading org.wso2.carbon.business.messaging.hl7.transport bundle");
            }

            // Save the transport configuration in the registry if not already done so
            new TransportPersistenceManager(configContext.getAxisConfiguration()).saveTransportConfiguration(HL7TransportService.TRANSPORT_NAME,
                    ctxt.getBundleContext().getBundle().getResource(HL7TransportService.TRANSPORT_CONF));

            // Instantiate HL7TransportService
            HL7Transport = new HL7TransportService();

            // This should ideally contain properties of HL7TransportService as a collection of
            // key/value pair. Here we do not require to add any elements.
            //props = new Properties();

            // Register the HL7TransportService under TransportService interface.
            // This will make TransportManagement component to find this.
            ctxt.getBundleContext()
                    .registerService(TransportService.class.getName(), HL7Transport, null);

            if (log.isDebugEnabled()) {
                log.debug("Successfully registered the https transport service");
            }
        } catch (Throwable e) {
            log.error("Error while activating MLLP transport management bundle", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("MLLP Transport bundle is deactivated ");
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        this.contextService = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        this.contextService = null;
    }
}
