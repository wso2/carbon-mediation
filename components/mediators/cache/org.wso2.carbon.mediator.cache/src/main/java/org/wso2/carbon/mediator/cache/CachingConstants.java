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

import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.cache.digest.DOMHASHGenerator;
import org.wso2.carbon.mediator.cache.digest.DigestGenerator;
import javax.xml.namespace.QName;

/**
 * This class holds the mediator caching related constants
 */
public final class CachingConstants {

	/** String key to store the the request hash in the message contetx */
	public static final String REQUEST_HASH = "requestHash";

	/** String key to store the cached response in the message context */
	public static final String CACHED_OBJECT = "CachableResponse";

	/** String key to store the cache object */
	public static final String CACHE_MANAGER = "cacheManager";

	/** QName of the cache mediator which will be used by the module */
	public static final QName CACHE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "cache");

	/** Default DigestGenerator for the caching impl */
	public static final DigestGenerator DEFAULT_XML_IDENTIFIER = new DOMHASHGenerator();

	/** per-mediator cache scope attribute value */
	public static final String SCOPE_PER_MEDIATOR = "per-mediator";

	/** per-host cache scope attribute value */
	public static final String SCOPE_PER_HOST = "per-host";

	/** distributed cache scope attribute value */
	public static final String SCOPE_DISTRIBUTED = "distributed";

	/** in memory cache scope attribute value */
	public static final String TYPE_MEMORY = "memory";

	/** disk based cache scope attribute value */
	public static final String TYPE_DISK = "disk";

	/** Default cache size (in-memory) */
	public static final int DEFAULT_CACHE_SIZE = 1000;

	/** Primary cache name */
	public static final String MEDIATOR_CACHE = "mediatorCache";

	/** Default cache invalidation time */
	public static final Integer CACHE_INVALIDATION_TIME = 1000 * 24 * 3600;

}
