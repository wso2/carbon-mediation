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

package org.wso2.carbon.mediator.bam.config.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediator.bam.config.BamServerConfig;
import org.wso2.carbon.mediator.bam.config.BamServerConfigBuilder;
import org.wso2.carbon.mediator.bam.config.BamServerConfigXml;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;
import org.wso2.carbon.mediator.bam.config.stream.Property;
import org.wso2.carbon.mediator.bam.config.stream.StreamEntry;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for the configure_server_profiles.jsp
 */
public class BamServerProfileUtils {

    private static final Log log = LogFactory.getLog(BamServerProfileUtils.class);
    private BamServerProfileConfigAdminClient client;

    public BamServerProfileUtils(String cookie, String backendServerURL, ConfigurationContext configContext, Locale locale){
        try {
            client = new BamServerProfileConfigAdminClient(cookie, backendServerURL, configContext, locale);
        } catch (AxisFault e) {
            String errorMsg = "Error while creating BamServerProfileUtils. " + e.getMessage();
            log.error(errorMsg, e);
        }
    }

    public void addResource(String urlSet, String ip, String authenticationPort, String receiverPort, String userName, String password, boolean isSecure, boolean isLoadbalanced,
                            String streamConfigurationListString, String bamServerProfileLocation){

        String encryptedPassword = this.encryptPassword(password);
        String isSecureString, isLoadbalancedString;
        if(isSecure){
            isSecureString = "true";
        } else {
            isSecureString = "false";
        }
        if(isLoadbalanced){
            isLoadbalancedString = "true";
        } else {
            isLoadbalancedString = "false";
        }

        StreamConfigListBuilder streamConfigListBuilder = new StreamConfigListBuilder();
        List<StreamConfiguration> streamConfigurations = streamConfigListBuilder.getStreamConfigurationListFromString(streamConfigurationListString);
        BamServerConfigXml mediatorConfigurationXml = new BamServerConfigXml();
        OMElement storeXml = mediatorConfigurationXml.buildServerProfile(urlSet, ip, authenticationPort, receiverPort, userName, encryptedPassword, isSecureString, isLoadbalancedString, streamConfigurations);
        String stringStoreXml = storeXml.toString();

        try {
            client.saveResourceString(stringStoreXml, this.getRealBamServerProfilePath(bamServerProfileLocation));
        } catch (RemoteException e) {
            String errorMsg = "Error while adding resource. " + e.getMessage();
            log.error(errorMsg, e);
        }
    }

    public BamServerConfig getResource(String bamServerProfileLocation){
        try {
            String resourceString =  client.getResourceString(this.getRealBamServerProfilePath(bamServerProfileLocation));
            OMElement resourceElement = new StAXOMBuilder(new ByteArrayInputStream(resourceString.getBytes(Charset.forName("UTF-8")))).getDocumentElement();

            BamServerConfigBuilder bamServerConfigBuilder = new BamServerConfigBuilder();
            bamServerConfigBuilder.createBamServerConfig(resourceElement);
            return bamServerConfigBuilder.getBamServerConfig();
        } catch (RemoteException e) {
            String errorMsg = "Error while getting the resource. " + e.getMessage();
            log.error(errorMsg, e);
        } catch (XMLStreamException e) {
            String errorMsg = "Error while creating OMElement from the string. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return null;
    }

    public boolean resourceAlreadyExists(String bamServerProfileLocation){
        try {
            return client.resourceAlreadyExists(this.getRealBamServerProfilePath(bamServerProfileLocation));
        } catch (RemoteException e) {
            String errorMsg = "Error while checking the resource. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return true;
    }
    
    public void addCollection(String path){
        try {
            client.addCollection(path);
        } catch (RemoteException e) {
            String errorMsg = "Error while adding the collection. " + e.getMessage();
            log.error(errorMsg, e);
        }
    }
    
    public boolean removeResource(String path){
        try {
            return client.removeResource(path);
        } catch (RemoteException e) {
            String errorMsg = "Error while removing the resource. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return false;
    }

    public String encryptPassword(String plainTextPassword){
        try {
            return client.encryptAndBase64Encode(plainTextPassword);
        } catch (RemoteException e) {
            String errorMsg = "Error while encrypting the password. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return "";
    }

    public String decryptPassword(String cipherTextPassword){
        try {
            return client.base64DecodeAndDecrypt(cipherTextPassword);
        } catch (RemoteException e) {
            String errorMsg = "Error while decrypting the password. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return "";
    }

    public String getStreamConfigurationListString(StreamConfiguration streamConfiguration){
        StringBuilder returnStringBuilder = new StringBuilder("");
        if(streamConfiguration != null){
            List<Property> properties = streamConfiguration.getProperties();
            for (Property property : properties) {
                returnStringBuilder.append(property.getKey() + "::" + property.getValue() + "::" + property.getType() + "::");
                if(property.isExpression()){
                    returnStringBuilder.append("expression");
                } else {
                    returnStringBuilder.append("value");
                }
                returnStringBuilder.append(";");
            }
            returnStringBuilder.append("^");
            List<StreamEntry> streamEntries = streamConfiguration.getEntries();
            if(streamEntries.size() == 2){
                returnStringBuilder.append("dump;dump");
            } else if (streamEntries.size() == 1 && streamEntries.get(0).getValue().equals("$SOAPHeader")){
                returnStringBuilder.append("dump;notDump");
            } else if (streamEntries.size() == 1 && streamEntries.get(0).getValue().equals("$SOAPBody")){
                returnStringBuilder.append("notDump;dump");
            } else if (streamEntries.size() == 0){
                returnStringBuilder.append("notDump;notDump");
            }
            return returnStringBuilder.toString();
        } else {
            return "";
        }
    }

    private String getRealBamServerProfilePath(String shortServerProfilePath){
        if(shortServerProfilePath != null){
            String registryType = shortServerProfilePath.split("::")[0];
            if (isNotNullOrEmpty(registryType) && registryType.equals("conf")){
                return shortServerProfilePath.split("::")[1];
            }
            return shortServerProfilePath;
        }
        return shortServerProfilePath;
    }

    public boolean isNotNullOrEmpty(String string){
        return string != null && !string.equals("");
    }
}