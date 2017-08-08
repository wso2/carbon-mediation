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

package org.wso2.carbon.mediation.connector.message.util;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.connector.exceptions.AS4ErrorMapper;
import org.wso2.carbon.mediation.connector.exceptions.AS4Exception;
import org.wso2.carbon.mediation.connector.message.beans.Messaging;
import org.wso2.carbon.mediation.connector.pmode.PModeRepository;
import org.wso2.carbon.mediation.connector.pmode.impl.PMode;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class AS4Utils {

    private static final Log log = LogFactory.getLog(AS4Utils.class);

    /**
     * Create {@link OMNode} object from {@link Messaging} object
     * @param marshaller {@link Marshaller} instance for {@link Messaging} class
     * @param messaging {@link Messaging} object to be converted to {@link OMNode}
     * @return OMNode object created from {@link Messaging} object
     * @throws IOException
     * @throws XMLStreamException
     * @throws PropertyException
     */
    public static OMNode getOMNode(final Marshaller marshaller, final Messaging messaging)
            throws IOException, XMLStreamException, PropertyException {

        final PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream();
        in.connect(out);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        new Thread(new Runnable() {
            public void run() {
                try {
                    // write the original OutputStream to the PipedOutputStream
                    marshaller.marshal(messaging, out);
                } catch (JAXBException e) {
                    log.error(e);
                }
            }
        }).start();

        //Create a new builder with the StAX reader
        StAXOMBuilder builder = new StAXOMBuilder(in);
        OMNode node = builder.getDocumentElement();
        node.close(true);
        return node;
    }

    /**
     * Validate Messaging object to see whether it contains required fields
     * @param messaging {@link Messaging} object
     * @throws AS4Exception with error message and error code for failed validation
     */
    public static void validateMessaging(Messaging messaging, PModeRepository pmodeRepository) throws AS4Exception {

        if (messaging == null) {
            throw new AS4Exception("Messaging element not found", AS4ErrorMapper.ErrorCode.EBMS0001, null);
        }
        if (messaging.getUserMessage() == null) {
            throw new AS4Exception("UserMessage element not found", AS4ErrorMapper.ErrorCode.EBMS0001, null);
        }
        if (messaging.getUserMessage().getMessageInfo() == null) {
            throw new AS4Exception("MessageInfo element not found", AS4ErrorMapper.ErrorCode.EBMS0001, null);
        }

        String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
        if (messageId == null || messageId.isEmpty()) {
            throw new AS4Exception("MessageId element not found", AS4ErrorMapper.ErrorCode.EBMS0001, null);
        }
        if (messaging.getUserMessage().getCollaborationInfo() == null) {
            throw new AS4Exception("CollaborationInfo element not found", AS4ErrorMapper.ErrorCode.EBMS0001, messageId);
        }
        String agreementRef = messaging.getUserMessage().getCollaborationInfo().getAgreementRef();
        if (agreementRef == null || agreementRef.isEmpty()) {
            throw new AS4Exception("AgreementRef element not found", AS4ErrorMapper.ErrorCode.EBMS0001, messageId);
        }

        PMode pmode = pmodeRepository.findPModeFromAgreement(agreementRef);
        if (pmode == null) {
            throw new AS4Exception("No matching P-Mode found", AS4ErrorMapper.ErrorCode.EBMS0010, messageId);
        }
        if(!pmode.getInitiator().getParty().equals(messaging.getUserMessage().getPartyInfo().getFrom().getPartyId().getValue())) {
            throw new AS4Exception("No matching Initiator Party found in P-Mode", AS4ErrorMapper.ErrorCode.EBMS0010, messageId);
        }
        if(!pmode.getResponder().getParty().equals(messaging.getUserMessage().getPartyInfo().getTo().getPartyId().getValue())) {
            throw new AS4Exception("No matching Responder Party found in P-Mode", AS4ErrorMapper.ErrorCode.EBMS0010, messageId);
        }
    }

    /**
     * Determine if the error need to be notified to the producer end
     * @param pmode {@link PMode} object
     * @return true if <ProcessErrorNotifyProducer>true</ProcessErrorNotifyProducer>
     */
    public static boolean reportErrorToProducer(PMode pmode) {

        boolean notifyProducer = false;
        if(pmode.getErrorHandling() != null && pmode.getErrorHandling().getReport() != null) {
            notifyProducer = pmode.getErrorHandling().getReport().isProcessErrorNotifyProducer();
        }
        return notifyProducer;
    }

    /**
     * Determine if the error need to be sent as response to the sending MSH
     * @param pmode {@link PMode} object
     * @return true if <AsResponse>true</AsResponse>
     */
    public static boolean isSendErrorAsResponse(PMode pmode) {

        boolean notifyProducer = false;
        if(pmode.getErrorHandling() != null && pmode.getErrorHandling().getReport() != null) {
            notifyProducer = pmode.getErrorHandling().getReport().isAsResponse();
        }
        return notifyProducer;
    }
}
