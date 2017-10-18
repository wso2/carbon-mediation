/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.connector.file;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.connector.AS4Constants;
import org.wso2.carbon.mediation.connector.exceptions.AS4ErrorMapper;
import org.wso2.carbon.mediation.connector.exceptions.AS4Exception;
import org.wso2.carbon.mediation.connector.message.beans.Messaging;
import org.wso2.carbon.mediation.connector.message.beans.PartInfo;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class AS4FileWriter {

    private static final Log log = LogFactory.getLog(AS4FileWriter.class);

    /**
     * Write the AS4 payloads to files
     * @param messaging Messaging object
     */
    public void saveAS4Payloads(Messaging messaging, org.apache.axis2.context.MessageContext axis2MsgContext,
                                String dataInFolder) throws AS4Exception {

        String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
        List<PartInfo> partInfoList = messaging.getUserMessage().getPayloadInfo().getPartInfo();
        if(partInfoList != null) {
            for (PartInfo partInfo : partInfoList) {
                // Write attachment
                if(partInfo.getHref().startsWith(AS4Constants.ATTACHMENT_HREF_PREFIX)) {
                    String cid = partInfo.getHref().substring(AS4Constants.ATTACHMENT_HREF_PREFIX.length());
                    DataHandler dataHandler = axis2MsgContext.getAttachment(cid);
                    File partFile = new File(dataInFolder + File.separator + cid);
                    try {
                        dataHandler.writeTo(new FileOutputStream(partFile));
                    } catch (IOException e) {
                        log.error("Error writing payload to file : " + cid, e);
                        throw new AS4Exception("Error writing payload to file : " + cid,
                                AS4ErrorMapper.ErrorCode.EBMS0004, messageId);
                    }
                // Write soap body payload
                } else if (partInfo.getHref().startsWith(AS4Constants.SOAP_BODY_HREF_PREFIX)) {
                    String cid = partInfo.getHref().substring(AS4Constants.SOAP_BODY_HREF_PREFIX.length());
                    OMElement payloadElement = axis2MsgContext.getEnvelope().getBody().getFirstElement();
                    if(payloadElement != null) {
                        try {
                            File partFile = new File(dataInFolder + File.separator + cid);
                            FileOutputStream outputStream = new FileOutputStream(partFile);
                            payloadElement.serialize(outputStream);
                        } catch (Exception e) {
                            log.error("Error writing payload to file : " + cid, e);
                            throw new AS4Exception("Error writing payload to file : " + cid,
                                    AS4ErrorMapper.ErrorCode.EBMS0004, messageId);
                        }
                    } else {
                        throw new AS4Exception("Payload not found in the soap body : " + cid,
                                AS4ErrorMapper.ErrorCode.EBMS0001, messageId);
                    }
                }
            }
        }
    }

    /**
     * Write the <eb3:Messaging></eb3:Messaging> to file
     * @param messageId messageId of the incoming message
     * @param messagingElement {@link OMElement} object containing <eb3:Messaging></eb3:Messaging> header
     * @param dataInFolder file save location
     * @throws AS4Exception
     */
    public void saveAS4Message(String messageId, OMElement messagingElement, String dataInFolder) throws AS4Exception {

        File directory = new File(dataInFolder);
        if(!directory.exists()) {
            directory.mkdir();
        }
        File userMessageFile = new File(dataInFolder + File.separator + messageId);
        try {
            FileOutputStream outputStream = new FileOutputStream(userMessageFile);
            messagingElement.serialize(outputStream);
        } catch (Exception e) {
            log.error(e);
            throw new AS4Exception("Error writing <eb3:Messaging> to file", AS4ErrorMapper.ErrorCode.EBMS0004, messageId);
        }
    }
}
