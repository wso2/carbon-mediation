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

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.mediator.bam.config.BamMediatorException;
import org.wso2.carbon.mediator.bam.config.BamServerConfig;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;
import org.wso2.carbon.mediator.bam.builders.CorrelationDataBuilder;
import org.wso2.carbon.mediator.bam.builders.MetaDataBuilder;
import org.wso2.carbon.mediator.bam.builders.PayloadDataBuilder;

/**
 * This is the main class of the Event Stream that extract data from mediator and send events.
 */
public class Stream {
	private static final Log log = LogFactory.getLog(Stream.class);
	public static final String ENABLE_MEDIATION_STATS = "EnableMediationStats";
	public static final String CLOUD_DEPLOYMENT_PROP = "IsCloudDeployment";
	public static final String SERVER_CONFIG_BAM_URL = "BamServerURL";
	public static final String DEFAULT_BAM_SERVER_URL = "tcp://127.0.0.1:7611";
	private DataPublisher asyncDataPublisher;
	private DataPublisher loadBalancingDataPublisher;
	private boolean isPublisherCreated;
	private BamServerConfig bamServerConfig;
	private StreamConfiguration streamConfiguration;
	private PayloadDataBuilder payloadDataBuilder;
	private MetaDataBuilder metaDataBuilder;
	private CorrelationDataBuilder correlationDataBuilder;

	public Stream() {
		loadBalancingDataPublisher = null;
		isPublisherCreated = false;
		payloadDataBuilder = new PayloadDataBuilder();
		metaDataBuilder = new MetaDataBuilder();
		correlationDataBuilder = new CorrelationDataBuilder();
	}

	public void sendEvents(MessageContext messageContext) throws BamMediatorException {
		ActivityIDSetter activityIDSetter = new ActivityIDSetter();
		activityIDSetter.setActivityIdInTransportHeader(messageContext);
		try {
			if (!isPublisherCreated) {
				initializeDataPublisher(this);
				isPublisherCreated = true;
			}
			this.publishEvent(messageContext);
		} catch (BamMediatorException e) {
			String errorMsg = "Problem occurred while logging events in the BAM Mediator. " + e.getMessage();
			log.error(errorMsg, e);
			throw new BamMediatorException(errorMsg, e);
		}
	}

	private synchronized static void initializeDataPublisher(Stream stream) throws BamMediatorException {
		try {
			if (!stream.isPublisherCreated) {
				stream.createDataPublisher();
				stream.isPublisherCreated = true;
			}
		} catch (BamMediatorException e) {
			String errorMsg = "Problem initializing the Data Publisher or Stream Definition. " + e.getMessage();
			log.error(errorMsg, e);
			throw new BamMediatorException(errorMsg, e);
		}
	}

	private void createDataPublisher() throws BamMediatorException {
		if (this.isCloudDeployment()) { // In Stratos environment
			this.createLoadBalancingDataPublisher(this.getServerConfigBAMServerURL(),
			                                      this.bamServerConfig.getUsername(),
			                                      this.bamServerConfig.getPassword());
	        /*asyncDataPublisher = new AsyncDataPublisher(this.getServerConfigBAMServerURL(),
                                                        this.bamServerConfig.getUsername(),
                                                        this.bamServerConfig.getPassword());*/
		} else { // In normal Carbon environment
			if (this.bamServerConfig.isLoadbalanced()) {
				this.createLoadBalancingDataPublisher(this.bamServerConfig.getUrlSet(),
				                                      this.bamServerConfig.getUsername(),
				                                      this.bamServerConfig.getPassword());
			} else {
				try {
					if (this.bamServerConfig.isSecure()) {
						asyncDataPublisher = new DataPublisher("Thrift", "ssl://" + this.bamServerConfig.getIp() + ":" +
						                                                 this.bamServerConfig.getAuthenticationPort(),
						                                       "ssl://" + this.bamServerConfig.getIp() + ":" +
						                                       this.bamServerConfig.getAuthenticationPort(),
						                                       this.bamServerConfig.getUsername(),
						                                       this.bamServerConfig.getPassword());
					} else {
						asyncDataPublisher = new DataPublisher("Thrift", "tcp://" + this.bamServerConfig.getIp() + ":" +
						                                                 this.bamServerConfig.getReceiverPort(),
						                                       "ssl://" + this.bamServerConfig.getIp() + ":" +
						                                       this.bamServerConfig.getAuthenticationPort(),
						                                       this.bamServerConfig.getUsername(),
						                                       this.bamServerConfig.getPassword());
					}
				} catch (DataEndpointAgentConfigurationException | DataEndpointException |
						DataEndpointConfigurationException | DataEndpointAuthenticationException | TransportException e) {
					String errorMsg = "Exception occurred while creating the AsyncDataPublisher " + e.getMessage();
					log.error(errorMsg, e);
					throw new BamMediatorException(errorMsg, e);
				}
			}
		}

		log.info("Data Publisher Created.");
	}

	private void createLoadBalancingDataPublisher(String urlSet, String username, String password)
			throws BamMediatorException {
		try {
			this.loadBalancingDataPublisher = new DataPublisher(urlSet, username, password);
		} catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException
				| DataEndpointAuthenticationException | TransportException e) {
			String errorMsg = "Exception occurred while creating the LoadBalancingDataPublisher " + e.getMessage();
			log.error(errorMsg, e);
			throw new BamMediatorException(errorMsg, e);
		}
	}

	private String getServerConfigBAMServerURL() {
		String[] bamServerUrl = ServerConfiguration.getInstance().getProperties(SERVER_CONFIG_BAM_URL);
		if (null != bamServerUrl) {
			return bamServerUrl[bamServerUrl.length - 1];
		} else {
			return DEFAULT_BAM_SERVER_URL;
		}
	}

	private boolean isCloudDeployment() {
		String[] cloudDeploy = ServerConfiguration.getInstance().getProperties(CLOUD_DEPLOYMENT_PROP);
		return null != cloudDeploy && Boolean.parseBoolean(cloudDeploy[cloudDeploy.length - 1]);
	}

	private void publishEvent(MessageContext messageContext) throws BamMediatorException {
		org.apache.axis2.context.MessageContext msgCtx =
				((Axis2MessageContext) messageContext).getAxis2MessageContext();
		AxisConfiguration axisConfiguration = msgCtx.getConfigurationContext().getAxisConfiguration();
		try {
			Object[] metaData = this.metaDataBuilder.createMetadata(messageContext, axisConfiguration);
			Object[] correlationData = this.correlationDataBuilder.createCorrelationData(messageContext);
			Object[] payloadData =
					this.payloadDataBuilder.createPayloadData(messageContext, msgCtx, this.streamConfiguration);

			if (this.bamServerConfig.isLoadbalanced()) {
				loadBalancingDataPublisher.publish(DataBridgeCommonsUtils
						                                   .generateStreamId(this.streamConfiguration.getName(),
						                                                     this.streamConfiguration.getVersion()),
				                                   metaData, correlationData, payloadData);
			} else {
				asyncDataPublisher.publish(DataBridgeCommonsUtils.generateStreamId(this.streamConfiguration.getName(),
				                                                                   this.streamConfiguration
						                                                                   .getVersion()), metaData,
				                           correlationData, payloadData);
			}
		} catch (Exception e) {
			String errorMsg = "Error occurred while sending the event. " + e.getMessage();
			log.error(errorMsg, e);
			throw new BamMediatorException(errorMsg, e);
		}
	}

	public void setBamServerConfig(BamServerConfig bamServerConfig) {
		this.bamServerConfig = bamServerConfig;
	}

	public void setStreamConfiguration(StreamConfiguration streamConfiguration) {
		this.streamConfiguration = streamConfiguration;
	}

	public StreamConfiguration getStreamConfiguration() {
		return streamConfiguration;
	}
}
