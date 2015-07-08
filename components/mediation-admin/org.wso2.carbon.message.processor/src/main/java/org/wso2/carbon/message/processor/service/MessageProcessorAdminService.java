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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MessageProcessorFactory;
import org.apache.synapse.config.xml.MessageProcessorSerializer;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.processor.impl.ScheduledMessageProcessor;
import org.apache.synapse.message.processor.impl.forwarder.MessageForwardingProcessorView;
import org.apache.synapse.message.processor.impl.forwarder.ScheduledMessageForwardingProcessor;
import org.apache.synapse.message.processor.impl.sampler.SamplingProcessor;
import org.apache.synapse.message.processor.impl.sampler.SamplingProcessorView;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.initializer.services.CAppArtifactDataService;
import org.wso2.carbon.message.processor.util.ConfigHolder;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

@SuppressWarnings({"UnusedDeclaration"})
public class MessageProcessorAdminService extends AbstractServiceBusAdmin {
    private static Log log = LogFactory.getLog(MessageProcessorAdminService.class);

    public static final int MSGS_PER_PAGE = 10;
    private static final String artifactType = ServiceBusConstants.MESSAGE_PROCESSOR_TYPE;

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
                MediationPersistenceManager mp = getMediationPersistenceManager();
                mp.saveItem(messageProcessor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
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
                CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                        getcAppArtifactDataService();
                String artifactName = getArtifactName(artifactType, messageProcessor.getName());
                if (cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), artifactName)) {
                    cAppArtifactDataService.setEdited(getTenantId(), artifactName);
                } else {
                    MediationPersistenceManager mp = getMediationPersistenceManager();
                    mp.saveItem(messageProcessor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
                }

            } else {
                String message = "Unable to Update Message Processor ";
                handleException(log, message, null);
            }

        } catch (XMLStreamException e) {
            String message = "Unable to Modify Message Processor ";
            handleException(log, message, e);
        } catch (Exception e) {
            handleException(log, "Unable to Modify Message Processor ", e);
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


            MediationPersistenceManager pm = getMediationPersistenceManager();
            pm.deleteItem(processor.getName(),
                    fileName, ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);

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
                CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                        getcAppArtifactDataService();
                for (String name : names) {
                    MessageProcessorMetaData data = new MessageProcessorMetaData();
                    data.setName(name);
                    if (cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, name))) {
                        data.setDeployedFromCApp(true);
                    }
                    if (cAppArtifactDataService.isArtifactEdited(getTenantId(), getArtifactName(artifactType, name))) {
                        data.setEdited(true);
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
                CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                        getcAppArtifactDataService();
                String artifactName = getArtifactName(artifactType, processorName);

                if (processor instanceof ScheduledMessageForwardingProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (!view.isActive()) {
                        view.activate();

                        if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), artifactName)) {
                            getMediationPersistenceManager()
                                    .saveItem(processor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
                        }

                    } else {
                        log.warn("Scheduled Message Forwarding Processor is already active");
                    }
                } else if (processor instanceof SamplingProcessor) {
                    SamplingProcessorView view =
                            ((SamplingProcessor) processor).getView();
                    if (!view.isActive()) {
                        view.activate();

                        if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), artifactName)) {
                            getMediationPersistenceManager()
                                    .saveItem(processor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
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
                CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                        getcAppArtifactDataService();
                String artifactName = getArtifactName(artifactType, processorName);

                if (processor instanceof ScheduledMessageForwardingProcessor) {
                    MessageForwardingProcessorView view =
                            ((ScheduledMessageForwardingProcessor) processor).getView();
                    if (view.isActive()) {
                        view.deactivate();

                        if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), artifactName)) {
                            getMediationPersistenceManager()
                                    .saveItem(processor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
                        }
                    } else {
                        log.warn("Scheduled Message Forwarding Processor - already deActive");
                    }
                } else if (processor instanceof SamplingProcessor) {
                    SamplingProcessorView view = ((SamplingProcessor) processor).getView();
                    if (view.isActive()) {
                        view.deactivate();

                        if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), artifactName)) {
                            getMediationPersistenceManager()
                                    .saveItem(processor.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR);
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

}
