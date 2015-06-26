/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.inbound.stub.types.carbon.InboundEndpointDTO;
import org.wso2.carbon.inbound.stub.types.carbon.ParameterDTO;

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
   public static final String REGISTRY_KEY_PREFIX = "key:";
    
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
		this.suspend = inboundEndpoint.getSuspend();
		this.injectingSeq = inboundEndpoint.getInjectingSeq();
		this.onErrorSeq = inboundEndpoint.getOnErrorSeq();
		this.fileName = inboundEndpoint.getFileName();
		this.parameters = new HashMap<String, String>();
		if(inboundEndpoint.getParameters() != null){
			for(ParameterDTO parameterDTO :inboundEndpoint.getParameters()){
				if(parameterDTO.getKey() != null){
					this.parameters.put(parameterDTO.getName(), REGISTRY_KEY_PREFIX + parameterDTO.getKey());
				}else	if(parameterDTO.getValue() != null){
					this.parameters.put(parameterDTO.getName(), parameterDTO.getValue());
				}else{
					this.parameters.put(parameterDTO.getName(), "");
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
