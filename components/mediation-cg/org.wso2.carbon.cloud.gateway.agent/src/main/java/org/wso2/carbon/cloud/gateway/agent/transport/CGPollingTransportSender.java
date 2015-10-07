/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.gateway.agent.transport;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.wso2.carbon.cloud.gateway.common.CGConstant;
import org.wso2.carbon.cloud.gateway.common.thrift.gen.Message;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

/**
 * CSG Polling transport sender implementation
 */
public class CGPollingTransportSender extends AbstractTransportSender {

    @Override
    public void init(ConfigurationContext cfgCtx,
                     TransportOutDescription transportOut) throws AxisFault {
        super.init(cfgCtx, transportOut);
    }


    @Override
    public void sendMessage(MessageContext msgCtx,
                            String targetEPR,
                            OutTransportInfo outTransportInfo) throws AxisFault {
        // we could not use addressing information for correlation due to the following reasons
        // 1. There can be messages with no addressing information
        // 2. Since message content is not touch there is no way to read the message ID
        String relatesTo = (String) msgCtx.getOperationContext().getMessageContext(
                WSDL2Constants.MESSAGE_LABEL_IN).getProperty(CGConstant.CG_CORRELATION_KEY);
        if (log.isDebugEnabled()) {
            log.debug("A response was received without addressing information. " +
                    "Correlation key '" + relatesTo + "' calculated from the IN message context");
        }

        Message thriftMsg = new Message();
        thriftMsg.setMessageId(relatesTo);
        thriftMsg.setSoapAction(msgCtx.getSoapAction());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        MessageFormatter messageFormatter = MessageProcessorSelector.getMessageFormatter(msgCtx);
        if (messageFormatter == null) throw new AxisFault("No MessageFormatter in MessageContext");

        OMOutputFormat format = BaseUtils.getOMOutputFormat(msgCtx);
        thriftMsg.setContentType(messageFormatter.getContentType(msgCtx, format, msgCtx.getSoapAction()));


        try {
            if (msgCtx.isDoingREST()) {
                // result comes as the body of envelope
                MappedNamespaceConvention mnc = new MappedNamespaceConvention();
                XMLStreamWriter jsonWriter = new MappedXMLStreamWriter(mnc, new OutputStreamWriter(out, format.getCharSetEncoding()));
                jsonWriter.writeStartDocument();
                OMElement element = msgCtx.getEnvelope().getBody().getFirstElement();
                Iterator<OMElement> iterator = element.getChildElements();
                while (iterator.hasNext()) {
                    OMElement childElement = iterator.next();
                    childElement.serializeAndConsume(jsonWriter);
                }
                jsonWriter.writeEndDocument();
            } else {
                thriftMsg.setIsDoingMTOM(msgCtx.isDoingMTOM());
                thriftMsg.setIsDoingSwA(msgCtx.isDoingSwA());
                msgCtx.getEnvelope().serialize(out);
            }
        } catch (XMLStreamException e) {
            handleException("Cloud not serialize the request message", e);
        } catch (UnsupportedEncodingException e) {
            handleException("Cloud not serialize the request message", e);
        }
        thriftMsg.setMessage(out.toByteArray());

        CGPollingTransportBuffers buffer = (CGPollingTransportBuffers) msgCtx.getOperationContext().getMessageContext(
                WSDL2Constants.MESSAGE_LABEL_IN).getProperty(CGConstant.CG_POLLING_TRANSPORT_BUF_KEY);

        buffer.addResponseMessage(thriftMsg);
    }
}
