package org.wso2.carbon.mediation.ntask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.Task;
import org.wso2.carbon.ntask.core.AbstractTask;
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
            return synapseTaskProperties.put(name, property) == property;
        }
    }

    public static boolean removeProperty(String name) {
        if (name == null) {
            return false;
        }
        synchronized (lock) {
            return synapseTaskProperties.remove(name) == null;
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
            taskInstance = synapseTaskProperties.get(taskName);
        }
        if (taskInstance == null) {
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
            logger.debug("Executing NTaskAdapter: " + getProperties()
                + " Worker node[" + CarbonUtils.isWorkerNode() + "]" );
        }
        // kernel/core/org.wso2.carbon.core
        synapseTask.execute();
    }

    private boolean isInitialized() { return initialized; }
}
