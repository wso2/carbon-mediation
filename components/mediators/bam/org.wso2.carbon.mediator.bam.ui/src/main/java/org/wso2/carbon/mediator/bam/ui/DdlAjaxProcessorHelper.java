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

package org.wso2.carbon.mediator.bam.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediator.bam.config.BamServerConfig;
import org.wso2.carbon.mediator.bam.config.BamServerConfigBuilder;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper class of the dropdown_ajaxprocessor.jsp
 */
public class DdlAjaxProcessorHelper {

    private static final Log log = LogFactory.getLog(DdlAjaxProcessorHelper.class);
    private BamServerProfileConfigAdminClient client;

    public DdlAjaxProcessorHelper(String cookie, String backendServerURL,
                                  ConfigurationContext configContext, Locale locale){
        try {
            client = new BamServerProfileConfigAdminClient(cookie, backendServerURL, configContext, locale);
        } catch (AxisFault e) {
            String errorMsg = "Error while creating the BamServerProfileConfigAdminClient. " + e.getMessage();
            log.error(errorMsg, e);
        }
    }

    private BamServerConfig getResource(String bamServerProfileLocation){
        try {
            String resourceString =  client.getResourceString(bamServerProfileLocation);
            OMElement resourceElement = new StAXOMBuilder(new ByteArrayInputStream(resourceString.getBytes(Charset.forName("UTF-8")))).getDocumentElement();

            BamServerConfigBuilder bamServerConfigBuilder = new BamServerConfigBuilder();
            bamServerConfigBuilder.createBamServerConfig(resourceElement);
            return bamServerConfigBuilder.getBamServerConfig();
        } catch (RemoteException e) {
            String errorMsg = "Error while getting the resource. " + e.getMessage();
            log.error(errorMsg, e);
        } catch (XMLStreamException e) {
            String errorMsg = "Error while creating OMElement from a string. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return null;
    }

    public boolean isNotNullOrEmpty(String string){
        return string != null && !string.equals("");
    }

    public String getServerProfileNames(String serverProfilePath){
        StringBuilder serverProfileNamesStringBuilder = new StringBuilder("");
        try {
            String[] serverProfileNames = client.getServerProfilePathList(serverProfilePath);
            for (String serverProfileName : serverProfileNames) {
                serverProfileNamesStringBuilder.append("<option>" +
                                                       serverProfileName.split("/")[serverProfileName.split("/").length-1] +
                                                       "</option>");
            }
        } catch (RemoteException e) {
            String errorMsg = "Error while getting Server Profile Names. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return serverProfileNamesStringBuilder.toString();
    }

    public String getStreamConfigurationNames(String serverProfilesLocation){
        StringBuilder streamNamesBuilder = new StringBuilder("");
        BamServerConfig bamServerConfig = this.getResource(serverProfilesLocation);
        List<StreamConfiguration> streamConfigurations = bamServerConfig.getStreamConfigurations();
        List<String> foundStreamNames = new ArrayList<String>();
        for (StreamConfiguration configuration : streamConfigurations) {
            if(!foundStreamNames.contains(configuration.getName())){ // Add only unique stream names
                streamNamesBuilder.append("<option>" + configuration.getName() + "</option>");
            }
            foundStreamNames.add(configuration.getName());
        }
        return streamNamesBuilder.toString();
    }
    
    public String getVersionListForStreamName(String serverProfilePath, String streamName){
        StringBuilder streamVersionsBuilder = new StringBuilder("");
        BamServerConfig bamServerConfig = this.getResource(serverProfilePath);
        List<StreamConfiguration> streamConfigurations = bamServerConfig.getStreamConfigurations();
        for (StreamConfiguration configuration : streamConfigurations) {
            if(configuration.getName().equals(streamName)){
                streamVersionsBuilder.append("<option>" + configuration.getVersion() + "</option>");
            }
        }
        return streamVersionsBuilder.toString();
    }

}
