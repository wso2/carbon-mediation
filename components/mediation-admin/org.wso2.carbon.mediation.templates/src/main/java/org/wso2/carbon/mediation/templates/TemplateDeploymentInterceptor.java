/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediation.templates;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.deployers.SequenceDeployer;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;

import java.util.Properties;

/**
 * Takes care of deploying sequences on the carbon environment.</p>
 *
 * <p>This deployer actually intercepts the deployment of the <code>sequences</code>
 * to the Synapse environment from the {@link org.apache.synapse.deployers.SequenceDeployer}
 * and handles the persistence to the registry on the carbon environment.
 */
public class TemplateDeploymentInterceptor extends SequenceDeployer {

    MediationPersistenceManager mpm;

    @Override
    public void init(ConfigurationContext configCtx) {
        super.init(configCtx);
        mpm = ServiceBusUtils.getMediationPersistenceManager(configCtx.getAxisConfiguration());
    }

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {
        String seqName = super.deploySynapseArtifact(artifactConfig, fileName, properties);
        if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
            mpm.saveItemToRegistry(seqName, ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        }
        return seqName;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {
        String seqName = super.updateSynapseArtifact(
                artifactConfig, fileName, existingArtifactName, properties);
        if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
            mpm.saveItemToRegistry(seqName, ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        }
        return seqName;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {
        super.undeploySynapseArtifact(artifactName);
        if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
            mpm.deleteItemFromRegistry(artifactName, ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        }
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {
        super.restoreSynapseArtifact(artifactName);
        if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
            mpm.saveItemToRegistry(artifactName, ServiceBusConstants.ITEM_TYPE_SEQUENCE);
        }
    }
}
