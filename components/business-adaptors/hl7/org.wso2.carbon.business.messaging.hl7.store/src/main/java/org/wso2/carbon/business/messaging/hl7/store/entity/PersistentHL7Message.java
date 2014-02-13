package org.wso2.carbon.business.messaging.hl7.store.entity;

import org.apache.openjpa.persistence.jdbc.Index;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="PersistentHL7Store")
public class PersistentHL7Message {

    @Id
    @GeneratedValue(generator="uuid-hex")
    @Column(unique = true)
    private String id;

    @Basic
    @Index
    private String storeName;

    @Basic
    @Index
    private String messageId;

    @Basic
    @Index
    private String controlId;

    @Lob @Basic(fetch=FetchType.LAZY)
    private byte[] message;

    @Basic
    @Index
    private Date date = new Date();

    @Basic
    @Index
    private long timestamp = System.currentTimeMillis() / 1000L;


    public PersistentHL7Message(String storeName, String messageId, String controlId, byte[] message) {
        this.storeName = storeName;
        this.messageId = messageId;
        this.controlId = controlId;
        this.message = message;
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

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
