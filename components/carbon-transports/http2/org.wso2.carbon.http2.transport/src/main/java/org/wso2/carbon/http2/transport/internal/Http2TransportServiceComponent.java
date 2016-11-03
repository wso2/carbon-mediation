/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.http2.transport.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.transports.TransportPersistenceManager;
import org.wso2.carbon.core.transports.TransportService;
import org.wso2.carbon.http2.transport.service.Http2TransportService;
import org.wso2.carbon.http2.transport.service.Https2TransportService;
import org.wso2.carbon.http2.transport.service.ServiceReferenceHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

public class Http2TransportServiceComponent {
    private static Log log = LogFactory.getLog(Http2TransportServiceComponent.class);

    private ConfigurationContextService contextService;

    public Http2TransportServiceComponent() {
    }

    protected void activate(ComponentContext ctxt) {
        ConfigurationContext configContext;
        Http2TransportService Http2Transport;
        Https2TransportService Https2Transport;
        if (log.isDebugEnabled()) {
            log.debug("Http2 Transport bundle is activated");
        }

        try {
            if (contextService != null) {
                configContext = contextService.getServerConfigContext();
            } else {
                throw new Exception("ConfigurationContext is not found while loading "
                        + "org.wso2.carbon.http2.transport bundle");
            }

            new TransportPersistenceManager(configContext.getAxisConfiguration())
                    .saveTransportConfiguration(Http2TransportService.TRANSPORT_NAME,
                            ctxt.getBundleContext().getBundle()
                                    .getResource(Http2TransportService.TRANSPORT_CONF));
            new TransportPersistenceManager(configContext.getAxisConfiguration())
                    .saveTransportConfiguration(Https2TransportService.TRANSPORT_NAME,
                            ctxt.getBundleContext().getBundle()
                                    .getResource(Https2TransportService.TRANSPORT_CONF));

            Http2Transport = new Http2TransportService();
            Https2Transport = new Https2TransportService();

            ctxt.getBundleContext()
                    .registerService(TransportService.class.getName(), Http2Transport, null);
            ctxt.getBundleContext()
                    .registerService(TransportService.class.getName(), Https2Transport, null);

            if (log.isDebugEnabled()) {
                log.debug("Successfully registered the http(s)2 transport service");
            }
        } catch (Throwable e) {
            log.error("Error while activating http2 transport management bundle", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("Http2 Transport bundle is deactivated ");
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        this.contextService = contextService;
        ServiceReferenceHolder.getInstance().setConfigurationContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        this.contextService = null;
        ServiceReferenceHolder.getInstance().setConfigurationContextService(null);
    }
}
