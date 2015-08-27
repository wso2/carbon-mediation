/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.service.data.publisher.publish;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.PublisherUtil;
import org.wso2.carbon.bam.service.data.publisher.conf.EventConfigNStreamDef;
import org.wso2.carbon.bam.service.data.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.conf.Property;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.Event;
import org.wso2.carbon.bam.service.data.publisher.data.EventData;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.util.StatisticsType;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceAgentUtil {

    private static Log log = LogFactory.getLog(ServiceAgentUtil.class);

    private static Map<String,EventPublisherConfig> eventPublisherConfigMap =
            new HashMap<String, EventPublisherConfig>();

    private static boolean isPublishingEnabled = false;

    public static void setPublishingEnabled(boolean isPublishingEnabled) {
        ServiceAgentUtil.isPublishingEnabled = isPublishingEnabled;
    }

    public static boolean getPublishingEnabled() {
        return isPublishingEnabled;
    }

    public static EventPublisherConfig getEventPublisherConfig(String key) {
        return eventPublisherConfigMap.get(key);
    }

    public static Map<String, EventPublisherConfig> getEventPublisherConfigMap() {
        return eventPublisherConfigMap;
    }

    public static void removeExistingEventPublisherConfigValue(String key) {
        if (eventPublisherConfigMap != null) {
            eventPublisherConfigMap.put(key, null);
        }
    }

    public static Event makeEventList(PublishData publishData,
                                      EventConfigNStreamDef eventingConfigData) {

        EventData event = publishData.getEventData();

        List<Object> correlationData = new ArrayList<Object>();
        List<Object> metaData = new ArrayList<Object>();
        List<Object> eventData = new ArrayList<Object>();

        StatisticsType statisticsType = findTheStatisticType(event);

        addCommonEventData(event, eventData);
        addStatisticEventData(event, eventData);
        addStatisticsMetaData(event,metaData);



        addPropertiesAsMetaData(eventingConfigData, metaData);

        Event publishEvent = new Event();
        publishEvent.setCorrelationData(correlationData);
        publishEvent.setMetaData(metaData);
        publishEvent.setEventData(eventData);
        publishEvent.setStatisticsType(statisticsType);

        return publishEvent;
    }

    private static void addPropertiesAsMetaData(EventConfigNStreamDef eventingConfigData,
                                                List<Object> metaData) {
        Property[] properties = eventingConfigData.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && !property.getKey().isEmpty()) {
                    metaData.add(property.getValue());
                }
            }
        }
    }

    private static StatisticsType findTheStatisticType(EventData event) {
        StatisticsType statisticsType = null;
        if (event.getSystemStatistics() != null) {
            statisticsType = StatisticsType.SERVICE_STATS;
        }
        return statisticsType;
    }

    public static StatisticsType findTheStatisticType(EventingConfigData eventingConfigData) {
        StatisticsType statisticsType = null;
        if (eventingConfigData.isServiceStatsEnable()) {
            statisticsType = StatisticsType.SERVICE_STATS;
        }
        return statisticsType;
    }

    private static void addCommonEventData(EventData event, List<Object> eventData) {
        eventData.add(event.getServiceName());
        eventData.add(event.getOperationName());
        eventData.add(event.getTimestamp().getTime());
    }

    private static void addStatisticEventData(EventData event, List<Object> eventData) {
        SystemStatistics systemStatistics = event.getSystemStatistics();
        eventData.add(systemStatistics.getCurrentInvocationResponseTime());
        eventData.add(systemStatistics.getCurrentInvocationRequestCount());
        eventData.add(systemStatistics.getCurrentInvocationResponseCount());
        eventData.add(systemStatistics.getCurrentInvocationFaultCount());
    }

    private static void addStatisticsMetaData(EventData event, List<Object> metaData) {
        metaData.add(event.getRequestURL());
        metaData.add(event.getRemoteAddress());
        metaData.add(event.getContentType());
        metaData.add(event.getUserAgent());
        // adding server host or more correctly monitored server url
        metaData.add(PublisherUtil.getHostAddress());

        metaData.add(event.getReferer());
    }

    public static void extractInfoFromHttpHeaders(EventData eventData, Object requestProperty) {

        if (requestProperty instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) requestProperty;
            eventData.setRequestURL(httpServletRequest.getRequestURL().toString());
            eventData.setRemoteAddress(PublisherUtil.getHostAddress());
            eventData.setContentType(httpServletRequest.getContentType());
            eventData.setUserAgent(httpServletRequest.getHeader(
                    BAMDataPublisherConstants.HTTP_HEADER_USER_AGENT));
//            eventData.setHost(httpServletRequest.getHeader(
//                    BAMDataPublisherConstants.HTTP_HEADER_HOST));
            eventData.setReferer(httpServletRequest.getHeader(
                    BAMDataPublisherConstants.HTTP_HEADER_REFERER));
        }

    }

    public static BAMServerInfo addBAMServerInfo(EventConfigNStreamDef eventingConfigData) {
        BAMServerInfo bamServerInfo = new BAMServerInfo();
        bamServerInfo.setBamServerURL(eventingConfigData.getUrl());
        bamServerInfo.setBamUserName(eventingConfigData.getUserName());
        bamServerInfo.setBamPassword(eventingConfigData.getPassword());
        return bamServerInfo;
    }
}
