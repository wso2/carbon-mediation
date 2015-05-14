/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediator.cache;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.util.FixedByteArrayOutputStream;
import org.apache.synapse.util.MessageHelper;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediator.cache.digest.DigestGenerator;
import org.wso2.carbon.mediator.cache.util.RequestHash;
import org.wso2.carbon.mediator.cache.util.SOAPMessageHelper;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanRegistrationException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * CacheMediator will cache the response messages indexed using the hash value of the request message,
 * and subsequent messages with the same request (request hash will be generated and checked for the equality) within
 * the cache expiration period will be served from the stored responses in the cache
 *
 * @see org.apache.synapse.Mediator
 */
public class CacheMediator extends AbstractMediator implements ManagedLifecycle {

	/**
	 * Cache configuration ID.
	 */
	private String id = null;

	/**
	 * The scope of the cache
	 */
	private String scope = CachingConstants.SCOPE_PER_HOST;

	/**
	 * This specifies whether the mediator should be in the incoming path (to check the request) or in the outgoing
	 * path (to cache the response).
	 */
	private boolean collector = false;

	/**
	 * This is used to define the logic used by the mediator to evaluate the hash values of incoming messages.
	 */
	private DigestGenerator digestGenerator = CachingConstants.DEFAULT_XML_IDENTIFIER;

	/**
	 * The size of the messages to be cached in memory. If this is 0 then no disk cache,
	 * and if there is no size specified in the config  factory will asign a default value to enable disk based caching.
	 */
	private int inMemoryCacheSize = CachingConstants.DEFAULT_CACHE_SIZE;

	/**
	 * The size of the messages to be cached in memory. Disk based and hirearchycal caching is not implemented yet.
	 */
	private int diskCacheSize = 0;

	/**
	 * The time duration for which the cache is kept.
	 */
	private long timeout = 0L;

	/**
	 * The SequenceMediator to the onCacheHit sequence to be executed when an incoming message is identified as an
	 * equivalent to a previously received message based on the value defined for the Hash Generator field.
	 */
	private SequenceMediator onCacheHitSequence = null;

	/**
	 * The reference to the onCacheHit sequence to be executed when an incoming message is identified as an
	 * equivalent to a previously received message based on the value defined for the Hash Generator field.
	 */
	private String onCacheHitRef = null;

	/**
	 * The maximum size of the messages to be cached. This is specified in bytes.
	 */
	private int maxMessageSize = 0;

	/**
	 * Prefix of the cache key
	 */
	private static final String CACHE_KEY_PREFIX = "mediation.cache_key_";

	/**
	 * Key to use in cache configuration
	 */
	private String cacheKey = "mediation.cache_key";

	/**
	 * This holds whether the global cache already initialized or not.
	 */
	private static boolean mediatorCacheInit = false;

	@Override
	public void init(SynapseEnvironment se) {
		if (onCacheHitSequence != null) {
			onCacheHitSequence.init(se);
		}
	}

	@Override
	public void destroy() {
		if (onCacheHitSequence != null) {
			onCacheHitSequence.destroy();
		}
	}

	@Override
	public boolean isContentAware() {
		return true;
	}

	@Override
	public boolean mediate(MessageContext synCtx) {
		SynapseLog synLog = getLog(synCtx);

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("Start : Cache mediator");

			if (synLog.isTraceTraceEnabled()) {
				synLog.traceTrace("Message : " + synCtx.getEnvelope());
			}
		}

		// if maxMessageSize is specified check for the message size before processing
		if (maxMessageSize > 0) {
			FixedByteArrayOutputStream fbaos = new FixedByteArrayOutputStream(maxMessageSize);
			try {
				MessageHelper.cloneSOAPEnvelope(synCtx.getEnvelope()).serialize(fbaos);
			} catch (XMLStreamException e) {
				handleException("Error in checking the message size", e, synCtx);
			} catch (SynapseException syne) {
				synLog.traceOrDebug("Message size exceeds the upper bound for caching, request will not be cached");
				return true;
			} finally {
				try {
					if (fbaos != null) {
						fbaos.close();
					}
				} catch (IOException e) {
					handleException("Error occurred while closing the FixedByteArrayOutputStream ", e, synCtx);
				}
			}
		}

		ConfigurationContext cfgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext().getConfigurationContext();

		if (cfgCtx == null) {
			handleException("Unable to perform caching, "
			                + " ConfigurationContext cannot be found", synCtx);
			return false; // never executes.. but keeps IDE happy
		}

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("Looking up cache at scope : " + scope + " with ID : " + cacheKey);
		}

		boolean result = true;
		try {

			if (synCtx.isResponse()) {
				processResponseMessage(synCtx, cfgCtx, synLog);

			} else {
				result = processRequestMessage(synCtx, synLog);
			}

		} catch (ClusteringFault clusteringFault) {
			synLog.traceOrDebug("Unable to replicate Cache mediator state among the cluster");
		}

		synLog.traceOrDebug("End : Cache mediator");

		exposeData(synCtx);

		return result;
	}

	/**
	 * Process a response message through this cache mediator. This finds the Cache used, and
	 * updates it for the corresponding request hash
	 *
	 * @param synLog the Synapse log to use
	 * @param synCtx the current message (response)
	 * @param cfgCtx the abstract context in which the cache will be kept
	 * @throws ClusteringFault is there is an error in replicating the cfgCtx
	 */
	private void processResponseMessage(MessageContext synCtx, ConfigurationContext cfgCtx,
	                                    SynapseLog synLog) throws ClusteringFault {

		if (!collector) {
			handleException("Response messages cannot be handled in a non collector cache", synCtx);
		}
		org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
		OperationContext operationContext = msgCtx.getOperationContext();
		CachableResponse response = (CachableResponse) operationContext.getProperty(CachingConstants.CACHED_OBJECT);

		if (response != null) {
			if (synLog.isTraceOrDebugEnabled()) {
				synLog.traceOrDebug("Storing the response message into the cache at scope : " + scope + " with ID : "
				                    + cacheKey + " for request hash : " + response.getRequestHash());
			}
			if (synLog.isTraceOrDebugEnabled()) {
				synLog.traceOrDebug("Storing the response for the message with ID : " + synCtx.getMessageID() + " " +
				                    "with request hash ID : " + response.getRequestHash() + " in the cache : " +
				                    cacheKey);
			}

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			try {
				synCtx.getEnvelope().serialize(outStream);
				response.setResponseEnvelope(outStream.toByteArray());
				if (msgCtx.isDoingREST()) {
					response.setSOAP11(synCtx.isSOAP11());
					Map<String, String> headers =
							(Map) msgCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
					String messageType = (String) msgCtx.getProperty(Constants.Configuration.MESSAGE_TYPE);
					Map<String, Object> headerProperties = new HashMap<String, Object>();
					headerProperties.put(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
					headerProperties.put(Constants.Configuration.MESSAGE_TYPE, messageType);
					response.setHeaderProperties(headerProperties);
				}

			} catch (XMLStreamException e) {
				handleException("Unable to set the response to the Cache", e, synCtx);
			}

			if (response.getTimeout() > 0) {
				response.setExpireTimeMillis(System.currentTimeMillis() + response.getTimeout());
			}

			getMediatorCache().put(response.getRequestHash(), response);
			// Finally, we may need to replicate the changes in the cache
			Replicator.replicate(cfgCtx);
		} else {
			synLog.auditWarn("A response message without a valid mapping to the " +
			                 "request hash found. Unable to store the response in cache");
		}

	}

	/**
	 * Processes a request message through the cache mediator. Generates the request hash and looks
	 * up for a hit, if found; then the specified named or anonymous sequence is executed or marks
	 * this message as a response and sends back directly to client.
	 *
	 * @param synCtx incoming request message
	 * @param synLog the Synapse log to use
	 * @return should this mediator terminate further processing?
	 * @throws ClusteringFault if there is an error in replicating the cfgCtx
	 */
	private boolean processRequestMessage(MessageContext synCtx,
	                                      SynapseLog synLog) throws ClusteringFault {

		if (collector) {
			handleException("Request messages cannot be handled in a collector cache", synCtx);
		}

		OperationContext opCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext().getOperationContext();
		String requestHash = null;

		try {
			requestHash = digestGenerator.getDigest(((Axis2MessageContext) synCtx).getAxis2MessageContext());
			synCtx.setProperty(CachingConstants.REQUEST_HASH, requestHash);
		} catch (CachingException e) {
			handleException("Error in calculating the hash value of the request", e, synCtx);
		}

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("Generated request hash : " + requestHash);
		}

		RequestHash hash = new RequestHash(requestHash);
		CachableResponse cachedResponse = getMediatorCache().get(requestHash);
		org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
		opCtx.setProperty(CachingConstants.REQUEST_HASH, requestHash);

		byte[] responseEnvelop;
		Map<String, Object> headerProperties;
		if (cachedResponse != null && (responseEnvelop = cachedResponse.getResponseEnvelope()) != null) {
			// get the response from the cache and attach to the context and change the
			// direction of the message
			if (!cachedResponse.isExpired()) {
				if (synLog.isTraceOrDebugEnabled()) {
					synLog.traceOrDebug("Cache-hit for message ID : " + synCtx.getMessageID());
				}
				cachedResponse.setInUse(true);
				// mark as a response and replace envelope from cache
				synCtx.setResponse(true);
				opCtx.setProperty(CachingConstants.CACHED_OBJECT, cachedResponse);

				SOAPEnvelope omSOAPEnv = null;

				try {
					if (msgCtx.isDoingREST()) {
						if ((headerProperties = cachedResponse.getHeaderProperties()) != null) {

							omSOAPEnv = SOAPMessageHelper
									.buildSOAPEnvelopeFromBytes(responseEnvelop, cachedResponse.isSOAP11());
							msgCtx.removeProperty("NO_ENTITY_BODY");
							msgCtx.removeProperty(Constants.Configuration.CONTENT_TYPE);
							msgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS,
							                   headerProperties
									                   .get(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
							msgCtx.setProperty(Constants.Configuration.MESSAGE_TYPE,
							                   headerProperties.get(Constants.Configuration.MESSAGE_TYPE));
						}

					} else {
						omSOAPEnv = SOAPMessageHelper
								.buildSOAPEnvelopeFromBytes(responseEnvelop, msgCtx.isSOAP11());
						//finally set soap envelope by obtaining built response envelope
						cachedResponse.setResponseEnvelope(omSOAPEnv.toString().getBytes());
					}

					if (omSOAPEnv != null) {
						synCtx.setEnvelope(omSOAPEnv);
					}
				} catch (AxisFault axisFault) {
					handleException("Error setting response envelope from cache : "
					                + cacheKey, synCtx);
				} catch (IOException ioe) {
					handleException("Error setting response envelope from cache : "
					                + cacheKey, ioe, synCtx);
				} catch (SOAPException soape) {
					handleException("Error setting response envelope from cache : "
					                + cacheKey, soape, synCtx);
				}

				// take specified action on cache hit
				if (onCacheHitSequence != null) {
					// if there is an onCacheHit use that for the mediation
					synLog.traceOrDebug("Delegating message to the onCachingHit "
					                    + "Anonymous sequence");
					ContinuationStackManager.addReliantContinuationState(synCtx, 0, getMediatorPosition());
					if (onCacheHitSequence.mediate(synCtx)) {
						ContinuationStackManager.removeReliantContinuationState(synCtx);
					}

				} else if (onCacheHitRef != null) {
					if (synLog.isTraceOrDebugEnabled()) {
						synLog.traceOrDebug("Delegating message to the onCachingHit " +
						                    "sequence : " + onCacheHitRef);
					}
					ContinuationStackManager.updateSeqContinuationState(synCtx, getMediatorPosition());
					synCtx.getSequence(onCacheHitRef).mediate(synCtx);

				} else {

					if (synLog.isTraceOrDebugEnabled()) {
						synLog.traceOrDebug("Request message " + synCtx.getMessageID() +
						                    " was served from the cache : " + cacheKey);
					}
					// send the response back if there is not onCacheHit is specified
					synCtx.setTo(null);
					Axis2Sender.sendBack(synCtx);

				}
				// stop any following mediators from executing
				return false;

			} else {
				cachedResponse.reincarnate(timeout);
				if (synLog.isTraceOrDebugEnabled()) {
					synLog.traceOrDebug("Existing cached response has expired. Resetting cache element");
				}
				getMediatorCache().put(hash.getRequestHash(), cachedResponse);
				opCtx.setProperty(CachingConstants.CACHED_OBJECT, cachedResponse);
				Replicator.replicate(opCtx);
			}
		} else {
			cacheNewResponse(msgCtx, hash);
		}

		return true;
	}

	/**
	 * Caches the CachableResponse object with currently available attributes against the requestHash in Cache<String,
	 * CachableResponse>
	 *
	 * @param msgContext axis2 message context of the request message
	 * @param requestHash the request hash that has already been computed
	 * @throws ClusteringFault if there is an error in replicating the cfgCtx
	 */
	private void cacheNewResponse(org.apache.axis2.context.MessageContext msgContext, RequestHash requestHash)
			throws ClusteringFault {
		OperationContext opCtx = msgContext.getOperationContext();
		CachableResponse response = new CachableResponse();
		response.setRequestHash(requestHash.getRequestHash());
		response.setTimeout(timeout);
		getMediatorCache().put(requestHash.getRequestHash(), response);
		opCtx.setProperty(CachingConstants.CACHED_OBJECT, response);
		Replicator.replicate(opCtx);
	}

	/**
	 * Exposes the whole mediator cache through jmx MBean
	 *
	 * @param msgCtx cache response msgCtx
	 */
	public void exposeData(MessageContext msgCtx) {
		String serverPackage = "org.wso2.carbon.mediation";
		String objectName = serverPackage + ":type=Cache,tenant=" + PrivilegedCarbonContext
				.getThreadLocalCarbonContext().getTenantDomain() + ",manager=" + Caching.getCacheManagerFactory().
				getCacheManager(CachingConstants.CACHE_MANAGER).getName() + ",name=" + getMediatorCache().getName();
		try {
			MBeanServer mserver = getMBeanServer();
			ObjectName cacheMBeanObjName = new ObjectName(objectName);
			Set set = mserver.queryNames(new ObjectName(objectName), null);
			if (set.isEmpty()) {
				MediatorCacheInvalidator cacheMBean = new MediatorCacheInvalidator(
						PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
						PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), msgCtx);

				mserver.registerMBean(cacheMBean, cacheMBeanObjName);
			}
		} catch (MalformedObjectNameException e) {
			handleException("The format of the string does not correspond to a valid ObjectName.", e, msgCtx);
		} catch (InstanceAlreadyExistsException e) {
			handleException("MBean with the name "+objectName+" is already registered.", e, msgCtx);
		} catch (NotCompliantMBeanException e) {
			handleException("MBean implementation is not compliant with JMX specification standard MBean.", e, msgCtx);
		} catch (MBeanRegistrationException e) {
			handleException("Could not register MediatorCacheInvalidator MBean.", e, msgCtx);
		}
	}

	/**
	 * Obtains existing mbean server instance or create new one
	 *
	 * @return MBeanServer instance
	 */
	private MBeanServer getMBeanServer() {
		MBeanServer mserver;
		if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
			mserver = MBeanServerFactory.findMBeanServer(null).get(0);
		} else {
			mserver = MBeanServerFactory.createMBeanServer();
		}
		return mserver;
	}

	/**
	 * Creates default cache to keep mediator cache
	 *
	 * @return global cache
	 */
	public static Cache<String, CachableResponse> getMediatorCache() {
		if (mediatorCacheInit) {
			return Caching.getCacheManagerFactory().getCacheManager(CachingConstants.CACHE_MANAGER)
			              .getCache(CachingConstants.MEDIATOR_CACHE);
		} else {
			CacheManager cacheManager =
					Caching.getCacheManagerFactory().getCacheManager(CachingConstants.CACHE_MANAGER);
			mediatorCacheInit = true;

			return cacheManager.<String, CachableResponse>createCacheBuilder(CachingConstants.MEDIATOR_CACHE).
					setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
					CachingConstants.CACHE_INVALIDATION_TIME)).setExpiry(CacheConfiguration.ExpiryType.ACCESSED,
			        new CacheConfiguration.Duration(TimeUnit.SECONDS,CachingConstants.CACHE_INVALIDATION_TIME))
			        .setStoreByValue(false).build();
		}
	}

	/**
	 * This methods gives the ID of the cache configuration.
	 *
	 * @return string cache configuration ID.
	 */
	public String getId() {
		return id;
	}

	/**
	 * This methods sets the ID of the cache configuration.
	 *
	 * @param id cache configuration ID to be set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * This method gives the scope of the cache.
	 *
	 * @return value of the cache scope.
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * This method sets the scope of the cache.
	 *
	 * @param scope cache scope to be set.
	 */
	public void setScope(String scope) {
		this.scope = scope;
		if (CachingConstants.SCOPE_PER_MEDIATOR.equals(scope)) {
			cacheKey = CACHE_KEY_PREFIX + id;
		}
	}

	/**
	 * This method gives whether the mediator should be in the incoming path or in the outgoing path as a boolean.
	 *
	 * @return boolean true if incoming path false if outgoing path.
	 */
	public boolean isCollector() {
		return collector;
	}

	/**
	 * This method sets whether the mediator should be in the incoming path or in the outgoing path as a boolean.
	 *
	 * @param collector boolean value to be set as collector.
	 */
	public void setCollector(boolean collector) {
		this.collector = collector;
	}

	/**
	 * This method gives the DigestGenerator to evaluate the hash values of incoming messages.
	 *
	 * @return DigestGenerator used evaluate hash values.
	 */
	public DigestGenerator getDigestGenerator() {
		return digestGenerator;
	}

	/**
	 * This method sets the DigestGenerator to evaluate the hash values of incoming messages.
	 *
	 * @param digestGenerator DigestGenerator to be set to evaluate hash values.
	 */
	public void setDigestGenerator(DigestGenerator digestGenerator) {
		this.digestGenerator = digestGenerator;
	}

	/**
	 * This method gives the size of the messages to be cached in memory.
	 *
	 * @return memory cache size in bytes.
	 */
	public int getInMemoryCacheSize() {
		return inMemoryCacheSize;
	}

	/**
	 * This method sets the size of the messages to be cached in memory.
	 *
	 * @param inMemoryCacheSize value(number of bytes) to be set as memory cache size.
	 */
	public void setInMemoryCacheSize(int inMemoryCacheSize) {
		this.inMemoryCacheSize = inMemoryCacheSize;
	}

	/**
	 * This method gives the size of the messages to be cached in disk.
	 *
	 * @return disk cache size in bytes.
	 */
	public int getDiskCacheSize() {
		return diskCacheSize;
	}

	/**
	 * This method sets the size of the messages to be cached in disk.
	 *
	 * @param diskCacheSize value(number of bytes) to be set as disk cache size.
	 */
	public void setDiskCacheSize(int diskCacheSize) {
		this.diskCacheSize = diskCacheSize;
	}

	/**
	 * This method gives the timeout period in milliseconds.
	 *
	 * @return timeout in milliseconds
	 */
	public long getTimeout() {
		return timeout / 1000;
	}

	/**
	 * This method sets the timeout period as milliseconds.
	 *
	 * @param timeout millisecond timeout period to be set.
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout * 1000;
	}

	/**
	 * This method gives SequenceMediator to be executed.
	 *
	 * @return sequence mediator to be executed.
	 */
	public SequenceMediator getOnCacheHitSequence() {
		return onCacheHitSequence;
	}

	/**
	 * This method sets SequenceMediator to be executed.
	 *
	 * @param onCacheHitSequence sequence mediator to be set.
	 */
	public void setOnCacheHitSequence(SequenceMediator onCacheHitSequence) {
		this.onCacheHitSequence = onCacheHitSequence;
	}

	/**
	 * This method gives reference to the onCacheHit sequence to be executed.
	 *
	 * @return reference to the onCacheHit sequence.
	 */
	public String getOnCacheHitRef() {
		return onCacheHitRef;
	}

	/**
	 * This method sets reference to the onCacheHit sequence to be executed.
	 *
	 * @param onCacheHitRef reference to the onCacheHit sequence to be set.
	 */
	public void setOnCacheHitRef(String onCacheHitRef) {
		this.onCacheHitRef = onCacheHitRef;
	}

	/**
	 * This method gives the maximum size of the messages to be cached in bytes.
	 *
	 * @return maximum size of the messages to be cached in bytes.
	 */
	public int getMaxMessageSize() {
		return maxMessageSize;
	}

	/**
	 * This method sets the maximum size of the messages to be cached in bytes.
	 *
	 * @param maxMessageSize maximum size of the messages to be set in bytes.
	 */
	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

}
