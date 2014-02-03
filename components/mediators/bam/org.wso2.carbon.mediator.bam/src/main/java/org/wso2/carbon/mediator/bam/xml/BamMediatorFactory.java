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
package org.wso2.carbon.mediator.bam.xml;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.wso2.carbon.mediator.bam.BamMediator;
import org.wso2.carbon.mediator.bam.config.BamServerConfig;
import org.wso2.carbon.mediator.bam.config.BamServerConfigBuilder;
import org.wso2.carbon.mediator.bam.config.CryptographyManager;
import org.wso2.carbon.mediator.bam.config.RegistryManager;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Creates the BAM mediator with given configuration XML taken from the registry which is mentioned in the sequence.
 */
public class BamMediatorFactory extends AbstractMediatorFactory {
    private static final Log log = LogFactory.getLog(BamMediatorFactory.class);
    public static final QName BAM_Q = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "bam");
    public static final String SERVER_PROFILE_LOCATION = "bamServerProfiles";

    public Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        BamMediator bam = new BamMediator();
        BamServerConfigBuilder bamServerConfigBuilder = new BamServerConfigBuilder();
        String resourceString;
        String serverProfilePath = SERVER_PROFILE_LOCATION + "/" + this.getServerProfileName(omElement);
        String streamName = this.getStreamName(omElement);
        String streamVersion = this.getStreamVersion(omElement);
        if(isNotNullOrEmpty(serverProfilePath) && isNotNullOrEmpty(streamName) && isNotNullOrEmpty(streamVersion)){
            bam.setServerProfile(serverProfilePath);
        }
        RegistryManager registryManager = new RegistryManager();
        if(registryManager.resourceAlreadyExists(serverProfilePath)){
            resourceString = registryManager.getResourceString(serverProfilePath);
            try {
                OMElement resourceElement = new StAXOMBuilder(new ByteArrayInputStream(resourceString.getBytes(Charset.forName("UTF-8")))).getDocumentElement();
                boolean bamServerConfigCreated = bamServerConfigBuilder.createBamServerConfig(resourceElement);
                if(bamServerConfigCreated){
                    this.updateBamMediator(bamServerConfigBuilder, bam, streamName, streamVersion);
                }
            } catch (XMLStreamException e) {
                String errorMsg = "Failed to create XML OMElement from the String. " + e.getMessage();
                log.error(errorMsg, e);
            }
        }
        return bam;
    }

    public QName getTagQName() {
        return BAM_Q;
    }

    private String getServerProfileName(OMElement omElement){
        OMElement serverProfileElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "serverProfile"));
        if(serverProfileElement != null){
            OMAttribute serverProfileAttr = serverProfileElement.getAttribute(new QName("name"));
            if(serverProfileAttr != null){
                return serverProfileAttr.getAttributeValue();
            }
            else {
                return null;
            }
        }
        return null;
    }
    
    private String getStreamName(OMElement omElement){

        OMElement serverProfileElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "serverProfile"));
        if(serverProfileElement != null){
            OMElement streamConfigElement = serverProfileElement.getFirstChildWithName(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "streamConfig"));
            if(streamConfigElement != null){
                OMAttribute streamNameAttr = streamConfigElement.getAttribute(new QName("name"));
                if(streamNameAttr != null){
                    return streamNameAttr.getAttributeValue();
                }
                else{
                    return null;
                }
            }
            return null;
        }
        return null;
    }

    private String getStreamVersion(OMElement omElement){

        OMElement serverProfileElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "serverProfile"));
        if(serverProfileElement != null){
            OMElement streamConfigElement = serverProfileElement.getFirstChildWithName(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "streamConfig"));
            if(streamConfigElement != null){
                OMAttribute streamVersionAttr = streamConfigElement.getAttribute(new QName("version"));
                if(streamVersionAttr != null){
                    return streamVersionAttr.getAttributeValue();
                }
                else{
                    return null;
                }
            }
            return null;
        }
        return null;
    }

    private void updateBamMediator(BamServerConfigBuilder bamServerConfigBuilder, BamMediator bamMediator, String streamName, String streamVersion){
        BamServerConfig bamServerConfig=  bamServerConfigBuilder.getBamServerConfig();
        CryptographyManager cryptographyManager = new CryptographyManager();
        bamServerConfig.setPassword(cryptographyManager.base64DecodeAndDecrypt(bamServerConfig.getPassword()));
        StreamConfiguration streamConfiguration = bamServerConfig.getAUniqueStreamConfiguration(streamName, streamVersion);
        bamMediator.getStream().setBamServerConfig(bamServerConfig);
        bamMediator.getStream().setStreamConfiguration(streamConfiguration);

    }

    private boolean isNotNullOrEmpty(String string){
        return string != null && !string.equals("");
    }
}
