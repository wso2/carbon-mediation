package org.wso2.carbon.mediation.ntask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.Task;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.mediation.ntask.internal.NtaskService;
import org.wso2.carbon.ntask.core.AbstractTask;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.HashMap;
import java.util.Map;

public class NTaskAdapter extends AbstractTask {
    private static final Log logger = LogFactory.getLog(NTaskAdapter.class.getName());

    private static final Map<String, Object> synapseTaskProperties = new HashMap<String, Object>();

    private static final Object lock = new Object();

    private boolean initialized = false;

    private org.apache.synapse.task.Task synapseTask;

    public static boolean addProperty(String name, Object property) {
        if (name == null) {
            return false;
        }
        synchronized (lock) {
            return synapseTaskProperties.put(NTaskTaskManager.tenantId() + name, property) == property;
        }
    }

    public static boolean removeProperty(String name) {
        if (name == null) {
            return false;
        }
        synchronized (lock) {
            return synapseTaskProperties.remove(NTaskTaskManager.tenantId() + name) == null;
        }
    }

    public void init() {
        Map<String, String> properties = getProperties();
        if (properties == null) {
            return;
        }
        String taskName = properties.get("task.name");// taskName = "name::group"
        if (taskName == null) {
            return;
        }
        Object taskInstance;
        synchronized (lock) {
            taskInstance = synapseTaskProperties.get(NTaskTaskManager.tenantId() + taskName);
        }
        if (taskInstance == null) {
            //If tenant is not super tenant and not loaded load tenant.
            checkLoadTenant(properties.get(TaskInfo.TENANT_ID_PROP));
            // Nothing to execute.
            return;
        }
        if (!(taskInstance instanceof Task)) {
            return;
        }
        synapseTask = (Task) taskInstance;
        initialized = true;
    }

    public void execute() {
        if (!isInitialized()) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("#execute Executing NTaskAdapter: " + getProperties()
                + " Worker-node[" + CarbonUtils.isWorkerNode() + "]" );
        }
        //needs to keep the tenant loaded
        Map<String, String> properties = getProperties();
        checkLoadTenant(properties.get(TaskInfo.TENANT_ID_PROP));
        synapseTask.execute();
    }
    /**
     * If tenant is not super tenant and not loaded load tenant.
     * */
    private void checkLoadTenant(String sTenantId){
        if(sTenantId != null ){
            Integer iTenantId = Integer.parseInt(sTenantId);
            if(!iTenantId.equals(MultitenantConstants.SUPER_TENANT_ID)){
                NtaskService.loadTenant(iTenantId);   
            }            
        }
    }
    
    private boolean isInitialized() { return initialized; }
}
