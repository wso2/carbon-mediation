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
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.mediators.template.TemplateContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * AS4 connector test cases
 */
public class AS4ConnectorTestCase {

    @Test(groups = {"wso2.esb"}, description = "as4 send test case")
    public void testAS4Send() throws Exception {

        SynapseConfiguration synapseConfiguration = new SynapseConfiguration();
        MessageContext messageContext = new Axis2MessageContext(new org.apache.axis2.context.MessageContext(),
                synapseConfiguration, new Axis2SynapseEnvironment(synapseConfiguration) {
        });

        Stack stack = new Stack();
        TemplateContext templateContext = new TemplateContext("context", null);
        Map mappedValues = new HashMap();
        mappedValues.put("pmode", "http://wso2.org/examples/agreement0");
        templateContext.setMappedValues(mappedValues);
        stack.push(templateContext);
        messageContext.setProperty("_SYNAPSE_FUNCTION_STACK", stack);

        SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope envelope = soapFactory.createSOAPEnvelope();
        envelope.addChild(soapFactory.createSOAPHeader());
        envelope.addChild(soapFactory.createSOAPBody());
        OMNamespace namespace = soapFactory.createOMNamespace(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu");
        OMAttribute omAttribute = soapFactory.createOMAttribute("Id", namespace, "1234");
        envelope.getBody().addAttribute(omAttribute);
        OMElement payload = SynapseConfigUtils.stringToOM(
                " <CrossIndustryInvoice xmlns=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:2\">\n" +
                     "content here\n" +
                     "</CrossIndustryInvoice>");
        envelope.getBody().addChild(payload);
        messageContext.setEnvelope(envelope);

        AS4Sender as4Sender = new AS4Sender();
        as4Sender.connect(messageContext);

        String localPart = messageContext.getEnvelope().getHeader().getFirstElement().getQName().getLocalPart();
        Assert.assertEquals(localPart, "Messaging", "<Messaging> soap header not found.");
    }

    @Test(groups = {"wso2.esb"}, description = "as4 receive test case")
    public void testAS4Receive() throws Exception {

        SynapseConfiguration synapseConfiguration = new SynapseConfiguration();
        MessageContext messageContext = new Axis2MessageContext(new org.apache.axis2.context.MessageContext(),
                synapseConfiguration, new Axis2SynapseEnvironment(synapseConfiguration) {
        });

        Stack stack = new Stack();
        TemplateContext templateContext = new TemplateContext("context", null);
        Map mappedValues = new HashMap();
        mappedValues.put("dataIn", "as4DataIn");
        templateContext.setMappedValues(mappedValues);
        stack.push(templateContext);
        messageContext.setProperty("_SYNAPSE_FUNCTION_STACK", stack);

        SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope envelope = soapFactory.createSOAPEnvelope();
        envelope.addChild(soapFactory.createSOAPHeader());
        envelope.addChild(soapFactory.createSOAPBody());

        String messagingHeader = "<eb3:Messaging xmlns:eb3=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/\" " +
                "xmlns:ns2=\"http://www.w3.org/2003/05/soap-envelope\" ns2:mustUnderstand=\"true\">\n" +
                "    <eb3:UserMessage>\n" +
                "        <eb3:MessageInfo>\n" +
                "            <eb3:Timestamp>2017-08-09T15:17:48.763+05:30</eb3:Timestamp>\n" +
                "            <eb3:MessageId>1a13d7ec-22f4-4bf0-9612-d31c08484524</eb3:MessageId>\n" +
                "        </eb3:MessageInfo>\n" +
                "        <eb3:PartyInfo>\n" +
                "            <eb3:From>\n" +
                "                <eb3:PartyId>org:wso2:example:company:A</eb3:PartyId>\n" +
                "                <eb3:Role>Sender</eb3:Role>\n" +
                "            </eb3:From>\n" +
                "            <eb3:To>\n" +
                "                <eb3:PartyId>org:wso2:example:company:B</eb3:PartyId>\n" +
                "                <eb3:Role>Receiver</eb3:Role>\n" +
                "            </eb3:To>\n" +
                "        </eb3:PartyInfo>\n" +
                "        <eb3:CollaborationInfo>\n" +
                "            <eb3:AgreementRef>http://wso2.org/examples/agreement0</eb3:AgreementRef>\n" +
                "            <eb3:Service>Examples</eb3:Service>\n" +
                "            <eb3:Action>StoreMessage</eb3:Action>\n" +
                "            <eb3:ConversationId>6961ba0c-6cb0-44c0-a084-a043c95c40f0</eb3:ConversationId>\n" +
                "        </eb3:CollaborationInfo>\n" +
                "        <eb3:PayloadInfo>\n" +
                "            <eb3:PartInfo href=\"#1234\" />\n" +
                "        </eb3:PayloadInfo>\n" +
                "    </eb3:UserMessage>\n" +
                "</eb3:Messaging>";

        OMElement messagingElement = SynapseConfigUtils.stringToOM(messagingHeader);
        envelope.getHeader().addChild(messagingElement);

        OMNamespace namespace = soapFactory.createOMNamespace(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu");
        OMAttribute omAttribute = soapFactory.createOMAttribute("Id", namespace, "1234");
        envelope.getBody().addAttribute(omAttribute);
        OMElement payload = SynapseConfigUtils.stringToOM(
                " <CrossIndustryInvoice xmlns=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:2\">\n" +
                        "content here\n" +
                        "</CrossIndustryInvoice>");
        envelope.getBody().addChild(payload);
        messageContext.setEnvelope(envelope);

        AS4Receiver as4Receiver = new AS4Receiver();
        as4Receiver.connect(messageContext);

        String localPart = messageContext.getEnvelope().getHeader().getFirstElement().getFirstElement().getQName().getLocalPart();
        Assert.assertEquals(localPart, "SignalMessage", "<SignalMessage> not found.");
    }
}
