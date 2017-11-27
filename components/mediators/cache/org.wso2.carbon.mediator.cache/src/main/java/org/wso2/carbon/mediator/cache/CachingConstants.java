/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.cache.digest.DigestGenerator;
import org.wso2.carbon.mediator.cache.digest.HttpRequestHashGenerator;

import javax.xml.namespace.QName;

/**
 * This has the common constants used in the classes related to the cache mediator implementation.
 */
public class CachingConstants {
    /**
     * Default DigestGenerator for the caching impl.
     */
    public static final DigestGenerator DEFAULT_HASH_GENERATOR = new HttpRequestHashGenerator();

    /**
     * QName of the cache mediator which will be used by the module.
     */
    public static final QName CACHE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                                                  CachingConstants.CACHE_LOCAL_NAME);

    /**
     * Local name of the cache mediator which will be used by the module.
     */
    public static final String CACHE_LOCAL_NAME = "cache";

    /**
     * This holds the default timeout of the mediator cache.
     */
    public static final long DEFAULT_TIMEOUT = 5000;

    /**
     * To represent all-values in certain places in the cache mediator
     */
    public static final String ALL = "*";

    /**
     * The HTTP protocol.
     */
    public static final String HTTP_PROTOCOL_TYPE = "HTTP";

    /**
     * The regex for the 2xx response code.
     */
    public static final String ANY_RESPONSE_CODE = ".*";

    /**
     * String key to store the the request hash in the message context.
     */
    public static final String REQUEST_HASH = "requestHash";

    /**
     * String key to store the cached response in the message context.
     */
    public static final String CACHED_OBJECT = "CachableResponse";

    /**
     * The the header that would be used to return the hashed value to invalidate this value.
     */
    public static final String CACHE_KEY = "cacheKey";

    /**
     * The default size for the maxSize and maxMessageSize
     */
    public static final int DEFAULT_SIZE = -1;

    /**
     * Following names represent the local names used in QNames in MediatorFactory, Serializer and the UI
     * CacheMediator.
     */
    public static final String TIMEOUT_STRING = "timeout";
    public static final String COLLECTOR_STRING = "collector";
    public static final String MAX_MESSAGE_SIZE_STRING = "maxMessageSize";
    public static final String ON_CACHE_HIT_STRING = "onCacheHit";
    public static final String SEQUENCE_STRING = "sequence";
    public static final String PROTOCOL_STRING = "protocol";
    public static final String METHODS_STRING = "methods";
    public static final String HEADERS_TO_EXCLUDE_STRING = "headersToExcludeInHash";
    public static final String TYPE_STRING = "type";
    public static final String RESPONSE_CODES_STRING = "responseCodes";
    public static final String HASH_GENERATOR_STRING = "hashGenerator";
    public static final String IMPLEMENTATION_STRING = "implementation";
    public static final String MAX_SIZE_STRING = "maxSize";

}
