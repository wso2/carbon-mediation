/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.bam.service.data.publisher.data;

public class BAMServerInfo {

    private String bamServerURL;
    private String bamUserName;
    private String bamPassword;

    public String getBamServerURL() {
        return bamServerURL;
    }

    public void setBamServerURL(String bamServerURL) {
        this.bamServerURL = bamServerURL;
    }

    public String getBamUserName() {
        return bamUserName;
    }

    public void setBamUserName(String bamUserName) {
        this.bamUserName = bamUserName;
    }

    public String getBamPassword() {
        return bamPassword;
    }

    public void setBamPassword(String bamPassword) {
        this.bamPassword = bamPassword;
    }

}
