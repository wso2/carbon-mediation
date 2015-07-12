/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.application.deployer.synapse;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.deployers.LibraryArtifactDeployer;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.application.deployer.synapse.internal.DataHolder;
import org.wso2.carbon.application.deployer.synapse.internal.SynapseAppDeployerDSComponent;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.library.service.LibraryInfo;
import org.wso2.carbon.mediation.library.service.MediationLibraryAdminService;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class SynapseAppDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(SynapseAppDeployer.class);

    private Map<String, Boolean> acceptanceList = null;

    private static String MAIN_XML="<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"main\"/>";
    private static String FAULT_XML="<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"fault\"/>";
    private static String MAIN_SEQ_REGEX = "main-\\d+\\.\\d+\\.\\d+\\.xml";
    private static String FAULT_SEQ_REGEX = "fault-\\d+\\.\\d+\\.\\d+\\.xml";

    /**
     * Deploy the artifacts which can be deployed through this deployer (endpoints, sequences,
     * proxy service etc.).
     *
     * @param carbonApp  - CarbonApplication instance to check for artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig)
            throws DeploymentException{
        List<Artifact.Dependency> artifacts = carbonApp.getAppConfig().getApplicationArtifact()
                .getDependencies();

        deployClassMediators(artifacts, axisConfig);
        deploySynapseLibrary(artifacts, axisConfig);
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();

            if (!validateArtifact(artifact)) {
                continue;
            }

            String artifactDirName = getArtifactDirName(artifact);
            if (artifactDirName == null) {
                continue;
            }
            Deployer deployer = getDeployer(axisConfig, artifactDirName);
            String artifactDir = getArtifactDirPath(axisConfig, artifactDirName);

            artifact.setRuntimeObjectName(artifact.getName());

            if (deployer != null) {
                String fileName = artifact.getFiles().get(0).getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                File artifactInRepo = new File(artifactDir + File.separator + fileName);

                if (SynapseAppDeployerConstants.SEQUENCE_TYPE.equals(artifact.getType()) &&
                    handleMainFaultSeqDeployment(artifact, axisConfig)) {
                } else if (artifactInRepo.exists()) {
                    log.warn("Artifact " + fileName + " already found in " + artifactInRepo.getAbsolutePath() +
                    ". Ignoring CAPP's artifact");
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                } else {
                    try {
                        deployer.deploy(new DeploymentFileData(new File(artifactPath), deployer));
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                    } catch (DeploymentException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * Un-deploys Synapse artifacts found in this application. Just delete the files from the
     * hot folders. Synapse hot deployer will do the rest..
     *
     * @param carbonApplication - CarbonApplication instance
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfig)
            throws DeploymentException{

        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig()
                .getApplicationArtifact().getDependencies();

        for (Artifact.Dependency dep : artifacts) {

            Artifact artifact = dep.getArtifact();

            if (!validateArtifact(artifact)) {
                continue;
            }

            Deployer deployer;
            String artifactDir = null;

            if (SynapseAppDeployerConstants.MEDIATOR_TYPE.endsWith(artifact.getType())) {
                deployer = getClassMediatorDeployer(axisConfig);
            } else if(SynapseAppDeployerConstants.SYNAPSE_LIBRARY_TYPE.equals(artifact.getType())) {
                deployer = getSynapseLibraryDeployer(axisConfig);
            } else {
                String artifactDirName = getArtifactDirName(artifact);
                if (artifactDirName == null) {
                    continue;
                }

                deployer = getDeployer(axisConfig, artifactDirName);
                artifactDir = getArtifactDirPath(axisConfig, artifactDirName);
            }

            if (deployer != null && AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED.
                                            equals(artifact.getDeploymentStatus())) {

                String fileName = artifact.getFiles().get(0).getName();
                String artifactName = artifact.getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                File artifactInRepo = new File(artifactDir + File.separator + fileName);

                try {
                    if (SynapseAppDeployerConstants.MEDIATOR_TYPE.endsWith(artifact.getType())) {
                        deployer.undeploy(artifactPath);
                    } else if (SynapseAppDeployerConstants.SYNAPSE_LIBRARY_TYPE.equals(artifact.getType())){
                        MediationLibraryAdminService mediationLibraryAdminService = new MediationLibraryAdminService();
                        String libQName = mediationLibraryAdminService.getArtifactName(artifactPath);
                        mediationLibraryAdminService.deleteImport(libQName);
                        deployer.undeploy(artifactPath);
                    } else if (SynapseAppDeployerConstants.SEQUENCE_TYPE.equals(artifact.getType())
                               && handleMainFaultSeqUndeployment(artifact, axisConfig)) {
                    } else if (artifactInRepo.exists()) {
                        log.info("Deleting artifact at " + artifactInRepo.getAbsolutePath());
                        if (!artifactInRepo.delete()) {
                            log.error("Unable to delete " + artifactInRepo.getAbsolutePath());
                        }
                    } else {
                        // use reflection to avoid having synapse as a dependency

                        Class[] paramString = new Class[1];
                        paramString[0] = String.class;
                        Method method = deployer.getClass().getDeclaredMethod("undeploySynapseArtifact", paramString);
                        method.invoke(deployer, artifactName);
                    }

                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_PENDING);
                    File artifactFile = new File(artifactPath);
                    if (artifactFile.exists() && !artifactFile.delete()) {
                        log.warn("Couldn't delete App artifact file : " + artifactPath);
                    }
                } catch (Exception e) {
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                    log.error("Error occured while trying to un deploy : "+ artifactName);
                }
            }
        }
    }

    /**
     * Deploy class mediators contains in the CApp
     *
     * @param artifacts List of Artifacts contains in the capp
     * @param axisConfig AxisConfiguration of the current tenant
     * @throws DeploymentException if something goes wrong while deployment
     */
    private void deployClassMediators(List<Artifact.Dependency> artifacts,
                                     AxisConfiguration axisConfig) throws DeploymentException {
        for (Artifact.Dependency dependency : artifacts) {

            Artifact artifact = dependency.getArtifact();
            if (!validateArtifact(artifact)) {
                continue;
            }

            if (SynapseAppDeployerConstants.MEDIATOR_TYPE.endsWith(artifact.getType())) {

                Deployer deployer = getClassMediatorDeployer(axisConfig);

                if (deployer != null) {
                    artifact.setRuntimeObjectName(artifact.getName());
                    String fileName = artifact.getFiles().get(0).getName();
                    String artifactPath = artifact.getExtractedPath() + File.separator + fileName;

                    try {
                        deployer.deploy(new DeploymentFileData(new File(artifactPath), deployer));
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                    } catch (DeploymentException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * Deploy synapse libraries contains in the CApp
     *
     * @param artifacts  List of Artifacts contains in the capp
     * @param axisConfig AxisConfiguration of the current tenant
     * @throws DeploymentException if something goes wrong while deployment
     */
    private void deploySynapseLibrary(List<Artifact.Dependency> artifacts,
                                      AxisConfiguration axisConfig) throws DeploymentException {
        for (Artifact.Dependency dependency : artifacts) {

            Artifact artifact = dependency.getArtifact();
            if (!validateArtifact(artifact)) {
                continue;
            }

            if (SynapseAppDeployerConstants.SYNAPSE_LIBRARY_TYPE.equals(artifact.getType())) {

                Deployer deployer = getSynapseLibraryDeployer(axisConfig);

                if (deployer != null) {
                    artifact.setRuntimeObjectName(artifact.getName());
                    String fileName = artifact.getFiles().get(0).getName();
                    String artifactPath = artifact.getExtractedPath() + File.separator + fileName;

                    try {
                        deployer.deploy(new DeploymentFileData(new File(artifactPath), deployer));
                        DeploymentFileData dfd = new DeploymentFileData(new File(artifactPath), deployer);
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                        MediationLibraryAdminService mediationLibraryAdminService = new MediationLibraryAdminService();
                        try {
                            String artifactName = mediationLibraryAdminService.getArtifactName(artifactPath);
                            String libName = artifactName.substring(artifactName.lastIndexOf("}")+1);
                            String libraryPackage = artifactName.substring(1, artifactName.lastIndexOf("}"));
                            mediationLibraryAdminService.updateStatus(artifactName, libName, libraryPackage, ServiceBusConstants.ENABLED);
                        } catch (AxisFault axisFault) {
                            axisFault.printStackTrace();
                        }

                    } catch (DeploymentException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * Handle main and fault sequence deployment.
     * Since main.xml and fault.xml is already in filesystem, we only can update those.
     * NO direct deployer call and sync deployment
     *
     * @param artifact Sequence Artifact
     * @param axisConfig AxisConfiguration of the current tenant
     * @return whether main or fault sequence is handled
     */
    private boolean handleMainFaultSeqDeployment(Artifact artifact,
                                                 AxisConfiguration axisConfig) {

        String fileName = artifact.getFiles().get(0).getName();
        String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
        boolean isMainOrFault = false;

        if (fileName.matches(MAIN_SEQ_REGEX) || fileName.matches(SynapseAppDeployerConstants.MAIN_SEQ_FILE)) {
            isMainOrFault = true;
            try {
                String mainXMLPath = getMainXmlPath(axisConfig);
                log.info("Copying main sequence to " + mainXMLPath);
                FileUtils.copyFile(new File(artifactPath), new File(mainXMLPath));
                artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
            } catch (IOException e) {
                log.error("Error copying main.xml to sequence directory", e);
            }
        } else if (fileName.matches(FAULT_SEQ_REGEX) || fileName.matches(SynapseAppDeployerConstants.FAULT_SEQ_FILE)) {
            isMainOrFault = true;
            try {
                String faultXMLPath = getFaultXmlPath(axisConfig);
                log.info("Copying fault sequence to " + faultXMLPath);
                FileUtils.copyFile(new File(artifactPath), new File(faultXMLPath));
                artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
            } catch (IOException e) {
                log.error("Error copying main.xml to sequence directory", e);
            }
        }
        return isMainOrFault;
    }

    /**
     * Handle main and fault sequence un-deployment.
     * Since main.xml and fault.xml is already in filesystem, we only can update those.
     * NO direct deployer call
     *
     * @param artifact Sequence Artifact
     * @param axisConfig AxisConfiguration of the current tenant
     * @return whether main or fault sequence is handled
     * @throws java.io.IOException
     */
    private boolean handleMainFaultSeqUndeployment(Artifact artifact,
                                                     AxisConfiguration axisConfig)
            throws IOException {

        boolean isMainOrFault = false;
        String fileName = artifact.getFiles().get(0).getName();
        if (fileName.matches(MAIN_SEQ_REGEX) || fileName.matches(SynapseAppDeployerConstants.MAIN_SEQ_FILE)) {
            isMainOrFault = true;
            String mainXMLPath = getMainXmlPath(axisConfig);
            FileUtils.deleteQuietly(new File(mainXMLPath));
            FileUtils.writeStringToFile(new File(mainXMLPath), MAIN_XML);

        } else if (fileName.matches(FAULT_SEQ_REGEX) || fileName.matches(SynapseAppDeployerConstants.FAULT_SEQ_FILE)) {
            isMainOrFault = true;
            String faultXMLPath = getFaultXmlPath(axisConfig);
            FileUtils.deleteQuietly(new File(faultXMLPath));
            FileUtils.writeStringToFile(new File(faultXMLPath), FAULT_XML);
        }

        return isMainOrFault;
    }

    /**
     * Check whether a particular artifact type can be accepted for deployment. If the type doesn't
     * exist in the acceptance list, we assume that it doesn't require any special features to be
     * installed in the system. Therefore, that type is accepted.
     * If the type exists in the acceptance list, the acceptance value is returned.
     *
     * @param serviceType - service type to be checked
     * @return true if all features are there or entry is null. else false
     */
    private boolean isAccepted(String serviceType) {
        if (acceptanceList == null) {
            acceptanceList = AppDeployerUtils.buildAcceptanceList(SynapseAppDeployerDSComponent
                    .getRequiredFeatures());
        }
        Boolean acceptance = acceptanceList.get(serviceType);
        return (acceptance == null || acceptance);
    }

    /**
     * Validate artifact
     *
     * @param artifact artifact to be validated
     * @return validation passed or not
     */
    private boolean validateArtifact(Artifact artifact) {
        if (artifact == null) {
            return false;
        }

        if (!isAccepted(artifact.getType())) {
            log.warn("Can't deploy artifact : " + artifact.getName() + " of type : " +
                     artifact.getType() + ". Required features are not installed in the system");
            return false;
        }

        List<CappFile> files = artifact.getFiles();
        if (files.size() != 1) {
            log.error("Synapse artifact types must have a single file to " +
                      "be deployed. But " + files.size() + " files found.");
            return false;
        }

        return true;
    }

    /**
     * Finds the correct deployer for the given artifact type
     *
     * @param axisConfig - AxisConfiguration instance
     * @return Deployer instance
     */
    private Deployer getDeployer(AxisConfiguration axisConfig, String directory) {
        Deployer deployer = null;
        // access the deployment engine through axis config
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        String tenantId = AppDeployerUtils.getTenantIdString(axisConfig);
        SynapseEnvironmentService environmentService = DataHolder.getInstance().
                getSynapseEnvironmentService(Integer.parseInt(tenantId));
        if (environmentService != null) {
            String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(
                    environmentService.getSynapseEnvironment().getServerContextInformation());
            String endpointDirPath = synapseConfigPath
                                     + File.separator + directory;
            deployer = deploymentEngine.getDeployer(endpointDirPath,
                                                    ServiceBusConstants.ARTIFACT_EXTENSION);
        }
        return deployer;
    }

    /**
     * Get the deployer for the Class Mediators
     *
     * @param axisConfig AxisConfiguration instance
     * @return Deployer instance
     */
    private Deployer getClassMediatorDeployer(AxisConfiguration axisConfig) {
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        String classMediatorPath = axisConfig.getRepository().getPath() +
                                   File.separator + SynapseAppDeployerConstants.MEDIATORS_FOLDER;
        return deploymentEngine.
                getDeployer(classMediatorPath, ServiceBusConstants.CLASS_MEDIATOR_EXTENSION);
    }

    /**
     * Get the deployer for the Synapse Library
     *
     * @param axisConfig AxisConfiguration instance
     * @return Deployer instance
     */
    private Deployer getSynapseLibraryDeployer(AxisConfiguration axisConfig) {
        String synapseLibraryPath = axisConfig.getRepository().getPath() +
                SynapseAppDeployerConstants.SYNAPSE_LIBS;
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        deploymentEngine.addDeployer(new LibraryArtifactDeployer(), synapseLibraryPath, ServiceBusConstants.SYNAPSE_LIBRARY_EXTENSION);

        return deploymentEngine.
                getDeployer(synapseLibraryPath, ServiceBusConstants.SYNAPSE_LIBRARY_EXTENSION);
    }

    /**
     * Get the artifact directory name for the artifact type
     *
     * @param artifact  synapse artifact
     * @return artifact directory
     */
    private String getArtifactDirName(Artifact artifact) {

        String artifactType = artifact.getType();
        if (SynapseAppDeployerConstants.SEQUENCE_TYPE.equals(artifactType)) {
            return SynapseAppDeployerConstants.SEQUENCES_FOLDER;
        } else if (SynapseAppDeployerConstants.ENDPOINT_TYPE.equals(artifactType)) {
            return SynapseAppDeployerConstants.ENDPOINTS_FOLDER;
        } else if (SynapseAppDeployerConstants.PROXY_SERVICE_TYPE.equals(artifactType)) {
            return SynapseAppDeployerConstants.PROXY_SERVICES_FOLDER;
        } else if (SynapseAppDeployerConstants.LOCAL_ENTRY_TYPE.equals(artifactType)) {
            return SynapseAppDeployerConstants.LOCAL_ENTRIES_FOLDER;
        } else if (SynapseAppDeployerConstants.EVENT_SOURCE_TYPE.equals(artifactType)) {
            return SynapseAppDeployerConstants.EVENTS_FOLDER;
        } else if (SynapseAppDeployerConstants.TASK_TYPE.equals(artifactType)) {
            return SynapseAppDeployerConstants.TASKS_FOLDER;
        } else if (SynapseAppDeployerConstants.MESSAGE_STORE_TYPE.endsWith(artifactType)) {
            return SynapseAppDeployerConstants.MESSAGE_STORE_FOLDER;
        } else if (SynapseAppDeployerConstants.MESSAGE_PROCESSOR_TYPE.endsWith(artifactType)) {
            return SynapseAppDeployerConstants.MESSAGE_PROCESSOR_FOLDER;
        } else if (SynapseAppDeployerConstants.API_TYPE.endsWith(artifactType)) {
            return SynapseAppDeployerConstants.APIS_FOLDER;
        } else if (SynapseAppDeployerConstants.TEMPLATE_TYPE.endsWith(artifactType)) {
            return SynapseAppDeployerConstants.TEMPLATES_FOLDER;
        } else if (SynapseAppDeployerConstants.INBOUND_ENDPOINT_TYPE.endsWith(artifactType)) {
           return SynapseAppDeployerConstants.INBOUND_ENDPOINT_FOLDER;            
        } else if (SynapseAppDeployerConstants.SYNAPSE_LIBRARY_TYPE.equals(artifactType)) {
            return SynapseAppDeployerConstants.SYNAPSE_LIBS;
        }
        return null;
    }

    /**
     * Get the absolute path of the artifact directory
     *
     * @param axisConfiguration axis configuration
     * @param artifactDirName synapse artifact directory name
     * @return absolute path of artifact directory
     */
    private String getArtifactDirPath(AxisConfiguration axisConfiguration, String artifactDirName) {
        if (artifactDirName.equals(SynapseAppDeployerConstants.SYNAPSE_LIBS)) {
            return axisConfiguration.getRepository().getPath() +
                    SynapseAppDeployerConstants.SYNAPSE_LIBS;
        } else {
            return axisConfiguration.getRepository().getPath() +
                    SynapseAppDeployerConstants.SYNAPSE_CONFIGS +
                    File.separator + SynapseAppDeployerConstants.DEFAULT_DIR +
                    File.separator + artifactDirName;
        }
    }

    private String getMainXmlPath(AxisConfiguration axisConfig) {
        return axisConfig.getRepository().getPath() +
               SynapseAppDeployerConstants.SYNAPSE_CONFIGS +
               File.separator + SynapseAppDeployerConstants.DEFAULT_DIR +
               File.separator + SynapseAppDeployerConstants.SEQUENCES_FOLDER +
               File.separator + SynapseAppDeployerConstants.MAIN_SEQ_FILE;
    }

    private String getFaultXmlPath(AxisConfiguration axisConfig) {
        return axisConfig.getRepository().getPath() +
               SynapseAppDeployerConstants.SYNAPSE_CONFIGS +
               File.separator + SynapseAppDeployerConstants.DEFAULT_DIR +
               File.separator + SynapseAppDeployerConstants.SEQUENCES_FOLDER +
               File.separator + SynapseAppDeployerConstants.FAULT_SEQ_FILE;
    }

}


