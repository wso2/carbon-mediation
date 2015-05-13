package org.wso2.carbon.inbound.endpoint.osgi.service;

/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="inbound.endpoint.service" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class InboundEndpointServiceDSComponent {

    private static final Log log = LogFactory.getLog(InboundEndpointServiceDSComponent.class);

    protected void activate(ComponentContext ctx) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Activating Inbound Endpoint service....!");
        }
        BundleContext bndCtx = ctx.getBundleContext();
        bndCtx.registerService(InboundEndpointService.class.getName(),
                new InboundEndpointServiceImpl(), null);

    }

    protected void deactivate(ComponentContext compCtx) throws Exception {
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService bound to the ESB initialization process");
        }
        ServiceReferenceHolder.getInstance().setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unbound from the ESB environment");
        }
        ServiceReferenceHolder.getInstance().setConfigurationContextService(null);
    }
}
