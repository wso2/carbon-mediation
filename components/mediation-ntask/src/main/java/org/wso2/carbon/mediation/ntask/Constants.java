package org.wso2.carbon.mediation.ntask;

public class Constants {
    public static final String TASK_TYPE_ESB = "ESB_TASK";
    public static final String TASK_EXECUTING_TENANT_ID = "CURRENT_TASK_EXECUTING_TENANT_IDENTIFIER";
    /**
     * This property is used to skip running task in manager only clustering setup. This property value defaults to
     * "true" which means by default in worker manager cluster tasks won't run in manager nodes.
     */
    public static final String MEDIATION_NTASK_SKIP_RUNNING_TASKS = "mediation.ntask.skip.running.tasks";
    public static final String MEDIATION_NTASK_SKIP_RUNNING_TASKS_DEFAULT_VALUE = "true";
}
