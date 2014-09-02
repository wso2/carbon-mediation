package org.wso2.carbon.inbound.endpoint.inboundfactory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;

import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.inbound.InboundRequestProcessorFactory;
import org.wso2.carbon.inbound.endpoint.protocol.file.VFSProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.http.core.impl.InboundHttpListner;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundConstants;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSProcessor;

import java.util.Properties;

public class InboundRequestProcessorFactoryImpl implements InboundRequestProcessorFactory {
    private static final Log log = LogFactory.getLog(InboundRequestProcessorFactoryImpl.class);
    private static final Object obj = new Object();

    public static enum Protocols {jms, file, http}

    ;

    @Override
    public InboundRequestProcessor createInboundProcessor(String protocol, String classImpl, String name,
                                                          Properties properties, String injectingSeq, String onErrorSeq,
                                                          SynapseEnvironment synapseEnvironment) {
        synchronized (obj) {
            InboundRequestProcessor inboundRequestProcessor = null;
            if (protocol != null && Protocols.jms.toString().equals(protocol)) {
                if (properties.getProperty(InboundConstants.INBOUND_ENDPOINT_INTERVAL) != null) {
                    long value = Long.parseLong(properties.getProperty(InboundConstants.INBOUND_ENDPOINT_INTERVAL));
                    inboundRequestProcessor =  new JMSProcessor(name, properties, value, injectingSeq,
                            onErrorSeq, synapseEnvironment);
                }
            } else if (protocol != null && Protocols.file.toString().equals(protocol)) {
                long value = Long.parseLong(properties.getProperty(InboundConstants.INBOUND_ENDPOINT_INTERVAL));
                inboundRequestProcessor =  new VFSProcessor(name, properties, value, injectingSeq,
                        onErrorSeq, synapseEnvironment);
            } else if (protocol != null && Protocols.http.toString().equals(protocol)) {
                String port = properties.getProperty(InboundConstants.INBOUND_ENDPOINT_PARAMETER_HTTP_PORT);
                inboundRequestProcessor = new InboundHttpListner(port, synapseEnvironment,
                        injectingSeq, onErrorSeq);
            } else if (classImpl != null) {
                long value = Long.parseLong(properties.getProperty(InboundConstants.INBOUND_ENDPOINT_INTERVAL));
                inboundRequestProcessor =  new GenericProcessor(name, classImpl, properties,
                        value, injectingSeq, onErrorSeq, synapseEnvironment);
            } else {
                log.error("Protocol or Class should be specified.");
            }
            return inboundRequestProcessor;
        }
    }

}
