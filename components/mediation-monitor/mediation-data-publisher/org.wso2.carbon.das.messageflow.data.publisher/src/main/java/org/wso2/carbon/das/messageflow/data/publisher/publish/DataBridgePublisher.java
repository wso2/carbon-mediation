/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.das.messageflow.data.publisher.publish;

import org.apache.log4j.Logger;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.das.data.publisher.util.DASDataPublisherConstants;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.utils.CarbonUtils;

public class DataBridgePublisher {

    private static DataPublisher publisher;
    private static Logger log = Logger.getLogger(DataBridgePublisher.class);
    private static String receiverUrl;
    private static String authUrl;
    private static String username;
    private static String password;

    private synchronized static void initDataPublisher() {
        if (publisher == null) {
            try {
                loadConfigs();

                publisher = new DataPublisher(null, receiverUrl, authUrl, username, password);
                if (log.isDebugEnabled()) {
                    log.debug("Connected to analytics sever with the following details, " +
                            " ReceiverURL:" + receiverUrl + ", AuthURL:" + authUrl + ", Username:" + username);
                }
            } catch (DataEndpointAgentConfigurationException | DataEndpointConfigurationException |
                    DataEndpointException | DataEndpointAuthenticationException | TransportException e) {
                log.error("Error while creating databridge publisher", e);
            }
        }
    }

    public static DataPublisher getDataPublisher() {
        if (publisher == null) {
            initDataPublisher();
        }
        return  publisher;
    }

    private static void loadConfigs() {
        String agentConfPath = CarbonUtils.getCarbonConfigDirPath() + DASDataPublisherConstants.DATA_AGENT_CONFIG_PATH;
        AgentHolder.setConfigPath(agentConfPath);

        receiverUrl = ServerConfiguration.getInstance().getFirstProperty(DASDataPublisherConstants.ANALYTICS_RECEIVER_URL);
        authUrl = ServerConfiguration.getInstance().getFirstProperty(DASDataPublisherConstants.ANALYTICS_AUTH_URL);
        username = ServerConfiguration.getInstance().getFirstProperty(DASDataPublisherConstants.ANALYTICS_USERNAME);
        password = ServerConfiguration.getInstance().getFirstProperty(DASDataPublisherConstants.ANALYTICS_PASSWORD);

    }
}
