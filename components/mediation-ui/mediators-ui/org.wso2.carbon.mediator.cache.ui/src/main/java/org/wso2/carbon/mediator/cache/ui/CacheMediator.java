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
package org.wso2.carbon.mediator.cache.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.cache.CachingConstants;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;

import javax.xml.namespace.QName;

/**
 * Object of this class is used to store mediator information in UI side.
 */
public class CacheMediator extends AbstractListMediator {

    /**
     * QName of the collector.
     */
    private static final QName ATT_COLLECTOR = new QName(CachingConstants.COLLECTOR_STRING);

    /**
     * QName of the maximum message size.
     */
    private static final QName ATT_MAX_MSG_SIZE = new QName(CachingConstants.MAX_MESSAGE_SIZE_STRING);

    /**
     * QName of the timeout.
     */
    private static final QName ATT_TIMEOUT = new QName(CachingConstants.TIMEOUT_STRING);

    /**
     * QName of the mediator sequence.
     */
    private static final QName ATT_SEQUENCE = new QName(CachingConstants.SEQUENCE_STRING);

    /**
     * QName of the implementation type.
     */
    private static final QName ATT_TYPE = new QName(CachingConstants.TYPE_STRING);

    /**
     * QName of the maximum message size.
     */
    private static final QName ATT_SIZE = new QName(CachingConstants.MAX_SIZE_STRING);

    /**
     * QName of the onCacheHit mediator sequence reference.
     */
    private static final QName ON_CACHE_HIT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                          CachingConstants.ON_CACHE_HIT_STRING);

    /**
     * QName of the cache implementation.
     */
    private static final QName IMPLEMENTATION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                            CachingConstants.IMPLEMENTATION_STRING);

    /**
     * This holds the default timeout of the mediator cache.
     */
    private static final long DEFAULT_TIMEOUT = 5000L;

    /**
     * QName of the onCacheHit mediator sequence reference.
     */
    private static final QName PROTOCOL_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                      CachingConstants.PROTOCOL_STRING);

    /**
     * QName of the hTTPMethodToCache.
     */
    private static final QName HTTP_METHODS_TO_CACHE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                                   CachingConstants.METHODS_STRING);

    /**
     * QName of the headersToExcludeInHash.
     */
    private static final QName HEADERS_TO_EXCLUDE_IN_HASH_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                                        CachingConstants.HEADERS_TO_EXCLUDE_STRING);
    /**
     * QName of the response codes to include when hashing.
     */
    private static final QName RESPONSE_CODES_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                            CachingConstants.RESPONSE_CODES_STRING);

    /**
     * QName of the digest generator.
     */
    private static final QName HASH_GENERATOR_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                            CachingConstants.HASH_GENERATOR_STRING);

    /**
     * QName of the enableCacheControlHeader.
     */
    private static final QName ENABLE_CACHE_CONTROL_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                            CachingConstants.ENABLE_CACHE_CONTROL_STRING);
    /**
     * QName of the includeAgeHeader.
     */
    private static final QName INCLUDE_AGE_HEADER_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                            CachingConstants.INCLUDE_AGE_HEADER_STRING);

    /**
     * This specifies whether the mediator should be in the incoming path (to check the request) or in the outgoing path
     * (to cache the response).
     */
    private boolean collector = false;

    /**
     * This is used to define the logic used by the mediator to evaluate the hash values of incoming messages.
     */
    private String digestGenerator = CachingConstants.DEFAULT_HASH_GENERATOR.getClass().toString();

    /**
     * The size of the messages to be cached in memory. If this is 0 then no disk cache, and if there is no size
     * specified in the config  factory will asign a default value to enable disk based caching.
     */
    private int inMemoryCacheSize = -1;

    /**
     * The time duration for which the cache is kept.
     */
    private long timeout = CachingConstants.DEFAULT_TIMEOUT;

    /**
     * The reference to the onCacheHit sequence to be executed when an incoming message is identified as an equivalent
     * to a previously received message based on the value defined for the Hash Generator field.
     */
    private String onCacheHitRef = null;

    /**
     * The maximum size of the messages to be cached. This is specified in bytes.
     */
    private int maxMessageSize = -1;

    /**
     * The regex expression of the HTTP response code to be cached.
     */
    private String responseCodes = CachingConstants.ANY_RESPONSE_CODE;

    /**
     * The headers to exclude when caching.
     */
    private String headersToExcludeInHash = "";

    /**
     * The protocol type used in caching.
     */
    private String protocolType = CachingConstants.HTTP_PROTOCOL_TYPE;

    /**
     * The http method type that needs to be cached.
     */
    private String hTTPMethodsToCache = CachingConstants.ALL;

    /**
     * This is used to specify whether cache-control headers need to honored. By default false.
     */
    private boolean cacheControlEnabled = CachingConstants.DEFAULT_ENABLE_CACHE_CONTROL;

    /**
     * This variable is used to specify whether an Age header need to included in the cached response.
     */
    private boolean addAgeHeaderEnabled = CachingConstants.DEFAULT_ADD_AGE_HEADER;

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
     * @return Name of the digestGenerator used evaluate hash values.
     */
    public String getDigestGenerator() {
        return digestGenerator;
    }

    /**
     * This method sets the DigestGenerator to evaluate the hash values of incoming messages.
     *
     * @param digestGenerator Name of the digestGenerator to be set to evaluate hash values.
     */
    public void setDigestGenerator(String digestGenerator) {
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

    /**
     * This method gives the local name of the mediator.
     *
     * @return local name of mediator.
     */
    public String getTagLocalName() {
        return CachingConstants.CACHE_LOCAL_NAME;
    }

    /**
     * This method gives the HTTP method that needs to be cached.
     *
     * @return the HTTP method to be cached
     */
    public String getHTTPMethodsToCache() {
        return hTTPMethodsToCache;
    }

    /**
     * This sets the HTTP method that needs to be cached.
     *
     * @param hTTPMethodToCache the HTTP method to be cached
     */
    public void setHTTPMethodsToCache(String hTTPMethodToCache) {
        this.hTTPMethodsToCache = hTTPMethodToCache;
    }

    /**
     * Returns the protocolType of the message.
     *
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
     * Returns the response codes (regex expression).
     *
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
    }

    /**
     * This method gives array of headers that would be excluded when hashing.
     *
     * @return array of headers to exclude from hashing
     */
    public String getHeadersToExcludeInHash() {
        return headersToExcludeInHash;
    }

    /**
     * This method sets the array of headers that would be excluded when hashing.
     *
     * @param headersToExcludeInHash array of headers to exclude from hashing.
     */
    public void setHeadersToExcludeInHash(String headersToExcludeInHash) {
        this.headersToExcludeInHash = headersToExcludeInHash;
    }

    /**
     * This method returns whether cache-control headers need to be honored when caching.
     *
     * @return cacheControlEnabled whether enable cache control or not.
     */
    public boolean isCacheControlEnabled() {
        return cacheControlEnabled;
    }

    /**
     * This method sets whether cache-control headers need to be honored when caching.
     *
     * @param cacheControlEnabled specifies whether cache-control headers need to be honored.
     */
    public void setCacheControlEnabled(boolean cacheControlEnabled) {
        this.cacheControlEnabled = cacheControlEnabled;
    }

    /**
     * This method returns whether an Age header need to be included in the cached response.
     *
     * @return addAgeHeaderEnabled whether include an Age header or not.
     */
    public boolean isAddAgeHeaderEnabled() {
        return addAgeHeaderEnabled;
    }

    /**
     * This method sets whether an Age header need to be included in the cached response.
     *
     * @param addAgeHeaderEnabled specifies whether include an Age header or not.
     */
    public void setAddAgeHeaderEnabled(boolean addAgeHeaderEnabled) {
        this.addAgeHeaderEnabled = addAgeHeaderEnabled;
    }

    /**
     * {@inheritDoc}
     */
    public OMElement serialize(OMElement parent) {
        OMElement cache = fac.createOMElement(CachingConstants.CACHE_LOCAL_NAME, synNS);
        saveTracingState(cache, this);

        if (collector) {
            cache.addAttribute(fac.createOMAttribute(CachingConstants.COLLECTOR_STRING, nullNS, "true"));
        } else {

            cache.addAttribute(fac.createOMAttribute(CachingConstants.COLLECTOR_STRING, nullNS, "false"));

            if (timeout > -1) {
                cache.addAttribute(
                        fac.createOMAttribute(CachingConstants.TIMEOUT_STRING, nullNS, Long.toString(timeout)));
            }

            if (maxMessageSize > -1) {
                cache.addAttribute(fac.createOMAttribute(CachingConstants.MAX_MESSAGE_SIZE_STRING, nullNS,
                                                         Integer.toString(maxMessageSize)));
            }

            if (onCacheHitRef != null) {
                OMElement onCacheHit = fac.createOMElement(CachingConstants.ON_CACHE_HIT_STRING, synNS);
                onCacheHit.addAttribute(fac.createOMAttribute(CachingConstants.SEQUENCE_STRING, nullNS, onCacheHitRef));
                cache.addChild(onCacheHit);
            } else if (getList().size() > 0) {
                OMElement onCacheHit = fac.createOMElement(CachingConstants.ON_CACHE_HIT_STRING, synNS);
                serializeChildren(onCacheHit, getList());
                cache.addChild(onCacheHit);
            }

            OMElement protocolElem = fac.createOMElement(CachingConstants.PROTOCOL_STRING, synNS);
            protocolElem.addAttribute(fac.createOMAttribute(CachingConstants.TYPE_STRING, nullNS, protocolType));
            if (CachingConstants.HTTP_PROTOCOL_TYPE.equals(protocolType)) {

                if (hTTPMethodsToCache != null) {
                    OMElement methodElem = fac.createOMElement(CachingConstants.METHODS_STRING, synNS);
                    methodElem.setText(hTTPMethodsToCache);
                    protocolElem.addChild(methodElem);
                }

                if (headersToExcludeInHash != null) {
                    OMElement headerElem = fac.createOMElement(CachingConstants.HEADERS_TO_EXCLUDE_STRING, synNS);
                    headerElem.setText(headersToExcludeInHash);
                    protocolElem.addChild(headerElem);
                }

                OMElement responseCodesElem = fac.createOMElement(CachingConstants.RESPONSE_CODES_STRING, synNS);
                responseCodesElem.setText(responseCodes);
                protocolElem.addChild(responseCodesElem);

                OMElement enableCacheControlElem = fac.createOMElement(CachingConstants.ENABLE_CACHE_CONTROL_STRING,
                        synNS);
                enableCacheControlElem.setText(String.valueOf(cacheControlEnabled));
                protocolElem.addChild(enableCacheControlElem);

                OMElement includeAgeHeaderElem = fac.createOMElement(CachingConstants.INCLUDE_AGE_HEADER_STRING,
                        synNS);
                includeAgeHeaderElem.setText(String.valueOf(addAgeHeaderEnabled));
                protocolElem.addChild(includeAgeHeaderElem);
            }

            if (digestGenerator != null) {
                OMElement hashGeneratorElem = fac.createOMElement(CachingConstants.HASH_GENERATOR_STRING, synNS);
                hashGeneratorElem.setText(digestGenerator);
                protocolElem.addChild(hashGeneratorElem);
            }

            cache.addChild(protocolElem);

            if (inMemoryCacheSize > -1) {
                OMElement implElem = fac.createOMElement(CachingConstants.IMPLEMENTATION_STRING, synNS);
                implElem.addAttribute(fac.createOMAttribute(CachingConstants.MAX_SIZE_STRING, nullNS,
                                                            Integer.toString(inMemoryCacheSize)));
                cache.addChild(implElem);
            }
        }

        if (parent != null) {
            parent.addChild(cache);
        }

        return cache;
    }

    /**
     * Creates the cache mediator with given configuration XML as OMElement.
     * @param elem OMElement to be converted to cache mediator Object.
     */
    public void build(OMElement elem) {
        OMAttribute collectorAttr = elem.getAttribute(ATT_COLLECTOR);
        if (collectorAttr != null && collectorAttr.getAttributeValue() != null &&
                "true".equals(collectorAttr.getAttributeValue())) {
            collector = true;
        } else {
            collector = false;
            OMAttribute timeoutAttr = elem.getAttribute(ATT_TIMEOUT);
            if (timeoutAttr != null && timeoutAttr.getAttributeValue() != null) {
                this.timeout = Long.parseLong(timeoutAttr.getAttributeValue());
            } else {
                this.timeout = DEFAULT_TIMEOUT;
            }

            OMAttribute maxMessageSizeAttr = elem.getAttribute(ATT_MAX_MSG_SIZE);
            if (maxMessageSizeAttr != null && maxMessageSizeAttr.getAttributeValue() != null) {
                this.maxMessageSize = Integer.parseInt(maxMessageSizeAttr.getAttributeValue());
            }

            OMElement protocolElem = elem.getFirstChildWithName(PROTOCOL_Q);
            if (protocolElem != null) {
                OMAttribute typeAttr = protocolElem.getAttribute(ATT_TYPE);
                if (typeAttr != null &&
                        typeAttr.getAttributeValue() != null) {
                    OMElement hashGeneratorElem = protocolElem.getFirstChildWithName(HASH_GENERATOR_Q);
                    if (hashGeneratorElem != null) {
                        digestGenerator = hashGeneratorElem.getText();
                    }

                    protocolType = typeAttr.getAttributeValue().toUpperCase();
                    if (CachingConstants.HTTP_PROTOCOL_TYPE.equals(protocolType)) {
                        OMElement methodElem = protocolElem.getFirstChildWithName(HTTP_METHODS_TO_CACHE_Q);
                        if (methodElem != null) {
                            hTTPMethodsToCache = methodElem.getText();
                        }
                        OMElement headersToExclude = protocolElem.getFirstChildWithName(
                                HEADERS_TO_EXCLUDE_IN_HASH_Q);
                        if (headersToExclude != null) {
                            headersToExcludeInHash = headersToExclude.getText();
                        }
                        OMElement enableCacheControlElem = protocolElem.getFirstChildWithName(
                                ENABLE_CACHE_CONTROL_Q);
                        cacheControlEnabled = Boolean.parseBoolean(enableCacheControlElem.getText());

                        OMElement addAgeHeaderEnabledElem = protocolElem.getFirstChildWithName(
                                INCLUDE_AGE_HEADER_Q);
                        addAgeHeaderEnabled = Boolean.parseBoolean(addAgeHeaderEnabledElem.getText());
                    }
                    OMElement responseElem = protocolElem.getFirstChildWithName(RESPONSE_CODES_Q);
                    if (responseElem != null) {
                        String responses = responseElem.getText();
                        if (!"".equals(responses) && responses != null) {
                            responseCodes = responses;
                        }
                    }
                }
            }
        }

        OMElement implElem = elem.getFirstChildWithName(IMPLEMENTATION_Q);
        if (implElem != null) {
            OMAttribute sizeAttr = implElem.getAttribute(ATT_SIZE);
            if (sizeAttr != null &&
                    sizeAttr.getAttributeValue() != null) {
                inMemoryCacheSize = Integer.parseInt(sizeAttr.getAttributeValue());
            }
        }
        OMElement onCacheHitElem = elem.getFirstChildWithName(ON_CACHE_HIT_Q);
        if (onCacheHitElem != null) {
            OMAttribute sequenceAttr = onCacheHitElem.getAttribute(ATT_SEQUENCE);
            if (sequenceAttr != null && sequenceAttr.getAttributeValue() != null) {
                this.onCacheHitRef = sequenceAttr.getAttributeValue();
            } else if (onCacheHitElem.getFirstElement() != null) {
                addChildren(onCacheHitElem, this);
            }
        }
    }
}

