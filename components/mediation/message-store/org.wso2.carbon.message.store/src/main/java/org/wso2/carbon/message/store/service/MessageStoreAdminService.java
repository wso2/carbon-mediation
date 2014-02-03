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
package org.wso2.carbon.message.store.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MessageStoreFactory;
import org.apache.synapse.config.xml.MessageStoreSerializer;
import org.apache.synapse.message.store.MessageStore;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@SuppressWarnings({"UnusedDeclaration"})
public class MessageStoreAdminService extends AbstractServiceBusAdmin {

    private static Log log = LogFactory.getLog(MessageStoreAdminService.class);


    public static final int MSGS_PER_PAGE = 10;

    /**
     * Get an XML configuration element for a message store from the FE and creates and add
     * the MessageStore to the synapse configuration.
     *
     * @param xml string that contain the message store configuration.
     * @throws AxisFault if some thing goes wrong when creating
     *                   a MessageStore with the given xml.
     */
    public void addMessageStore(String xml) throws AxisFault {
        try {
            OMElement msElem = createElement(xml);
            MessageStore messageStore =
                    MessageStoreFactory.createMessageStore(msElem, new Properties());
            if (messageStore != null && messageStore.getName() != null) {
                // Here we must Init the message store and set a file name and then
                // save it to the configuration.
                // Then We must persist the created Message Store
                SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
                String fileName = ServiceBusUtils.generateFileName(messageStore.getName());
                messageStore.setFileName(fileName);
                messageStore.init(getSynapseEnvironment());
                synapseConfiguration.addMessageStore(messageStore.getName(), messageStore);
                MediationPersistenceManager mp = getMediationPersistenceManager();
                mp.saveItem(messageStore.getName(), ServiceBusConstants.ITEM_TYPE_MESSAGE_STORE);
            } else {
                String message = "Unable to create Message Store ";
                handleException(log, message, null);
            }

        } catch (XMLStreamException e) {
            String message = "Unable to create Message Store ";
            handleException(log, message, e);
        }

    }

    /**
     * Modify and Existing Message store based on the given XML that is passed from the FE
     * This this case user may change the message store implementation/ change parameters
     * So we add and init new message store and then remove and destroy old.
     * @param xml XML configuration for the changed Message store
     * @throws AxisFault if Some thing goes wrong when modifying the
     *                   Message store
     */
    public void modifyMessageStore(String xml) throws AxisFault {
        try {
            OMElement msElem = createElement(xml);
            MessageStore messageStore =
                    MessageStoreFactory.createMessageStore(msElem, new Properties());

            if(messageStore == null) {
                String message = "Unable to edit the message Store. Error in the configuration";
                handleException(log,message,null);
            }


            SynapseConfiguration configuration = getSynapseConfiguration();
            MessageStore oldMessageStore = configuration.getMessageStore(messageStore.getName());
            if(oldMessageStore != null) {
                // this means there is an existing message store

                //1st we clean up the old
                configuration.removeMessageStore(oldMessageStore.getName());
                oldMessageStore.destroy();

                // then we startup the new.
                String fileName = oldMessageStore.getFileName();
                messageStore.setFileName(fileName);
                messageStore.init(getSynapseEnvironment());
                configuration.addMessageStore(messageStore.getName(),messageStore);
                MediationPersistenceManager mp = getMediationPersistenceManager();
                mp.saveItem(messageStore.getName(),ServiceBusConstants.ITEM_TYPE_MESSAGE_STORE);
            } else {
                assert false;
                String message = "Unexpected Error!!! Message store with name "
                        + messageStore.getName() + " does not exist";
                handleException(log,message,null);
            }

        } catch (XMLStreamException e) {
            String message = "Unable to Modify Message Store ";
            handleException(log, message, e);
        }
    }


    public String getMessageStore(String name) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();

        assert configuration != null;
        MessageStore store = configuration.getMessageStore(name);
        if (store != null) {

        } else {
            handleException(log, "Message Store " + name + " does not exist", null);
        }

        return MessageStoreSerializer.serializeMessageStore(null, store).toString();

    }


    /**
     * Delete the MessageStore instance with given name in the synapse configuration
     *
     * @param name of the MessageStore to be deleted
     * @throws AxisFault if Message store does not exist
     */
    public void deleteMessageStore(String name) throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();

        assert configuration != null;
        if (configuration.getMessageStore(name) != null) {
            MessageStore removedMessageStore = configuration.getMessageStore(name);
            configuration.removeMessageStore(name);
            String fileName = removedMessageStore.getFileName();
            removedMessageStore.destroy();

            MediationPersistenceManager pm = getMediationPersistenceManager();
            pm.deleteItem(removedMessageStore.getName(),
                    fileName,ServiceBusConstants.ITEM_TYPE_MESSAGE_STORE);

        } else {
            handleException(log, "Message Store " + name + " does not exist", null);
        }
    }

    /**
     * Get all the Current Message store names defined in the configuration
     *
     * @return array of Strings that contains MessageStore names
     * @throws AxisFault
     */
    public String[] getMessageStoreNames() throws AxisFault {
        SynapseConfiguration configuration = getSynapseConfiguration();

        assert configuration != null;
        Collection<String> names = configuration.getMessageStores().keySet();
        return names.toArray(new String[names.size()]);
    }


    /**
     * Get the number of messages in the Message store with given name
     *
     * @param name of the MessageStore
     * @return number of message stores in the given store
     * @throws AxisFault if Message store does not exists
     */
    public int getSize(String name) throws AxisFault {

        MessageStore store = getMessageStoreImpl(name);
        if (store != null) {
            return store.size();
        } else {
            handleException(log, "Message Store " + name + " does not exist !!!", null);
        }

        //This code block will never reach as handleException method will always returns a Exception
        return 0;
    }

    /**
     * Get the implementation class name of the MessageStore
     *
     * @param name of the MessageStore
     * @return implementation class name of the Message Store
     * @throws AxisFault
     */
    public String getClassName(String name) throws AxisFault {

        MessageStore store = getMessageStoreImpl(name);

        if (store != null) {
            return store.getClass().getName();
        } else {
            handleException(log, "Message Store " + name + " does not exist !!!", null);
        }

        //This code block will never reach as handleException method will always returns a Exception
        return null;
    }

    /**
     * Message information of All Messages in MessageStore
     *
     * @param name of the MessageStore
     * @return Array of details of the All Messages in store
     * @throws AxisFault
     */
    public MessageInfo[] getAllMessages(String name) throws AxisFault {

        MessageStore store = getMessageStoreImpl(name);
        if (store != null) {

            List<MessageContext> messageContexts = store.getAll();
            List<MessageInfo> messageInfoList = new ArrayList<MessageInfo>();

            for (MessageContext mc : messageContexts) {
                MessageInfo info = createMessageInfo(mc);
                if (info != null) {
                    messageInfoList.add(info);
                }
            }
        } else {
            handleException(log, "Message Store " + name + " does not exist !!!", null);
        }

        //This code block will never execute as handleException will always throw an Exception
        return null;
    }

    /**
     * Get Array of Messages that are needed for a given Page number
     *
     * @param name       of the message store
     * @param pageNumber of to be displayed
     * @return Array of Message information for a given page
     * @throws AxisFault
     */
    public MessageInfo[] getPaginatedMessages(String name, int pageNumber)
            throws AxisFault {
        MessageStore store = getMessageStoreImpl(name);
        if (store != null) {
            int itemsPerPageInt = MSGS_PER_PAGE;
            int numberOfPages = (int) Math.ceil((double) store.size() / itemsPerPageInt);

            if(numberOfPages == 0) {
                numberOfPages = 1;
            }
            if (pageNumber > numberOfPages - 1) {
                pageNumber = numberOfPages - 1;
            }

            int startIndex = (pageNumber * itemsPerPageInt);
            int endIndex = ((pageNumber + 1) * itemsPerPageInt);

            List<MessageInfo> paginatedMsgList = new ArrayList<MessageInfo>();
            for (int i = startIndex; i < endIndex && i < store.size(); i++) {

                MessageInfo info = createMessageInfo(store.get(i));

                if (info != null) {
                    paginatedMsgList.add(info);
                }
            }

            MessageInfo[] returnMsgs =
                    paginatedMsgList.toArray(new MessageInfo[paginatedMsgList.size()]);
            return returnMsgs;
        } else {
            handleException(log, "Message Store " + name + " does not exist", null);
        }

        return new MessageInfo[0];
    }

    /**
     * Get the Content of a given message
     *
     * @param name      of the message store
     * @param messageId Message id of the Message
     * @return String that contain the content of the message
     * @throws AxisFault
     */
    public String getEnvelope(String name, String messageId)
            throws AxisFault {

        SynapseConfiguration configuration = getSynapseConfiguration();

        assert configuration != null;
        MessageStore store = configuration.getMessageStore(name);

        if (store != null) {
            MessageContext synCtx = store.get(messageId);
            if (synCtx != null) {
                return synCtx.getEnvelope().toString();
            } else {
                handleException(log, "Message with id " + messageId + " Does not exist", null);
            }
        } else {
            handleException(log, "Message Store " + name + " does not exist !!!", null);
        }

        //This code block will never execute as handleException will always throw an Exception
        return null;
    }

    public void deleteAllMessages(String name) throws AxisFault {

        MessageStore store = getMessageStoreImpl(name);

        if (store != null) {
            store.clear();
        } else {
            handleException(log, "Message Store " + name + " does not exist", null);
        }
    }

    public void deleteMessage(String name, String messageId)
            throws AxisFault {
        MessageStore store = getMessageStoreImpl(name);

        if (store != null) {
            MessageContext synCtx = store.remove(messageId);
            if (synCtx == null) {
                handleException(log, "Message with id " + messageId + " Does not exist", null);
            }
        } else {
            handleException(log, "Message Store " + name + " does not exist", null);
        }
    }

    public void deleteFirstMessages(String name) throws AxisFault {

        MessageStore store = getMessageStoreImpl(name);

        if (store != null) {
            store.remove();
        } else {
            handleException(log, "Message Store " + name + " does not exist", null);
        }
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

    private static MessageInfo createMessageInfo(MessageContext messageContext) {
        if (messageContext == null) {
            return null;
        }

        MessageInfo messageInfo = new MessageInfo();

        messageInfo.setMessageId(messageContext.getMessageID());
        messageInfo.setSoapXml(messageContext.getEnvelope().toString());
        return messageInfo;
    }


    private MessageStore getMessageStoreImpl(String name) {
        SynapseConfiguration configuration = getSynapseConfiguration();

        assert configuration != null;
        return configuration.getMessageStore(name);
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
		byte[] bytes = null;
		try {
			bytes = str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to extract bytes in UTF-8 encoding. "
					+ "Extracting bytes in the system default encoding"
					+ e.getMessage());
			bytes = str.getBytes();
		}

        InputStream in = new ByteArrayInputStream(bytes);
        return new StAXOMBuilder(in).getDocumentElement();
    }


}
