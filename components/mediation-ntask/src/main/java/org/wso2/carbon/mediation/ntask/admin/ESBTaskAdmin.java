package org.wso2.carbon.mediation.ntask.admin;

import org.wso2.carbon.core.AbstractAdmin;

public class ESBTaskAdmin extends AbstractAdmin {
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

}
