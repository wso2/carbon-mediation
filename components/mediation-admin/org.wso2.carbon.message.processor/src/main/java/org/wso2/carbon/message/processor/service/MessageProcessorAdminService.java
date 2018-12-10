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
package org.wso2.carbon.message.processor.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MessageProcessorFactory;
import org.apache.synapse.config.xml.MessageProcessorSerializer;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.MessageProducer;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.processor.impl.ScheduledMessageProcessor;
import org.apache.synapse.message.processor.impl.failover.FailoverMessageForwardingProcessorView;
import org.apache.synapse.message.processor.impl.failover.FailoverScheduledMessageForwardingProcessor;
import org.apache.synapse.message.processor.impl.forwarder.MessageForwardingProcessorView;
import org.apache.synapse.message.processor.impl.forwarder.ScheduledMessageForwardingProcessor;
import org.apache.synapse.message.processor.impl.sampler.SamplingProcessor;
import org.apache.synapse.message.processor.impl.sampler.SamplingProcessorView;
import org.opensaml.xml.signature.P;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

@SuppressWarnings({"UnusedDeclaration"})
public class MessageProcessorAdminService extends AbstractServiceBusAdmin {
    private static Log log = LogFactory.getLog(MessageProcessorAdminService.class);

    public static final int MSGS_PER_PAGE = 10;
    private static String CONF_LOCATION = "conf.location";
    public final static String DEFAULT_AXIS2_XML;

    static {
        String confPath = System.getProperty(CONF_LOCATION);
        if (confPath == null) {
            confPath = Paths.get("repository", "conf").toString();
        }
        DEFAULT_AXIS2_XML = Paths.get(confPath, "axis2", "axis2_blocking_client.xml").toString();
    }


    /**
     * Get an XML configuration element for a message processor from the FE and creates and add
     * the MessageStore to the synapse configuration.
     *
     * @param xml string that contain the message processor configuration.
     * @throws AxisFault if some thing goes wrong when creating
     *                   a MessageProcessor with the given xml.
     */
    public void addMessageProcessor(String xml) throws AxisFault {
        try {
            OMElement msElem = createElement(xml);
            MessageProcessor messageProcessor =
                    MessageProcessorFactory.createMessageProcessor(msElem);
            if (messageProcessor != null && messageProcessor.getName() != null) {
                SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
                String fileName = ServiceBusUtils.generateFileName(messageProcessor.getName());
                messageProcessor.init(getSynapseEnvironment());
                messageProcessor.setFileName(fileName);
                synapseConfiguration.addMessageProcessor(messageProcessor.getName(),
                        messageProcessor);
                if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                    MediationPersistenceManager mp = getMediationPersistenceManager();
                    mp.saveItem(messageProcessor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
                }
            } else {
                String message = "Unable to create Message Processor ";
                handleException(log, message, null);
            }

        } catch (XMLStreamException e) {
            String message = "Unable to create Message Processor ";
            handleException(log, message, e);
        }

    }

    /**
     * Modify and Existing Message processor based on the given XML that is passed from the FE
     *
     * @param xml XML configuration for the changed Message processor
     * @throws AxisFault if Some thing goes wrong when modifying the
     *                   Message processor
     */
    public void modifyMessageProcessor(String xml) throws AxisFault {
        try {
            OMElement msElem = createElement(xml);
            MessageProcessor messageProcessor =
                    MessageProcessorFactory.createMessageProcessor(msElem);
            if (messageProcessor != null && messageProcessor.getName() != null) {
                SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
                MessageProcessor removedProcessor =
                        synapseConfiguration.removeMessageProcessor(messageProcessor.getName());
                if (removedProcessor != null) {
                    removedProcessor.destroy();
                }
                messageProcessor.init(getSynapseEnvironment());
                String fileName = ServiceBusUtils.generateFileName(messageProcessor.getName());
                messageProcessor.setFileName(fileName);
                synapseConfiguration.addMessageProcessor(messageProcessor.getName(),
                        messageProcessor);

                if (removedProcessor.getArtifactContainerName() != null) {
                    messageProcessor.setArtifactContainerName(removedProcessor.getArtifactContainerName());
                    messageProcessor.setIsEdited(true);
                }
                else {
                    if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                        MediationPersistenceManager mp = getMediationPersistenceManager();
                        mp.saveItem(messageProcessor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
                    }
                }
            } else {
                String message = "Unable to Update Message Processor ";
                handleException(log, message, null);
            }

        } catch (XMLStreamException e) {
            String message = "Unable to Modify Message Processor ";
            handleException(log, message, e);
        }
    }


    /**
     * Get the Synapse configuration for a Message processor
     *
     * @param name name of the message processor
     * @return XML String that contain the configuration
     * @throws AxisFault
     */
    public String getMessageProcessor(String name) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();

        assert configuration != null;
        MessageProcessor processor = configuration.getMessageProcessors().get(name);
        String xml = null;
        if (processor != null) {
            xml = MessageProcessorSerializer.serializeMessageProcessor(null, processor).toString();
        } else {
            handleException(log, "Message Processor " + name + " does not exist", null);
        }

        return xml;

    }


    /**
     * Delete the MessageProcessor instance with given name in the synapse configuration
     *
     * @param name of the MessageProcessor  to be deleted
     * @throws AxisFault if Message processor does not exist
     */
    public void deleteMessageProcessor(String name) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();

        assert configuration != null;
        if (configuration.getMessageProcessors().containsKey(name)) {
            MessageProcessor processor = configuration.removeMessageProcessor(name);
            String fileName = processor.getFileName();
            if (processor != null) {
                processor.destroy();
            }

            if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                MediationPersistenceManager pm = getMediationPersistenceManager();
                pm.deleteItem(processor.getName(),
                        fileName, ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
            }

        } else {
            handleException(log, "Message Store " + name + " does not exist", null);
        }
    }

    /**
     * Get all the Current Message processor names defined in the configuration
     *
     * @return array of Strings that contains MessageStore names
     * @throws AxisFault
     */
    public String[] getMessageProcessorNames() throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();

        assert configuration != null;
        Collection<String> names = configuration.getMessageProcessors().keySet();
        return names.toArray(new String[names.size()]);
    }

    /**
     * Get All the Messages Stored in the Message Store associated with the Processor
     *
     * @param processorName ScheduledMessageForwarding Processor Name
     * @return Array of Message ids.
     * @throws AxisFault
     */
    public String[] getMessageIds(String processorName) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        String[] messageIds = null;
        try {

            assert configuration != null;
            if (configuration.getMessageProcessors().containsKey(processorName)) {
                MessageProcessor processor =
                        configuration.getMessageProcessors().get(processorName);

                if (processor instanceof ScheduledMessageProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (!view.isActive()) {
                        List<String> msgList = view.messageIdList();
                        messageIds = msgList.toArray(new String[msgList.size()]);

                    } else {
                        log.warn("Can't access Scheduled Message Forwarding Processor - Processor is active");
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error While accessing MessageProcessor view ");
            throw new AxisFault(e.getMessage());
        }

        return messageIds;
    }

    /**
     * Get all the Current Message processor data defined in the configuration
     *
     * @return Array of  MessageProcessorMetaDatas.
     * @throws AxisFault
     */
    public MessageProcessorMetaData[] getMessageProcessorDataList() throws AxisFault {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration configuration = getSynapseConfiguration();
            Collection<String> names = configuration.getMessageProcessors().keySet();

            List<MessageProcessorMetaData> messageProcessorDataList = new ArrayList<MessageProcessorMetaData>();
            if (names != null && !names.isEmpty()) {
                for (String name : names) {
                    MessageProcessor messageProcessor = configuration.getMessageProcessors().get(name);
                    MessageProcessorMetaData data = new MessageProcessorMetaData();
                    data.setName(name);
                    if (messageProcessor.getArtifactContainerName() != null) {
                        data.setArtifactContainerName(messageProcessor.getArtifactContainerName());
                    }
                    if (messageProcessor.isEdited()) {
                        data.setIsEdited(true);
                    }
                    messageProcessorDataList.add(data);
                }
            }
            return messageProcessorDataList.toArray(new MessageProcessorMetaData[messageProcessorDataList.size()]);

        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the SOAP Envelope of the Requested message from the associated Store
     *
     * @param processorName Message Processor Name
     * @param messageId     Message id of the Message
     * @return
     * @throws AxisFault
     */
    public String getEnvelope(String processorName, String messageId) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        String message = null;
        try {

            assert configuration != null;
            if (configuration.getMessageProcessors().containsKey(processorName)) {
                MessageProcessor processor =
                        configuration.getMessageProcessors().get(processorName);

                if (processor instanceof ScheduledMessageProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (!view.isActive()) {
                        message = view.getEnvelope(messageId);
                    } else {
                        log.warn("Can't access Scheduled Message Forwarding Processor - Processor is active");
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error While accessing MessageProcessor view ");
            throw new AxisFault(e.getMessage());
        }

        return message;
    }

    /**
     * Delete a Message with given Message id for the associated store
     *
     * @param processorName message processor name
     * @param messageId     message id of the message to be deleted
     * @throws AxisFault
     */
    public void deleteMessage(String processorName, String messageId) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        try {

            assert configuration != null;
            if (configuration.getMessageProcessors().containsKey(processorName)) {
                MessageProcessor processor =
                        configuration.getMessageProcessors().get(processorName);

                if (processor instanceof ScheduledMessageProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (!view.isActive()) {
                        view.delete(messageId);
                    } else {
                        log.warn("Can't access Scheduled Message Forwarding Processor - Processor is active");
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error While accessing MessageProcessor view ");
            throw new AxisFault(e.getMessage());
        }

    }

    /**
     * Delete first messages from the Message Store associated with the Processor
     *
     * @param processorName Message Processor Name
     * @throws AxisFault
     */
    public void deleteFirstMessages(String processorName) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        throw new AxisFault("Unsupported operation.");
    }

    /**
     * Delete all messages from the Message Store associated with the Processor
     *
     * @param processorName Message Processor Name
     * @throws AxisFault
     */
    public void deleteAllMessages(String processorName) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        try {
            assert configuration != null;
            if (configuration.getMessageProcessors().containsKey(processorName)) {
                MessageProcessor processor =
                        configuration.getMessageProcessors().get(processorName);

                if (processor instanceof ScheduledMessageProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (!view.isActive()) {
                        view.deleteAll();
                    } else {
                        log.warn("Can't access Scheduled Message Forwarding Processor - Processor is active");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error While accessing MessageProcessor view ");
            throw new AxisFault(e.getMessage());
        }

    }

    /**
     * Resend a Give Message
     *
     * @param processorName
     * @param messageId
     * @throws AxisFault
     */
    public void resend(String processorName, String messageId) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        try {

            assert configuration != null;
            if (configuration.getMessageProcessors().containsKey(processorName)) {
                MessageProcessor processor =
                        configuration.getMessageProcessors().get(processorName);

                if (processor instanceof ScheduledMessageProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (!view.isActive()) {
                        view.resend(messageId);
                    } else {
                        log.warn("Can't access Scheduled Message Forwarding Processor - Processor is active");
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error While accessing MessageProcessor view ");
            throw new AxisFault(e.getMessage());
        }

    }

    /**
     * Resend first Message
     *
     * @param processorName
     * @param messageId
     * @throws AxisFault
     */
    public void resendFirstMessage(String processorName, String messageId) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        throw new AxisFault("Unsupported operation.");
    }


    /**
     * messageID
     * Resend All messages
     *
     * @param processorName
     * @throws AxisFault
     */
    public void resendAll(String processorName) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        try {

            assert configuration != null;
            if (configuration.getMessageProcessors().containsKey(processorName)) {
                MessageProcessor processor =
                        configuration.getMessageProcessors().get(processorName);

                if (processor instanceof ScheduledMessageProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (!view.isActive()) {
                        view.resendAll();
                    } else {
                        log.warn("Can't access Scheduled Message Forwarding Processor - Processor is active");
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error While accessing MessageProcessor view ");
            throw new AxisFault(e.getMessage());
        }

    }

    /**
     * Get the Number of Messages in the message store associated with the processor
     *
     * @param processorName
     * @return
     */
    public int getSize(String processorName) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        int size = 0;
        try {

            assert configuration != null;
            if (configuration.getMessageProcessors().containsKey(processorName)) {
                MessageProcessor processor =
                        configuration.getMessageProcessors().get(processorName);

                if (processor instanceof ScheduledMessageProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (!view.isActive()) {
                        size = view.getSize();
                    } else {
                        log.warn("Can't access Scheduled Message Forwarding Processor - Processor is active");
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error While accessing MessageProcessor view ");
            throw new AxisFault(e.getMessage());
        }

        return size;
    }

    /**
     * Get the Active Status of the message processor
     *
     * @param processorName
     * @return
     */
    public boolean isActive(String processorName) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        boolean active = false;
        try {
            assert configuration != null;
            if (configuration.getMessageProcessors().containsKey(processorName)) {
                MessageProcessor processor =
                        configuration.getMessageProcessors().get(processorName);

                if (processor instanceof ScheduledMessageForwardingProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if(view != null){
                    	active = view.isActive();
                    }
                } else if (processor instanceof SamplingProcessor) {
                    SamplingProcessorView view =
                            ((SamplingProcessor) processor).getView();
                    if(view != null){
                    	active = view.isActive();
                    }
                } else if (processor instanceof FailoverScheduledMessageForwardingProcessor) {
                    FailoverMessageForwardingProcessorView view =
                            ((FailoverScheduledMessageForwardingProcessor) processor).getView();
                    if (view != null) {
                        active = view.isActive();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error While accessing MessageProcessor view ");
            throw new AxisFault(e.getMessage());
        }

        return active;
    }

    public void activate(String processorName) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();
        try {

            assert configuration != null;
            if (configuration.getMessageProcessors().containsKey(processorName)) {
                MessageProcessor processor =
                        configuration.getMessageProcessors().get(processorName);

                if (processor instanceof ScheduledMessageForwardingProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (!view.isActive()) {
                        view.activate();
                        if (processor.getArtifactContainerName() == null) {
                            if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                                getMediationPersistenceManager()
                                        .saveItem(processor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
                            }
                        }
                    } else {
                        log.warn("Scheduled Message Forwarding Processor is already active");
                    }
                } else if (processor instanceof FailoverScheduledMessageForwardingProcessor) {
                    FailoverMessageForwardingProcessorView view =
                            ((FailoverScheduledMessageForwardingProcessor) processor).getView();
                    if (!view.isActive()) {
                        view.activate();
                    } else {
                        log.warn("Scheduled Failover Message Forwarding Processor is already active");
                    }

                } else if (processor instanceof SamplingProcessor) {
                    SamplingProcessorView view =
                            ((SamplingProcessor) processor).getView();
                    if (!view.isActive()) {
                        view.activate();
                        if (processor.getArtifactContainerName() == null) {
                            if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                                getMediationPersistenceManager()
                                        .saveItem(processor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
                            }
                        }
                    } else {
                        log.warn("Sampling Processor is already active");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error While accessing MessageProcessor view ");
            throw new AxisFault(e.getMessage());
        }

    }


    public void deactivate(String processorName) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();

        try {

            assert configuration != null;
            if (configuration.getMessageProcessors().containsKey(processorName)) {
                MessageProcessor processor =
                        configuration.getMessageProcessors().get(processorName);

                if (processor instanceof ScheduledMessageForwardingProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (view.isActive()) {
                        view.deactivate();
                    } else {
                        log.warn("Scheduled Message Forwarding Processor - already deActive");
                    }
                } else if (processor instanceof FailoverScheduledMessageForwardingProcessor) {

                    FailoverMessageForwardingProcessorView view =
                            ((FailoverScheduledMessageForwardingProcessor) processor).getView();
                    if (view.isActive()) {
                        view.deactivate();
                    } else {
                        log.warn("Scheduled Failover Message Forwarding Processor - already deActive");
                    }

                } else if (processor instanceof SamplingProcessor) {
                    SamplingProcessorView view = ((SamplingProcessor) processor).getView();
                    if (view.isActive()) {
                        view.deactivate();
                        if (processor.getArtifactContainerName() == null) {
                            if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                                getMediationPersistenceManager()
                                        .saveItem(processor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
                            }
                        }
                    } else {
                        log.warn("Sampling Message Processor - already in the deactivated state");
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error While accessing MessageProcessor view ");
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * Get the implementation class name of the MessageProcessor
     *
     * @param name of the MessageProcessor
     * @return implementation class name of the Message Processor
     * @throws AxisFault
     */
    public String getClassName(String name) throws AxisFault {

        MessageProcessor processorImpl = getMessageProcessorImpl(name);

        if (processorImpl != null) {
            return processorImpl.getClass().getName();
        } else {
            handleException(log, "Message Processor " + name + " does not exist !!!", null);
        }

        //This code block will never reach as handleException method will always returns a Exception
        return null;
    }


    private void handleException(Log log, String message, Exception e)
            throws AxisFault {

        if (e == null) {

            AxisFault exception =
                    new AxisFault(message);
            log.error(message, exception);
            throw exception;

        } else {
            message = message + " :: " + e.getMessage();
            log.error(message, e);
            throw new AxisFault(message, e);
        }
    }


    private MessageProcessor getMessageProcessorImpl(String name) {
        SynapseConfiguration configuration = getSynapseConfiguration();
        assert configuration != null;
        return configuration.getMessageProcessors().get(name);
    }

    /**
     * Creates an <code>OMElement</code> from the given string
     *
     * @param str the XML string
     * @return the <code>OMElement</code> representation of the given string
     * @throws javax.xml.stream.XMLStreamException
     *          if building the <code>OmElement</code> is unsuccessful
     */
    private OMElement createElement(String str) throws XMLStreamException {
        InputStream in = new ByteArrayInputStream(str.getBytes());
        return new StAXOMBuilder(in).getDocumentElement();
    }

    public String[] getDefinedEndpoints() throws AxisFault {
        String[] endpoints = null;
        try {
            SynapseConfiguration configuration = getSynapseConfiguration();
            Collection<String> endpointsSet = configuration.getDefinedEndpoints().keySet();
            endpoints = endpointsSet.toArray(new String[endpointsSet.size()]);
        } catch (Exception e) {
            log.error("Error while getting the defined endpoints");
            handleException(log, "Error while getting the defined endpoints", e);
        }

        return endpoints;
    }
    
    /**
     * Checks whether given Axis2ClientRepo is valid one or not
     * @param input location of the Axis2 Client Repository
     * @return <code>true</code> if the given axis client repository valid, <code>false</code> otherwise.
     * @throws AxisFault If an ERROR is encountered or given repository location is invalid.
     */
    public boolean validateAxis2ClientRepo(String input) throws AxisFault {
        try {
            ConfigurationContextFactory.createConfigurationContextFromFileSystem(input,
                                                                                 DEFAULT_AXIS2_XML);
            return true;
        } catch (AxisFault e) {
            handleException(log, "Error while validating the Axis2 Client Repository", e);
            return false;
        }
    }


    /*
     * Get the poisonMessage passed from the synapse
     */

    public String getMessage(String processorName) throws Exception {
        SynapseConfiguration configuration = getSynapseConfiguration();
        MessageConsumer messageConsumer = getMessageConsumer(configuration,processorName);
        String msg = null;

        try {
            msg = configuration.getMessage(messageConsumer);
        } catch (Exception e) {
            log.error("MessageProcessorAdminService : Failed to get message" + e);
        }

        messageConsumer.cleanup(); //Removes the subscription after getting the message.
        return msg;
    }

    /*
     * Send request to Synapse to pop the poisonMessage
     */
    public void popMessage(String processorName) {

        SynapseConfiguration configuration = getSynapseConfiguration();
        MessageConsumer messageConsumer = getMessageConsumer(configuration,processorName);

        try {
             configuration.popMessage(messageConsumer);
        } catch (Exception e) {
           log.error("Failed to pop the message", e);
        }

        messageConsumer.cleanup();
    }

    public void redirectMessage(String processorName, String storeName){
        SynapseConfiguration configuration = getSynapseConfiguration();
        MessageConsumer messageConsumer = getMessageConsumer(configuration,processorName);
        MessageProducer messageProducer = configuration.getMessageStore(storeName).getProducer();

        try {
            configuration.redirectMessage(messageProducer, messageConsumer);
        } catch (Exception e) {
            log.error("Failed to pop the message",e);
        }

        messageConsumer.cleanup();

    }

    private MessageConsumer getMessageConsumer(SynapseConfiguration configuration, String processorName) {
        MessageProcessor processor = configuration.getMessageProcessors().get(processorName);
        final String messageStoreName = processor.getMessageStoreName();
        MessageConsumer messageConsumer = configuration.getMessageStore(messageStoreName).getConsumer();
        return messageConsumer;
    }

}
