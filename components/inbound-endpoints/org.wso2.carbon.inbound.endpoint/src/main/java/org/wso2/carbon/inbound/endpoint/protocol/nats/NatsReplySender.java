package org.wso2.carbon.inbound.endpoint.protocol.nats;

import io.nats.client.Connection;
import org.apache.synapse.MessageContext;
import org.apache.synapse.inbound.InboundResponseSender;

public class NatsReplySender implements InboundResponseSender {

    String replyTo;
    Connection connection;

    public NatsReplySender(String replyTo, Connection connection) {
        this.replyTo = replyTo;
        this.connection = connection;
    }

    @Override public void sendBack(MessageContext messageContext) {
        connection.publish(replyTo, messageContext.getEnvelope().getBody().toString().getBytes());
    }
}
