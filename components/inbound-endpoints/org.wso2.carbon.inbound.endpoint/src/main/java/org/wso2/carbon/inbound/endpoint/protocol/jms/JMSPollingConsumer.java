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

import java.util.Date;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.inbound.endpoint.protocol.jms.factory.CachedJMSConnectionFactory;

public class JMSPollingConsumer {

    private static final Log logger = LogFactory.getLog(JMSPollingConsumer.class.getName());

    private CachedJMSConnectionFactory jmsConnectionFactory;
    private JMSInjectHandler injectHandler;
    private long scanInterval;
    private Long lastRanTime;
    private String strUserName;
    private String strPassword;
    private Integer iReceiveTimeout;
    
    public JMSPollingConsumer(CachedJMSConnectionFactory jmsConnectionFactory,
            Properties jmsProperties, long scanInterval) {
        this.jmsConnectionFactory = jmsConnectionFactory;
        strUserName = jmsProperties.getProperty(JMSConstants.PARAM_JMS_USERNAME);
        strPassword = jmsProperties.getProperty(JMSConstants.PARAM_JMS_PASSWORD);
        
        String strReceiveTimeout = jmsProperties.getProperty(JMSConstants.RECEIVER_TIMEOUT);
        if(strReceiveTimeout != null){
            try{
                iReceiveTimeout = Integer.parseInt(strReceiveTimeout.trim());  
            }catch(NumberFormatException e){
                logger.warn("Invalid value for transport.jms.ReceiveTimeout : " + strReceiveTimeout);
                iReceiveTimeout = null;
            }
        }
        
        this.scanInterval = scanInterval;
        this.lastRanTime = null;
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
            long currentTime = (new Date()).getTime();
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

        Connection connection = null;
        Session session = null;
        Destination destination = null;
        MessageConsumer messageConsumer = null;

        try {
            connection = jmsConnectionFactory.getConnection(strUserName, strPassword);
            if (connection == null) {
                logger.warn("Inbound JMS endpoint unable to get a connection.");
                return null;
            }
            session = jmsConnectionFactory.getSession(connection);
            destination = jmsConnectionFactory.getDestination(connection);
            messageConsumer = jmsConnectionFactory.getMessageConsumer(session, destination);            
            Message msg = receiveMessage(messageConsumer);         
            if (msg == null) {
                logger.debug("Inbound JMS Endpoint. No JMS message received.");
                return null;
            }
            while (msg != null) {
                if (!JMSUtils.inferJMSMessageType(msg).equals(TextMessage.class.getName())) {
                    logger.error("JMS Inbound transport support JMS TextMessage type only. Found message type "
                            + JMSUtils.inferJMSMessageType(msg));
                    return null;
                }

                if (injectHandler != null) {

                    boolean commitOrAck = true;
                    commitOrAck = injectHandler.invoke(msg);
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
            logger.error("Error while receiving JMS message. " + e.getMessage(), e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Error while receiving JMS message. " + e.getMessage(), e);
        } finally {
            if (messageConsumer != null) {
                jmsConnectionFactory.closeConsumer(messageConsumer);
            }
            if (session != null) {
                jmsConnectionFactory.closeSession(session);
            }
            if (connection != null) {
                jmsConnectionFactory.closeConnection(connection);
            }
        }
        return null;
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
}
