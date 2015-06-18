/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.bam;

import org.apache.axis2.description.AxisService;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.mediator.bam.config.BamMediatorException;
import org.wso2.carbon.mediator.bam.config.stream.Property;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;
import org.wso2.carbon.mediator.bam.config.stream.StreamEntry;
import java.util.List;

/**
 * Extracts the current message payload/header data according to the given configuration.
 * Extracted information is sent as an event.
 */
public class BamMediator extends AbstractMediator {

    private static final String ADMIN_SERVICE_PARAMETER = "adminService";
    private static final String HIDDEN_SERVICE_PARAMETER = "hiddenService";

    private String serverProfile = "";
    private Stream stream = new Stream();
    private boolean isContentAware = false;

    public void setContentAwareness(StreamConfiguration streamConfiguration) {
        List<StreamEntry> streamEntryList = streamConfiguration.getEntries();
        for (StreamEntry streamEntry : streamEntryList) {
            if ("$SOAPBody".equals(streamEntry.getValue())) { // Check if Dump Message Body was selected in UI
                this.isContentAware = true;
                return;
            }
        }
        List<Property> properties = streamConfiguration.getProperties();
        for (Property property : properties) {
            if (property.isExpression() && property.getValue() != null) {
                this.isContentAware = checkContentAware(property.getValue());
            }
        }
    }

    private boolean checkContentAware(String xpathString) {

        boolean contentAware = false;

        // For message body and xpath expressions on message body
        if (xpathString.contains("/") || xpathString.contains("$body")) {
            contentAware = true;
        }

        // For special property access
        if (xpathString.contains("get-property('From'") ||
            xpathString.contains("get-property('FAULT')")) {
            contentAware = true;
        }

        return contentAware;
    }

    public BamMediator() {

    }

    public boolean isContentAware() {
        return this.isContentAware;
    }

    public boolean mediate(MessageContext messageContext) {

        SynapseLog synLog = getLog(messageContext);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : BAM mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        try {
            stream.sendEvents(messageContext);
        } catch (BamMediatorException e) {
            return true;
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : BAM mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        return true;
    }

    public Stream getStream() {
        return stream;
    }

    public String getServerProfile() {
        return serverProfile;
    }

    public void setServerProfile(String serverProfile) {
        this.serverProfile = serverProfile;
    }

}
