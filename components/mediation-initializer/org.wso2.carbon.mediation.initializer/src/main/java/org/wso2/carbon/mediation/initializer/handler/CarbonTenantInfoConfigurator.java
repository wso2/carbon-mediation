/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.carbonext.TenantInfoConfigurator;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class CarbonTenantInfoConfigurator implements TenantInfoConfigurator {
    private static final Log logger = LogFactory.getLog(CarbonTenantInfoConfigurator.class.getName());

    @Override
    public boolean extractTenantInfo(MessageContext messageContext) {
        if (logger.isDebugEnabled()) {
            logger.info(">>>>>>>>>>>>>>Extracting Tenant Info...");
        }
        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = cc.getTenantDomain();
        int tenantId = cc.getTenantId();
        messageContext.setProperty("tenant.info.domain", tenantDomain);
        messageContext.setProperty("tenant.info.id", tenantId);
        if (logger.isDebugEnabled()) {
            logger.info("      tenant domain: " + tenantDomain);
            logger.info("      tenant id: " + tenantId);
            logger.info("<<<<<<<<<<<<<<<");
        }
        return true;
    }

    @Override
    public boolean applyTenantInfo(MessageContext messageContext) {
        if (logger.isDebugEnabled()) {
            logger.info(">>>>>>>>>>>>>Applying Tenant Info...");
        }
        Object p = messageContext.getProperty("tenant.info.domain");
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        if (p != null) {
            tenantDomain = (String) p;
        }
        p = messageContext.getProperty("tenant.info.id");
        if (p != null && p instanceof Integer) {
            tenantId = (Integer) p;
        }
        PrivilegedCarbonContext.destroyCurrentContext();
        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        cc.setTenantDomain(tenantDomain);
        cc.setTenantId(tenantId);
        if (logger.isDebugEnabled()) {
            logger.info("      tenant domain: " + cc.getTenantDomain());
            logger.info("      tenant id: " + cc.getTenantId());
            logger.info("<<<<<<<<<<<<<<<");
        }
        return true;
    }
}
