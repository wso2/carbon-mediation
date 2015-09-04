/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.FeedEP;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.inbound.endpoint.common.InboundRequestProcessorImpl;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;

public class FeedEP extends InboundRequestProcessorImpl {

    private static final Log log = LogFactory.getLog(FeedEP.class.getName());
    private static final String ENDPOINT_POSTFIX = "FeedEP" + COMMON_ENDPOINT_POSTFIX;
    private Properties rssProperties;
    private String injectingSeq;
    private String onErrorSeq;
    private boolean sequential;
    private String host;
    private String feedType;
    private ConsumeFeed consume;
    private long scanInterval;
    RegistryHandler registryHandler;
    private  String dateFormat;

    public FeedEP(InboundProcessorParams params) {
        registryHandler = new RegistryHandler();
        this.name = params.getName();
        this.rssProperties = params.getProperties();
        String inboundEndpointInterval =
                rssProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_INTERVAL);
        if (inboundEndpointInterval != null) {
            try {
                this.scanInterval = Long.parseLong(inboundEndpointInterval);
            } catch (NumberFormatException nfe) {
                log.error("Invalid numeric value for interval. " + nfe.getMessage(), nfe);
                throw new SynapseException("Invalid numeric value for interval. ", nfe);
            }
        }
        this.sequential = true;
        this.host = rssProperties.getProperty("feed.url");
        this.feedType = rssProperties.getProperty("feed.type");
        log.info("URL : " + host + "Feed Type : " + feedType);
        if(rssProperties.getProperty("TimeFormat")!=null){
            this.dateFormat=rssProperties.getProperty("TimeFormat");
        }

        String inboundEndpointSequential =
                rssProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL);
        if (inboundEndpointSequential != null) {
            try {
                this.sequential = Boolean.parseBoolean(inboundEndpointSequential);
            } catch (Exception e) {
                log.error("The sequential value should be true or false. " + e.getMessage(), e);
                throw new SynapseException("The sequential value should be true or false ", e);
            }
        }
        this.coordination = true;
        String inboundCoordination =
                rssProperties.getProperty(PollingConstants.INBOUND_COORDINATION);
        if (inboundCoordination != null) {
            try {
                this.coordination = Boolean.parseBoolean(inboundCoordination);
            } catch (Exception e) {
                log.error("The Coordination value should be true or false. " + e.getMessage(), e);
                throw new SynapseException("The Coordination value should be true or false ", e);
            }
        }

        this.injectingSeq = params.getInjectingSeq();
        this.onErrorSeq = params.getOnErrorSeq();
        this.synapseEnvironment = params.getSynapseEnvironment();
    }

    public void destroy() {
        try {
            if (registryHandler.readFromRegistry(name) != null) {
                registryHandler.deleteFromRegitry(name);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        super.destroy();
    }


    @Override
    public void init() {
        RssInject rssInject =
                new RssInject(injectingSeq, onErrorSeq, sequential,
                        synapseEnvironment, "TEXT");
        consume = new ConsumeFeed(rssInject, scanInterval, host, feedType, registryHandler, name,dateFormat);
        InboundTask task = new FeedTask(consume, interval);
        start(task, ENDPOINT_POSTFIX);
    }

}