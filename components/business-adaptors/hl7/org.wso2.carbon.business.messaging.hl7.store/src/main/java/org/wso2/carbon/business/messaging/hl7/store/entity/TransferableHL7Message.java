package org.wso2.carbon.business.messaging.hl7.store.entity;


/**
 * Wrapper for PersistentHL7 Messages using primitive data types.
 */
public class TransferableHL7Message {

    private String id;
    private String storeName;
    private String messageId;
    private String controlId;
    private String rawMessage;
    private String envelope;
    private String date;
    private long timestamp;

    public TransferableHL7Message(String id, String storeName, String messageId, String controlId, String rawMessage, String envelope, String date, long timestamp) {
        this.id = id;
        this.storeName = storeName;
        this.messageId = messageId;
        this.controlId = controlId;
        this.rawMessage = rawMessage;
        this.envelope = envelope;
        this.date = date;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getControlId() {
        return controlId;
    }

    public void setControlId(String controlId) {
        this.controlId = controlId;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public String getEnvelope() {
        return envelope;
    }

    public void setEnvelope(String envelope) {
        this.envelope = envelope;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
