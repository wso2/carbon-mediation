package org.wso2.carbon.mediation.ntaskadmin;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.ntask.NTaskTaskManager;
import org.wso2.carbon.ntask.core.impl.remote.RemoteTaskManager;

import java.util.List;

public class ESBNTaskAdmin extends AbstractServiceBusAdmin {

    private NTaskTaskManager ntaskManager;

    public ESBNTaskAdmin() {
        ntaskManager = (NTaskTaskManager)getSynapseConfiguration().getTaskManager();
    }

    //Adding ESB specific task operations

    public List<String> getRunningESBTaskList() throws Exception {
        return ntaskManager.getRunningTaskList();
    }

    public boolean isESBTaskRunning(String taskName) throws Exception {
        return ntaskManager.isTaskRunning(taskName);
    }

    public int getRunningESBTaskCount() throws Exception {
        return ntaskManager.getRunningTaskCount();
    }

    public boolean deleteESBTask(String name) throws Exception {
        return ntaskManager.delete(name);
    }

    public boolean pauseESBTask(String name) throws Exception {
        return ntaskManager.pause(name);
    }

    public boolean resumeESBTask(String name) throws Exception {
        return ntaskManager.resume(name);
    }

    public boolean pauseAllESBTasks() throws Exception {
        return ntaskManager.pauseAll();
    }

    public boolean resumeAllESBTasks() throws Exception {
        return ntaskManager.resumeAll();
    }

    public String[] getESBTaskNames() throws Exception {
        return ntaskManager.getTaskNames();
    }


}