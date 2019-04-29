/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.prometheus.publisher.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.util.MiscellaneousUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.carbon.integrator.core.handler.BasicAuthConstants;
import org.wso2.carbon.prometheus.publisher.publisher.MetricPublisher;
import org.wso2.carbon.prometheus.publisher.util.PrometheusPublisherConstants;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Resource class for endpoint exposing metric data
 */
public class MetricResource extends APIResource {

    private static Log log = LogFactory.getLog(MetricResource.class);
    private MetricPublisher metricPublisher;

    public MetricResource(String urlTemplate) {

        super(urlTemplate);
        metricPublisher = new MetricPublisher();
    }

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add("GET");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext synCtx) {

        buildMessage(synCtx);
        synCtx.setProperty("Success", true);

        OMElement textRootElem =
                OMAbstractFactory.getOMFactory().createOMElement(BaseConstants.DEFAULT_TEXT_WRAPPER);

        if (isPublishingEnabled()) {

            if (log.isDebugEnabled()) {
                log.debug("Retrieving metric data to be published to Prometheus");
            }

            List<String> metrics = metricPublisher.getMetrics();

            if (metrics != null && !metrics.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving metric data successful");
                }
                textRootElem.setText(String.join("", metrics));
            } else {
                textRootElem.setText("");
                log.info("No metrics retrieved to be published to Prometheus");
            }
        } else {
            textRootElem.setText("");
        }

        synCtx.getEnvelope().getBody().addChild(textRootElem);

        org.apache.axis2.context.MessageContext axisCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axisCtx.setProperty(Constants.Configuration.MESSAGE_TYPE, "text/plain");
        axisCtx.setProperty(Constants.Configuration.CONTENT_TYPE, "text/plain");
        axisCtx.removeProperty(BasicAuthConstants.NO_ENTITY_BODY);

        return true;
    }

    /**
     * Reads config data from prometheus-conf.xml
     */
    private boolean isPublishingEnabled() {

        OMElement apiConfig = MiscellaneousUtil.loadXMLConfig(PrometheusPublisherConstants.PROMETHEUS_CONFIG_FILE);
        QName root = new QName(PrometheusPublisherConstants.STAT_CONFIG_ELEMENT);
        QName enableElement = new QName(PrometheusPublisherConstants.PROMETHEUS_PUBLISHING_ENABLED);

        if (apiConfig != null) {
            if (!root.toString().equals(apiConfig.getLocalName())) {
                log.error("Invalid Prometheus configuration file");
                return false;
            }
            Iterator iterator = apiConfig.getChildrenWithName(enableElement);
            if (iterator.hasNext()) {
                OMElement enabled = (OMElement) iterator.next();
                return Boolean.parseBoolean(enabled.getText());
            }
        }
        return false;
    }
}
