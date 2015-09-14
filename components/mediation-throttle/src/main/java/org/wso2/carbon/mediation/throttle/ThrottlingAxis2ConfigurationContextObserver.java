/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.mediation.throttle;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantUtils;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.mediation.throttle.internal.ThrottleServiceComponent;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 *  This class will be notified when a new Tenant AxisConfig is created
 */
public class ThrottlingAxis2ConfigurationContextObserver
        extends AbstractAxis2ConfigurationContextObserver {

    private Log log = LogFactory.getLog(ThrottlingAxis2ConfigurationContextObserver.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        int tenantId = TenantUtils.getTenantId(configurationContext);
        try {
            UserRegistry registry =
                    ThrottleServiceComponent.getRegistryService().getConfigSystemRegistry(tenantId);
            ThrottlingUtils.saveTemplatePoliciesToRegistry(registry);
        } catch (Exception e) {
            log.error("Could not persist throttling templat policies for tenant " + tenantId);
        }
    }

}
