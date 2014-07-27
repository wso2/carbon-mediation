/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.inbound.endpoint.inboundfactory;


import org.apache.synapse.core.SynapseEnvironment;


import org.apache.synapse.inbound.InboundListner;
import org.apache.synapse.inbound.ListnerFactory;
import org.wso2.carbon.inbound.endpoint.protocol.http.netty.impl.InboundHttpListner;


public class InboundListnerFactory implements ListnerFactory {
    private static final Object obj = new Object();
    private static final Object objT = new Object();
    public static enum Protocols {jms, file,http,https};
    public InboundListner createInboundListner(String protocol,int port, SynapseEnvironment synapseEnvironment, String injectingSeq, String onErrorSeq,String outSequence) {
        synchronized (obj) {
            InboundListner inboundListner = null;
            if(Protocols.http.toString().equals(protocol)){
                inboundListner = new InboundHttpListner(port,synapseEnvironment,injectingSeq,onErrorSeq,outSequence);
            }else if(Protocols.https.toString().equals(protocol)){
                // pollingProcessor = new VFSProcessor(name, properties, scanInterval, injectingSeq, onErrorSeq, synapseEnvironment);
            }
            return inboundListner;
        }
    }

    public InboundListner createDefaultInboundListner(String protocol,String ports, SynapseEnvironment synapseEnvironment,  String injectSeq,  String onErrorSeq,  String outSeq) {
        synchronized (objT) {
            return new org.wso2.carbon.inbound.endpoint.protocol.http.core.impl.InboundHttpListner(ports, synapseEnvironment, injectSeq, onErrorSeq, outSeq);
        }
    }

}
