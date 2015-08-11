/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint.protocol.generic;
import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;

public class GenericInboundListener implements InboundRequestProcessor {

    private static final Logger log = Logger.getLogger(GenericInboundListener.class);
    protected final String injectingSequence;
    protected final String onErrorSequence;
    protected final String name;
    protected final String host;
    protected int port;
    protected final InboundProcessorParams params;

    public GenericInboundListener(InboundProcessorParams inboundParams) {

        this.injectingSequence = inboundParams.getInjectingSeq();
        this.onErrorSequence = inboundParams.getOnErrorSeq();
        this.name = inboundParams.getName();
        this.params = inboundParams;
        this.host = inboundParams.getProperties().getProperty(GenericConstants.LISTENING_INBOUND_HOST);
        try {
            this.port = Integer.parseInt(inboundParams.getProperties().getProperty(GenericConstants.LISTENING_INBOUND_PORT));
        } catch (NumberFormatException e){
            handleException("Illegal port number specified", e);
        }
    }

    @Override
    public void init() {
        startListener();
    }

    public void close() {
        destroy();
    }

    @Override
    public void destroy() {
    }

    public boolean startListener() {
        log.info("GenericInboundListener started listening to port: " + port);

        return true;
    }

    protected void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
