package org.wso2.carbon.rest.api;

import java.util.Properties;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.deployers.APIDeployer;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sound.midi.SysexMessage;
import javax.xml.namespace.QName;

public class ApiDeployer extends APIDeployer {
	
	@Override
    public void init(ConfigurationContext configCtx) {
        super.init(configCtx);
    }

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        if(!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
            OMAttribute context = artifactConfig.getAttribute(new QName("context"));
            if(context != null){
                if(!context.getAttributeValue().startsWith("/"+MultitenantConstants.TENANT_AWARE_URL_PREFIX+"/" + tenantDomain)){
                    context.setAttributeValue("/"+MultitenantConstants.TENANT_AWARE_URL_PREFIX+"/" + tenantDomain + context.getAttributeValue());
                }
            }
        }
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
