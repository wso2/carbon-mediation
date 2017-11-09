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

package org.wso2.carbon.mediation.connector.message;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.mediation.connector.AS4Constants;
import org.wso2.carbon.mediation.connector.message.beans.*;
import org.wso2.carbon.mediation.connector.message.util.AS4Utils;
import org.wso2.carbon.mediation.connector.message.util.MessageIdGenerator;
import org.wso2.carbon.mediation.connector.pmode.PModeRepository;
import org.wso2.carbon.mediation.connector.pmode.impl.PMode;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Date;

/**
 * This class represents AS4 Message to be sent by sending MSH
 */
public class AS4Message {

    private static Log log = LogFactory.getLog(AS4Message.class);

    private String pmodeAgreement;
    private PMode pmode;

    public AS4Message(String pmodeAgreement, PModeRepository pmodeRepository) {
        this.pmodeAgreement = pmodeAgreement;
        pmode = pmodeRepository.findPModeFromAgreement(pmodeAgreement);
    }

    /**
     * Get the {@link PMode} object
     * @return
     */
    public PMode getPmode() {
        return pmode;
    }

    /**
     * Generate SOAP Envelope to be sent as AS4 message
     * @param messageContext MessageContext object
     * @throws JAXBException
     * @throws IOException
     * @throws XMLStreamException
     */
    public void generateAS4Message(MessageContext messageContext) throws JAXBException, IOException, XMLStreamException {

        Messaging userMessage = generateUserMessage(messageContext);
        JAXBContext jaxbContext = JAXBContext.newInstance(Messaging.class);
        Marshaller messagingMarshaller = jaxbContext.createMarshaller();
        OMNode node = AS4Utils.getOMNode(messagingMarshaller, userMessage);
        messageContext.getEnvelope().getHeader().addChild(node);
        messageContext.setTo(new EndpointReference(pmode.getProtocol().getAddress()));

        if(((Axis2MessageContext) messageContext).getAxis2MessageContext().isDoingSwA()) {
            messageContext.setProperty(Constants.Configuration.ENABLE_SWA, true);
            ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty(Constants.Configuration.ENABLE_SWA, true);
        }
    }

    /**
     * Generate <Messaging></Messaging> element
     * @param messageContext MessageContext object
     * @return Messaging object with values set
     * @throws JAXBException
     * @throws IOException
     * @throws XMLStreamException
     */
    private Messaging generateUserMessage(MessageContext messageContext)
            throws JAXBException, IOException, XMLStreamException {

        Messaging messaging = new Messaging();
        messaging.setMustUnderstand("true");

        UserMessage userMessage = new UserMessage();

        MessageInfo messageInfo = new MessageInfo();
        Date processingStartTime = new Date();
        messageInfo.setTimestamp(processingStartTime);

        String messageId = MessageIdGenerator.createMessageId();
        messageInfo.setMessageId(messageId);
        userMessage.setMessageInfo(messageInfo);

        PartyInfo partyInfo = new PartyInfo();
        From from = new From();

        PartyId fromPartyId = new PartyId();
        fromPartyId.setValue(pmode.getInitiator().getParty());

        from.setPartyId(fromPartyId);
        from.setRole(pmode.getInitiator().getRole());

        To to = new To();
        PartyId toPartyId = new PartyId();
        toPartyId.setValue(pmode.getResponder().getParty());
        to.setPartyId(toPartyId);
        to.setRole(pmode.getResponder().getRole());

        partyInfo.setFrom(from);
        partyInfo.setTo(to);
        userMessage.setPartyInfo(partyInfo);

        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setAgreementRef(pmode.getAgreement().getName());
        collaborationInfo.setService(pmode.getBusinessInfo().getService());
        collaborationInfo.setAction(pmode.getBusinessInfo().getAction());
        collaborationInfo.setConversationId(MessageIdGenerator.generateConversationId());
        userMessage.setCollaborationInfo(collaborationInfo);

        PayloadInfo payloadInfo = generatePayloadInfo(messageContext);
        userMessage.setPayloadInfo(payloadInfo);

        messaging.setUserMessage(userMessage);
        return messaging;
    }

    /**
     * Generate <PayloadInfo></PayloadInfo>
     * @param messageContext MessageContext object
     * @return PayloadInfo object with values set
     */
    private PayloadInfo generatePayloadInfo(MessageContext messageContext) {

        PayloadInfo payloadInfo = new PayloadInfo();

        // Payload info using attachments
        Attachments attachments = ((Axis2MessageContext) messageContext).getAxis2MessageContext().getAttachmentMap();
        String[] contentIDs = attachments.getAllContentIDs();
        for (String contentID : contentIDs) {
            if(contentID.contains("rootpart")) {
                continue; // Ignore rootpart@ cid
            }
            PartInfo partInfo = new PartInfo();
            partInfo.setHref(MessageIdGenerator.generateHrefForAttachment(contentID));
            PartProperties partProperties = new PartProperties();

            Property mimeProperty = new Property();
            mimeProperty.setName(AS4Constants.MIME_TYPE);
            String contentTypeStr = ((Axis2MessageContext) messageContext).getAxis2MessageContext().getAttachment(contentID).getContentType();
            mimeProperty.setValue(contentTypeStr.substring(0, contentTypeStr.indexOf(';')));

            partProperties.addPartProperty(mimeProperty);
            partInfo.setPartPropertiesObj(partProperties);
            payloadInfo.addPartInfo(partInfo);
        }

        // Payload info using SOAP body content -> At most one payload inside soap body
        SOAPBody soapBody = messageContext.getEnvelope().getBody();
        OMAttribute attribute = soapBody.getAttribute(new QName(AS4Constants.WSU_NAMESPACE, AS4Constants.ATTRIBUTE_ID));
        if(attribute != null) {
            String id = attribute.getAttributeValue();
            PartInfo partInfoN = new PartInfo();
            partInfoN.setHref(MessageIdGenerator.generateHrefForSOAPBodyPayload(id));
            payloadInfo.addPartInfo(partInfoN);
        } 
        return payloadInfo;
    }
}
