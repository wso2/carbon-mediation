package org.wso2.carbon.bam.service.data.publisher.publish;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.service.data.publisher.conf.EventConfigNStreamDef;
import org.wso2.carbon.bam.service.data.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.bam.service.data.publisher.data.Event;
import org.wso2.carbon.bam.service.data.publisher.util.StatisticsType;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

public class EventPublisher {

    private static Log log = LogFactory.getLog(EventPublisher.class);

    public void publish(Event event, EventConfigNStreamDef configData) {
        List<Object> correlationData = event.getCorrelationData();
        List<Object> metaData = event.getMetaData();
        List<Object> payLoadData = event.getEventData();

        String key = null;
        EventPublisherConfig eventPublisherConfig = null;

        StreamDefinition streamDef = null;
        if (event.getStatisticsType().equals(StatisticsType.SERVICE_STATS)) {
            key = configData.getUrl() + "_" + configData.getUserName() + "_" +
                    configData.getPassword() + "_" + StatisticsType.SERVICE_STATS.name();
            eventPublisherConfig = ServiceAgentUtil.getEventPublisherConfig(key);
            streamDef = configData.getStreamDefinition();
        }
        //create data publisher

        if (!configData.isLoadBalancingConfig()) {
            try {
                if (eventPublisherConfig == null) {
                    synchronized (EventPublisher.class) {
                        eventPublisherConfig = ServiceAgentUtil.getEventPublisherConfig(key);
                        if (null == eventPublisherConfig) {
                            eventPublisherConfig = new EventPublisherConfig();
                            AsyncDataPublisher asyncDataPublisher = new AsyncDataPublisher(configData.getUrl(),
                                    configData.getUserName(),
                                    configData.getPassword(), EventPublisherConfig.getAgent());
                            asyncDataPublisher.addStreamDefinition(streamDef);
                            eventPublisherConfig.setDataPublisher(asyncDataPublisher);
                            ServiceAgentUtil.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                        }
                    }
                }

                AsyncDataPublisher asyncDataPublisher = eventPublisherConfig.getDataPublisher();

                asyncDataPublisher.publish(streamDef.getName(), streamDef.getVersion(), getObjectArray(metaData),
                        getObjectArray(correlationData),
                        getObjectArray(payLoadData));

            } catch (AgentException e) {
                log.error("Error occurred while sending the event", e);
            }
        } else {
            try {
                if (eventPublisherConfig == null) {
                    synchronized (EventPublisher.class) {

                        eventPublisherConfig = ServiceAgentUtil.getEventPublisherConfig(key);
                        if (null == eventPublisherConfig) {
                            eventPublisherConfig = new EventPublisherConfig();
                            ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
                            ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(configData.getUrl());

                            for (String aReceiverGroupURL : receiverGroupUrls) {
                                ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
                                String[] urls = aReceiverGroupURL.split(",");
                                for (String aUrl : urls) {
                                    DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), configData.getUserName(),
                                            configData.getPassword());
                                    dataPublisherHolders.add(aNode);
                                }
                                ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
                                allReceiverGroups.add(group);
                            }

                            LoadBalancingDataPublisher loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);

                            loadBalancingDataPublisher.addStreamDefinition(streamDef);
                            eventPublisherConfig.setLoadBalancingPublisher(loadBalancingDataPublisher);
                            ServiceAgentUtil.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                        }
                    }
                }

                LoadBalancingDataPublisher loadBalancingDataPublisher = eventPublisherConfig.getLoadBalancingDataPublisher();

                loadBalancingDataPublisher.publish(streamDef.getName(), streamDef.getVersion(), getObjectArray(metaData), getObjectArray(correlationData),
                        getObjectArray(payLoadData));

            } catch (AgentException e) {
                log.error("Error occurred while sending the event", e);
            }
        }


    }


    private Object[] getObjectArray(List<Object> list) {
        if (list.size() > 0) {
            return list.toArray();
        }
        return null;
    }
}
