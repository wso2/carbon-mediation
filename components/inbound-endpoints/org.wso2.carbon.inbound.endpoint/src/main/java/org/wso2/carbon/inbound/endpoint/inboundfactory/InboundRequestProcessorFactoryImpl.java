package org.wso2.carbon.inbound.endpoint.inboundfactory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.inbound.InboundRequestProcessorFactory;
import org.wso2.carbon.inbound.endpoint.protocol.file.VFSProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.jms.JMSProcessor;

public class InboundRequestProcessorFactoryImpl implements InboundRequestProcessorFactory {
    private static final Log log = LogFactory.getLog(InboundRequestProcessorFactoryImpl.class);
    private static final Object obj = new Object();

    public static enum Protocols {jms, file, http};

    @Override
    public InboundRequestProcessor createInboundProcessor(
            InboundProcessorParams params) {
        synchronized (obj) {
            String protocol = params.getProtocol();
            InboundRequestProcessor inboundRequestProcessor = null;
            if (protocol != null) {
                if (Protocols.jms.toString().equals(protocol)) {
                    inboundRequestProcessor = new JMSProcessor(params);
                } else if (Protocols.file.toString().equals(protocol)) {
                    inboundRequestProcessor = new VFSProcessor(params);
                } else if (Protocols.http.toString().equals(protocol)) {
                    //inboundRequestProcessor = new InboundHttpListener(params);
                }
            } else if (params.getClassImpl() != null) {
                inboundRequestProcessor = new GenericProcessor(params);
            } else {
                log.error("Protocol or Class should be specified.");
            }
            return inboundRequestProcessor;
        }
    }

}
