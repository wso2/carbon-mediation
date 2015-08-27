/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.wso2.carbon.bam.service.data.publisher.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.service.data.publisher.conf.EventConfigNStreamDef;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.bam.service.data.publisher.publish.ServiceAgentUtil;
import org.wso2.carbon.bam.service.data.publisher.publish.StreamDefinitionCreatorUtil;
import org.wso2.carbon.bam.service.data.publisher.util.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.util.StatisticsType;
import org.wso2.carbon.bam.service.data.publisher.util.TenantEventConfigData;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.Map;

/* This class extends AbstractAxis2ConfigurationContextObserver to engage Service stats module,
* when a new tenant is created.
*/
public class ServiceStatisticsAxis2ConfigurationContextObserver extends
        AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(ServiceStatisticsAxis2ConfigurationContextObserver.class);

    public void createdConfigurationContext(ConfigurationContext configContext) {

        //Enaging module for the tenant if the service publishing is enabled in the bam.xml
        if (StatisticsServiceComponent.isPublishingEnabled()) {
            AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            setEventingConfigDataSpecificForTenant(tenantId);

            AxisModule serviceStatisticsModule = axisConfiguration
                    .getModule(ServiceStatisticsPublisherConstants.BAM_SERVICE_STATISTICS_PUBLISHER_MODULE_NAME);
            if (serviceStatisticsModule != null) {
                try {
                    axisConfiguration
                            .engageModule(ServiceStatisticsPublisherConstants.BAM_SERVICE_STATISTICS_PUBLISHER_MODULE_NAME);
                } catch (AxisFault e) {
                    log.error("Cannot engage ServiceStatistics module for the tenant :" + tenantId, e);
                }
            }
        }
    }

    private void setEventingConfigDataSpecificForTenant(int tenantId) {
        Map<Integer, EventConfigNStreamDef> eventingConfigDataMap = TenantEventConfigData.getTenantSpecificEventingConfigData();
        RegistryPersistenceManager persistenceManager = new RegistryPersistenceManager();
        EventingConfigData eventingConfigData = persistenceManager.getEventingConfigData();
        EventConfigNStreamDef eventConfigNStreamDef = new RegistryPersistenceManager().
                fillEventingConfigData(eventingConfigData);

        StatisticsType statisticsType = ServiceAgentUtil.findTheStatisticType(eventingConfigData);
        if (statisticsType != null) {
            StreamDefinition streamDefinition = StreamDefinitionCreatorUtil.getStreamDefinition(
                    eventingConfigData, statisticsType);
            eventConfigNStreamDef.setStreamDefinition(streamDefinition);
            eventingConfigDataMap.put(tenantId, eventConfigNStreamDef);
        }
    }


    public void terminatedConfigurationContext(ConfigurationContext configCtx) {

    }

    public void terminatingConfigurationContext(ConfigurationContext configCtx) {

    }

}