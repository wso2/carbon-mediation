/**
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.initializer.handler;

import org.apache.synapse.commons.util.ext.TenantInfoInitiator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class CarbonTenantInfoInitiator implements TenantInfoInitiator {

    @Override
    public void initTenantInfo(){
        //Nothing to do here,
    }

    /**
     * initialize tenant information based on the request URI
     * @param uri request URI
     */
    @Override
    public void initTenantInfo(String uri) {
        String tenantDomain = TenantAxisUtils.getTenantDomain(uri);
        if (tenantDomain == null) {
            //should be super tenant
            PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        } else {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        }
    }

    @Override
    public void cleanTenantInfo() {
        PrivilegedCarbonContext.destroyCurrentContext();
    }

}
