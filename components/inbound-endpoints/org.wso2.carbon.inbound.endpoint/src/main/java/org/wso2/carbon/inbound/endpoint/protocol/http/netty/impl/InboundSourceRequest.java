/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.http.netty.impl;


import io.netty.channel.ChannelHandlerContext;
import org.apache.synapse.core.SynapseEnvironment;

import java.util.HashMap;
import java.util.Map;

public class InboundSourceRequest {

    private Map<String, String> httpheaders = new HashMap<String, String>();

    private ChannelHandlerContext channelHandlerContext;
    private byte[] contentBytes;
    private byte[] trailingHeaders;
    private SynapseEnvironment synapseEnvironment;
    private String injectSeq;
    private String faultSeq;
    private String to;
    private String replyTo;




    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getInjectSeq() {
        return injectSeq;
    }

    public void setInjectSeq(String injectSeq) {
        this.injectSeq = injectSeq;
    }

    public String getFaultSeq() {
        return faultSeq;
    }

    public void setFaultSeq(String faultSeq) {
        this.faultSeq = faultSeq;
    }

    public SynapseEnvironment getSynapseEnvironment() {
        return synapseEnvironment;
    }

    public void setSynapseEnvironment(SynapseEnvironment synapseEnvironment) {
        this.synapseEnvironment = synapseEnvironment;
    }

    private Map<String, String> httptrailingHeaders = new HashMap<String, String>();

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public Map<String, String> getHttptrailingHeaders() {
        return httptrailingHeaders;
    }

    public void addHttpTrailingheaders(String key, String value) {
        this.httptrailingHeaders.put(key, value);
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    public byte[] getTrailingHeaders() {
        return trailingHeaders;
    }

    public void setTrailingHeaders(byte[] trailingHeaders) {
        this.trailingHeaders = trailingHeaders;
    }

    public byte[] getContentBytes() {
        return contentBytes;
    }

    public void setContentBytes(byte[] contentBytes) {
        this.contentBytes = contentBytes;
    }

    public Map<String, String> getHttpheaders() {
        return httpheaders;
    }

    public void addHttpheaders(String key, String value) {
        this.httpheaders.put(key, value);
    }


}
