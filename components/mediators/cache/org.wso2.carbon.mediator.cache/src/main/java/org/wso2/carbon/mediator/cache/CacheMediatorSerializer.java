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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.MediatorSerializer;
import org.apache.synapse.config.xml.MediatorSerializerFinder;

import java.util.List;

/**
 * Serializes the given Cache mediator to an OMElement.
 */
public class CacheMediatorSerializer extends AbstractMediatorSerializer {


    /**
     * Serializes the cache mediator into XML element.
     *
     * @param mediator mediator to be serialized
     * @return the OMElement to which the serialization should be attached
     */
    protected OMElement serializeSpecificMediator(Mediator mediator) {
        if (!(mediator instanceof CacheMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + mediator.getType());
        }

        CacheMediator cacheMediator = (CacheMediator) mediator;
        OMElement cacheElem = fac.createOMElement(CachingConstants.CACHE_LOCAL_NAME, synNS);
        saveTracingState(cacheElem, mediator);

        if (cacheMediator.isCollector()) {
            cacheElem.addAttribute(fac.createOMAttribute(CachingConstants.COLLECTOR_STRING, nullNS, "true"));
        } else {
            cacheElem.addAttribute(fac.createOMAttribute(CachingConstants.COLLECTOR_STRING, nullNS, "false"));

            if (cacheMediator.getTimeout() > -1) {
                cacheElem.addAttribute(
                        fac.createOMAttribute(CachingConstants.TIMEOUT_STRING, nullNS,
                                              Long.toString(cacheMediator.getTimeout())));
            }

            if (cacheMediator.getMaxMessageSize() > -1) {
                cacheElem.addAttribute(
                        fac.createOMAttribute(CachingConstants.MAX_MESSAGE_SIZE_STRING, nullNS,
                                              Integer.toString(cacheMediator.getMaxMessageSize())));
            }

            OMElement onCacheHit;
            if (cacheMediator.getOnCacheHitRef() != null) {
                onCacheHit = fac.createOMElement(CachingConstants.ON_CACHE_HIT_STRING, synNS);
                onCacheHit.addAttribute(
                        fac.createOMAttribute(CachingConstants.SEQUENCE_STRING, nullNS,
                                              cacheMediator.getOnCacheHitRef()));
                cacheElem.addChild(onCacheHit);
            } else if (cacheMediator.getOnCacheHitSequence() != null) {
                onCacheHit = fac.createOMElement(CachingConstants.ON_CACHE_HIT_STRING, synNS);
                serializeChildren(onCacheHit, cacheMediator.getOnCacheHitSequence().getList());
                cacheElem.addChild(onCacheHit);
            }

            OMElement protocolElem = fac.createOMElement(CachingConstants.PROTOCOL_STRING, synNS);
            protocolElem.addAttribute(
                    fac.createOMAttribute(CachingConstants.TYPE_STRING, nullNS, cacheMediator.getProtocolType()));
            if (CachingConstants.HTTP_PROTOCOL_TYPE.equals(cacheMediator.getProtocolType())) {

                String[] methods = cacheMediator.getHTTPMethodsToCache();
                if (!(methods.length == 0 && methods[0].isEmpty())) {
                    StringBuilder method = new StringBuilder();
                    for (int i = 0; i < methods.length; i++) {
                        if (i != methods.length - 1) {
                            method.append(methods[i]).append(", ");
                        } else {
                            method.append(methods[i]);
                        }
                    }
                    OMElement methodElem = fac.createOMElement(CachingConstants.METHODS_STRING, synNS);
                    methodElem.setText(method.toString());
                    protocolElem.addChild(methodElem);
                }

                String[] headers = cacheMediator.getHeadersToExcludeInHash();
                if (!(headers.length == 0 && headers[0].isEmpty())) {
                    StringBuilder header = new StringBuilder();
                    for (int i = 0; i < headers.length; i++) {
                        if (i != headers.length - 1) {
                            header.append(headers[i]).append(", ");
                        } else {
                            header.append(headers[i]);
                        }
                    }
                    OMElement headerElem = fac.createOMElement(CachingConstants.HEADERS_TO_EXCLUDE_STRING, synNS);
                    headerElem.setText(header.toString());
                    protocolElem.addChild(headerElem);
                }

                String responseCodes = cacheMediator.getResponseCodes();
                OMElement responseCodesElem = fac.createOMElement(CachingConstants.RESPONSE_CODES_STRING, synNS);
                responseCodesElem.setText(responseCodes);
                protocolElem.addChild(responseCodesElem);

            }

            if (cacheMediator.getDigestGenerator() != null) {
                OMElement hashGeneratorElem = fac.createOMElement(CachingConstants.HASH_GENERATOR_STRING, synNS);
                hashGeneratorElem.setText(cacheMediator.getDigestGenerator().getClass().getName());
                protocolElem.addChild(hashGeneratorElem);
            }

            cacheElem.addChild(protocolElem);

            if (cacheMediator.getInMemoryCacheSize() > -1) {
                OMElement implElem = fac.createOMElement(CachingConstants.IMPLEMENTATION_STRING, synNS);
                implElem.addAttribute(fac.createOMAttribute(CachingConstants.MAX_SIZE_STRING, nullNS,
                                                            Integer.toString(cacheMediator.getInMemoryCacheSize())));
                cacheElem.addChild(implElem);
            }
        }
        return cacheElem;
    }

    /**
     * {@inheritDoc}
     */
    public String getMediatorClassName() {
        return CacheMediator.class.getName();
    }

    /**
     * Creates XML representation of the child mediators.
     *
     * @param parent The mediator for which the XML representation child should be attached
     * @param list   The mediators list for which the XML representation should be created
     */
    protected void serializeChildren(OMElement parent, List<Mediator> list) {
        for (Mediator child : list) {
            MediatorSerializer medSer = MediatorSerializerFinder.getInstance().getSerializer(child);
            if (medSer != null) {
                medSer.serializeMediator(parent, child);
            } else {
                handleException("Unable to find a serializer for mediator : " + child.getType());
            }
        }
    }
}
