package org.wso2.carbon.inbound.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.inbound.stub.types.carbon.InboundEndpointDTO;

public class InboundDescription {

	private String name;
	private String type;
	private String classImpl;
    private long interval;
    private boolean suspend;
    private String injectingSeq;
    private String onErrorSeq;
    private Map<String, String> parameters;
    private String fileName;
	public InboundDescription(InboundEndpointDTO inboundEndpoint){
		this.name = inboundEndpoint.getName();
		String protocol = inboundEndpoint.getProtocol();
		if(protocol != null && !protocol.trim().equals("")){
			this.type = protocol;
			this.classImpl = null;
		}else{
			this.type = InboundClientConstants.TYPE_CLASS;
			this.classImpl = inboundEndpoint.getClassImpl();	
		}		
		this.interval = inboundEndpoint.getInterval()/1000;
		this.suspend = inboundEndpoint.isSuspendSpecified();
		this.injectingSeq = inboundEndpoint.getInjectingSeq();
		this.onErrorSeq = inboundEndpoint.getOnErrorSeq();
		this.fileName = inboundEndpoint.getFileName();
		this.parameters = new HashMap<String, String>();
		if(inboundEndpoint.getParameters() != null){
			for(String strVal:inboundEndpoint.getParameters()){
				String[]arrVal = strVal.split("~:~");
				if(arrVal.length >= 2){
					this.parameters.put(arrVal[0], arrVal[1]);
				}else{
					this.parameters.put(arrVal[0], "");
				}			
			}
		}
	}

	public InboundDescription(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClassImpl() {
		return classImpl;
	}

	public void setClassImpl(String classImpl) {
		this.classImpl = classImpl;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public boolean isSuspend() {
		return suspend;
	}

	public void setSuspend(boolean suspend) {
		this.suspend = suspend;
	}

	public String getInjectingSeq() {
		return injectingSeq;
	}

	public void setInjectingSeq(String injectingSeq) {
		this.injectingSeq = injectingSeq;
	}

	public String getOnErrorSeq() {
		return onErrorSeq;
	}

	public void setOnErrorSeq(String onErrorSeq) {
		this.onErrorSeq = onErrorSeq;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
	
}
