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
    public boolean isAlive() {
        return true;
    }


    /**
     * Set availability of connectivity with the message store
     *
     * @param isAlive connection availability.
     */
    @Override
    public void setAlive(boolean isAlive) {
    }

    @Override
    public void setId(int i) {

    }

    @Override
    public String getId() {
        return null;
    }
}
