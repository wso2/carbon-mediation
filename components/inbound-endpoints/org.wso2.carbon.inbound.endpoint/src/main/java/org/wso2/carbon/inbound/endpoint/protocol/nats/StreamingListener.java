package org.wso2.carbon.inbound.endpoint.protocol.nats;

import java.io.IOException;

public class StreamingListener implements NatsMessageListener {

    @Override public boolean createConnection() throws IOException, InterruptedException {
        return false;
    }

    @Override public void consumeMessage(String sequenceName) {
        throw new UnsupportedOperationException();
    }

    @Override public void closeConnection() {
        throw new UnsupportedOperationException();
    }
}
