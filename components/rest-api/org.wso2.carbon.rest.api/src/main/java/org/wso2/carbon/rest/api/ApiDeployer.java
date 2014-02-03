package org.wso2.carbon.rest.api;

import java.util.Properties;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.deployers.APIDeployer;

public class ApiDeployer extends APIDeployer {
	
	@Override
    public void init(ConfigurationContext configCtx) {
        super.init(configCtx);
    }

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {
        String proxyName = super.deploySynapseArtifact(artifactConfig, fileName, properties);
        return proxyName;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {
        String proxyName = super.updateSynapseArtifact(
                artifactConfig, fileName, existingArtifactName, properties);
        return proxyName;
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
