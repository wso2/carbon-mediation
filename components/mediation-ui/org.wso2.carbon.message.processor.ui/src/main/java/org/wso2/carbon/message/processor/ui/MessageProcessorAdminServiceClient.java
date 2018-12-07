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
package org.wso2.carbon.message.processor.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.message.processor.MessageProcessor;
import org.wso2.carbon.message.processor.service.xsd.MessageProcessorMetaData;
import org.wso2.carbon.message.processor.stub.MessageProcessorAdminServiceStub;
import org.wso2.carbon.message.processor.ui.utils.MessageProcessorData;

import java.util.ArrayList;
import java.util.List;

public class MessageProcessorAdminServiceClient {

    private MessageProcessorAdminServiceStub stub;

    private static final String adminServiceName = "MessageProcessorAdminService";

    private static Log log = LogFactory.getLog(MessageProcessorAdminServiceClient.class);


    public static int MESSAGE_PROCESSORS_PER_PAGE = 10;


    public MessageProcessorAdminServiceClient(String cookie, String backendServerUrl,
                                              ConfigurationContext configurationContext) throws AxisFault {

        String serviceURL = backendServerUrl + adminServiceName;
        stub = new MessageProcessorAdminServiceStub(configurationContext, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * This method will add Message processor to the synapse configuration
     *
     * @param xml
     * @throws Exception
     */
    public void addMessageProcessor(String xml) throws Exception {
        try {
            if (xml != null) {
                stub.addMessageProcessor(xml);
            } else {
                handleException("Error Can't add message processor. Error in the configuration " + xml);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }


    public void modifyMessageProcessor(String xml) throws Exception {
        try {
            if (xml != null) {
                stub.modifyMessageProcessor(xml);
            } else {
                handleException("Error Can't change message processor. Error in the configuration " + xml);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }


    public void deleteMessageProcessor(String name) throws Exception {
        try {
            if (name != null) {
                stub.deleteMessageProcessor(name);
            } else {
                handleException("Error Can't delete Message Processor " + name);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String[] getMessageProcessorNames() throws Exception {
        String[] messageStoreNames = null;
        try {
            messageStoreNames = stub.getMessageProcessorNames();
        } catch (Exception e) {
            handleException(e);
        }

        return messageStoreNames;
    }

    public String[] getPaginatedMessageProcessorNames(int pageNumber) throws Exception {
        int numberOfPages = 0;

        String[] processorNames = getMessageProcessorNames();
        if(processorNames != null) {
            numberOfPages = (int) Math.ceil((double) processorNames.length /
                MESSAGE_PROCESSORS_PER_PAGE);
        }

        if(pageNumber == 0) {
            numberOfPages = 1;
        }

        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }

        int startIndex = (pageNumber * MESSAGE_PROCESSORS_PER_PAGE);
        int endIndex = ((pageNumber + 1) * MESSAGE_PROCESSORS_PER_PAGE);


        if(processorNames == null) {
            return null;
        }
        ArrayList<String> nameList = new ArrayList<String>();
        for(int i = startIndex ; i < endIndex && i < processorNames.length ; i++) {
            nameList.add(processorNames[i]);
        }

        return nameList.toArray(new String[nameList.size()]);
    }

    public MessageProcessorMetaData[] getPaginatedMessageProcessorData(int pageNumber) throws Exception {
        int numberOfPages = 0;

        MessageProcessorMetaData[] messageProcessorMetaDatas = getMessageProcessorMetaData();
        if (messageProcessorMetaDatas != null) {
            numberOfPages = (int) Math.ceil((double) messageProcessorMetaDatas.length /
                    MESSAGE_PROCESSORS_PER_PAGE);
        }
        if (pageNumber == 0) {
            numberOfPages = 1;
        }
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }

        int startIndex = (pageNumber * MESSAGE_PROCESSORS_PER_PAGE);
        int endIndex = ((pageNumber + 1) * MESSAGE_PROCESSORS_PER_PAGE);

        if (messageProcessorMetaDatas == null) {
            return null;
        }
        ArrayList<MessageProcessorMetaData> nameList = new ArrayList<MessageProcessorMetaData>();
        for (int i = startIndex; i < endIndex && i < messageProcessorMetaDatas.length; i++) {
            nameList.add(messageProcessorMetaDatas[i]);
        }

        return nameList.toArray(new MessageProcessorMetaData[nameList.size()]);
    }

    public MessageProcessorData getMessageProcessor(String name) throws Exception {
        MessageProcessorData data = null;
        try {
            if (name != null) {
                String xml = stub.getMessageProcessor(name);
                assert xml != null;
                data = new MessageProcessorData(xml);
            } else {
                handleException("Error Can't access Message processor" + name);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return data;
    }

    public String getClassName(String name) throws Exception {
        String className = null;
        try {
            if (name != null) {
                className = stub.getClassName(name);
            } else {
                handleException("Error accessing Message processor" + name);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return className;
    }

    public MessageProcessorMetaData[] getMessageProcessorMetaData() throws Exception {
        MessageProcessorMetaData[] messageProcessorMetaDatas = null;
        try {
            messageProcessorMetaDatas = stub.getMessageProcessorDataList();
        } catch (Exception e) {
            handleException(e);
        }

        return messageProcessorMetaDatas;
    }

    public String[] getMessageIds(String name) throws Exception {
        String[] messageIds = null;
        try {
            if(name != null) {
               messageIds = stub.getMessageIds(name);
            }
        } catch (Exception e) {
            handleException(e);
        }
        return messageIds;
    }


    public String getEnvelope(String name, String messageId) throws Exception {
        String message = null;
        try {
            if (name != null) {
                message = stub.getEnvelope(name, messageId);
            }
        } catch (Exception e) {
            handleException(e);
        }
        return message;
    }


    public void deleteAllMessages(String name) throws Exception {
        String message = null;
        try {
            if (name != null) {
                stub.deleteAllMessages(name);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }


    public void deleteMessage(String name , String messageId) throws Exception {
        try {
            if (name != null) {
                stub.deleteMessage(name,messageId);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void deleteFirstMessage(String name) throws Exception {
        try {
            if (name != null) {
                stub.deleteFirstMessages(name);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void resendAllMessages(String name) throws Exception {
         try {
            if (name != null) {
                stub.resendAll(name);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }


    public void resendMessage(String name , String messageId) throws Exception {
         try {
            if (name != null) {
                stub.resend(name, messageId);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void resendFirstMessage(String name , String messageId) throws Exception {
         try {
            if (name != null) {
                stub.resendFirstMessage(name, messageId);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }


    public String[] getPaginatedMessages(String name, int pageNumber) throws Exception {
        List<String> midList = new ArrayList<String>();
        try {
            if (name != null) {
                String[] messageIds = stub.getMessageIds(name);
                if(messageIds == null || messageIds.length == 0) {
                    return null;
                }
                int size = messageIds.length;

                if (size <= 0) {
                    return new String[0];
                }

                int numberOfPages = (int) Math.ceil((double) size / MESSAGE_PROCESSORS_PER_PAGE);

                if (numberOfPages == 0) {
                    numberOfPages = 1;
                }

                if (pageNumber > numberOfPages - 1) {
                    pageNumber = numberOfPages - 1;
                }

                int startIndex = (pageNumber * MESSAGE_PROCESSORS_PER_PAGE);
                int endIndex = ((pageNumber + 1) * MESSAGE_PROCESSORS_PER_PAGE);
                for (int i = startIndex; i < endIndex && i < size; i++) {
                    String id = messageIds[i];
                    if(id != null) {
                        midList.add(id);
                    }
                }

                return midList.toArray(new String[midList.size()]);
            }
        } catch (Exception e) {
            handleException(e);
        }
        return new String[0];
    }

    public String[] getDefinedEndpoints() throws Exception {
        try {
            return stub.getDefinedEndpoints();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public boolean isActive(String name) throws Exception {

        boolean active = false;
        try {
            if (name != null) {
                active = stub.isActive(name);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return active;
    }

    public void deactivate(String name) throws Exception {
        try {
            if (name != null) {
                stub.deactivate(name.trim());
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void activate(String name) throws Exception {
        try {
            if (name != null) {
                stub.activate(name.trim());
            }
        } catch (Exception e) {
            handleException(e);
        }
    }


    public int getSize(String name) throws Exception {
        int size = 0;
        try {
            if (name != null) {
                size = stub.getSize(name);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return size;
    }
    
    /**
     * Checks whether given Axis2ClientRepo is valid one or not
     * @param axis2ClientRepo input location of the Axis2 Client Repository given by the end user.
     * @return <code>true</code> if the given axis client repository valid, <code>false</code> otherwise.
     * @throws Exception If any error is encountered during the validation process.
     */
    public boolean validateAxis2ClientRepo(String axis2ClientRepo) throws Exception {
        try {
            if (axis2ClientRepo != null) {
                return stub.validateAxis2ClientRepo(axis2ClientRepo);
            }
        } catch (Exception e) {
            handleException(e);
        }
        return true;
    }

    private void handleException(Exception e) throws Exception {
        String message = "Error Executing MessageProcessorAdminServiceClient" + e.getMessage();
        log.error(message, e);
        throw e;
    }

    private void handleException(String message) throws Exception {
        log.error(message);
        throw new Exception(message);
    }

    public String getMessage(String processorName) throws Exception {
        String msg = null;
        try{
            if(processorName!=null) {
                msg = stub.getMessage(processorName);
            }
        } catch (Exception e) {
            handleException(e);
        }
        return msg;
    }

    public void popMessage(String processorName) throws Exception {
        try{
            if(processorName!=null) {
                stub.popMessage(processorName);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void redirectMessage(String processorName) throws Exception {
        try{
            if(processorName!=null) {
                stub.redirectMessage(processorName);
            }
        } catch (Exception e) {
            handleException(e);
        }

    }

}
