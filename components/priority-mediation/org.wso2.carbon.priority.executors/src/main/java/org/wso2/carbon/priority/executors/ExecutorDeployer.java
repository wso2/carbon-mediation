package org.wso2.carbon.priority.executors;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.deployers.PriorityExecutorDeployer;


import java.util.Properties;

public class ExecutorDeployer extends PriorityExecutorDeployer {
    @Override
    public void init(ConfigurationContext configCtx) {
        super.init(configCtx);
    }

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig,
                                        String fileName, Properties properties) {

        String name = super.deploySynapseArtifact(artifactConfig, fileName, properties);
        return name;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {
        String epName = super.updateSynapseArtifact(artifactConfig, fileName,
                existingArtifactName, properties);
        return epName;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {
        super.undeploySynapseArtifact(artifactName);
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {
        super.restoreSynapseArtifact(artifactName);
    }
}
