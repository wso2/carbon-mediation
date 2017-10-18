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
package org.wso2.carbon.endpoint.ui.endpoints.failover;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.endpoints.DefinitionFactory;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.synapse.endpoints.Template;
import org.wso2.carbon.endpoint.ui.endpoints.ListEndpoint;
import org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper;

import java.util.Iterator;
import java.util.Properties;


public class FailoverEndpoint extends ListEndpoint {

    private String properties = null;
    private String name;
    private boolean buildMessage;

    public String getTagLocalName() {
        return "failover";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public boolean isBuildMessage() {
        return buildMessage;
    }

    public void setBuildMessage(boolean buildMessage) {
        this.buildMessage = buildMessage;
    }

    public OMElement serialize(OMElement parent) {

        // top element
        OMElement endpoint = fac.createOMElement("endpoint", synNS);
        if (name != null && !"".equals(name)) {
            endpoint.addAttribute(fac.createOMAttribute(
                    "name", nullNS, getName()));
        }

        // failover element
        OMElement failover = fac.createOMElement("failover", synNS);
        if (buildMessage) {
            failover.addAttribute(fac.createOMAttribute("buildMessage", nullNS, Boolean.toString(true)));
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

        endpoint.addChild(failover);
        // serialize child endpoints
        serializeChildren(failover, getList());

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
        org.apache.synapse.endpoints.Endpoint failoverEndpoint = EndpointFactory
                .getEndpointFromElement(elem, isAnonymous, new Properties());

        if (failoverEndpoint instanceof org.apache.synapse.endpoints.FailoverEndpoint) {
            org.apache.synapse.endpoints.FailoverEndpoint failOverEndpoint =
                    (org.apache.synapse.endpoints.FailoverEndpoint) failoverEndpoint;
            buildMessage = failOverEndpoint.isBuildMessageAtt();
        }
        if (failoverEndpoint.getName() != null) {
            name = failoverEndpoint.getName().equals("anonymous") ? "" : failoverEndpoint.getName();
        }
        properties = EndpointConfigurationHelper.buildPropertyString((AbstractEndpoint) failoverEndpoint);

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

    // templates are not supported
    public void build(Template template, DefinitionFactory factory) {
    }

    @Override
    public boolean isRetryAvailable() {
        return true;
    }
}
