/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.mediation.connector;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.connector.exceptions.AS4ErrorMapper;
import org.wso2.carbon.mediation.connector.exceptions.AS4Exception;
import org.wso2.carbon.mediation.connector.message.beans.MessageInfo;
import org.wso2.carbon.mediation.connector.message.beans.Messaging;
import org.wso2.carbon.mediation.connector.message.beans.SignalMessage;
import org.wso2.carbon.mediation.connector.message.beans.Error;
import org.wso2.carbon.mediation.connector.message.util.AS4Utils;
import org.wso2.carbon.mediation.connector.message.util.MessageIdGenerator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.util.Date;

/**
 * Handle AS4 Error messages
 */

public class AS4ErrorHandler {

    private static final Log log = LogFactory.getLog(AS4ErrorHandler.class);

    /**
     * Generate AS4 error messages and set to {@link MessageContext}
     * @param messageContext {@link MessageContext} object
     * @param e {@link AS4Exception} object
     */
    public static void generateErrorMessage(MessageContext messageContext, AS4Exception e) {

        SignalMessage signalMessage = new SignalMessage();

        MessageInfo responseMessageInfo = new MessageInfo();
        responseMessageInfo.setTimestamp(new Date());
        responseMessageInfo.setMessageId(MessageIdGenerator.createMessageId());

        signalMessage.setMessageInfo(responseMessageInfo);

        Error error = new Error();
        AS4ErrorMapper.setErrorDetailsAndDesc(e.getAs4ErrorCode(), error, e.getMessage());
        if (e.getMessageId() != null) {
            responseMessageInfo.setRefToMessageId(e.getMessageId());
            error.setRefToMessageInError(e.getMessageId());
        }
        signalMessage.setError(error);

        Messaging responseMessaging = new Messaging();
        responseMessaging.setMustUnderstand("true");
        responseMessaging.setSignalMessage(signalMessage);

        OMNode node = null;
        try {
            Marshaller messagingMarshaller = JAXBContext.newInstance(Messaging.class).createMarshaller();
            node = AS4Utils.getOMNode(messagingMarshaller, responseMessaging);
        } catch (Exception ex) {
            log.error("Error creating AS4 error message : " + ex.getMessage(), ex);
        }

        SOAPEnvelope soapEnvelope = OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope();
        soapEnvelope.addChild(OMAbstractFactory.getSOAP12Factory().createSOAPHeader());
        soapEnvelope.addChild(OMAbstractFactory.getSOAP12Factory().createSOAPBody());
        soapEnvelope.getHeader().addChild(node);

        try {
            messageContext.setEnvelope(soapEnvelope);
        } catch (AxisFault axisFault) {
            log.error("Error attaching AS4 error response to message context : " + axisFault.getMessage(), axisFault);
        }
    }
}
