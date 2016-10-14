package org.wso2.carbon.inbound.endpoint.protocol.http2.common;

/**
 * Created by chanakabalasooriya on 8/31/16.
 */
import org.apache.axis2.AxisFault;
import org.apache.synapse.MessageContext;

public interface SourceHandler {
    void sendResponse(MessageContext synCtx) throws AxisFault;
}
