package org.wso2.carbon.das.messageflow.data.publisher.publish;

import org.apache.log4j.Logger;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.das.data.publisher.util.DASDataPublisherConstants;
import org.wso2.carbon.das.messageflow.data.publisher.services.MediationConfigReporterThread;
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


    private synchronized static DataPublisher initDataPublisher(){
        if (publisher == null){
            try {
                loadConfigs();

                publisher = new DataPublisher(null,receiverUrl, authUrl, username, password);
                if (log.isDebugEnabled()) {
                    log.debug("Connected to analytics sever with the following details, " +
                            " ReceiverURL:" + receiverUrl + ", AuthURL:" + authUrl + ", Username:" + username);
                }
            } catch (DataEndpointAgentConfigurationException e) {
                log.error("Error while creating databridge publisher", e);
            } catch (DataEndpointException e) {
                log.error("Error while creating databridge publisher", e);
            } catch (DataEndpointConfigurationException e) {
                log.error("Error while creating databridge publisher", e);
            } catch (DataEndpointAuthenticationException e) {
                log.error("Error while creating databridge publisher", e);
            } catch (TransportException e) {
                log.error("Error while creating databridge publisher", e);
            }
        }

        return publisher;
    }

    public static DataPublisher getDataPublisher() {
        if (publisher == null) {
            initDataPublisher();
        }
        return  publisher;
    }

    public static void publish(Event event) {
        if (publisher != null) {
            publisher.publish(event);
        }
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
