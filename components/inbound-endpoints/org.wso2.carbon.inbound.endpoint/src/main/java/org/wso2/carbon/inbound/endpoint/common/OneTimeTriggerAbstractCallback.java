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

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Generic callback for one time trigger inbound endpoints
 */

public abstract class OneTimeTriggerAbstractCallback {

    private volatile Semaphore callbackSuspensionSemaphore = new Semaphore(0);
    private AtomicBoolean isCallbackSuspended = new AtomicBoolean(false);
    private AtomicBoolean isShutdownFlagSet = new AtomicBoolean(false);


    protected void handleReconnection() throws InterruptedException {
        isCallbackSuspended.set(true);
        callbackSuspensionSemaphore.acquire();
        if (!isShutdownFlagSet.get()) {
            reConnect();
        }
        isCallbackSuspended.set(false);
    }

    protected void shutdown() {
        isShutdownFlagSet.set(true);
        if (isCallbackSuspended.get()) {
            callbackSuspensionSemaphore.release();
        }
    }

    protected abstract void reConnect();

    public void releaseCallbackSuspension() {
        callbackSuspensionSemaphore.release();
    }

    public boolean isCallbackSuspended() {
        return isCallbackSuspended.get();
    }
}
