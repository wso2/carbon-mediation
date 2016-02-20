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
package org.wso2.carbon.inbound;

import java.util.Map;

import org.apache.synapse.SynapseConstants;
import org.apache.synapse.inbound.InboundEndpoint;

public class InboundEndpointDTO {

    // Name of the inbound endpoint
    private String name;
    // protocol or class should be specified
    private String protocol;
    private String classImpl;
    private boolean isSuspend;
    // Sequences related to injection
    private String injectingSeq;
    private String onErrorSeq;
    private ParameterDTO[]parameters;
    private String fileName;
    private String artifactContainerName;
    private boolean isEdited;
    private boolean isStatisticsEnable;
    private boolean isTracingEnable;

    public InboundEndpointDTO(InboundEndpoint inboundEndpoint) {
        this.name = inboundEndpoint.getName();
        this.protocol = inboundEndpoint.getProtocol();
        this.classImpl = inboundEndpoint.getClassImpl();
        this.isSuspend = inboundEndpoint.isSuspend();
        this.injectingSeq = inboundEndpoint.getInjectingSeq();
        this.onErrorSeq = inboundEndpoint.getOnErrorSeq();
        this.fileName = inboundEndpoint.getFileName();
        this.artifactContainerName = inboundEndpoint.getArtifactContainerName();
        this.isEdited = inboundEndpoint.getIsEdited();
        isStatisticsEnable = ((inboundEndpoint.getAspectConfiguration() != null) &&
                              inboundEndpoint.getAspectConfiguration().isStatisticsEnable());
        isTracingEnable = ((inboundEndpoint.getAspectConfiguration() != null) &&
                           inboundEndpoint.getAspectConfiguration().isTracingEnabled());
        Map<String, String> mParams = inboundEndpoint.getParametersMap();        
        if (mParams != null && !mParams.isEmpty()) {
            parameters = new ParameterDTO[mParams.keySet().size()];
            int i = 0;
            for (String strKey : mParams.keySet()) {
                ParameterDTO parameterDTO = new ParameterDTO();
                parameterDTO.setName(strKey);              	 
              	 parameterDTO.setValue(mParams.get(strKey));
              	 parameterDTO.setKey(inboundEndpoint.getParameterKey(strKey));
                parameters[i++] = parameterDTO;
            }
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
   
    /**
     * @return the parameters
     */
    public ParameterDTO[] getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(ParameterDTO[]parameters) {
        this.parameters = parameters;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }

    public String getArtifactContainerName() {
        return artifactContainerName;
    }

    public void setArtifactContainerName(String artifactContainerName) {
        this.artifactContainerName = artifactContainerName;
    }

    public void setStatisticsEnable(boolean enableStatistics) {
        this.isStatisticsEnable = enableStatistics;
    }

    public boolean getStatisticsEnable() {
        return isStatisticsEnable;
    }

    public boolean getTracingEnable() {
        return isTracingEnable;
    }

    public void setTracingEnable(boolean tracingEnable) {
        isTracingEnable = tracingEnable;
    }
}
