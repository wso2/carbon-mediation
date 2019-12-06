package org.wso2.carbon.inbound.endpoint.protocol.nats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
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

    void initializeMessageListener() {
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
                poll(); // todo handle all Exceptions here
            } else {
                printDebugLog("Skip cycle since concurrent rate is higher than the scan interval : NATS Inbound EP ");
            }
            printDebugLog("End : NATS Inbound EP : ");
        } catch (IOException e) {
            log.error("An error occured while connecting to NATS server. " + e);
        } catch (InterruptedException e) {
            log.error("An error occurred while consuming the message. " + e);
            messageListener.closeConnection();
        } catch (Exception e) {
            log.error("Error while retrieving or injecting NATS message. " + e.getMessage(), e);
            messageListener.closeConnection();
        }
    }

    public Object poll() throws IOException, InterruptedException {
        if (messageListener.createConnection() && injectHandler != null) {
            messageListener.consumeMessage(name);
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
