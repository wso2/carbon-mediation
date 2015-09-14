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

package org.wso2.carbon.bam.mediationstats.data.publisher.ui;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Helper class for the test_server_ajaxprocessor.jsp
 */
public class TestServerAjaxProcessorHelper {

    public boolean isNotNullOrEmpty(String string){
        return string != null && !string.equals("");
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
