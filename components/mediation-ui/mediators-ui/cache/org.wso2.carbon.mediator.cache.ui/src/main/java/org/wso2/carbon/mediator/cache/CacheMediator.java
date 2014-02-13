/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediator.cache;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.caching.core.CachingConstants;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class CacheMediator extends AbstractListMediator {

    private static final QName ATT_ID = new QName("id");
    private static final QName ATT_COLLECTOR = new QName("collector");
    private static final QName ATT_HASH_GENERATOR = new QName("hashGenerator");
    private static final QName ATT_MAX_MSG_SIZE = new QName("maxMessageSize");
    private static final QName ATT_TIMEOUT = new QName("timeout");
    private static final QName ATT_SCOPE = new QName("scope");
    private static final QName ATT_SEQUENCE = new QName("sequence");
    private static final QName ATT_TYPE = new QName("type");
    private static final QName ATT_SIZE = new QName("maxSize");
    private static final QName ON_CACHE_HIT_Q =
        new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "onCacheHit");
    private static final QName IMPLEMENTATION_Q =
        new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "implementation");
    private static final long DEFAULT_TIMEOUT = 5000L;
    private static final int DEFAULT_DISK_CACHE_SIZE = 200;

    private String id = null;
    private String scope = CachingConstants.SCOPE_PER_HOST;
    private boolean collector = false;
    private String digestGenerator = CachingConstants.DEFAULT_XML_IDENTIFIER.getClass().toString();
    private int inMemoryCacheSize = CachingConstants.DEFAULT_CACHE_SIZE;
    private int diskCacheSize = 0;
    private long timeout = 0L;
    private String onCacheHitRef = null;
    private int maxMessageSize = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isCollector() {
        return collector;
    }

    public void setCollector(boolean collector) {
        this.collector = collector;
    }

    public String getDigestGenerator() {
        return digestGenerator;
    }

    public void setDigestGenerator(String digestGenerator) {
        this.digestGenerator = digestGenerator;
    }

    public int getInMemoryCacheSize() {
        return inMemoryCacheSize;
    }

    public void setInMemoryCacheSize(int inMemoryCacheSize) {
        this.inMemoryCacheSize = inMemoryCacheSize;
    }

    public int getDiskCacheSize() {
        return diskCacheSize;
    }

    public void setDiskCacheSize(int diskCacheSize) {
        this.diskCacheSize = diskCacheSize;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getOnCacheHitRef() {
        return onCacheHitRef;
    }

    public void setOnCacheHitRef(String onCacheHitRef) {
        this.onCacheHitRef = onCacheHitRef;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    public String getTagLocalName() {
        return "cache";
    }

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
                cache.addAttribute(fac.createOMAttribute("hashGenerator", nullNS,
                    digestGenerator));
            }

            if (timeout != 0) {
                cache.addAttribute(
                    fac.createOMAttribute("timeout", nullNS, Long.toString(timeout)));
            }

            if (maxMessageSize != 0) {
                cache.addAttribute(
                    fac.createOMAttribute("maxMessageSize", nullNS,
                        Integer.toString(maxMessageSize)));
            }

            if (onCacheHitRef != null) {
                OMElement onCacheHit = fac.createOMElement("onCacheHit", synNS);
                onCacheHit.addAttribute(
                    fac.createOMAttribute("sequence", nullNS, onCacheHitRef));
                cache.addChild(onCacheHit);
            } else if (getList().size() > 0) {
                OMElement onCacheHit = fac.createOMElement("onCacheHit", synNS);
                serializeChildren(onCacheHit, getList());
                cache.addChild(onCacheHit);
            }

            if (inMemoryCacheSize != 0) {
                OMElement implElem = fac.createOMElement("implementation", synNS);
                implElem.addAttribute(fac.createOMAttribute("type", nullNS, "memory"));
                implElem.addAttribute(fac.createOMAttribute("maxSize", nullNS,
                    Integer.toString(inMemoryCacheSize)));
                cache.addChild(implElem);
            }

            if (diskCacheSize != 0) {
                OMElement implElem = fac.createOMElement("implementation", synNS);
                implElem.addAttribute(fac.createOMAttribute("type", nullNS, "disk"));
                implElem.addAttribute(fac.createOMAttribute("maxSize", nullNS,
                    Integer.toString(diskCacheSize)));
                cache.addChild(implElem);
            }
        }

        if (parent != null) {
            parent.addChild(cache);
        }

        return cache;
    }

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

            for (Iterator itr = elem.getChildrenWithName(IMPLEMENTATION_Q); itr.hasNext();) {
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
                        throw new MediatorException("unknown implementation type for the Cache mediator");
                    }
                }
            }
        }
    }

    private boolean isValidScope(String scope, String id) {
        if (CachingConstants.SCOPE_PER_HOST.equals(scope)) {
            return true;
        } else if (CachingConstants.SCOPE_PER_MEDIATOR.equals(scope)) {
            if (id != null) {
                return true;
            } else {
                throw new MediatorException("Id is required for a cache wirth scope : " + scope);                
            }
        } else if (CachingConstants.SCOPE_DISTRIBUTED.equals(scope)) {
            throw new MediatorException("Scope distributed is not supported yet by the Cache mediator");
        } else {
            throw new MediatorException("Unknown scope " + scope + " for the Cache mediator");
        }
    }

}
