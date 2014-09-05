package org.wso2.carbon.inbound.endpoint.inboundfactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.inbound.InboundResponseSenderFactory;
import org.wso2.carbon.inbound.endpoint.protocol.http.core.impl.InboundHttpSourceResponseWorker;

/**
 * Created by isurur on 7/30/14.
 */
public class InboundResponseSenderFactoryImpl implements InboundResponseSenderFactory {
    private static final Log log = LogFactory.getLog(InboundResponseSenderFactoryImpl.class);
    private static final Object obj = new Object();

    public static enum Protocols {http,http_cxf_ws_rm}
    @Override
    public InboundResponseSender getInboundResponseSender(String protocol) {
        synchronized (obj) {
            InboundResponseSender inboundResponseSender= null;
            if (protocol != null && Protocols.http.toString().equals(protocol)) {
                inboundResponseSender =  new InboundHttpSourceResponseWorker();
                ( (InboundHttpSourceResponseWorker)inboundResponseSender).setType(Protocols.http.toString());
            } else if (protocol != null && Protocols.http_cxf_ws_rm.toString().equals(protocol)) {
            } else {
                //log.error("response worker type should be specified.");
            }
            return inboundResponseSender;
        }
    }

}
