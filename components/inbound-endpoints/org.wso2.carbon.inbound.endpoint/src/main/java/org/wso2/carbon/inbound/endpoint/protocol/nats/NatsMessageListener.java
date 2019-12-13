package org.wso2.carbon.inbound.endpoint.protocol.nats;

import java.io.IOException;

public interface NatsMessageListener {
    boolean createConnection() throws IOException, InterruptedException;
    void consumeMessage(String sequenceName) throws InterruptedException;
    void closeConnection();
}
