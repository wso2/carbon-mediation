/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.websocket.transport.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.websocket.transport.service.SecureWebsocketTransportService;
import org.wso2.carbon.websocket.transport.service.ServiceReferenceHolder;
import org.wso2.carbon.websocket.transport.service.WebsocketTransportService;
import org.wso2.carbon.core.transports.TransportService;
import org.wso2.carbon.core.transports.TransportPersistenceManager;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "ws.transport.services",
        immediate = true)
public class WebsocketTransportServiceComponent {

    private static Log log = LogFactory.getLog(WebsocketTransportServiceComponent.class);

    private ConfigurationContextService contextService;

    public WebsocketTransportServiceComponent() {

    }

    @Activate
    protected void activate(ComponentContext ctxt) {

        ConfigurationContext configContext;
        WebsocketTransportService WebsocketTransport;
        SecureWebsocketTransportService SecureWebsocketTransport;
        if (log.isDebugEnabled()) {
            log.debug("Websocket Transport bundle is activated");
        }
        try {
            if (contextService != null) {
                configContext = contextService.getServerConfigContext();
            } else {
                throw new Exception("ConfigurationContext is not found while loading org.wso2.carbon.websocket" +
                        ".transport bundle");
            }
            new TransportPersistenceManager(configContext.getAxisConfiguration()).saveTransportConfiguration
                    (WebsocketTransportService.TRANSPORT_NAME, ctxt.getBundleContext().getBundle().getResource
                            (WebsocketTransportService.TRANSPORT_CONF));
            new TransportPersistenceManager(configContext.getAxisConfiguration()).saveTransportConfiguration
                    (SecureWebsocketTransportService.TRANSPORT_NAME, ctxt.getBundleContext().getBundle().getResource
                            (SecureWebsocketTransportService.TRANSPORT_CONF));
            WebsocketTransport = new WebsocketTransportService();
            SecureWebsocketTransport = new SecureWebsocketTransportService();
            ctxt.getBundleContext().registerService(TransportService.class.getName(), WebsocketTransport, null);
            ctxt.getBundleContext().registerService(TransportService.class.getName(), SecureWebsocketTransport, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully registered the ws(s) transport service");
            }
        } catch (Throwable e) {
            log.error("Error while activating Websocket transport management bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        log.debug("Websocket Transport bundle is deactivated ");
    }

    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {

        this.contextService = contextService;
        ServiceReferenceHolder.getInstance().setConfigurationContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

        this.contextService = null;
        ServiceReferenceHolder.getInstance().setConfigurationContextService(null);
    }
}
