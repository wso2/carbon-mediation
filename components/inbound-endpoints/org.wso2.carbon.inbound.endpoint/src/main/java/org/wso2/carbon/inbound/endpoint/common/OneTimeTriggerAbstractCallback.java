/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.common;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.inbound.endpoint.persistence.service.InboundEndpointPersistenceServiceDSComponent;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Generic callback for one time trigger inbound endpoints
 */

public abstract class OneTimeTriggerAbstractCallback {

    private volatile Semaphore callbackSuspensionSemaphore = new Semaphore(0);
    private AtomicBoolean isCallbackSuspended = new AtomicBoolean(false);
    private AtomicBoolean isShutdownFlagSet = new AtomicBoolean(false);
    private PrivilegedCarbonContext carbonContext;
    private boolean isInboundRunnerMode = false;

    protected void handleReconnection() throws InterruptedException {
        if (!isInboundRunnerMode) {
            isCallbackSuspended.set(true);
            callbackSuspensionSemaphore.acquire();
            if (!isShutdownFlagSet.get()) {
                reConnect();
            }
            isCallbackSuspended.set(false);
        } else {
            reConnect();
        }
    }

    protected void shutdown() {
        isShutdownFlagSet.set(true);
        if (isCallbackSuspended.get()) {
            callbackSuspensionSemaphore.release();
        }
    }

    protected abstract void reConnect();

    public void releaseCallbackSuspension() {
        if (callbackSuspensionSemaphore.availablePermits() < 1) {
            callbackSuspensionSemaphore.release();
        }
    }

    public boolean isCallbackSuspended() {
        return isCallbackSuspended.get();
    }

    public void preserveCarbonContext(PrivilegedCarbonContext carbonContext){
        //this is needed since we have to keep tenant loaded in later stage but at that point
        //we have no access to the PrivilegedCarbonContext if callbacks happens in a different
        //thread this is different to generic polling inbound endpoints
        this.carbonContext = carbonContext;
    }

    public void loadTenantContext(){
        //if carbon context is null tenant loading happen via task manager
        if (carbonContext != null) {
            int tenantId = carbonContext.getTenantId();
            String tenantDomain = null;
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
                tenantDomain = carbonContext.getTenantDomain();
            }
            //Keep the tenant loaded
            if (tenantDomain != null) {
                ConfigurationContextService configurationContext =
                        InboundEndpointPersistenceServiceDSComponent.getConfigContextService();
                if (configurationContext != null) {
                    ConfigurationContext mainConfigCtx = configurationContext.getServerConfigContext();
                    TenantAxisUtils.getTenantConfigurationContext(tenantDomain, mainConfigCtx);
                }
            }
        }
    }

    public void setInboundRunnerMode(boolean isInboundRunnerMode) {
        this.isInboundRunnerMode = isInboundRunnerMode;
    }
}
