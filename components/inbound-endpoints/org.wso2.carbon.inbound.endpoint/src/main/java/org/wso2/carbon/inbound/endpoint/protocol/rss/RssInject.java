/*
 * Copyright (c) 2005, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.rss;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * RssInject uses to mediate the received Feeds
 */
@SuppressWarnings("deprecation")
public class RssInject implements InjectHandler {
	private static final Log log = LogFactory.getLog(RssInject.class);

	private String injectingSeq;
	private String onErrorSeq;
	private boolean sequential;
	private SynapseEnvironment synapseEnvironment;
	private String contentType;

	public RssInject(String injectingSeq2, String onErrorSeq2, boolean sequential2,
	                 SynapseEnvironment synapseEnvironment2, String contentType) {
		this.injectingSeq = injectingSeq2;
		this.onErrorSeq = onErrorSeq2;
		this.sequential = sequential2;
		this.synapseEnvironment = synapseEnvironment2;
		this.contentType = contentType;
	}

	/**
	 * Determine the message builder to use, set the feed message to the
	 * message context and
	 * inject the message to the sequence
	 */
	public boolean invoke(Object object) {
		log.debug("Feed Parse into invoke ");
		org.apache.synapse.MessageContext msgCtx = null;
		try {
			msgCtx = createMessageContext();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.info("Processed Feeds Message ");
		MessageContext axis2MsgCtx = null;
		try {
			axis2MsgCtx =
			              ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx).getAxis2MessageContext();
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		// Determine the message builder to use
		Builder builder = null;
		if (contentType == null) {
			log.debug("No content type specified. Using SOAP builder.");
			builder = new SOAPBuilder();
		} else {
			int index = contentType.indexOf(';');
			String type = index > 0 ? contentType.substring(0, index) : contentType;
			try {
				builder = BuilderUtil.getBuilderFromSelector(type, axis2MsgCtx);
			} catch (AxisFault axisFault) {
				log.error("Error while creating message builder :: " + axisFault.getMessage(),
				          axisFault);

			}
			if (builder == null) {
				if (log.isDebugEnabled()) {
					log.info("No message builder found for type '" + type +
					         "'. Falling back to SOAP.");
				}
				builder = new SOAPBuilder();
			}
		}

		try {
			OMElement documentElement = (OMElement) object;
			msgCtx.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
		} catch (AxisFault axisFault) {
			log.error("Error while setting message to the message context :: " +
			                  axisFault.getMessage(), axisFault);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		// Inject the message to the sequence.

		if (injectingSeq == null || injectingSeq.equals("")) {
			log.error("Sequence name not specified. Sequence : " + injectingSeq);
			return false;
		}
		SequenceMediator seq =
		                       (SequenceMediator) synapseEnvironment.getSynapseConfiguration()
		                                                            .getSequence(injectingSeq);
		try {
			seq.setErrorHandler(onErrorSeq);
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		if (seq != null) {
			if (log.isDebugEnabled()) {
				log.info("injecting message to sequence : " + injectingSeq);
			}
			synapseEnvironment.injectInbound(msgCtx, seq, sequential);
		} else {
			log.error("Sequence: " + injectingSeq + " not found");
		}

		return true;
	}

	/**
	 * Create the initial message context for Feed
	 */
	private org.apache.synapse.MessageContext createMessageContext() {
		org.apache.synapse.MessageContext msgCtx = synapseEnvironment.createMessageContext();
		MessageContext axis2MsgCtx =
		                             ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx).getAxis2MessageContext();
		axis2MsgCtx.setServerSide(true);
		axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());
		msgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, true);
		PrivilegedCarbonContext carbonContext =
		                                        PrivilegedCarbonContext.getThreadLocalCarbonContext();
		axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, carbonContext.getTenantDomain());
		return msgCtx;
	}

}
