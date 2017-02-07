/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transports.sap;

import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.ext.ServerDataEventListener;

import java.nio.file.Paths;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SAP requires to provide destination/server properties. <code>CarbonDestinationDataProvider </code>
 * provides a carbon specifc implementation where the server properties are loaded from a property
 * file *.serv * and client properties are loaded from a property file called *.dest
 */
public class CarbonDestinationDataProvider implements DestinationDataProvider, ServerDataProvider {

    private static final Log log = LogFactory.getLog(CarbonDestinationDataProvider.class);

    /**
     * Returns the client/server SAP properties
     * @param server server or client properties
     * @return SAP properties
     */
    public Properties getServerProperties(String server) {
        File file = getConfigurationFile(server, true);
        if (file != null) {
            if (log.isDebugEnabled()) {
                log.debug("Loading server configuration from: " + file.getPath());
            }

            try {
                Properties props = new Properties();
                props.load(new FileInputStream(file));
                return props;
            } catch (IOException e) {
                log.error("Error while loading server configuration from: " + file.getPath(), e);
            }
        }

        return null;
    }

    public void setServerDataEventListener(ServerDataEventListener listener) {

    }

    public Properties getDestinationProperties(String destination) {
        File file = getConfigurationFile(destination, false);
        if (file != null) {
            if (log.isDebugEnabled()) {
                log.debug("Loading destination configuration from: " + file.getPath());
            }

            try {
                Properties props = new Properties();
                props.load(new FileInputStream(file));
                return props;
            } catch (IOException e) {
                log.error("Error while loading destination configuration from: " + file.getPath(), e);
            }
        }

        return null;
    }

    public void setDestinationDataEventListener(DestinationDataEventListener listener) {

    }

    public boolean supportsEvents() {
        return false;
    }

    private File getConfigurationFile(String destination, boolean server) {
        String fileName = destination + "." + (server ? "server" : "dest");
        String confPath = System.getProperty("conf.location");
        if (confPath == null) {
            confPath = Paths.get("repository", "conf").toString();
        }
        File file1 = Paths.get(confPath, "sap", fileName).toFile();
        if (file1.exists()) {
            return file1;
        }

        File file2 = new File(fileName);
        if (file2.exists()) {
            return file2;
        }

        log.warn("JCo configuration file for the destination : " + destination + " does not " +
                "exist - Please specify the JCo configuration in " + file1.getPath() + " or " +
                file2.getPath());
        return null;
    }
}
