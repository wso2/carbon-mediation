package org.wso2.carbon.mediation.ntask.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.TaskStartupObserver;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.mediation.ntask.NTaskTaskManager;

import org.wso2.carbon.mediation.ntask.TaskServiceObserver;
import org.wso2.carbon.ntask.core.TaskStartupHandler;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * 
 * @scr.component name="esbntask.taskservice" immediate="true"
 * @scr.reference name="tasks.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="org.wso2.carbon.tenantPreLoadComponent.user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="0..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * 
 **/

public class NtaskService {
    private static final Log logger = LogFactory.getLog(NtaskService.class);

    private static final List<TaskServiceObserver> observers = new ArrayList<TaskServiceObserver>();

    private static TaskService taskService;

    private static final List<TaskStartupObserver> startupObservers = new ArrayList<TaskStartupObserver>();

    private static ConfigurationContextService ccServiceInstance;

    private static final Object lock = new Object();
       
    private static RealmService realmService = null;
    
    private static ConfigurationContextService configContextService = null;

    
    protected void activate(ComponentContext context) {
        try {

            BundleContext bundleContext = context.getBundleContext();
            //bundleContext
            //        .registerService(this.getClass().getName(), new NtaskService(), null);
            //bundleContext.registerService(ServerStartupHandler.class.getName(),
            //        new NTaskTaskManager(), null);

            logger.debug("ntask-integration bundle is activated.");
        } catch (Throwable e) {
            logger.error("Could not activate NTaskService. Error: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context) {
        logger.debug("ntask-integration bundle is deactivated.");
    }

    protected void setTaskService(TaskService taskService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting the Task Service [" + taskService + "].");
        }
        NtaskService.taskService = taskService;
        updateAndCleanupObservers();        
    }

    protected void unsetTaskService(TaskService taskService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Unsetting the Task Service [" + taskService + "]");
        }
        NtaskService.taskService = null;
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        configContextService = contextService;
        if (logger.isDebugEnabled()) {
            logger.debug("Setting Configuration Context Service [" + contextService + "]");
        }
        NtaskService.ccServiceInstance = contextService;
        updateAndCleanupObservers();
    }

    private void updateAndCleanupObservers() {
        Iterator<TaskServiceObserver> i = observers.iterator();
        while (i.hasNext()) {
            TaskServiceObserver observer = i.next();
            if (observer.update(null)) {
                i.remove();
            }
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {        
        if (logger.isDebugEnabled()) {
            logger.debug("Unsetting Configuration Context Service [" + contextService + "]");
        }
        NtaskService.ccServiceInstance = null;
    }

    public static ConfigurationContextService getCcServiceInstance() {
        return NtaskService.ccServiceInstance;
    }

    public static void addObserver(TaskServiceObserver o) {
        if (observers.contains(o)) {
            return;
        }
        observers.add(o);
    }

    public static TaskService getTaskService() {
        return NtaskService.taskService;
    }

    protected void setRealmService(RealmService realmSrv) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting the Realm Service.");
        }
        realmService = realmSrv;
    }

    protected void unsetRealmService(RealmService realmService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Un-setting the Realm Service.");
        }
        realmService = null;
    }
    
    /**
     * Load the tenants if there are scheduled tasks
     * */
    public static void loadTenant(int tenantId){        
        //Avoid if the services are not loaded
        if(realmService == null || configContextService == null){
            if(logger.isDebugEnabled()){
                logger.debug("Unable to load tenants with scheduled tasks. Required services are not loaded.");
            }
            return;
        }       
        
        try {          
            ConfigurationContext mainConfigCtx = configContextService.getServerConfigContext();
            String tenantDomain = realmService.getTenantManager().getDomain(tenantId);           
            TenantAxisUtils.getTenantConfigurationContext(tenantDomain, mainConfigCtx);
            if(logger.isDebugEnabled()){
           	  logger.info("Load the tenant. Id : " + tenantId + " Domain : " + tenantDomain);       	  
             }             
        } catch (Exception e) {
            logger.error("Error when loading tenant before executing the task.", e);
        }
    }
    
}
