package org.wso2.carbon.mediation.ntask.admin;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.mediation.ntask.NTaskTaskManager;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.impl.remote.RemoteTaskManager;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;

import java.util.List;

public class ESBTaskAdmin extends AbstractServiceBusAdmin {

    private NTaskTaskManager ntaskManager;

    public ESBTaskAdmin() {
        ntaskManager = (NTaskTaskManager)getSynapseConfiguration().getTaskManager();
    }


    public void addRemoteTask(/*StaticTaskInformation taskInfo*/) throws Exception {
        //RemoteTaskManager.getInstance().addTask(taskInfo);
    }

    public void rescheduleRemoteTask(String taskName/*,
                                     TriggerInformation stTriggerInfo*/) throws Exception {
        //RemoteTaskManager.getInstance().rescheduleTask(taskName, stTriggerInfo);
    }

    public /*DeployedTaskInformation*/ void getRemoteTask(String name) throws Exception {
        //return RemoteTaskManager.getInstance().getTask(name);
    }

    public boolean deleteRemoteTask(String name) throws Exception {
        //return RemoteTaskManager.getInstance().deleteTask(name);
        return false;
    }

    public void pauseRemoteTask(String name) throws Exception {
        //RemoteTaskManager.getInstance().pauseTask(name);
    }

    public void resumeRemoteTask(String name) throws Exception {
//        RemoteTaskManager.getInstance().resumeTask(name);
    }

    public String[] getAllRemoteTasks() throws Exception {
//        return RemoteTaskManager.getInstance().getAllTasks();
        return null;
    }

    public void addRemoteSystemTask(/*StaticTaskInformation taskInfo,*/
                                    int targetTenantId) throws Exception {
//        RemoteTaskManager.getInstance().addSystemTask(targetTenantId, taskInfo);
    }

    public void rescheduleRemoteSystemTask(String taskName/*, TriggerInformation stTriggerInfo*/,
                                           int targetTenantId) throws Exception {

    }

    public /*DeployedTaskInformation*/ void getRemoteSystemTask(String name,
                                                                int targetTenantId) throws Exception {
        try {
//            return RemoteTaskManager.getInstance().getSystemTask(targetTenantId, name);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean deleteRemoteSystemTask(String name,
                                          int targetTenantId) throws Exception {
//        return RemoteTaskManager.getInstance().deleteSystemTask(targetTenantId, name);
        return false;
    }

    public void pauseRemoteSystemTask(String name,
                                      int targetTenantId) throws Exception {
//        RemoteTaskManager.getInstance().pauseSystemTask(targetTenantId, name);
    }

    public void resumeRemoteSystemTask(String name,
                                       int targetTenantId) throws Exception {
//        RemoteTaskManager.getInstance().resumeSystemTask(targetTenantId, name);
    }

    public String[] getAllRemoteSystemTasks(int targetTenantId) throws Exception {
        //return RemoteTaskManager.getInstance().getAllSystemTasks(targetTenantId);
        return null;
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

