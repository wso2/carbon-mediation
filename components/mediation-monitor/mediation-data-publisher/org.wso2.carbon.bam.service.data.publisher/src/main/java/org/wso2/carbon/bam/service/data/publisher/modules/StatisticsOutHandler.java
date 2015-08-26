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
package org.wso2.carbon.bam.service.data.publisher.modules;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.PublisherUtil;
import org.wso2.carbon.bam.service.data.publisher.conf.EventConfigNStreamDef;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.data.Event;
import org.wso2.carbon.bam.service.data.publisher.data.EventData;
import org.wso2.carbon.bam.service.data.publisher.data.PublishData;
import org.wso2.carbon.bam.service.data.publisher.internal.StatisticsServiceComponent;
import org.wso2.carbon.bam.service.data.publisher.publish.EventPublisher;
import org.wso2.carbon.bam.service.data.publisher.publish.ServiceAgentUtil;
import org.wso2.carbon.bam.service.data.publisher.util.TenantEventConfigData;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.statistics.StatisticsConstants;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class StatisticsOutHandler extends AbstractHandler {

    private static Log log = LogFactory.getLog(StatisticsOutHandler.class);

    @Override
    public Handler.InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        SystemStatisticsUtil systemStatisticsUtil;
        SystemStatistics systemStatistics;

        try {
            int tenantID = PublisherUtil.getTenantId(messageContext);
            Map<Integer, EventConfigNStreamDef> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
            EventConfigNStreamDef eventingConfigData = tenantSpecificEventConfig.get(tenantID);

            //Check service stats enable -- if true -- go
            if (eventingConfigData != null && eventingConfigData.isServiceStatsEnable()) {

                systemStatisticsUtil = StatisticsServiceComponent.getSystemStatisticsUtil();
                systemStatistics = systemStatisticsUtil.getSystemStatistics(messageContext);
                AxisOperation axisOperation = messageContext.getAxisOperation();

                AxisService axisService = messageContext.getAxisService();
                if (axisService == null || SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup()) ||
                    axisService.isClientSide()) {
                    return Handler.InvocationResponse.CONTINUE;
                }

                MessageContext inMessageContext = MessageContext.getCurrentMessageContext();

                //If already set in the activity handlers get it or create new publish data
/*
                PublishData publishData = (PublishData) messageContext.getProperty(
                        BAMDataPublisherConstants.PUBLISH_DATA);

                EventData eventData;
                if (publishData != null) {
                    eventData = publishData.getEventData();
                } else {
                    publishData = new PublishData();
                    eventData = new EventData();
                    Date date = new Date();
                    Timestamp timestamp = new Timestamp(date.getTime());
                    eventData.setTimestamp(timestamp);
                    if (axisOperation != null) {
                        eventData.setOperationName(axisOperation.getName().getLocalPart());
                    } else {
                        eventData.setOperationName(null);
                    }

                    if (axisService != null) {
                        eventData.setServiceName(messageContext.getAxisService().getName());
                    } else {
                        eventData.setServiceName(null);
                    }

                    //This is a hack for setting message id when sending request to a non-existing operation.
                    if (eventingConfigData.isMsgDumpingEnable() && axisService != null &&
                        axisOperation == null) {
                        eventData.setOutMessageId(new ActivityOutHandler().getUniqueId());
                    }

                }
*/

                PublishData publishData = new PublishData();
                EventData eventData = new EventData();
/*                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                eventData.setTimestamp(timestamp);*/
                if (axisOperation != null) {
                    eventData.setOperationName(axisOperation.getName().getLocalPart());
                } else {
                    eventData.setOperationName(null);
                }

                if (axisService != null) {
                    eventData.setServiceName(messageContext.getAxisService().getName());
                } else {
                    eventData.setServiceName(null);
                }

                //This is a hack for setting message id when sending request to a non-existing operation.
/*                if (eventingConfigData.isMsgDumpingEnable() && axisService != null &&
                    axisOperation == null) {
                    eventData.setOutMessageId(new ActivityOutHandler().getUniqueId());
                }*/


                // Skip resetting same info if already set by activity in/out handlers
/*                if (!eventingConfigData.isMsgDumpingEnable()) {

                    Timestamp timestamp = null;
                    if (inMessageContext != null) {
                        timestamp = new Timestamp(Long.parseLong(inMessageContext.getProperty(
                                StatisticsConstants.REQUEST_RECEIVED_TIME).toString()));
                        Object requestProperty = inMessageContext.getProperty(
                                HTTPConstants.MC_HTTP_SERVLETREQUEST);
                        ServiceAgentUtil.extractInfoFromHttpHeaders(eventData, requestProperty);
                    } else {
                        Date date = new Date();
                        timestamp = new Timestamp(date.getTime());
                    }

                    eventData.setTimestamp(timestamp);

                }*/

                Timestamp timestamp = null;
                if (inMessageContext != null) {
                    timestamp = new Timestamp(Long.parseLong(inMessageContext.getProperty(
                            StatisticsConstants.REQUEST_RECEIVED_TIME).toString()));
                    Object requestProperty = inMessageContext.getProperty(
                            HTTPConstants.MC_HTTP_SERVLETREQUEST);
                    ServiceAgentUtil.extractInfoFromHttpHeaders(eventData, requestProperty);
                } else {
                    Date date = new Date();
                    timestamp = new Timestamp(date.getTime());
                }

                eventData.setTimestamp(timestamp);

                eventData.setSystemStatistics(systemStatistics);

                publishData.setEventData(eventData);

                BAMServerInfo bamServerInfo = ServiceAgentUtil.addBAMServerInfo(eventingConfigData);
                publishData.setBamServerInfo(bamServerInfo);

                Event event = ServiceAgentUtil.makeEventList(publishData, eventingConfigData);
                EventPublisher publisher = new EventPublisher();
                publisher.publish(event, eventingConfigData);
            }
        } catch (Throwable ignore) {
            log.error("Error at SystemStatisticsOutHandler. " +
                      "But continuing message processing for message id: " +
                      messageContext.getMessageID(), ignore);
        }

        return Handler.InvocationResponse.CONTINUE;
    }


}
