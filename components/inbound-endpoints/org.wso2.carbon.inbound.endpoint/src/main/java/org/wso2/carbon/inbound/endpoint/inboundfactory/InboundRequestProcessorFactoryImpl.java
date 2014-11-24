/**
* Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.inbound.endpoint.inboundfactory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.inbound.InboundRequestProcessorFactory;
import org.wso2.carbon.inbound.endpoint.protocol.file.VFSProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.http.core.impl.InboundHttpListener;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSProcessor;

public class InboundRequestProcessorFactoryImpl implements InboundRequestProcessorFactory {
    private static final Log log = LogFactory.getLog(InboundRequestProcessorFactoryImpl.class);

    public static enum Protocols {jms, file, http}

    /**
     * return underlying Request Processor Implementation according to protocol
     *
     * @param params <>parameters specific to transports</>
     * @return <>InboundRequestProcessor</>
     */
    @Override
    public InboundRequestProcessor createInboundProcessor(
            InboundProcessorParams params) {
        String protocol = params.getProtocol();
        InboundRequestProcessor inboundRequestProcessor = null;
        if (protocol != null) {
            if (Protocols.jms.toString().equals(protocol)) {
                inboundRequestProcessor = new JMSProcessor(params);
            } else if (Protocols.file.toString().equals(protocol)) {
                inboundRequestProcessor = new VFSProcessor(params);
            } else if (Protocols.http.toString().equals(protocol)) {
                inboundRequestProcessor = new InboundHttpListener(params);
            }
        } else if (params.getClassImpl() != null) {
            inboundRequestProcessor = new GenericProcessor(params);
        } else {
            throw new SynapseException("Protocol or Class should be specified for Inbound Endpoint " + params.getName());
        }
        return inboundRequestProcessor;
    }
}
