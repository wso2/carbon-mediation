/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.gateway.common;

/**
 * <code>CGServerBean </code> contains some meta information of the server where the CG_TRANSPORT_NAME
 * component will be deployed. These information are used to authenticate a client when deploying a
 * proxy in CG_TRANSPORT_NAME agent component.
 */
public class CGServerBean {

    /**
     * A readable name for the server, e.g. dev-server
     */
    private String name;


    /**
     * User name of the user
     */
    private String userName;

    /**
     * User password
     */
    private String passWord;

    /**
     * The remote carbon server host(ip)
     */
    private String host;

    /**
     * Remote carbon server ip
     */
    private String port;

    /**
     * Domain name of the
     */
    private String domainName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}

