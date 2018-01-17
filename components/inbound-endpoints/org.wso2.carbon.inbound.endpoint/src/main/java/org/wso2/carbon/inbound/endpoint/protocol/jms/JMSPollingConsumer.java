/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint.protocol.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.carbon.inbound.endpoint.protocol.jms.factory.CachedJMSConnectionFactory;

import java.util.Date;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class JMSPollingConsumer {

    private static final Log logger = LogFactory.getLog(JMSPollingConsumer.class.getName());

    /* Contents used for the process of reconnection */
    private static final int DEFAULT_RETRY_ITERATION = 0;
    private static final int DEFAULT_RETRY_DURATION = 1000;
    private static final double RECONNECTION_PROGRESSION_FACTOR = 2.0;
    private static final long MAX_RECONNECTION_DURATION = 60000;
    private static final int SCALE_FACTOR = 1000;

    private CachedJMSConnectionFactory jmsConnectionFactory;
    private JMSInjectHandler injectHandler;
    private long scanInterval;
    private Long lastRanTime;
    private String strUserName;
    private String strPassword;
    private Integer iReceiveTimeout;
    private String replyDestinationName;
    private String name;
    private Properties jmsProperties;
    private boolean isConnected;

    private Long reconnectDuration;
    private long retryDuration;
    private int retryIteration;

    private Connection connection = null;
    private Session session = null;
    private Destination destination = null;
    private MessageConsumer messageConsumer = null;
    private Destination replyDestination = null;

    private int currentNegativeCommitOrAckCount = 0;
    private boolean pollingSuspended = false;
    private int pollingSuspensionLimit = -1;
    private int pollingSuspensionPeriod = JMSConstants.DEFAULT_JMS_CLIENT_POLLING_SUSPENSION_PERIOD;
    private boolean pollingSuspensionEnabled = false; // by default polling is enabled.

    public JMSPollingConsumer( Properties jmsProperties, long scanInterval, String name) {
        this.jmsConnectionFactory = new CachedJMSConnectionFactory(jmsProperties);
        strUserName = jmsProperties.getProperty(JMSConstants.PARAM_JMS_USERNAME);
        strPassword = jmsProperties.getProperty(JMSConstants.PARAM_JMS_PASSWORD);
        this.name = name;
        this.retryIteration = DEFAULT_RETRY_ITERATION;
        this.retryDuration = DEFAULT_RETRY_DURATION;

        String pollingSuspensionLimitValue = jmsProperties
                .getProperty(JMSConstants.JMS_CLIENT_POLLING_RETRIES_BEFORE_SUSPENSION);

        if (pollingSuspensionLimitValue != null) {
            try {
                this.pollingSuspensionLimit = Integer.parseInt(pollingSuspensionLimitValue);
                if (pollingSuspensionLimit > -1) {
                    pollingSuspensionEnabled = true;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Polling suspension is enabled for Inbound Endpoint " + name);
                    }
                    if (pollingSuspensionLimit == 0) {
                        pollingSuspended = true;
                    }
                }
            } catch (NumberFormatException e) {
                throw new SynapseException(
                        "Invalid numeric value for " + JMSConstants.JMS_CLIENT_POLLING_RETRIES_BEFORE_SUSPENSION
                                + ". Inbound Endpoint " + name + " deployment failed.");
            }
            if (pollingSuspensionEnabled) {

                String pollingSuspensionPeriodValue = jmsProperties
                        .getProperty(JMSConstants.JMS_CLIENT_POLLING_SUSPENSION_PERIOD);

                if (pollingSuspensionPeriodValue != null) {
                    try {
                        this.pollingSuspensionPeriod = Integer.parseInt(pollingSuspensionPeriodValue);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid numeric value for " + JMSConstants.JMS_CLIENT_POLLING_SUSPENSION_PERIOD
                                + " . Default value of " + JMSConstants.DEFAULT_JMS_CLIENT_POLLING_SUSPENSION_PERIOD
                                + " milliseconds will be accounted.");
                    }
                } else {
                    logger.warn("No value specified for pollingSuspensionPeriod, hence default value of 60000 "
                            + "milliseconds will be accounted.");
                }
            }
        }
        
        String strReceiveTimeout = jmsProperties.getProperty(JMSConstants.RECEIVER_TIMEOUT);
        if(strReceiveTimeout != null){
            try{
                iReceiveTimeout = Integer.parseInt(strReceiveTimeout.trim());  
            }catch(NumberFormatException e){
                logger.warn("Invalid value for transport.jms.ReceiveTimeout : " + strReceiveTimeout);
                iReceiveTimeout = null;
            }
        }

        String strReconnectDuration = jmsProperties.getProperty(JMSConstants.JMS_RETRY_DURATION);
        if (strReconnectDuration != null) {
            try {
                this.reconnectDuration = Long.parseLong(strReconnectDuration.trim());
            } catch (NumberFormatException e) {
                logger.warn("Invalid value for transport.jms.retry.duration : " + strReconnectDuration);
                this.reconnectDuration = null;
            }
        }
        this.replyDestinationName = jmsProperties.getProperty(JMSConstants.PARAM_REPLY_DESTINATION);
        this.scanInterval = scanInterval;
        this.lastRanTime = null;
        this.jmsProperties = jmsProperties;
    }

    /**
     * 
     * Register a handler to implement injection of the retrieved message
     * 
     * @param injectHandler
     */
    public void registerHandler(JMSInjectHandler injectHandler) {
        this.injectHandler = injectHandler;
    }

    /**
     * This will be called by the task scheduler. If a cycle execution takes
     * more than the schedule interval, tasks will call this method ignoring the
     * interval. Timestamp based check is done to avoid that.
     */
    public void execute() {
        try {
            logger.debug("Executing : JMS Inbound EP : ");
            // Check if the cycles are running in correct interval and start
            // scan
            if (pollingSuspensionLimit == 0) {
                logger.info("Polling is suspended permanently since \""
                        + JMSConstants.JMS_CLIENT_POLLING_RETRIES_BEFORE_SUSPENSION + "\" is Zero.");
                return;
            }

            long currentTime = (new Date()).getTime();

            if (pollingSuspended) {
                if (lastRanTime + pollingSuspensionPeriod <= currentTime) {
                    pollingSuspended = false;
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Polling re-started since the suspension period of " + pollingSuspensionPeriod + " "
                                        + "milliseconds exceeded.");
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Polling is suspended. Polling will be re-activated in " + (pollingSuspensionPeriod - (
                                        currentTime - lastRanTime)) + " milliseconds.");
                    }
                    return;
                }
            }

            if (lastRanTime == null || ((lastRanTime + (scanInterval)) <= currentTime)) {
                lastRanTime = currentTime;
                poll();
            } else if (logger.isDebugEnabled()) {
                logger.debug("Skip cycle since concurrent rate is higher than the scan interval : JMS Inbound EP ");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("End : JMS Inbound EP : ");
            }
        } catch (Exception e) {
            logger.error("Error while retrieving or injecting JMS message. " + e.getMessage(), e);
        }
    }

    /**
     * Create connection with broker and retrieve the messages. Then inject
     * according to the registered handler
     */
    public Message poll() {
        logger.debug("Polling JMS messages.");
       
        try {
            connection = jmsConnectionFactory.getConnection(strUserName, strPassword);
            if (connection == null) {
                logger.warn("Inbound JMS endpoint unable to get a connection.");
                isConnected = false;
                return null;
            }
            if (retryIteration != DEFAULT_RETRY_ITERATION) {
                logger.info("Reconnection attempt: " + retryIteration + " for the JMS Inbound: " + name +
                        " was successful!");
                this.retryIteration = DEFAULT_RETRY_ITERATION;
                this.retryDuration = DEFAULT_RETRY_DURATION;
            }
            isConnected = true;
            session = jmsConnectionFactory.getSession(connection);
            //Fixing ESBJAVA-4446
            //Closing the connection if we cannot get a session.
            //Then in the next poll iteration it will create a new connection
            //instead of using cached connection
            if (session == null) {
                logger.warn("Inbound JMS endpoint unable to get a session.");
                jmsConnectionFactory.closeConnection();
                return null;
            }
            destination = jmsConnectionFactory.getDestination(session);
            if (replyDestinationName != null && !replyDestinationName.trim().equals("")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Using the reply destination as " + replyDestinationName +
                                 " in inbound endpoint.");
                }
                replyDestination =
                                   jmsConnectionFactory.createDestination(session,
                                                                          replyDestinationName);
            }
            messageConsumer = jmsConnectionFactory.getMessageConsumer(session, destination);
            if (messageConsumer == null) {
                logger.debug("Inbound JMS Endpoint. No JMS consumer initialized. No JMS message received.");
                if (session != null) {
                    jmsConnectionFactory.closeSession(session, true);
                }
                if (connection != null) {
                    jmsConnectionFactory.closeConnection(connection, true);
                }
                return null;
            }
            Message msg = receiveMessage(messageConsumer);
            if (msg == null) {
                logger.debug("Inbound JMS Endpoint. No JMS message received.");
                return null;
            }
            while (msg != null) {
                if (JMSUtils.inferJMSMessageType(msg) == null) {
                    logger.error("Invalid JMS Message type.");
                    return null;
                }

                if (injectHandler != null) {

                    boolean commitOrAck = true;
                    // Set the reply destination and connection
                    if (replyDestination != null) {
                        injectHandler.setReplyDestination(replyDestination);
                    }
                    injectHandler.setConnection(connection);
                    commitOrAck = injectHandler.invoke(msg, name);

                    if (pollingSuspensionEnabled) {
                        if (!commitOrAck) {
                            currentNegativeCommitOrAckCount++;
                            if (currentNegativeCommitOrAckCount >= pollingSuspensionLimit) {
                                pollingSuspended = true;
                                currentNegativeCommitOrAckCount = 0;
                                logger.info(
                                        "Suspending polling as the pollingSuspensionLimit of " + pollingSuspensionLimit
                                                + " reached. Polling will be re-started after "
                                                + pollingSuspensionPeriod + " milliseconds");
                                break;
                            }
                        } else {
                            currentNegativeCommitOrAckCount = 0;
                        }
                    }

                    // if client acknowledgement is selected, and processing
                    // requested ACK
                    if (jmsConnectionFactory.getSessionAckMode() == Session.CLIENT_ACKNOWLEDGE) {
                        if (commitOrAck) {
                            try {
                                msg.acknowledge();
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Message : " + msg.getJMSMessageID()
                                            + " acknowledged");
                                }
                            } catch (JMSException e) {
                                logger.error(
                                        "Error acknowledging message : " + msg.getJMSMessageID(), e);
                            }
                        } else {
                            // Need to create a new consumer and session since
                            // we need to rollback the message
                            if (messageConsumer != null) {
                                jmsConnectionFactory.closeConsumer(messageConsumer);
                            }
                            if (session != null) {
                                jmsConnectionFactory.closeSession(session);
                            }
                            session = jmsConnectionFactory.getSession(connection);
                            messageConsumer = jmsConnectionFactory.getMessageConsumer(session,
                                    destination);
                        }
                    }
                    // if session was transacted, commit it or rollback
                    if (jmsConnectionFactory.isTransactedSession()) {
                        try {
                            if (session.getTransacted()) {
                                if (commitOrAck) {
                                    session.commit();
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Session for message : "
                                                + msg.getJMSMessageID() + " committed");
                                    }
                                } else {
                                    session.rollback();
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Session for message : "
                                                + msg.getJMSMessageID() + " rolled back");
                                    }
                                }
                            }
                        } catch (JMSException e) {
                            logger.error("Error " + (commitOrAck ? "committing" : "rolling back")
                                    + " local session txn for message : " + msg.getJMSMessageID(),
                                    e);
                        }
                    }
                } else {
                    return msg;
                }
                msg = receiveMessage(messageConsumer);
            }

        } catch (JMSException e) {
            logger.error("Error while receiving JMS message for " + name, e);
            releaseResources(true);
        } catch (Exception e) {
            logger.error("Error while receiving JMS message for " + name, e);
        } finally {
            if (!isConnected) {
                if (reconnectDuration != null) {
                    retryDuration = reconnectDuration;
                    logger.error("Reconnection attempt : " + (retryIteration++) + " for JMS Inbound : " +
                            name + " failed. Next retry in " + (retryDuration / SCALE_FACTOR) +
                            " seconds. (Fixed Interval)");
                } else {
                    retryDuration = (long) (retryDuration * RECONNECTION_PROGRESSION_FACTOR);
                    if (retryDuration > MAX_RECONNECTION_DURATION) {
                        retryDuration = MAX_RECONNECTION_DURATION;
                        logger.info("InitialReconnectDuration reached to MaxReconnectDuration.");
                    }
                    logger.error("Reconnection attempt : " + (retryIteration++) + " for JMS Inbound : " +
                            name + " failed. Next retry in " + (retryDuration / SCALE_FACTOR) +
                            " seconds");
                }
                try {
                    Thread.sleep(retryDuration);
                } catch (InterruptedException ignore) {
                    Thread.currentThread().interrupt();
                    /* Occurs when the owner of this thread sets the Interrupted flag to TRUE. Inside the sleep method
                       this flag will be checked occasionally and throw an InterruptedException (and reset the flag)
                       whenever its set to true. Ideally, after catching this we should wrap up the work and exist.
                       Since this can only happen during an ESB shutdown it can be ignored here. But as a good
                       practice the Interrupted flag is set back to TRUE in this thread. */
                }
            }
            releaseResources(false);
        }
        return null;
    }

    /**
     * Release the JMS connection, session and consumer to the pool or forcefully close the resource.
     *
     * @param forcefullyClose false if the resource needs to be released to the pool and true other wise
     *
     */
    private void releaseResources(boolean forcefullyClose) {
        if (messageConsumer != null) {
            jmsConnectionFactory.closeConsumer(messageConsumer, forcefullyClose);
        }
        if (session != null) {
            jmsConnectionFactory.closeSession(session, forcefullyClose);
        }
        if (connection != null) {
            jmsConnectionFactory.closeConnection(connection, forcefullyClose);
        }
    }

    public void destroy(){
        if (messageConsumer != null) {
            jmsConnectionFactory.closeConsumer(messageConsumer, true);
        }
        if (session != null) {
            jmsConnectionFactory.closeSession(session, true);
        }
        if (connection != null) {
            jmsConnectionFactory.closeConnection(connection, true);
        }
    }
    
    private Message receiveMessage(MessageConsumer messageConsumer) throws JMSException{
        Message msg = null;
        if(iReceiveTimeout == null){
            msg = messageConsumer.receive(1);
        }else if(iReceiveTimeout > 0){
            msg = messageConsumer.receive(iReceiveTimeout);
        }else{
            msg = messageConsumer.receive();
        }
        return msg;
    }

    protected Properties getInboundProperites() {
        return jmsProperties;
    }
}
