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
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Object of this class is used to store mediator information in UI side.
 */
public class CacheMediator extends AbstractListMediator {

	/** QName of the ID of cache configuration */
	private static final QName ATT_ID = new QName("id");

	/** QName of the collector */
	private static final QName ATT_COLLECTOR = new QName("collector");

	/** QName of the digest generator */
	private static final QName ATT_HASH_GENERATOR = new QName("hashGenerator");

	/** QName of the maximum message size */
	private static final QName ATT_MAX_MSG_SIZE = new QName("maxMessageSize");

	/** QName of the timeout */
	private static final QName ATT_TIMEOUT = new QName("timeout");

	/** QName of the cache scope */
	private static final QName ATT_SCOPE = new QName("scope");

	/** QName of the mediator sequence  */
	private static final QName ATT_SEQUENCE = new QName("sequence");

	/** QName of the implementation type */
	private static final QName ATT_TYPE = new QName("type");

	/** QName of the maximum message size */
	private static final QName ATT_SIZE = new QName("maxSize");

	/** QName of the onCacheHit mediator sequence reference */
	private static final QName ON_CACHE_HIT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "onCacheHit");

	/** QName of the cache implementation */
	private static final QName IMPLEMENTATION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "implementation");

	/** This holds the default timeout of the mediator cache */
	private static final long DEFAULT_TIMEOUT = 5000L;

	/** This holds the default disk cache size used in cache mediator */
	private static final int DEFAULT_DISK_CACHE_SIZE = 200;

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
	private String digestGenerator = CachingConstants.DEFAULT_XML_IDENTIFIER.getClass().toString();

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
	 * The reference to the onCacheHit sequence to be executed when an incoming message is identified as an
	 * equivalent to a previously received message based on the value defined for the Hash Generator field.
	 */
	private String onCacheHitRef = null;

	/**
	 * The maximum size of the messages to be cached. This is specified in bytes.
	 */
	private int maxMessageSize = 0;

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
		return "cache";
	}

	/**
	 * Creates XML representation of the cache mediator as an OMElement
	 *
	 * @param parent OMElement which take child as created OMElement
	 */
	public OMElement serialize(OMElement parent) {
		OMElement cache = fac.createOMElement("cache", synNS);
		saveTracingState(cache, this);

		if (id != null) {
			cache.addAttribute(fac.createOMAttribute("id", nullNS, id));
		}

		if (scope != null) {
			cache.addAttribute(fac.createOMAttribute("scope", nullNS, scope));
		}

		if (collector) {
			cache.addAttribute(fac.createOMAttribute("collector", nullNS, "true"));
		} else {

			cache.addAttribute(fac.createOMAttribute("collector", nullNS, "false"));

			if (digestGenerator != null) {
				cache.addAttribute(fac.createOMAttribute("hashGenerator", nullNS, digestGenerator));
			}

			if (timeout != 0) {
				cache.addAttribute(fac.createOMAttribute("timeout", nullNS, Long.toString(timeout)));
			}

			if (maxMessageSize != 0) {
				cache.addAttribute(fac.createOMAttribute("maxMessageSize", nullNS, Integer.toString(maxMessageSize)));
			}

			if (onCacheHitRef != null) {
				OMElement onCacheHit = fac.createOMElement("onCacheHit", synNS);
				onCacheHit.addAttribute(fac.createOMAttribute("sequence", nullNS, onCacheHitRef));
				cache.addChild(onCacheHit);
			} else if (getList().size() > 0) {
				OMElement onCacheHit = fac.createOMElement("onCacheHit", synNS);
				serializeChildren(onCacheHit, getList());
				cache.addChild(onCacheHit);
			}

			if (inMemoryCacheSize != 0) {
				OMElement implElem = fac.createOMElement("implementation", synNS);
				implElem.addAttribute(fac.createOMAttribute("type", nullNS, "memory"));
				implElem.addAttribute(fac.createOMAttribute("maxSize", nullNS, Integer.toString(inMemoryCacheSize)));
				cache.addChild(implElem);
			}

			if (diskCacheSize != 0) {
				OMElement implElem = fac.createOMElement("implementation", synNS);
				implElem.addAttribute(fac.createOMAttribute("type", nullNS, "disk"));
				implElem.addAttribute(fac.createOMAttribute("maxSize", nullNS, Integer.toString(diskCacheSize)));
				cache.addChild(implElem);
			}
		}

		if (parent != null) {
			parent.addChild(cache);
		}

		return cache;
	}

	/**
	 * Creates the cache mediator with given configuration XML as OMElement
	 *
	 * @param elem OMElement to be converted to cache mediator Object.
	 */
	public void build(OMElement elem) {
		OMAttribute idAttr = elem.getAttribute(ATT_ID);
		if (idAttr != null && idAttr.getAttributeValue() != null) {
			this.id = idAttr.getAttributeValue();
		}

		OMAttribute scopeAttr = elem.getAttribute(ATT_SCOPE);
		if (scopeAttr != null && scopeAttr.getAttributeValue() != null &&
		    isValidScope(scopeAttr.getAttributeValue(), this.id)) {
			this.scope = scopeAttr.getAttributeValue();
		} else {
			this.scope = CachingConstants.SCOPE_PER_HOST;
		}

		OMAttribute collectorAttr = elem.getAttribute(ATT_COLLECTOR);
		if (collectorAttr != null && collectorAttr.getAttributeValue() != null &&
		    "true".equals(collectorAttr.getAttributeValue())) {
			collector = true;
		} else {
			collector = false;
			OMAttribute hashGeneratorAttr = elem.getAttribute(ATT_HASH_GENERATOR);
			if (hashGeneratorAttr != null && hashGeneratorAttr.getAttributeValue() != null) {
				this.digestGenerator = hashGeneratorAttr.getAttributeValue();
			}

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

			OMElement onCacheHitElem = elem.getFirstChildWithName(ON_CACHE_HIT_Q);
			if (onCacheHitElem != null) {
				OMAttribute sequenceAttr = onCacheHitElem.getAttribute(ATT_SEQUENCE);
				if (sequenceAttr != null && sequenceAttr.getAttributeValue() != null) {
					this.onCacheHitRef = sequenceAttr.getAttributeValue();
				} else if (onCacheHitElem.getFirstElement() != null) {
					addChildren(onCacheHitElem, this);
				}
			}

			for (Iterator itr = elem.getChildrenWithName(IMPLEMENTATION_Q); itr.hasNext(); ) {
				OMElement implElem = (OMElement) itr.next();
				OMAttribute typeAttr = implElem.getAttribute(ATT_TYPE);
				OMAttribute sizeAttr = implElem.getAttribute(ATT_SIZE);
				if (typeAttr != null && typeAttr.getAttributeValue() != null) {
					String type = typeAttr.getAttributeValue();
					if (CachingConstants.TYPE_MEMORY.equals(type) && sizeAttr != null &&
					    sizeAttr.getAttributeValue() != null) {
						inMemoryCacheSize = Integer.parseInt(sizeAttr.getAttributeValue());
					} else if (CachingConstants.TYPE_DISK.equals(type)) {
						if (sizeAttr != null && sizeAttr.getAttributeValue() != null) {
							this.diskCacheSize = Integer.parseInt(sizeAttr.getAttributeValue());
						} else {
							this.diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
						}
					} else {
						throw new MediatorException("unknown implementation type for the Cache mediator as there is " +
						                            "no type called " + typeAttr.getAttributeValue());
					}
				}
			}
		}
	}

	/**
	 * Checks the validity of the provided cache scope in cache mediator configuration
	 *
	 * @param scope value of the scope attribute parsed in configuration
	 * @param id    value of the id attribute parsed in configuration
	 * @return boolean value whether the scope is valid or not
	 */
	private boolean isValidScope(String scope, String id) {
		if (CachingConstants.SCOPE_PER_HOST.equals(scope)) {
			return true;
		} else if (CachingConstants.SCOPE_PER_MEDIATOR.equals(scope)) {
			if (id != null) {
				return true;
			} else {
				throw new MediatorException("Id is required for a cache with scope : " + scope);
			}
		} else if (CachingConstants.SCOPE_DISTRIBUTED.equals(scope)) {
			return true;
		} else {
			throw new MediatorException("Unknown scope " + scope + " for the Cache mediator");
		}
	}

}
