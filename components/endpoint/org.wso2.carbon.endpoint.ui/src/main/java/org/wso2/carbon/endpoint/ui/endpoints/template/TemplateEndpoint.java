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
package org.wso2.carbon.endpoint.ui.endpoints.template;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.endpoints.DefinitionFactory;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.endpoints.TemplateEndpointSerializer;
import org.apache.synapse.endpoints.Template;
import org.wso2.carbon.endpoint.ui.endpoints.Endpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TemplateEndpoint extends Endpoint {

    public String getTagLocalName() {
        return "template";
    }

    public String getEpName() {
        return epName;
    }

    public void setEpName(String epName) {
        this.epName = epName;
    }

    private String epName;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String address;
    private String targetTemplate = null;

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    private Map<String, String> parameters = new HashMap<String, String>();

    public String getTargetTemplate() {
        return targetTemplate;
    }

    public void setTargetTemplate(String targetTemplate) {
        this.targetTemplate = targetTemplate;
    }

    public void addParameter(String key, String value) {
        this.parameters.put(key, value);
    }

    public OMElement serialize(OMElement parent) {

        org.apache.synapse.endpoints.TemplateEndpoint templateEndpoint = new org.apache.synapse.endpoints.TemplateEndpoint();
        if (targetTemplate != null) {
            templateEndpoint.setTemplate(targetTemplate.trim());
        }

        //parameters
        for (String key : parameters.keySet()) {
            templateEndpoint.addParameter(key, parameters.get(key));
        }

        OMElement endpoint = new TemplateEndpointSerializer().serializeEndpoint(templateEndpoint);

        if (parent != null) {
            parent.addChild(endpoint);
        }
        return endpoint;
    }

    public void build(OMElement elem, boolean isAnonymous) {
        if (isAnonymous) {
            elem.addAttribute("name", "anonymous", elem.getOMFactory().createOMNamespace("", ""));
        }
        org.apache.synapse.endpoints.TemplateEndpoint templateEndpoint = (org.apache.synapse.endpoints.TemplateEndpoint) EndpointFactory.getEndpointFromElement(elem, isAnonymous, new Properties());
        buildData(templateEndpoint);
    }

    // templates are not supported for this endpoint type
    public void build(Template template, DefinitionFactory factory) {
    }

    private void buildData(org.apache.synapse.endpoints.TemplateEndpoint templateEndpoint) {
        if (templateEndpoint.getParameters() != null) {
            setParameters(templateEndpoint.getParameters());
        }
        setTargetTemplate(templateEndpoint.getTemplate());
    }

}
