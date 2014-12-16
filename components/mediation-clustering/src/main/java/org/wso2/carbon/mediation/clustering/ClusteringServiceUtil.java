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
package org.wso2.carbon.mediation.clustering;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

/**
 * 
 * This class provided common functions related to hazelcast clustering
 * 
 */
public class ClusteringServiceUtil {

    private static final Log log = LogFactory.getLog(ClusteringServiceUtil.class);

    /**
     * 
     * Check if the lock is applied for the given context
     * 
     * @param strContext
     * @return
     */
    public static boolean isLocked(String strContext) {
        HazelcastInstance hz = org.wso2.carbon.mediation.clustering.osgi.ClusteringService
                .getHazelcastInstance();
        if (hz != null) {
            ILock iLock = hz.getLock(strContext);
            return iLock.isLocked();
        }
        return false;
    }

    /**
     * 
     * Set a lock for the given context with default try with 1 second
     * 
     * @param strContext
     * @return
     */
    public static boolean setLock(String strContext) {
        HazelcastInstance hz = org.wso2.carbon.mediation.clustering.osgi.ClusteringService
                .getHazelcastInstance();
        if (hz != null) {
            ILock iLock = hz.getLock(strContext);
            if (!iLock.isLocked()) {
                try {
                    iLock.tryLock(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.error("Unable to put the distributed lock for " + strContext, e);
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * Set lock with given timeout value for the given context
     * 
     * @param strContext
     * @param lSeconds
     * @return
     */
    public static boolean setLock(String strContext, long lSeconds) {
        HazelcastInstance hz = org.wso2.carbon.mediation.clustering.osgi.ClusteringService
                .getHazelcastInstance();
        if (hz != null) {
            ILock iLock = hz.getLock(strContext);
            if (!iLock.isLocked()) {
                iLock.lock(lSeconds, TimeUnit.SECONDS);
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * Unlok the given context
     * 
     * @param strContext
     * @return
     */
    public static boolean releaseLock(String strContext) {

        HazelcastInstance hz = org.wso2.carbon.mediation.clustering.osgi.ClusteringService
                .getHazelcastInstance();
        if (hz != null) {
            ILock iLock = hz.getLock(strContext);
            if (iLock.isLocked()) {
                try {
                    iLock.unlock();
                } catch (IllegalMonitorStateException e) {
                    return false;
                }
            }
            return true;
        }
        return false;

    }

}
