/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mediator.cache.util;

import com.google.common.net.HttpHeaders;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.mediator.cache.CachableResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to apply the filter.
 */
public class HttpCachingFilter {

    private HttpCachingFilter() {
    }

    private static final String NO_CACHE_STRING = "no-cache";
    private static final String MAX_AGE_STRING = "max-age";

    public static boolean validateCachedResponse(CachableResponse cachedResponse, MessageContext synCtx) {
        Map<String, Object> httpHeaders = cachedResponse.getHeaderProperties();
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        String eTagValue = null;
        boolean isNoCache = false;
        long maxAge = -1;
        //Read cache-control, max-age and ETag header from cached response.
        if (httpHeaders != null) {
            if (httpHeaders.get(HttpHeaders.ETAG) != null) {
                eTagValue = String.valueOf(httpHeaders.get(HttpHeaders.ETAG));
            }
            if (httpHeaders.get(HttpHeaders.CACHE_CONTROL) != null) {
                String cacheControlHeaderValue = String.valueOf(httpHeaders.get(HttpHeaders.CACHE_CONTROL));
                List<String> cacheControlHeaders = Arrays.asList(cacheControlHeaderValue.split("\\s*,\\s*"));
                for (String cacheControlHeader : cacheControlHeaders) {
                    if (cacheControlHeader.equalsIgnoreCase(NO_CACHE_STRING)) {
                        isNoCache = true;
                    }
                    if (cacheControlHeader.contains(MAX_AGE_STRING)) {
                        maxAge = Long.parseLong(cacheControlHeader.split("=")[1]);
                    }
                }
            }
        }
        //Validate the TTL of the cached response.
        if (maxAge > -1) {
            long responseExpirationTime = cachedResponse.getResponseFetchedTime() + maxAge * 1000;
            if (responseExpirationTime < System.currentTimeMillis()) {
                return true;
            }
        }
        //Validate the response with ETag value.
        if (isNoCache && StringUtils.isNotEmpty(eTagValue)) {
            Map<String, Object> headerProp = new HashMap<>();
            headerProp.put("IF-None-Match", eTagValue);
            msgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headerProp);
            return true;
        }
        return false;
    }

    /**
     * This method sets the Age header.
     *
     * @param cachedResponse cached response to be returned.
     * @param msgCtx messageContext.
     */
    @SuppressWarnings("unchecked")
    public static void setAgeHeader(CachableResponse cachedResponse,
                                    org.apache.axis2.context.MessageContext msgCtx) {
        Map excessHeaders = new MultiValueMap();
        long responseCachedTime = cachedResponse.getResponseFetchedTime();
        long age = Math.abs((responseCachedTime - System.currentTimeMillis())/1000);
        excessHeaders.put(HttpHeaders.AGE, String.valueOf(age));

        msgCtx.setProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS, excessHeaders);
    }

    /**
     * Set the response fetched time in milliseconds.
     *
     * @param headers transport headers
     * @param response response to be cached
     * @throws ParseException throws exception if exception happen while parsing the date
     */
    public static void setResponseCachedTime(Map<String, String> headers, CachableResponse response) throws
            ParseException {
        long responseFetchedTime;
        String fetchedTime;
        if ((fetchedTime = headers.get(HttpHeaders.DATE)) != null) {
            String datePattern = "EEE, dd MMM yyyy HH:mm:ss z";
            SimpleDateFormat format = new SimpleDateFormat(datePattern);
                Date d = format.parse(fetchedTime);
                responseFetchedTime = d.getTime();
        } else {
            responseFetchedTime = System.currentTimeMillis();
        }
        response.setResponseFetchedTime(responseFetchedTime);
    }

    /**
     * This method returns whether no-store header exists in the response.
     *
     * @param synCtx messageContext with the transport headers.
     * @return whether no-store exists or not.
     */
    @SuppressWarnings("unchecked")
    public static boolean isNoStore(MessageContext synCtx) {
        Map<String, String> headers;
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        headers = (Map<String, String>) msgCtx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String cacheControlHeaderValue = headers.get(HttpHeaders.CACHE_CONTROL);

        return StringUtils.isNotEmpty(cacheControlHeaderValue) && cacheControlHeaderValue.contains("no-store");
    }
}
