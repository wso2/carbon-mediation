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

import org.apache.axis2.clustering.ClusteringAgent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.mediation.clustering.osgi.ClusteringService;

import com.hazelcast.core.HazelcastInstance;

public final class ClusteringAgentUtil {
    
    /**
     * 
     * Check if the node is a cluster member
     * 
     * @return
     */
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
    
    /**
     * 
     * Get the loaded clustering agent
     * 
     * @return
     */
    public static ClusteringAgent getClusteringAgent() {
        if (ClusteringService.getConfigurationContextService() != null) {
            return ClusteringService.getConfigurationContextService().getServerConfigContext()
                    .getAxisConfiguration().getClusteringAgent();
        }
        return null;
    }
    

    
}
