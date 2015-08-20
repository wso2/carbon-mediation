/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.endpoint.ext.wsrm;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.TransportUtils;
import org.apache.cxf.continuations.Continuation;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.transport.passthru.Pipe;
import org.wso2.carbon.endpoint.ext.wsrm.utils.RMConstants;

import javax.xml.stream.XMLStreamException;

/**
 * Once the response is received through Synapse, this class sends it back to the client
 */
public class InboundRMResponseSender implements InboundResponseSender {

    private static final Logger logger = Logger.getLogger(InboundRMResponseSender.class);

    @Override
    public void sendBack(MessageContext messageContext) {

        Continuation continuation = (Continuation) messageContext.getProperty(RMConstants.CXF_CONTINUATION);
        Exchange exchange = (Exchange) messageContext.getProperty(RMConstants.CXF_EXCHANGE);

        Message message = exchange.getOutMessage();
        message.put(RMConstants.CXF_RM_SYNAPSE_MEDIATED, Boolean.TRUE);

        //Retrieve the SOAP envelope from the MessageContext
        SOAPEnvelope envelope = messageContext.getEnvelope();

        org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();

        if (envelope.getBody().getFirstElement() == null) {

            Pipe pipe = (Pipe) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                    getProperty(RMConstants.PASS_THROUGH_TARGET_BUFFER);
            try {
                envelope = TransportUtils.createSOAPMessage(axis2MsgCtx, pipe.getInputStream(),
                        messageContext.getProperty(Constants.Configuration.CONTENT_TYPE).toString());
                message.put(RMConstants.SOAP_ENVELOPE, envelope);
            } catch (XMLStreamException e) {
                String streamExceptionMsg = "Error while extracting the response soap message";
                logger.error(streamExceptionMsg, e);
                throw new SynapseException(streamExceptionMsg, e);
            } catch (AxisFault axisFault) {
                String axisFaultMsg = "Error occurred when extracting the SOAPEnvelope from the response";
                logger.error(axisFaultMsg, axisFault);
                throw new SynapseException(axisFaultMsg, axisFault);
            } finally {
                continuation.resume();
            }
        } else {
            message.put(RMConstants.SOAP_ENVELOPE, envelope);
            //Resume the CXF continuation
            continuation.resume();
        }
    }
}
