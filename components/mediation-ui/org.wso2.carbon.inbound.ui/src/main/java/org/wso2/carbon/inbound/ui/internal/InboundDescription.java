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
	private String interval;
	private String sequential;
	private String coordination;
	private boolean suspend;
	private String injectingSeq;
	private String onErrorSeq;
	private Map<String, String> parameters;
	private String fileName;
	public static final String REGISTRY_KEY_PREFIX = "$registry:";
	private static final String INTERVAL_PARAM = "interval";
	private static final String SEQUENTIAL_PARAM = "sequential";
	private static final String COORDINATION_PARAM = "coordination";
	private static final String CLASS_TYPE = "class";
	private String artifactContainerName;
	private boolean isEdited;
	private boolean isStatisticsEnable;

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
		this.artifactContainerName = inboundEndpoint.getArtifactContainerName();
		this.isEdited = inboundEndpoint.getIsEdited();
		this.interval = "";
		if (inboundEndpoint.getParameters() != null) {
			for (ParameterDTO parameterDTO : inboundEndpoint.getParameters()) {
				if (parameterDTO.getKey() != null) {
					this.parameters.put(parameterDTO.getName(), REGISTRY_KEY_PREFIX + parameterDTO.getKey());
				} else {
					if (parameterDTO.getValue() == null) {
						this.parameters.put(parameterDTO.getName(), "");
					} else {
						if(getType().equals(CLASS_TYPE)) {
							if (INTERVAL_PARAM.equals(parameterDTO.getName())) {
								setInterval(("null".equals(parameterDTO.getValue())) ? "" : parameterDTO.getValue());
								continue;
							} else if (SEQUENTIAL_PARAM.equals(parameterDTO.getName())) {
								setSequential(("null".equals(parameterDTO.getValue())) ? "" : parameterDTO.getValue());
								continue;
							} else if (COORDINATION_PARAM.equals(parameterDTO.getName())) {
								setCoordination(("null".equals(parameterDTO.getValue())) ? "" : parameterDTO.getValue());
								continue;
							}
						}
						this.parameters.put(parameterDTO.getName(), parameterDTO.getValue());
					}
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

	public boolean getStatisticsEnable() {
		return isStatisticsEnable;
	}

	public void setStatisticsEnable(boolean isStatisticsEnable) {
		this.isStatisticsEnable = isStatisticsEnable;
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

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}	
	
	public String getSequential() {
        return sequential;
    }

    public void setSequential(String sequential) {
        this.sequential = sequential;
    }

    public String getCoordination() {
        return coordination;
    }

    public void setCoordination(String coordination) {
        this.coordination = coordination;
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

	public String getArtifactContainerName() {
		return artifactContainerName;
	}

	public void setArtifactContainerName(String artifactContainerName) {
		this.artifactContainerName = artifactContainerName;
	}

	public boolean getIsEdited() {
		return isEdited;
	}

	public void setIsEdited(boolean isEdited) {
		this.isEdited = isEdited;
	}
}
