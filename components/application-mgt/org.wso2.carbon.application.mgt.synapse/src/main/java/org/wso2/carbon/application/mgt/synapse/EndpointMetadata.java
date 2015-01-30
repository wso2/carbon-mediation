package org.wso2.carbon.application.mgt.synapse;

/**
 * Metadata object for holding information on Tasks
 */
public class EndpointMetadata {

    private String name;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
