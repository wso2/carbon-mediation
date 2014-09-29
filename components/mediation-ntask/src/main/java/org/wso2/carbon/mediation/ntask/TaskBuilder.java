package org.wso2.carbon.mediation.ntask;

import org.apache.synapse.task.TaskDescription;
import org.wso2.carbon.ntask.core.TaskInfo;

import java.util.*;

final class TaskBuilder {
    public static final String TASK_TYPE_ESB = "ESB_TASK";
    //public static final String REMOTE_SYSTEM_TASK_ID = "__REMOTE_SYSTEM_TASK_ID__";
    //public static final String REMOTE_TASK_URI = "__REMOTE_TASK_URI__";
    public static final String REMOTE_TASK_NAME = "__REMOTE_TASK_NAME__";
    //public static final String SYSTEM_TASK_FLAG = "__SYSTEM_TASK__";

    public static TaskInfo buildTaskInfo(TaskDescription taskDescription, Map<String, Object> properties) throws Exception {
        return buildTaskInfo(taskDescription, false, properties);
    }

    public static TaskInfo buildTaskInfo(TaskDescription description,
                                   boolean system, Map<String, Object> tmProperties) throws Exception {
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
        String cron = description.getCronExpression();
        if (cron != null && cron.trim().length() == 0) {
            cron = null;
        }
        triggerInfo.setCronExpression(cron);
        if (description.getStartTime() != null) {
            triggerInfo.setStartTime(description.getStartTime().getTime());
        }
        if (description.getEndTime() != null) {
            triggerInfo.setEndTime(description.getEndTime().getTime());
        }
        if (cron == null && !system && description.getInterval() < 1000) {
            throw new Exception("Task interval cannot be less than 1 second for user tasks");
        }
        triggerInfo.setIntervalMillis((int) description.getInterval());
        triggerInfo.setRepeatCount(description.getCount()
                > 0 ? description.getCount() - 1 : description.getCount());
        triggerInfo.setDisallowConcurrentExecution(true);
        Map<String, String> props = new HashMap<String, String>();
        props.put(REMOTE_TASK_NAME, description.getName());
        //String targetUrl = null;
        //if (system && targetUrl != null) {
        //    int i1 = targetUrl.lastIndexOf("/");
        //    String systemTaskId = targetUrl.substring(i1 + 1);
        //    targetUrl = targetUrl.substring(0, i1);
        //    props.put(SYSTEM_TASK_FLAG, Boolean.toString(true));
        //    props.put(REMOTE_SYSTEM_TASK_ID, systemTaskId);
        //}
        //props.put(REMOTE_TASK_URI, targetUrl);
        // copy the remaining properties
        Map<String, String> properties = description.getProperties();
        Iterator<String> iterator = properties.keySet().iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o != null) {
                props.put((String) o, properties.get(o));
            }
        }
        if (tmProperties != null) {
            for (String name : tmProperties.keySet()) {
                if (tmProperties.get(name) instanceof String) {
                    props.put(name, (String) tmProperties.get(name));
                }
            }
        }
        Object o = description.getResource(TaskDescription.CLASSNAME);
        String taskClassName;
        if (o instanceof String) {
            taskClassName = (String) o;
            props.put(TaskDescription.CLASSNAME, taskClassName);
        } else {
            return null;
        }
        String nameGroup = description.getName() + "::" + description.getTaskGroup();
        props.put("task.name", nameGroup);
        Object taskInstance = description.getResource(TaskDescription.INSTANCE);
        if (taskInstance instanceof org.apache.synapse.task.Task) {
            NTaskAdapter.addProperty(nameGroup, taskInstance);
        }
        return new TaskInfo(description.getName(), NTaskAdapter.class.getName(), props, triggerInfo);
    }

    public static TaskDescription buildTaskDescription(TaskInfo taskInfo) {
        TaskDescription taskDescription = new TaskDescription();
        taskDescription.setName(taskInfo.getName());
        Map<String, String> taskProps = taskInfo.getProperties();
        TaskInfo.TriggerInfo triggerInfo = taskInfo.getTriggerInfo();
        taskDescription.setCronExpression(triggerInfo.getCronExpression());
        taskDescription.setStartTime(dateToCal(triggerInfo.getStartTime()));
        taskDescription.setEndTime(dateToCal(triggerInfo.getEndTime()));
        taskDescription.setCount(triggerInfo.getRepeatCount()+1);
        taskDescription.setInterval(triggerInfo.getIntervalMillis());
        taskDescription.setIntervalInMs(true);

        return taskDescription;
    }

    private static Calendar dateToCal(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

}
