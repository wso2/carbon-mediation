/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.endpoint.ui.endpoints.loadbalance;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.endpoints.DefinitionFactory;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.endpoints.SALoadbalanceEndpointFactory;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.synapse.endpoints.LoadbalanceEndpoint;
import org.apache.synapse.endpoints.SALoadbalanceEndpoint;
import org.apache.synapse.endpoints.Template;
import org.wso2.carbon.endpoint.ui.endpoints.ListEndpoint;
import org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

public class LoadBalanceEndpoint extends ListEndpoint {

    private String sessionType = null;
    private String properties = null;
    private long sessionTimeout = 0;
    private String name;
    private String algorithmClassName = EndpointConfigurationHelper.ROUNDROBIN_ALGO_CLASS_NAME;

    public String getTagLocalName() {
        return "loadbalance";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getAlgorithmClassName() {
        return algorithmClassName;
    }

    public void setAlgorithmClassName(String algorithmClassName) {
        this.algorithmClassName = algorithmClassName;
    }

    public OMElement serialize(OMElement parent) {
        // top element
        OMElement endpoint = fac.createOMElement("endpoint", synNS);

        if (name != null && !"".equals(name)) {
            endpoint.addAttribute(fac.createOMAttribute(
                    "name", nullNS, getName()));
        }

        // load balance element
        OMElement loadbalance = fac.createOMElement("loadbalance", synNS);
        loadbalance.addAttribute(fac.createOMAttribute("algorithm", nullNS, algorithmClassName));

        // session
        if (sessionType != null) {
            OMElement session = fac.createOMElement("session", synNS);
            session.addAttribute(fac.createOMAttribute(
                    "type", nullNS, sessionType));
            OMElement sessionTimeout = fac.createOMElement("sessionTimeout", synNS);
            sessionTimeout.setText(String.valueOf(getSessionTimeout()));
            session.addChild(sessionTimeout);
            endpoint.addChild(session);
        }

        // Properties
        if (properties != null && properties.length() != 0) {
            String[] props = properties.split("::");
            for (String s : props) {
                String[] elements = s.split(",");
                OMElement property = fac.createOMElement("property", synNS);
                property.addAttribute(fac.createOMAttribute("name", nullNS, elements[0]));
                property.addAttribute(fac.createOMAttribute("value", nullNS, elements[1]));
                property.addAttribute(fac.createOMAttribute("scope", nullNS, elements[2]));
                endpoint.addChild(property);
            }
        }

        endpoint.addChild(loadbalance);
        // serialize child endpoints
        serializeChildren(loadbalance, getList());
        // add configuration to parent element
        if (parent != null) {
            parent.addChild(endpoint);
        }
        return endpoint;
    }

    public void build(OMElement elem, boolean isAnonymous) {

        if (isAnonymous) {
            elem.addAttribute("name", "anonymous", elem.getOMFactory().createOMNamespace("", ""));
        }
        org.apache.synapse.endpoints.Endpoint loadbalanceEndpoint = SALoadbalanceEndpointFactory.getEndpointFromElement(elem, isAnonymous, new Properties());

        if (loadbalanceEndpoint != null && loadbalanceEndpoint instanceof SALoadbalanceEndpoint) {
            SALoadbalanceEndpoint saLoadBalanceDataElement = (SALoadbalanceEndpoint) loadbalanceEndpoint;
            if (saLoadBalanceDataElement.getName() != null) {
                name = saLoadBalanceDataElement.getName().equals("anonymous") ? "" : saLoadBalanceDataElement.getName();
            }
            if (saLoadBalanceDataElement.getAlgorithm() != null) {
                algorithmClassName = saLoadBalanceDataElement.getAlgorithm().getClass().getName();
            }
            sessionTimeout = saLoadBalanceDataElement.getSessionTimeout();
            OMElement sessionElement = elem.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE, "session"));
            String session = sessionElement.getAttributeValue(new QName(null, "type"));
            if (session != null && !"".equals(session)) {
                sessionType = session;
            }
        } else {
            loadbalanceEndpoint = EndpointFactory.getEndpointFromElement(elem, isAnonymous, new Properties());
            if (loadbalanceEndpoint != null) {
                LoadbalanceEndpoint loadBalanceEndpoint = (LoadbalanceEndpoint) loadbalanceEndpoint;
                if (loadBalanceEndpoint.getName() != null) {
                    name = loadBalanceEndpoint.getName().equals("anonymous")  ? "" : loadBalanceEndpoint.getName();
                }
                if (loadBalanceEndpoint.getAlgorithm() != null) {
                    algorithmClassName = loadBalanceEndpoint.getAlgorithm().getClass().getName();
                }
            }
        }

        properties = EndpointConfigurationHelper.buildPropertyString((AbstractEndpoint) loadbalanceEndpoint);

        OMElement endpointElement = null;
        if (elem.getFirstElement().getLocalName() == getTagLocalName()) {
            endpointElement = elem.getFirstElement();
        } else {
            Iterator it = elem.getChildElements();
            while (it.hasNext()) {
                OMElement child = (OMElement) it.next();
                if (child.getLocalName() == getTagLocalName()) {
                    endpointElement = child;
                    break;
                }
            }
        }
        addChildren(endpointElement, this);
    }

    // templates are not supported for this endpoint type
    public void build(Template template, DefinitionFactory factory) {
    }

    @Override
    public boolean isRetryAvailable() {
        return true;
    }
}
