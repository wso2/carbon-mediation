/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediator.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.debug.constructs.EnclosedInlinedSequence;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.util.FixedByteArrayOutputStream;
import org.apache.synapse.util.MessageHelper;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediator.cache.digest.DigestGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.xml.stream.XMLStreamException;

/**
 * If a request comes to this class it creates and hash of the request and if the hash has a CachedResponse associated
 * with it the mediator will return the response without going to the backend. Otherwise it will pass on the request to
 * the next mediator.
 */
public class CacheMediator extends AbstractMediator implements ManagedLifecycle, EnclosedInlinedSequence {

    /**
     * The value of json content type as it appears in HTTP Content-Type header.
     */
    private final String jsonContentType = "application/json";
    /**
     * Cache configuration ID.
     */
    private String id;

    /**
     * The time duration for which the cache is kept.
     */
    private long timeout = CachingConstants.DEFAULT_TIMEOUT;

    /**
     * This specifies whether the mediator should be in the incoming path (to check the request) or in the outgoing path
     * (to cache the response).
     */
    private boolean collector = false;

    /**
     * The SequenceMediator to the onCacheHit sequence to be executed when an incoming message is identified as an
     * equivalent to a previously received message based on the value defined for the Hash Generator field.
     */
    private SequenceMediator onCacheHitSequence = null;

    /**
     * The reference to the onCacheHit sequence to be executed when an incoming message is identified as an equivalent
     * to a previously received message based on the value defined for the Hash Generator field.
     */
    private String onCacheHitRef = null;

    /**
     * The headers to exclude when caching.
     */
    private String[] headersToExcludeInHash = {""};

    /**
     * This is used to define the logic used by the mediator to evaluate the hash values of incoming messages.
     */
    private DigestGenerator digestGenerator = CachingConstants.DEFAULT_HASH_GENERATOR;

    /**
     * The size of the messages to be cached in memory. If this is -1 then cache can contain any number of messages.
     */
    private int inMemoryCacheSize = CachingConstants.DEFAULT_SIZE;

    /**
     * The compiled pattern for the regex of the responseCodes.
     */
    private Pattern responseCodePattern;

    /**
     * The maximum size of the messages to be cached. This is specified in bytes.
     */
    private int maxMessageSize = CachingConstants.DEFAULT_SIZE;

    /**
     * The regex expression of the HTTP response code to be cached.
     */
    private String responseCodes = CachingConstants.ANY_RESPONSE_CODE;

    /**
     * The protocol type used in caching.
     */
    private String protocolType = CachingConstants.HTTP_PROTOCOL_TYPE;

    /**
     * The http method type that needs to be cached.
     */
    private String[] hTTPMethodsToCache = {CachingConstants.ALL};

    /**
     * The cache manager to be used.
     */
    private CacheManager cacheManager;

    public CacheMediator(CacheManager cacheManager) {
        this.id = UUID.randomUUID().toString();
        responseCodePattern = Pattern.compile(responseCodes);
        this.cacheManager = cacheManager;
    }

    /**
     * {@inheritDoc}
     */
    public void init(SynapseEnvironment se) {
        if (onCacheHitSequence != null) {
            onCacheHitSequence.init(se);
        }
        exposeInvalidator(se.createMessageContext());
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {
        if (onCacheHitSequence != null) {
            onCacheHitSequence.destroy();
        }
        cacheManager.remove(id);
    }

    /**
     * {@inheritDoc}
     */
    public boolean mediate(MessageContext synCtx) {
        if (synCtx.getEnvironment().isDebuggerEnabled()) {
            if (super.divertMediationRoute(synCtx)) {
                return true;
            }
        }
        SynapseLog synLog = getLog(synCtx);
        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Cache mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        ConfigurationContext cfgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext().getConfigurationContext();

        if (cfgCtx == null) {
            handleException("Unable to perform caching,  ConfigurationContext cannot be found", synCtx);
            return false; // never executes.. but keeps IDE happy
        }
        boolean result = true;
        try {
            if (synCtx.isResponse()) {
                processResponseMessage(synCtx, cfgCtx, synLog);
            } else {
                result = processRequestMessage(synCtx, synLog);
            }
        } catch (ExecutionException e) {
            synLog.traceOrDebug("Unable to get the response");

        }
        return result;
    }

    /**
     * Caches the CachableResponse object with currently available attributes against the requestHash in
     * LoadingCache<String, CachableResponse>. Called in the load method of CachingBuilder
     *
     * @param requestHash the request hash that has already been computed
     */
    private CachableResponse cacheNewResponse(String requestHash) {
        CachableResponse response = new CachableResponse();
        response.setRequestHash(requestHash);
        response.setTimeout(timeout);
        return response;
    }

    /**
     * Processes a request message through the cache mediator. Generates the request hash and looks up for a hit, if
     * found; then the specified named or anonymous sequence is executed or marks this message as a response and sends
     * back directly to client.
     *
     * @param synCtx incoming request message
     * @param synLog the Synapse log to use
     * @return should this mediator terminate further processing?
     */
    private boolean processRequestMessage(MessageContext synCtx, SynapseLog synLog)
            throws ExecutionException {
        if (collector) {
            handleException("Request messages cannot be handled in a collector cache", synCtx);
        }
        org.apache.axis2.context.MessageContext msgCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
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
        CachableResponse cachedResponse = getMediatorCache().get(requestHash);
        synCtx.setProperty(CachingConstants.CACHED_OBJECT, cachedResponse);
        //This is used to store the http method of the request.
        String httpMethod = (String) msgCtx.getProperty(Constants.Configuration.HTTP_METHOD);
        cachedResponse.setHttpMethod(httpMethod);
        cachedResponse.setProtocolType(protocolType);
        cachedResponse.setResponseCodePattern(responseCodePattern);
        cachedResponse.setHTTPMethodsToCache(hTTPMethodsToCache);
        cachedResponse.setMaxMessageSize(maxMessageSize);
        Map<String, Object> headerProperties;
        if (cachedResponse.getResponsePayload() != null || cachedResponse.getResponseEnvelope() != null) {
            // get the response from the cache and attach to the context and change the
            // direction of the message
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Cache-hit for message ID : " + synCtx.getMessageID());
            }
            // mark as a response and replace envelope from cache
            synCtx.setResponse(true);
            try {
                if (cachedResponse.isJson()) {
                    byte[] payload = cachedResponse.getResponsePayload();
                    OMElement response = JsonUtil.getNewJsonPayload(msgCtx, payload, 0,
                                                                    payload.length, false, false);
                    if (msgCtx.getEnvelope().getBody().getFirstElement() != null) {
                        msgCtx.getEnvelope().getBody().getFirstElement().detach();
                    }
                    msgCtx.getEnvelope().getBody().addChild(response);

                } else {
                    msgCtx.setEnvelope(MessageHelper.cloneSOAPEnvelope(cachedResponse.getResponseEnvelope()));
                }
            } catch (AxisFault e) {
                handleException("Error creating response OM from cache : " + id, synCtx);
            }
            if (CachingConstants.HTTP_PROTOCOL_TYPE.equals(getProtocolType())) {
                if (cachedResponse.getStatusCode() != null) {
                    msgCtx.setProperty(NhttpConstants.HTTP_SC,
                                       Integer.parseInt(cachedResponse.getStatusCode().toString()));
                }
                if (cachedResponse.getStatusReason() != null) {
                    msgCtx.setProperty(PassThroughConstants.HTTP_SC_DESC, cachedResponse.getStatusReason());
                }
            }
            if (msgCtx.isDoingREST()) {

                msgCtx.removeProperty(PassThroughConstants.NO_ENTITY_BODY);
                msgCtx.removeProperty(Constants.Configuration.CONTENT_TYPE);
            }
            if ((headerProperties = cachedResponse.getHeaderProperties()) != null) {

                msgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS,
                                   headerProperties);
                msgCtx.setProperty(Constants.Configuration.MESSAGE_TYPE,
                                   headerProperties.get(Constants.Configuration.MESSAGE_TYPE));
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
                    synLog.traceOrDebug("Delegating message to the onCachingHit "
                                                + "sequence : " + onCacheHitRef);
                }
                ContinuationStackManager.updateSeqContinuationState(synCtx, getMediatorPosition());
                synCtx.getSequence(onCacheHitRef).mediate(synCtx);

            } else {

                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Request message " + synCtx.getMessageID() +
                                                " was served from the cache");
                }
                // send the response back if there is not onCacheHit is specified
                synCtx.setTo(null);
                //Todo continueExecution if needed
                Axis2Sender.sendBack(synCtx);

            }
            return false;
        }
        return true;
    }

    /**
     * Process a response message through this cache mediator. This finds the Cache used, and updates it for the
     * corresponding request hash
     *
     * @param synLog the Synapse log to use
     * @param synCtx the current message (response)
     * @param cfgCtx the abstract context in which the cache will be kept
     */
    @SuppressWarnings("unchecked")
    private void processResponseMessage(MessageContext synCtx, ConfigurationContext cfgCtx, SynapseLog synLog) {
        if (!collector) {
            handleException("Response messages cannot be handled in a non collector cache", synCtx);
        }
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        CachableResponse response = (CachableResponse) synCtx.getProperty(CachingConstants.CACHED_OBJECT);

        if (response != null) {
            boolean toCache = true;
            if (CachingConstants.HTTP_PROTOCOL_TYPE.equals(response.getProtocolType())) {
                Object httpStatus = msgCtx.getProperty(NhttpConstants.HTTP_SC);
                String statusCode = null;
                //Need to check the data type of HTTP_SC to avoid classcast exceptions.
                if (httpStatus instanceof String) {
                    statusCode = ((String) httpStatus).trim();
                } else if (httpStatus != null) {
                    statusCode = String.valueOf(httpStatus);
                }

                if (statusCode != null) {
                    // Now create matcher object.
                    Matcher m = response.getResponseCodePattern().matcher(statusCode);
                    if (m.matches()) {
                        response.setStatusCode(statusCode);
                        response.setStatusReason((String) msgCtx.getProperty(PassThroughConstants.HTTP_SC_DESC));
                    } else {
                        toCache = false;
                    }
                }

                if (toCache) {
                    toCache = false;
                    String httpMethod = response.getHttpMethod();
                    for (String method : response.getHTTPMethodsToCache()) {
                        if (method.equals("*") || method.equals(httpMethod)) {
                            toCache = true;
                            break;
                        }
                    }
                }
            }
            if (toCache) {
                String contentType = ((String) msgCtx.getProperty(Constants.Configuration.CONTENT_TYPE)).split(";")[0];

                if (contentType.equals(jsonContentType)) {
                    byte[] responsePayload = JsonUtil.jsonPayloadToByteArray(msgCtx);
                    if (response.getMaxMessageSize() > -1 &&
                            responsePayload.length > response.getMaxMessageSize()) {
                        synLog.traceOrDebug(
                                "Message size exceeds the upper bound for caching, request will not be cached");
                        return;
                    }
                    response.setResponsePayload(responsePayload);
                    response.setResponseEnvelope(null);
                    response.setJson(true);
                } else {
                    SOAPEnvelope clonedEnvelope = MessageHelper.cloneSOAPEnvelope(synCtx.getEnvelope());
                    if (response.getMaxMessageSize() > -1) {
                        FixedByteArrayOutputStream fbaos = new FixedByteArrayOutputStream(
                                response.getMaxMessageSize());
                        try {
                            clonedEnvelope.serialize(fbaos);
                        } catch (XMLStreamException e) {
                            handleException("Error in checking the message size", e, synCtx);
                        } catch (SynapseException syne) {
                            synLog.traceOrDebug(
                                    "Message size exceeds the upper bound for caching, request will not be cached");
                            return;
                        } finally {
                            try {
                                fbaos.close();
                            } catch (IOException e) {
                                handleException("Error occurred while closing the FixedByteArrayOutputStream ", e,
                                                synCtx);
                            }
                        }
                    }

                    response.setResponsePayload(null);
                    response.setResponseEnvelope(clonedEnvelope);
                    response.setJson(false);

                }

                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Storing the response message into the cache with ID : "
                                                + id + " for request hash : " + response.getRequestHash());
                }//remove
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug(
                            "Storing the response for the message with ID : " + synCtx.getMessageID() + " " +
                                    "with request hash ID : " + response.getRequestHash() + " in the cache");
                }

                Map<String, String> headers =
                        (Map<String, String>) msgCtx.getProperty(
                                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                String messageType = (String) msgCtx.getProperty(Constants.Configuration.MESSAGE_TYPE);
                Map<String, Object> headerProperties = new HashMap<>();
                //Individually copying All TRANSPORT_HEADERS to headerProperties Map instead putting whole
                //TRANSPORT_HEADERS map as single Key/Value pair to fix hazelcast serialization issue.
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    headerProperties.put(entry.getKey(), entry.getValue());
                }
                headerProperties.put(Constants.Configuration.MESSAGE_TYPE, messageType);
                headerProperties.put(CachingConstants.CACHE_KEY, response.getRequestHash());
                response.setHeaderProperties(headerProperties);
                msgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headerProperties);

            } else {
                response.clean();
            }
        } else {
            synLog.auditWarn("A response message without a valid mapping to the " +
                                     "request hash found. Unable to store the response in cache");
        }

    }

    /**
     * Creates default cache to keep mediator cache.
     *
     * @return global cache
     */
    public LoadingCache<String, CachableResponse> getMediatorCache() {
        LoadingCache<String, CachableResponse> cache = cacheManager.get(id);
        if (cache == null) {
            if (inMemoryCacheSize > -1) {
                cache = CacheBuilder.newBuilder().expireAfterWrite(timeout,
                                                                   TimeUnit.SECONDS).maximumSize(inMemoryCacheSize)
                        .build(new CacheLoader<String, CachableResponse>() {
                            @Override
                            public CachableResponse load(String requestHash) throws Exception {
                                return cacheNewResponse(requestHash);
                            }
                        });
            } else {
                cache = CacheBuilder.newBuilder().expireAfterWrite(timeout,
                                                                   TimeUnit.SECONDS).build(
                        new CacheLoader<String, CachableResponse>() {
                            @Override
                            public CachableResponse load(String requestHash) throws Exception {
                                return cacheNewResponse(requestHash);
                            }
                        });
            }
            cacheManager.put(id, cache);
        }
        return cache;
    }

    /**
     * {@inheritDoc}
     */
    public Mediator getInlineSequence(SynapseConfiguration synCfg, int inlinedSeqIdentifier) {
        if (inlinedSeqIdentifier == 0) {
            if (onCacheHitSequence != null) {
                return onCacheHitSequence;
            } else if (onCacheHitRef != null) {
                return synCfg.getSequence(onCacheHitRef);
            }
        }
        return null;
    }

    /**
     * Exposes the whole mediator cache through jmx MBean.
     */
    public void exposeInvalidator(MessageContext msgCtx) {
        String name = "org.wso2.carbon.mediator.cache:type=Cache,tenant=" +
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            ObjectName cacheMBeanObjName = new ObjectName(name);
            MBeanServer mserver = getMBeanServer();
            Set<ObjectName> set = mserver.queryNames(cacheMBeanObjName, null);
            if (set.isEmpty()) {
                MediatorCacheInvalidator cacheMBean = new MediatorCacheInvalidator(cacheManager,
                                                                                   PrivilegedCarbonContext
                                                                                           .getThreadLocalCarbonContext()
                                                                                           .getTenantDomain(),
                                                                                   PrivilegedCarbonContext
                                                                                           .getThreadLocalCarbonContext()
                                                                                           .getTenantId(), msgCtx);

                mserver.registerMBean(cacheMBean, cacheMBeanObjName);
            }
        } catch (MalformedObjectNameException e) {
            handleException("The format of the string does not correspond to a valid ObjectName.", e, msgCtx);
        } catch (NotCompliantMBeanException e) {
            handleException("MBean with the name " + name + " is already registered.", e, msgCtx);
        } catch (InstanceAlreadyExistsException e) {
            handleException("MBean implementation is not compliant with JMX specification standard MBean.", e, msgCtx);
        } catch (MBeanRegistrationException e) {
            handleException("Could not register MediatorCacheInvalidator MBean.", e, msgCtx);
        }
    }

    /**
     * Obtains existing mbean server instance or create new one.
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
     * This method gives the DigestGenerator to evaluate the hash values of incoming messages.
     *
     * @return DigestGenerator used evaluate hash values.
     */
    DigestGenerator getDigestGenerator() {
        return digestGenerator;
    }

    /**
     * This method sets the DigestGenerator to evaluate the hash values of incoming messages.
     *
     * @param digestGenerator DigestGenerator to be set to evaluate hash values.
     */
    void setDigestGenerator(DigestGenerator digestGenerator) {
        this.digestGenerator = digestGenerator;
    }

    /**
     * This method gives the timeout period in milliseconds.
     *
     * @return timeout in milliseconds
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * This method sets the timeout period as milliseconds.
     *
     * @param timeout millisecond timeout period to be set.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
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
     * This method gives array of headers that would be excluded when hashing.
     *
     * @return array of headers to exclude from hashing
     */
    public String[] getHeadersToExcludeInHash() {
        return headersToExcludeInHash;
    }

    /**
     * This method sets the array of headers that would be excluded when hashing.
     *
     * @param headersToExcludeInHash array of headers to exclude from hashing.
     */
    public void setHeadersToExcludeInHash(String... headersToExcludeInHash) {
        this.headersToExcludeInHash = headersToExcludeInHash;
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
     * This method gives the HTTP method that needs to be cached.
     *
     * @return the HTTP method to be cached
     */
    public String[] getHTTPMethodsToCache() {
        return hTTPMethodsToCache;
    }

    /**
     * This sets the HTTP method that needs to be cached.
     *
     * @param hTTPMethodToCache the HTTP method to be cached
     */
    public void setHTTPMethodsToCache(String... hTTPMethodToCache) {
        this.hTTPMethodsToCache = hTTPMethodToCache;
    }

    /**
     * @return the protocol type of the messages
     */
    public String getProtocolType() {
        return protocolType;
    }

    /**
     * This method sets protocolType of the messages.
     *
     * @param protocolType protocol type of the messages.
     */
    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    /**
     * @return The regex expression of the HTTP response code of the messages to be cached
     */
    public String getResponseCodes() {
        return responseCodes;
    }

    /**
     * This method sets the response codes that needs to be cached.
     *
     * @param responseCodes the response codes to be cached in regex form.
     */
    public void setResponseCodes(String responseCodes) {
        this.responseCodes = responseCodes;
        responseCodePattern = Pattern.compile(responseCodes);
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
