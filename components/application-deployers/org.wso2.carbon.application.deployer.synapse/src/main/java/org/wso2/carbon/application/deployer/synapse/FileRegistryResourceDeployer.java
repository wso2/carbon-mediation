package org.wso2.carbon.application.deployer.synapse;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.mediation.registry.EILightweightRegistry;
import org.wso2.carbon.mediation.registry.FileRegistrySingleton;

import java.util.List;



public class FileRegistryResourceDeployer implements AppDeploymentHandler {

    @Override
    public void deployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration) throws DeploymentException {
        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        for (Artifact.Dependency artifact : artifacts) {
            if(artifact.getServerRole().equals("EnterpriseIntegrator") && artifact.getArtifact().getType().equals
                    ("registry/resource")) {
                FileRegistrySingleton fileRegistrySingleton = FileRegistrySingleton.getInstance();
                EILightweightRegistry registry = fileRegistrySingleton.getLightweightRegistry();
                registry.newResource(artifact.getArtifact().getName(),false);
            }
        }
    }

    @Override
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration) throws DeploymentException {

    }
}


