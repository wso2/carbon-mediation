package org.wso2.carbon.business.messaging.hl7.transport;

import org.apache.axis2.transport.OutTransportInfo;
import org.wso2.carbon.business.messaging.hl7.common.HL7ProcessingContext;

/**
 * Out Transport info to keep incoming message attributes
 * 
 */
public class HL7TransportOutInfo implements OutTransportInfo {

	private String contentType;
	private String messageControllerID;
	private HL7ProcessingContext processingContext;

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public String getMessageControllerID() {
		return messageControllerID;
	}

	public void setMessageControllerID(String messageControllerID) {
		this.messageControllerID = messageControllerID;
	}

	public HL7ProcessingContext getProcessingContext() {
		return processingContext;
	}

	public void setProcessingContext(HL7ProcessingContext processingContext) {
		this.processingContext = processingContext;
	}

}
