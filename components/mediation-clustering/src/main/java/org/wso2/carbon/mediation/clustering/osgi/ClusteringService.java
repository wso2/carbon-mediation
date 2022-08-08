/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediation.clustering.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.utils.ConfigurationContextService;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 */
@Component(
        name = "esbclustering.agentservice",
        immediate = true)
public class ClusteringService {

    private static final Log log = LogFactory.getLog(ClusteringService.class);

    private static ConfigurationContextService configContextService;

    /**
     * @param contextService
     */
    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting Configuration Context Service [" + contextService + "]");
        }
        configContextService = contextService;
    }

    /**
     * @param contextService
     */
    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting Configuration Context Service [" + contextService + "]");
        }
        configContextService = null;
    }

    public static ConfigurationContextService getConfigurationContextService() {

        return configContextService;
    }

    /**
     * Get the hazelcast instance
     *
     * @return
     */
    public static HazelcastInstance getHazelcastInstance() {

        BundleContext ctx = FrameworkUtil.getBundle(ClusteringService.class).getBundleContext();
        ServiceReference ref = ctx.getServiceReference(HazelcastInstance.class);
        if (ref == null) {
            return null;
        }
        return (HazelcastInstance) ctx.getService(ref);
    }
}
