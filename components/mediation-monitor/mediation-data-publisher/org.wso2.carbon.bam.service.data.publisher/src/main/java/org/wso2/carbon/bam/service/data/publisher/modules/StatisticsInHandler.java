package org.wso2.carbon.bam.service.data.publisher.modules;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
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
import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;


public class StatisticsInHandler extends AbstractHandler {

    private static Log log = LogFactory.getLog(StatisticsInHandler.class);

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        if (messageContext.getFLOW() != MessageContext.IN_FLOW &&
            messageContext.getFLOW() != MessageContext.IN_FAULT_FLOW) {
            log.error("InOnlyMEPHandler not deployed in IN/IN_FAULT flow. Flow: " +
                      messageContext.getFLOW());
            return InvocationResponse.CONTINUE;
        }

        return Handler.InvocationResponse.CONTINUE;
    }


    public void flowComplete(MessageContext messageContext) {
        if (messageContext.getEnvelope() == null) {
            return;
        }
        AxisService axisService = messageContext.getAxisService();
        if (axisService == null ||
            SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup()) ||
            axisService.isClientSide()) {
            return;
        }

        //We can take the statistics from StatisticOutHandler.
        if (!isInOnlyMEP(messageContext)) {
            return;
        }

        SystemStatisticsUtil systemStatisticsUtil;
        SystemStatistics systemStatistics;
        OperationStatistics operationStatistics;

        try {
            int tenantID = PublisherUtil.getTenantId(messageContext);

            Map<Integer, EventConfigNStreamDef> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
            EventConfigNStreamDef eventingConfigData = tenantSpecificEventConfig.get(tenantID);

            //Check service stats enable -- if true -- go
            if (eventingConfigData != null && eventingConfigData.isServiceStatsEnable()) {

                systemStatisticsUtil = StatisticsServiceComponent.getSystemStatisticsUtil();
                systemStatistics = systemStatisticsUtil.getSystemStatistics(messageContext);
                AxisOperation axisOperation = messageContext.getAxisOperation();

                MessageContext inMessageContext = MessageContext.getCurrentMessageContext();

                //If already set in the activity handlers get it or create new publish data
/*                PublishData publishData = (PublishData) messageContext.getProperty(
                        BAMDataPublisherConstants.PUBLISH_DATA);

                EventData eventData;
                if (publishData != null) {
                    eventData = publishData.getEventData();
                } else {
                    publishData = new PublishData();
                    eventData = new EventData();
                }*/

                PublishData publishData = new PublishData();
                EventData eventData = new EventData();

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
                    if (axisOperation != null) {
                        eventData.setOperationName(axisOperation.getName().getLocalPart());
                    } else {
                        eventData.setOperationName(null);
                    }
                    if (axisService != null) {
                        eventData.setServiceName(axisService.getName());
                    } else {
                        eventData.setServiceName(null);
                    }

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
                if (axisOperation != null) {
                    eventData.setOperationName(axisOperation.getName().getLocalPart());
                } else {
                    eventData.setOperationName(null);
                }
                if (axisService != null) {
                    eventData.setServiceName(axisService.getName());
                } else {
                    eventData.setServiceName(null);
                }

                eventData.setSystemStatistics(systemStatistics);

                publishData.setEventData(eventData);

                // Skip if bam server info already set at activity handlers
/*                if (!eventingConfigData.isMsgDumpingEnable()) {
                    BAMServerInfo bamServerInfo = ServiceAgentUtil.addBAMServerInfo(eventingConfigData);
                    publishData.setBamServerInfo(bamServerInfo);
                }*/

                BAMServerInfo bamServerInfo = ServiceAgentUtil.addBAMServerInfo(eventingConfigData);
                publishData.setBamServerInfo(bamServerInfo);

                Event event = ServiceAgentUtil.makeEventList(publishData, eventingConfigData);
                EventPublisher publisher = new EventPublisher();
                publisher.publish(event, eventingConfigData);
            }
        } catch (Throwable ignore) {
            log.error("Error at SystemStatisticsInHandler. " +
                      "But continuing message processing for message id: " +
                      messageContext.getMessageID(), ignore);
        }

    }

    private boolean isInOnlyMEP(MessageContext messageContext) {

        if (messageContext.getOperationContext() == null) {
            return false;
        }

        String mep = messageContext.getOperationContext().getAxisOperation().getMessageExchangePattern();

        if (mep.equals(WSDL2Constants.MEP_URI_IN_ONLY) ||
            mep.equals(WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT) ||
            mep.equals(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY)) {

            return true;
        }

        return false;

    }
}
