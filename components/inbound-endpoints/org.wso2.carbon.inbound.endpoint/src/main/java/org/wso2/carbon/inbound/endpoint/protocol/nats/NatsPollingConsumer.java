package org.wso2.carbon.inbound.endpoint.protocol.nats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Date;
import java.util.Properties;

public class NatsPollingConsumer {

    private static final Log log = LogFactory.getLog(NatsProcessor.class.getName());

    private NatsInjectHandler injectHandler;
    private Properties natsProperties;
    private NatsMessageListener messageListener;
    private String subject;
    private long scanInterval;
    private Long lastRanTime;
    private String name;

    NatsPollingConsumer(Properties natsProperties, long scanInterval, String name) {
        this.natsProperties = natsProperties;
        this.scanInterval = scanInterval;
        this.name = name;
        this.subject = natsProperties.getProperty(NatsConstants.SUBJECT);
    }

    void startMessageListener() {
        printDebugLog("Create the NATS message listener.");
        messageListener = new NatsMessageListener(subject, injectHandler, natsProperties);
    }

    public void execute() {
        try {
            printDebugLog("Executing : NATS Inbound EP : ");
            // Check if the cycles are running in correct interval and start scan
            long currentTime = (new Date()).getTime();
            if (lastRanTime == null || ((lastRanTime + (scanInterval)) <= currentTime)) {
                lastRanTime = currentTime;
                poll();
            } else if (log.isDebugEnabled()) {
                log.debug("Skip cycle since concurrent rate is higher than the scan interval : NATS Inbound EP ");
            }
            printDebugLog("End : NATS Inbound EP : ");
        } catch (Exception e) {
            log.error("Error while retrieving or injecting NATS message." + e.getMessage(), e);
        }
    }

    public Object poll() {
        if (messageListener.createConnection()) {
            if (injectHandler != null) {
                messageListener.consumeMessage(name);
            }
        }
        return null;
    }

    void registerHandler(NatsInjectHandler processingHandler) {
        injectHandler = processingHandler;
    }

    public Properties getInboundProperties() {
        return natsProperties;
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
