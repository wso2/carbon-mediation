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

package org.wso2.carbon.business.messaging.hl7.message.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.business.messaging.hl7.message.HL7MessageBuilder;
import org.wso2.carbon.business.messaging.hl7.message.HL7MessageFormatter;

/**
 * @scr.component name="hl7.message.services" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class HL7MessageServiceComponent {

    private static Log log = LogFactory.getLog(HL7MessageServiceComponent.class);
    private ConfigurationContextService contextService;

    public HL7MessageServiceComponent() {
    }

    protected void activate(ComponentContext ctxt) {
        ConfigurationContext configContext;
        if (log.isDebugEnabled()) {
            log.debug("HL7 Message Service activated");
        }

        try {
            if (contextService != null) {
                // Getting server's configContext instance
                configContext = contextService.getServerConfigContext();
            } else {
                throw new Exception(
                        "ConfigurationContext is not found while loading org.wso2.carbon.transport.fix bundle");
            }
            configContext.getAxisConfiguration()
                    .addMessageBuilder("application/edi-hl7", new HL7MessageBuilder());
            configContext.getAxisConfiguration()
                    .addMessageFormatter("application/edi-hl7", new HL7MessageFormatter());
            if (log.isDebugEnabled()) {
                log.info("Set the HL7 message builder and formatter in the Axis2 context");
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully registered the HL7 Message Service");
            }
        } catch (Throwable e) {
            log.error("Error while activating HL7 Message Bundle", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("HL7 Message Service deactivated");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        this.contextService = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        this.contextService = null;
    }
}
