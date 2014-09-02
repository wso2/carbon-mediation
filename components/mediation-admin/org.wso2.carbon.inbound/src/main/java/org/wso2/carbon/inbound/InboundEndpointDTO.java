package org.wso2.carbon.inbound;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.synapse.inbound.InboundEndpoint;

public class InboundEndpointDTO {

    private String name;
    private String protocol;
    private String classImpl;
    private long interval;
    private boolean isSuspend;
    private String injectingSeq;
    private String onErrorSeq;
    private String [] parameters;
    private String fileName;




    public InboundEndpointDTO(InboundEndpoint inboundEndpoint){
		this.name = inboundEndpoint.getName();
		this.protocol = inboundEndpoint.getProtocol();
		this.classImpl = inboundEndpoint.getClassImpl();
		this.isSuspend = inboundEndpoint.isSuspend();
		this.injectingSeq = inboundEndpoint.getInjectingSeq();
		this.onErrorSeq = inboundEndpoint.getOnErrorSeq();
		this.fileName = inboundEndpoint.getFileName();
		Map<String, String>mParams = inboundEndpoint.getParametersMap();
		if(mParams != null && !mParams.isEmpty()){
			this.parameters = new String[mParams.size()];
			int i = 0;
			for(String strKey:mParams.keySet()){				
				this.parameters[i] = strKey + "~:~" + mParams.get(strKey);
				i++;
			}
		}else{
			this.parameters = new String[]{}; 
		}		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
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
		return isSuspend;
	}

	public void setSuspend(boolean isSuspend) {
		this.isSuspend = isSuspend;
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

	public String[] getParameters() {
		return parameters;
	}

	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


}
