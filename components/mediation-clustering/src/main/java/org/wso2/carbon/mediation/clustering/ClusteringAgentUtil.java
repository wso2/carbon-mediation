package org.wso2.carbon.mediation.clustering;

import org.apache.axis2.clustering.ClusteringAgent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.mediation.clustering.osgi.ClusteringService;

import com.hazelcast.core.HazelcastInstance;

public final class ClusteringAgentUtil {
    
    public static Boolean isSingleNode(){
        if(ClusteringService.getConfigurationContextService() == null){
            return null;
        }else{
            if(getClusteringAgent() == null){
                return true;
            }else{
                return false;
            }
        }
    }
    
    public static ClusteringAgent getClusteringAgent() {
        if (ClusteringService.getConfigurationContextService() != null) {
            return ClusteringService.getConfigurationContextService().getServerConfigContext()
                    .getAxisConfiguration().getClusteringAgent();
        }
        return null;
    }
    

    
}
