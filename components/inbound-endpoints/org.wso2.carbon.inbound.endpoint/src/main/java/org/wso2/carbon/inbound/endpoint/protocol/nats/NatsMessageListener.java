package org.wso2.carbon.inbound.endpoint.protocol.nats;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface NatsMessageListener {
    boolean createConnection() throws IOException, InterruptedException;
    void consumeMessage(String sequenceName) throws InterruptedException, IOException, TimeoutException;
    void closeConnection();
}
