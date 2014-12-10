package org.wso2.carbon.mediation.clustering;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

public class ClusteringServiceUtil {

    private static final Log log = LogFactory.getLog(ClusteringServiceUtil.class);
    
    public static boolean isLocked(String strContext) {
        HazelcastInstance hz = org.wso2.carbon.mediation.clustering.osgi.ClusteringService
                .getHazelcastInstance();
        if (hz != null) {
            ILock iLock = hz.getLock(strContext);
            return iLock.isLocked();
        }
        return false;
    }

    public static boolean setLock(String strContext) {
        HazelcastInstance hz = org.wso2.carbon.mediation.clustering.osgi.ClusteringService
                .getHazelcastInstance();
        if (hz != null) {
            ILock iLock = hz.getLock(strContext);
            if(!iLock.isLocked()){
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

    public static boolean setLock(String strContext, long lSeconds) {
        HazelcastInstance hz = org.wso2.carbon.mediation.clustering.osgi.ClusteringService
                .getHazelcastInstance();
        if (hz != null) {
            ILock iLock = hz.getLock(strContext);
            if(!iLock.isLocked()){
                iLock.lock(lSeconds, TimeUnit.SECONDS);
                return true;
            }
        }
        return false;
    }

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
