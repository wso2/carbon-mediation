package org.wso2.carbon.inbound.endpoint.protocol.nats;

import io.nats.streaming.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class StreamingListener implements NatsMessageListener {

    private static final Log log = LogFactory.getLog(StreamingListener.class.getName());
    private String subject;
    private NatsInjectHandler injectHandler;
    private Properties natsProperties;
    private StreamingConnection connection;

    public StreamingListener(String subject, NatsInjectHandler injectHandler, Properties natsProperties) {
        this.subject = subject;
        this.injectHandler = injectHandler;
        this.natsProperties = natsProperties;
    }

    @Override public boolean createConnection() throws IOException, InterruptedException {
        if (connection == null) {
            String natsStreamingUrl = natsProperties.getProperty(NatsConstants.NATS_STREAMING_URL);
            String natsStreamingClientId = natsProperties.getProperty(NatsConstants.NATS_STREAMING_CLIENT_ID);
            String natsStreamingClusterId = natsProperties.getProperty(NatsConstants.NATS_STREAMING_CLUSTER_ID);
            String connectWait = natsProperties.getProperty(NatsConstants.NATS_STREAMING_CONNECT_WAIT);
            String discoverPrefix = natsProperties.getProperty(NatsConstants.NATS_STREAMING_DISCOVER_PREFIX);
            String maxPingsOut = natsProperties.getProperty(NatsConstants.NATS_STREAMING_MAX_PINGS_OUT);
            String pingInterval = natsProperties.getProperty(NatsConstants.NATS_STREAMING_PING_INTERVAL);
            String traceConnection = natsProperties.getProperty(NatsConstants.NATS_STREAMING_TRACE_CONNECTION);

            Options.Builder builder = new Options.Builder().natsUrl(StringUtils.isEmpty(natsStreamingUrl) ? NatsConstants.DEFAULT_NATS_STREAMING_URL : natsStreamingUrl)
                    .clientId(natsStreamingClientId)
                    .clusterId(StringUtils.isEmpty(natsStreamingClusterId) ? NatsConstants.DEFAULT_NATS_STREAMING_CLUSTER_ID : natsStreamingClusterId);

            if (Boolean.parseBoolean(natsProperties.getProperty(NatsConstants.USE_CORE_NATS_CONNECTION))) {
                builder.natsConn(new CoreListener(subject, injectHandler, natsProperties).getNatsConnection());
            }

            if (StringUtils.isNotEmpty(connectWait)) {
                builder.connectWait(Duration.ofSeconds(Integer.parseInt(connectWait)));
            }

            if (StringUtils.isNotEmpty(discoverPrefix)) {
                builder.discoverPrefix(discoverPrefix);
            }

            if (StringUtils.isNotEmpty(maxPingsOut)) {
                builder.maxPingsOut(Integer.parseInt(maxPingsOut));
            }

            if (StringUtils.isNotEmpty(pingInterval)) {
                builder.pingInterval(Duration.ofSeconds(Integer.parseInt(pingInterval)));
            }

            if (Boolean.parseBoolean(traceConnection)) {
                builder.traceConnection();
            }

            StreamingConnectionFactory connectionFactory = new StreamingConnectionFactory(builder.build());
            connection = connectionFactory.createConnection();
        }
        return true;
    }

    @Override public void consumeMessage(String sequenceName)
            throws InterruptedException, IOException, TimeoutException {
        CountDownLatch latch = new CountDownLatch(1);

        SubscriptionOptions.Builder subscriptionOptions = new SubscriptionOptions.Builder();

        String durableName = natsProperties.getProperty(NatsConstants.NATS_STREAMING_DURABLE_NAME);
        String queueGroup = natsProperties.getProperty(NatsConstants.NATS_STREAMING_QUEUE_GROUP);
        boolean isManualAck = Boolean.parseBoolean(natsProperties.getProperty(NatsConstants.NATS_STREAMING_MANUAL_ACK));
        String ackWait = natsProperties.getProperty(NatsConstants.NATS_STREAMING_ACK_WAIT);
        String maxInFlight = natsProperties.getProperty(NatsConstants.NATS_STREAMING_MAX_IN_FLIGHT);
        String subscriptionTimeout = natsProperties.getProperty(NatsConstants.NATS_STREAMING_SUBSCRIPTION_TIMEOUT);
        String dispatcher = natsProperties.getProperty(NatsConstants.NATS_STREAMING_DISPATCHER);

        if (StringUtils.isNotEmpty(durableName)) {
            subscriptionOptions.durableName(durableName);
        }

        if (isManualAck) {
            subscriptionOptions.manualAcks();
        }

        if (StringUtils.isNotEmpty(ackWait)) {
            subscriptionOptions.ackWait(Duration.ofSeconds(Integer.parseInt(ackWait)));
        }

        if (StringUtils.isNotEmpty(maxInFlight)) {
            subscriptionOptions.maxInFlight(Integer.parseInt(maxInFlight));
        }

        if (StringUtils.isNotEmpty(subscriptionTimeout)) {
            subscriptionOptions.subscriptionTimeout(Duration.ofSeconds(Integer.parseInt(subscriptionTimeout)));
        }

        if (StringUtils.isNotEmpty(dispatcher)) {
            subscriptionOptions.dispatcher(dispatcher);
        }

        if (StringUtils.isNotEmpty(queueGroup)) {
            connection.subscribe(subject, queueGroup, natsMessage -> {
                String message = new String(natsMessage.getData());
                log.info("Message Received to NATS Inbound EP: " + message);
                injectHandler.invoke(message.getBytes(), sequenceName, null, null);
                latch.countDown();
                manuallyAck(isManualAck, natsMessage);
            }, subscriptionOptions.build());
        } else {
            connection.subscribe(subject, natsMessage -> {
                String message = new String(natsMessage.getData());
                log.info("Message Received to NATS Inbound EP: " + message);
                injectHandler.invoke(message.getBytes(), sequenceName, null, null);
                latch.countDown();
                manuallyAck(isManualAck, natsMessage);
            }, subscriptionOptions.build());
        }
        latch.await();
    }

    private void manuallyAck(boolean isManualAck, Message natsMessage) {
        if (isManualAck) {
            try {
                natsMessage.ack();
            } catch (IOException e) {
                log.error("An error occurred while sending manual ack. ", e);
            }
        }
    }

    @Override public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            log.error("An error occurred while closing the connection. ", e);
        }
        connection = null;
    }

    /**
     * Check if debug is enabled for logging.
     *
     * @param text log text
     */
    private void printDebugLog(String text) {
        if (log.isDebugEnabled()) {
            log.debug(text);
        }
    }
}
