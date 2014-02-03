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

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Locale;

/**
 * Helper class for the dropdown_ajaxprocessor.jsp
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

    public boolean isNotNullOrEmpty(String string){
        return string != null && !string.equals("");
    }

    public String getServerProfileNames(String serverProfilePath){
        StringBuilder serverProfileNamesString = new StringBuilder("");
        try {
            String[] serverProfileNames = client.getServerProfilePathList(serverProfilePath);
            if (serverProfileNames!= null) {
                for (String serverProfileName : serverProfileNames) {
                    serverProfileNamesString.append("<option>" +
                                                    serverProfileName.split("/")[serverProfileName.split("/").length-1] +
                                                    "</option>");
                }
            }
        } catch (RemoteException e) {
            String errorMsg = "Error while getting Server Profile Name List. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return serverProfileNamesString.toString();
    }

    public String backendServerExists(String ip, String port){
        try {
            new Socket(ip, Integer.parseInt(port));
            return "true";
        } catch (UnknownHostException e) {
            return "false";
        } catch (IOException e) {
            return "false";
        } catch (Exception e) {
            return "false";
        }
    }

}
