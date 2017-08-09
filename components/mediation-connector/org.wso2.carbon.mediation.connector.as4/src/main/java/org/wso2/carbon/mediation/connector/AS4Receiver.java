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

package org.wso2.carbon.mediation.connector;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.mediation.connector.exceptions.AS4ErrorMapper;
import org.wso2.carbon.mediation.connector.exceptions.AS4Exception;
import org.wso2.carbon.mediation.connector.file.AS4FileWriter;
import org.wso2.carbon.mediation.connector.message.beans.*;
import org.wso2.carbon.mediation.connector.message.util.AS4Utils;
import org.wso2.carbon.mediation.connector.message.util.MessageIdGenerator;
import org.wso2.carbon.mediation.connector.pmode.PModeRepository;
import org.wso2.carbon.mediation.connector.pmode.impl.PMode;
import org.wso2.carbon.mediation.connector.message.beans.MessageInfo;
import org.wso2.carbon.mediation.connector.message.beans.Messaging;
import org.wso2.carbon.mediation.connector.message.beans.SignalMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Date;

/**
 *  AS4 Message receiver class
 */
public class AS4Receiver extends AbstractConnector {

    boolean isSendErrorAsResponse;

    public void connect(MessageContext messageContext) throws ConnectException {

        if(log.isDebugEnabled()) {
            log.debug("AS4 connector received message");
        }
        String dataIn = (String) getParameter(messageContext, "dataIn");
        try {
            Messaging messaging = extractMessagingHeader(messageContext);
            validateIncomingAS4Message(messaging);
            isSendErrorAsResponse = isSendErrorAsResponse(messaging);
            saveMessageAndPayloads(messaging, ((Axis2MessageContext) messageContext).getAxis2MessageContext(), dataIn);
            generateAS4SignalMessage(messageContext, messaging);
        } catch (AS4Exception e) {
            log.error(e);
            if(isSendErrorAsResponse) {
                AS4ErrorHandler.generateErrorMessage(messageContext, e);
            }
        } catch (Exception e) {
            throw new ConnectException(e);
        }
    }

    /**
     * Extract the <eb3:Messaging></eb3:Messaging> Header and create the Messaging object
     * @param messageContext {@link MessageContext} object
     * @return Messaging object
     */
    private Messaging extractMessagingHeader(MessageContext messageContext) throws AS4Exception {

        org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        try {
            RelayUtils.buildMessage(axis2MsgContext);
        } catch (Exception e) {
            throw new AS4Exception("Error building incoming message : " + e.getMessage(), AS4ErrorMapper.ErrorCode.EBMS0004, null);
        }

        OMElement messagingElement = axis2MsgContext.getEnvelope().getHeader().getFirstElement();
        if (messagingElement == null) {
            throw new AS4Exception("eb3:Messaging SOAP header not found", AS4ErrorMapper.ErrorCode.EBMS0001, null);
        }

        InputStream userMessageStream = new ByteArrayInputStream(messagingElement.toString().getBytes());
        Messaging messaging;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Messaging.class);
            Unmarshaller messagingUnMarshaller = jaxbContext.createUnmarshaller();
            messaging = (Messaging) messagingUnMarshaller.unmarshal(userMessageStream);
        } catch (JAXBException e) {
            throw new AS4Exception("Error building message model: " + e.getMessage(), AS4ErrorMapper.ErrorCode.EBMS0004, null);
        }
        return messaging;
    }

    /**
     * Validate the incoming AS4 Message
     * @param messaging Messaging object
     * @throws AS4Exception if validation fails
     */
    private void validateIncomingAS4Message(Messaging messaging) throws AS4Exception, AxisFault {
        PModeRepository pmodeRepository = PModeRepository.getInstance();
        AS4Utils.validateMessaging(messaging, pmodeRepository);
    }

    /**
     * Determine whether the errors need to be sent to the sending MSH as response AS4 message
     * @param messaging Messaging object
     * @return true if need to send error response, false otherwise
     * @throws AxisFault
     */
    private boolean isSendErrorAsResponse(Messaging messaging) throws AxisFault {

        String agreementRef = messaging.getUserMessage().getCollaborationInfo().getAgreementRef();
        PMode pmode = PModeRepository.getInstance().findPModeFromAgreement(agreementRef);
        return AS4Utils.isSendErrorAsResponse(pmode);
    }

    /**
     * Write the <eb3:Messaging></eb3:Messaging> and payloads to files
     * @param messaging Messaging object
     */
    private void saveMessageAndPayloads(Messaging messaging, org.apache.axis2.context.MessageContext axis2MsgContext,
                                        String dataInFolder) throws AS4Exception {

        AS4FileWriter as4FileWriter = new AS4FileWriter();
        String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
        OMElement messagingElement = axis2MsgContext.getEnvelope().getHeader().getFirstElement();
        as4FileWriter.saveAS4Message(messageId, messagingElement, dataInFolder);
        as4FileWriter.saveAS4Payloads(messaging, axis2MsgContext, dataInFolder);
    }

    /**
     * Generate <eb3:Messaging></eb3:Messaging> Signal message containing the receipt
     * @param messageContext {@link MessageContext} object
     * @param messaging Messaging object
     */
    private void generateAS4SignalMessage(MessageContext messageContext, Messaging messaging) throws IOException,
            XMLStreamException, JAXBException {

        String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();

        Messaging responseMessaging = new Messaging();
        responseMessaging.setMustUnderstand("true");
        SignalMessage signalMessage = new SignalMessage();

        MessageInfo responseMessageInfo = new MessageInfo();
        responseMessageInfo.setTimestamp(new Date());
        responseMessageInfo.setMessageId(MessageIdGenerator.createMessageId());
        responseMessageInfo.setRefToMessageId(messageId);
        signalMessage.setMessageInfo(responseMessageInfo);

        responseMessaging.setSignalMessage(signalMessage);

        JAXBContext jaxbContext = JAXBContext.newInstance(Messaging.class);
        Marshaller messagingMarshaller = jaxbContext.createMarshaller();
        OMNode node = AS4Utils.getOMNode(messagingMarshaller, responseMessaging);

        SOAPEnvelope soapEnvelope = OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope();
        soapEnvelope.addChild(OMAbstractFactory.getSOAP12Factory().createSOAPHeader());
        soapEnvelope.addChild(OMAbstractFactory.getSOAP12Factory().createSOAPBody());
        soapEnvelope.getHeader().addChild(node);

        messageContext.setEnvelope(soapEnvelope);
        messageContext.setTo(null);
    }
}
