package org.wso2.carbon.ntaskint.core;


public class Task3 implements org.apache.synapse.task.Task {
    private String recipeName;
    private String parameter;

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public void execute() {
        System.out.println("=====================================>>>");
        System.out.println(recipeName + " "  + parameter);
    }
}
