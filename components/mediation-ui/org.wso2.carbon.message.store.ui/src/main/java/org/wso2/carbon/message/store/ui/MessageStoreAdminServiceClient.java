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
package org.wso2.carbon.message.store.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.message.store.stub.MessageInfo;
import org.wso2.carbon.message.store.stub.MessageStoreAdminServiceStub;
import org.wso2.carbon.message.store.stub.MessageStoreMetaData;
import org.wso2.carbon.message.store.ui.utils.MessageStoreData;
import org.wso2.carbon.ndatasource.ui.stub.NDataSourceAdminDataSourceException;
import org.wso2.carbon.ndatasource.ui.stub.NDataSourceAdminStub;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceInfo;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageStoreAdminServiceClient {

    private MessageStoreAdminServiceStub stub;
    private NDataSourceAdminStub nDataSourceAdminStub;

    private static final String adminServiceName = "MessageStoreAdminService";

    private static Log log = LogFactory.getLog(MessageStoreAdminServiceClient.class);


    public static int MESSAGE_STORES_PER_PAGE = 10;


    public MessageStoreAdminServiceClient(String cookie, String backendServerUrl,
                                          ConfigurationContext configurationContext) throws AxisFault {

        String serviceURL = backendServerUrl + adminServiceName;
        stub = new MessageStoreAdminServiceStub(configurationContext, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        // Get nDatasourceAdminStub
        nDataSourceAdminStub = new NDataSourceAdminStub(configurationContext, "local://services/NDataSourceAdmin");
    }

    /**
     * This method will add Message store to the synapse configuration
     *
     * @param xml
     * @throws Exception
     */
    public void addMessageStore(String xml) throws Exception {
        try {
            if (xml != null) {
                stub.addMessageStore(xml);
            } else {
                handleException("Error Can't add message store. Error in the configuration " + xml);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }


    public void modifyMessageStore(String xml) throws Exception {
        try {
            if (xml != null) {
                stub.modifyMessageStore(xml);
            } else {
                handleException("Error Can't change message store. Error in the configuration " + xml);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }


    public void deleteMessageStore(String name) throws Exception {
        try {
            if (name != null) {
                stub.deleteMessageStore(name);
            } else {
                handleException("Error Can't delete Message Store " + name);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String[] getMessageStoreNames() throws Exception {
        String[] messageStoreNames = null;
        try {
            messageStoreNames = stub.getMessageStoreNames();
        } catch (Exception e) {
            handleException(e);
        }

        return messageStoreNames;
    }

    public MessageStoreData[] getMessageStoreData() throws Exception {
        List<MessageStoreData> messageStores = new ArrayList<MessageStoreData>();
        try {
            MessageStoreMetaData[] temp = stub.getMessageStoreData();
            if (temp == null || temp.length == 0 || temp[0] == null) {
                return null;
            }
            for (MessageStoreMetaData info : temp) {
                MessageStoreData storeData = new MessageStoreData();
                storeData.setName(info.getName());
                storeData.setArtifactContainerName(info.getArtifactContainerName());
                storeData.setIsEdited(info.getIsEdited());
                messageStores.add(storeData);
            }
        } catch (Exception e) {
            handleException(e);
        }

        if (messageStores.size() > 0) {
            return messageStores.toArray(new MessageStoreData[messageStores.size()]);
        }
        return null;
    }

    public MessageStoreData[] getPaginatedMessageStoreData(int pageNumber) throws Exception {
        int numberOfPages = 0;
        MessageStoreData[] messageStoreNames = getMessageStoreData();
        if (messageStoreNames != null) {
            numberOfPages = (int) Math.ceil((double) messageStoreNames.length /
                    MESSAGE_STORES_PER_PAGE);
        }
        if (pageNumber == 0) {
            numberOfPages = 1;
        }
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }
        int startIndex = (pageNumber * MESSAGE_STORES_PER_PAGE);
        int endIndex = ((pageNumber + 1) * MESSAGE_STORES_PER_PAGE);

        if (messageStoreNames == null) {
            return null;
        }
        ArrayList<MessageStoreData> nameList = new ArrayList<MessageStoreData>();
        for (int i = startIndex; i < endIndex && i < messageStoreNames.length; i++) {
            nameList.add(messageStoreNames[i]);
        }

        return nameList.toArray(new MessageStoreData[nameList.size()]);
    }

    public String[] getPaginatedMessageStoreNames(int pageNumber) throws Exception {
        int numberOfPages = 0;
        String[] messageStoreNames = getMessageStoreNames();
        if(messageStoreNames != null) {
            numberOfPages = (int) Math.ceil((double) messageStoreNames.length /
                MESSAGE_STORES_PER_PAGE);
        }

        if(pageNumber == 0) {
            numberOfPages = 1;
        }

        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }

        int startIndex = (pageNumber * MESSAGE_STORES_PER_PAGE);
        int endIndex = ((pageNumber + 1) * MESSAGE_STORES_PER_PAGE);


        if(messageStoreNames == null) {
            return null;
        }
        ArrayList<String> nameList = new ArrayList<String>();
        for(int i = startIndex ; i < endIndex && i < messageStoreNames.length ; i++) {
            nameList.add(messageStoreNames[i]);
        }

        return nameList.toArray(new String[nameList.size()]);
    }

    public MessageInfo[] getPaginatedMessages(String name, int pageNumber) throws Exception {
        MessageInfo[] messageInfos = null;
        try {
            if (name != null) {
                messageInfos = stub.getPaginatedMessages(name, pageNumber);
            } else {
                handleException("Error Can't get messages form Message Store " + name);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return messageInfos;
    }

    public int getSize(String name) throws Exception {
        int size = 0;
        try {
            if (name != null) {
                size = stub.getSize(name);
            } else {
                handleException("Error Can't get Message Store size for Message store" + name);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return size;
    }

    public String getEnvelope(String name, String messageId) throws Exception {

        String envelope = null;

        try {
            if (name != null) {
                envelope = stub.getEnvelope(name, messageId);
            } else {
                handleException("Error Can't access Message store" + name);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return envelope;

    }

    public MessageStoreData getMessageStore(String name) throws Exception {
        MessageStoreData data = null;
        try {
            if (name != null) {
                String xml = stub.getMessageStore(name);
                assert xml != null;
                data = new MessageStoreData(xml);
            } else {
                handleException("Error Can't access Message store" + name);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return data;
    }

    public void deleteAllMessages(String name) throws Exception {

        try {
            if (name != null) {
                stub.deleteAllMessages(name);
            } else {
                handleException("Error Can't delete all messages from Message store" + name);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }


    public void deleteMessage(String name, String messageId) throws Exception {
        try {
            if (name != null) {
                stub.deleteMessage(name, messageId);
            } else {
                handleException("Error accessing Message store" + name);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void deleteFirstMessages(String name) throws Exception {
        try {
            if (name != null) {
                stub.deleteFirstMessages(name);
            } else {
                handleException("Error accessing Message store" + name);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String getClassName(String name) throws Exception {
        String className = null;
        try {
            if (name != null) {
                className = stub.getClassName(name);
            } else {
                handleException("Error accessing Message store" + name);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return className;
    }

    /**
     * Get carbon ndatasourcelist for jdbc message stores
     */
    public List<String> getAllDataSourceInformations() throws RemoteException,
                                                              NDataSourceAdminDataSourceException {

        WSDataSourceInfo wsDataSourceInfo[] = nDataSourceAdminStub.getAllDataSources();

        List<String> sourceList = new ArrayList<String>();
        if (wsDataSourceInfo == null || wsDataSourceInfo.length == 0) {
            return sourceList;
        }

        for(WSDataSourceInfo info : wsDataSourceInfo) {
            if (info.getDsMetaInfo().getJndiConfig() != null) {
                sourceList.add(info.getDsMetaInfo().getJndiConfig().getName());
            }
        }

        return sourceList;
    }

    public boolean isMBbased(String name) throws Exception{
        if ("org.apache.synapse.message.store.impl.jms.JmsStore".equals(getClassName(name))) {
            MessageStoreData data = getMessageStore(name);
            Map<String, String> paramsMap = data.getParams();
            // Only WSO2MB directly integrated stores must contain the following parameter
            if(paramsMap.containsKey("connectionfactory.QueueConnectionFactory")) {
                return true;
            }
        }
        return false;
    }

    private void handleException(Exception e) throws Exception {
        String message = "Error Executing MessageStoreAdminServiceClient" + e.getMessage();
        log.error(message, e);
        throw e;
    }

    private void handleException(String message) throws Exception {
        log.error(message);
        throw new Exception(message);
    }
}
