/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundProcessorParams;

public abstract class GenericEventBasedConsumer {

    public static final String PARAM_INBOUND_ENDPOINT_BEHAVIOR_EVENT_BASED = "eventBased";

    protected Properties properties;
    protected String name;
    protected SynapseEnvironment synapseEnvironment;
    protected String injectingSeq;
    protected String onErrorSeq;
    protected boolean coordination;
    protected boolean sequential;

    private static final Log log = LogFactory.getLog(GenericEventBasedConsumer.class);
    
    public GenericEventBasedConsumer(Properties properties, String name,
            SynapseEnvironment synapseEnvironment, String injectingSeq,
            String onErrorSeq, boolean coordination, boolean sequential) {
        this.properties = properties;
        this.name = name;
        this.synapseEnvironment = synapseEnvironment;
        this.injectingSeq = injectingSeq;
        this.onErrorSeq = onErrorSeq;
        this.coordination = coordination;
        this.sequential = sequential;
    }
    /**
     * 
     * This methods needs to be implemented when implementing the custom inbound
     * */
    public abstract void listen();

    /**
     * 
     * This methods needs to be implemented when terminating the inbound
     * */
    public abstract void destroy();    
    
    /**
     * States whether generic endpoint is a eventBased
     * Return true; if eventBased
     *
     * @param inboundParameters
     *            Inbound Parameters for endpoint
     * @return boolean
     */
    public static boolean isEventBasedInboundEndpoint(InboundProcessorParams inboundParameters) {
        return inboundParameters.getProperties()
                                .containsKey(GenericInboundListener.PARAM_INBOUND_ENDPOINT_BEHAVIOR) &&
               PARAM_INBOUND_ENDPOINT_BEHAVIOR_EVENT_BASED.equals(inboundParameters.getProperties()
                                .getProperty(GenericInboundListener.PARAM_INBOUND_ENDPOINT_BEHAVIOR));
    }
    
}
