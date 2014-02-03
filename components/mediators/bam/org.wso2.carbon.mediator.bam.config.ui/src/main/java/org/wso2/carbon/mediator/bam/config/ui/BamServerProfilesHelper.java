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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.rmi.RemoteException;
import java.util.Locale;

public class BamServerProfilesHelper {

    private static final Log log = LogFactory.getLog(BamServerProfilesHelper.class);
    private BamServerProfileConfigAdminClient client;

    public BamServerProfilesHelper(String cookie, String backendServerURL,
                                   ConfigurationContext configContext, Locale locale){
        try {
            client = new BamServerProfileConfigAdminClient(cookie, backendServerURL, configContext, locale);
        } catch (AxisFault e) {
            String errorMsg = "Error while creating the BamServerProfileConfigAdminClient. " + e.getMessage();
            log.error(errorMsg, e);
        }
    }

    public String[] getServerProfileList(String serverProfilePath){
        try {
            String[] profiles = client.getServerProfilePathList(serverProfilePath);
            if(profiles != null) {
                for (int i=0; i<profiles.length; i++) {
                    profiles[i] = profiles[i].split("/")[profiles[i].split("/").length-1];
                }
                return profiles;
            }
        } catch (RemoteException e) {
            String errorMsg = "Error while getting Server Profile Name List. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return new String[0];
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

    public boolean removeResource(String path){
        try {
            return client.removeResource(path);
        } catch (RemoteException e) {
            String errorMsg = "Error while removing the resource. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return false;
    }

    public boolean isNotNullOrEmpty(String string){
        return string != null && !string.equals("");
    }
}
