/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.rest.api;

import org.apache.synapse.rest.RESTConstants;


public class ResourceData {
	
	private String[] methods = new String[4];
	
	private String contentType;
	
	private String userAgent;
	
	private int protocol = RESTConstants.PROTOCOL_HTTP_AND_HTTPS;
	
	private String inSequenceKey;
	
	private String outSequenceKey;
	
	private String faultSequenceKey;
	
	private String uriTemplate;
	
	private String urlMapping;

    private String inSeqXml;

    private String outSeqXml;

    private String faultSeqXml;

	public String[] getMethods() {
		return methods;
	}

	public void setMethods(String[] methods) {
		this.methods = methods;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public String getInSequenceKey() {
		return inSequenceKey;
	}

	public void setInSequenceKey(String inSequenceKey) {
		this.inSequenceKey = inSequenceKey;
	}

	public String getOutSequenceKey() {
		return outSequenceKey;
	}

	public void setOutSequenceKey(String outSequenceKey) {
		this.outSequenceKey = outSequenceKey;
	}

	public String getFaultSequenceKey() {
		return faultSequenceKey;
	}

	public void setFaultSequenceKey(String faultSequenceKey) {
		this.faultSequenceKey = faultSequenceKey;
	}
	
	public String getUriTemplate() {
		return uriTemplate;
	}

	public void setUriTemplate(String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}

	public String getUrlMapping() {
		return urlMapping;
	}

	public void setUrlMapping(String urlMapping) {
		this.urlMapping = urlMapping;
	}

    public String getInSeqXml() {
        return inSeqXml;
    }

    public void setInSeqXml(String inSeqXml) {
        this.inSeqXml = inSeqXml.trim().replaceAll("&gt", ">").replaceAll("&lt", "<")
                .replaceAll("\n", "").replaceAll("\t", " ");
    }

    public String getOutSeqXml() {
        return outSeqXml;
    }

    public void setOutSeqXml(String outSeqXml) {
        this.outSeqXml = outSeqXml.trim().replaceAll("&gt", ">").replaceAll("&lt", "<")
                .replaceAll("\n", "").replaceAll("\t", " ");
    }

    public String getFaultSeqXml() {
        return faultSeqXml;
    }

    public void setFaultSeqXml(String faultSeqXml) {
        this.faultSeqXml = faultSeqXml.trim().replaceAll("&gt", ">").replaceAll("&lt", "<")
                .replaceAll("\n", "").replaceAll("\t", " ");
    }
}
