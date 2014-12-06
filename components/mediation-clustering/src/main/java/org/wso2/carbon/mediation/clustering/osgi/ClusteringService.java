package org.wso2.carbon.mediation.clustering.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.utils.ConfigurationContextService;

import com.hazelcast.core.HazelcastInstance;

/**
 * 
 * @scr.component name="esbclustering.agentservice" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * 
 **/

public class ClusteringService {

    private static final Log log = LogFactory.getLog(ClusteringService.class);
    
    private static ConfigurationContextService configContextService;
    
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Configuration Context Service [" + contextService + "]");
        }        
        configContextService = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {        
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Configuration Context Service [" + contextService + "]");
        }
        configContextService = null;
    }
    
    public static ConfigurationContextService  getConfigurationContextService(){
        return configContextService;
    }
    
    public static HazelcastInstance getHazelcastInstance() {
        BundleContext ctx = FrameworkUtil.getBundle(ClusteringService.class).getBundleContext();
        ServiceReference<HazelcastInstance> ref = ctx.getServiceReference(HazelcastInstance.class);
        if (ref == null) {
            return null;
        }
        return ctx.getService(ref);
    }
    
}
