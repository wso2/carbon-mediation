package org.wso2.carbon.business.messaging.hl7.store;

import org.apache.synapse.MessageContext;
import org.apache.synapse.message.MessageConsumer;

public class JDBCConsumer implements MessageConsumer {
    @Override
    public MessageContext receive() {
        return null;
    }

    @Override
    public boolean ack() {
        return false;
    }

    @Override
    public boolean cleanup() {
        return false;
    }

    @Override
    public void setId(int i) {

    }

    @Override
    public String getId() {
        return null;
    }
}
