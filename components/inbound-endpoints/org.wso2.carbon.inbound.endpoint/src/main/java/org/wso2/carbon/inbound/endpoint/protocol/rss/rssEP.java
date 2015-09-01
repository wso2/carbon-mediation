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

package org.wso2.carbon.inbound.endpoint.protocol.rss;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.inbound.endpoint.common.InboundRequestProcessorImpl;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;
import org.wso2.carbon.inbound.endpoint.protocol.rss.ConsumeFeed;
import org.wso2.carbon.inbound.endpoint.protocol.kafka.AbstractKafkaMessageListener;

public class rssEP extends InboundRequestProcessorImpl {

	private static final Log log = LogFactory.getLog(rssEP.class.getName());
	private static final String ENDPOINT_POSTFIX = "rss" + COMMON_ENDPOINT_POSTFIX;
	private Properties RssProperties;
	private String injectingSeq;
	private String onErrorSeq;
	private boolean sequential;
	private String host;
	private String FeedType;
	private ConsumeFeed consume;
	protected AbstractKafkaMessageListener messageListener;
	private long scanInterval;
	RegistryHandler registryHandler;

	public rssEP(InboundProcessorParams params) {
		registryHandler=new RegistryHandler();
		this.name = params.getName();
		this.RssProperties = params.getProperties();
		String inboundEndpointInterval =
		                                 RssProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_INTERVAL);
		if (inboundEndpointInterval != null) {
			try {
				this.scanInterval = Long.parseLong(inboundEndpointInterval);
			} catch (NumberFormatException nfe) {
				log.error("Invalid numeric value for interval. " + nfe.getMessage(), nfe);
				throw new SynapseException("Invalid numeric value for interval. ", nfe);
			}
		}
		this.sequential = true;
		this.host = RssProperties.getProperty("feed.url");
		if (host == null) {
			log.info("Host Address Can't be Empty");
		} else {
			log.info("endpoint url is " + host);
		}
		this.FeedType = RssProperties.getProperty("feed.type");

		String inboundEndpointSequential =
		                                   RssProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL);
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
		                             RssProperties.getProperty(PollingConstants.INBOUND_COORDINATION);
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
		try{
			if(registryHandler.readFromRegistry(name)!=null){
				registryHandler.DeleteFromRegitry(name);
			}
		}
		catch(Exception e){
			log.error(e.getMessage());
		}
		super.destroy();
	}
	
	
	@Override
	public void init() {		
		RssInject rssInject =
		                      new RssInject(injectingSeq, onErrorSeq, sequential,
		                                    synapseEnvironment, "TEXT");
		consume = new ConsumeFeed(rssInject, scanInterval, host, FeedType,registryHandler,name);
		InboundTask task = new FeedTask(consume, interval);
		start(task, ENDPOINT_POSTFIX);
	}

}