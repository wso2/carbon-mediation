/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.endpoint.ui.endpoints.http;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.endpoints.DefinitionFactory;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.endpoints.HTTPEndpoint;
import org.apache.synapse.endpoints.Template;
import org.wso2.carbon.endpoint.ui.endpoints.Endpoint;
import org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper;

import java.util.Properties;

public class HttpEndpoint extends Endpoint {
	
	private String uriTemplate; 
    private String method;
    
	private boolean httpGet = false;
	private boolean httpPost = false;
	private boolean httpPatch = false;
	private boolean httpPut = false; 
	private boolean httpDelete = false;
	private boolean httpHead = false; 
	private boolean httpOptions = false;

	/*
	 * private boolean httpTrace = false; 
	 * private boolean httpConnect = false; 
	 */
	
    private String endpointName;

    private String suspendDurationOnFailure;
    private String maxSusDuration;
    private String susProgFactor;
    private String errorCodes;

    private String timedOutErrorCodes;
    private String retryDisabledErrorCodes;
    private String retryTimeout;
    private String retryDelay;
    private String timeoutAction;
    private String timeoutActionDuration;
    
    private String description = "";
    private String properties;

    private boolean legacySupport = false;

    public static String legacyPrefix = HTTPEndpoint.legacyPrefix;

    public String getUriTemplate() {
    	return uriTemplate; 
    }
    
    public void setUriTemplate(String template) {
    	this.uriTemplate = template.replaceAll("&amp;","&"); 
    }
    
    public void setHttpGet(boolean get) {
    	this.httpGet = get;
    }
    
    public void setHttpPost(boolean post) {
    	this.httpPost = post;
    }    
    
    public void setHttpPatch(boolean patch) {
    	this.httpPatch = patch;
    }
    
    public void setHttpPut(boolean put) {
    	this.httpPut = put;
    }    
    
    public void setHttpDelete(boolean delete) {
    	this.httpDelete = delete;
    }
    
    public void setHttpHead(boolean head) {
    	this.httpHead = head;
    }

    public void setHttpOptions(boolean options) {
    	this.httpOptions = options;
    }
    
    public String getTagLocalName() {
        return "http";
    }
        
    public boolean isHttpGet() {
    	return httpGet;
    }
    
    public boolean isHttpPost() {
    	return httpPost; 
    }
    
    public boolean isHttpPatch() {
    	return httpPatch;
    }
    
    public boolean isHttpPut() {
    	return httpPut; 
    }
        
    public boolean isHttpDelete() {
    	return httpDelete; 
    }
    
    public boolean isHttpHead() {
    	return httpHead; 
    }

    public boolean isHttpOptions() {
        return httpOptions;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String name) {
        this.endpointName = name;
    }

    public String getSuspendDurationOnFailure() {
        return suspendDurationOnFailure;
    }

    public void setSuspendDurationOnFailure(String suspendDurationOnFailure) {
        this.suspendDurationOnFailure = suspendDurationOnFailure;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
        
        if ("get".equalsIgnoreCase(method)) {
        	setHttpGet(true);
        } else if ("post".equalsIgnoreCase(method)) {
        	setHttpPost(true);
        } else if ("patch".equalsIgnoreCase(method)) {
        	setHttpPatch(true);
        } else if ("put".equalsIgnoreCase(method)) {
        	setHttpPut(true);
        } else if ("delete".equalsIgnoreCase(method)) {
        	setHttpDelete(true);
        } else if ("head".equalsIgnoreCase(method)) {
        	setHttpHead(true);
        } else if ("options".equalsIgnoreCase(method)) {
            setHttpOptions(true);
        } else {
            setHttpGet(false);
        	setHttpPost(false);
        	setHttpPatch(false);
        	setHttpPut(false);
        	setHttpDelete(false);
        	setHttpHead(false);
            setHttpOptions(false);
        }
    }

    public String getTimeoutAction() {
        return timeoutAction;
    }

    public void setTimeoutAction(String timeoutAction) {
        if (timeoutAction.equals("101")) {
            this.timeoutAction = "discard";
        } else if (timeoutAction.equals("102")) {
            this.timeoutAction = "fault";
        } else if (timeoutAction.equals("100")) {
            this.timeoutAction = null;
        } else {
            this.timeoutAction = timeoutAction;
        }
    }

    public String getTimeoutActionDur() {
        return timeoutActionDuration;
    }

    public void setTimeoutActionDur(String timeoutActionDur) {
        this.timeoutActionDuration = timeoutActionDur;
    }

    public String getMaxSusDuration() {
        return maxSusDuration;
    }

    public void setMaxSusDuration(String maxSusDuration) {
        this.maxSusDuration = maxSusDuration;
    }

    public String getSusProgFactor() {
        return susProgFactor;
    }

    public void setSusProgFactor(String susProgFactor) {
        this.susProgFactor = susProgFactor;
    }

    public String getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(String errorCodes) {
        this.errorCodes = errorCodes;
    }

    public void setRetryDisabledErrorCodes(String retryDisabledErrorCodes) {
        this.retryDisabledErrorCodes = retryDisabledErrorCodes;
    }

    public String getRetryDisabledErrorCodes() {
        return retryDisabledErrorCodes;
    }

    public String getTimedOutErrorCodes() {
        return timedOutErrorCodes;
    }

    public void setTimedOutErrorCodes(String timedOutErrorCodes) {
        this.timedOutErrorCodes = timedOutErrorCodes;
    }

    public String getRetryTimeout() {
        return retryTimeout;
    }

    public void setRetryTimeout(String retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    public String getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(String retryDelay) {
        this.retryDelay = retryDelay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public void setLegacy(boolean legacySupport) {
        this.legacySupport = legacySupport;
    }

    public boolean isLegacy() {
        return legacySupport;
    }

    public OMElement serialize(OMElement parent) {

        // top element
        OMElement endpoint = fac.createOMElement("endpoint", synNS);
        if (endpointName != null && !"".equals(endpointName)) {
            endpoint.addAttribute(fac.createOMAttribute(
                    "name", nullNS, endpointName));
        }

        // http element]
        OMElement httpElement = fac.createOMElement("http",synNS);
        if (uriTemplate != null && !"".equals(uriTemplate)) {
            if (isLegacy()) {
        	    httpElement.addAttribute(fac.createOMAttribute("uri-template", nullNS, HTTPEndpoint.legacyPrefix + uriTemplate));
            } else {
                httpElement.addAttribute(fac.createOMAttribute("uri-template", nullNS, uriTemplate));
            }
        }       
        
        // method
        if (isHttpGet()) {
        	httpElement.addAttribute(fac.createOMAttribute("method", nullNS, "get"));
        } else if (isHttpPost()) {
        	httpElement.addAttribute(fac.createOMAttribute("method", nullNS, "post"));
        } else if (isHttpPatch()) {
        	httpElement.addAttribute(fac.createOMAttribute("method", nullNS, "patch"));
        } else if (isHttpPut()) {
        	httpElement.addAttribute(fac.createOMAttribute("method", nullNS, "put"));
        } else if (isHttpDelete()) {
        	httpElement.addAttribute(fac.createOMAttribute("method", nullNS, "delete"));
        } else if (isHttpHead()) {
        	httpElement.addAttribute(fac.createOMAttribute("method", nullNS, "head"));
        } else if (isHttpOptions()) {
            httpElement.addAttribute(fac.createOMAttribute("method", nullNS, "options"));
        }

        // Suspend configuration
        if ((errorCodes != null && !"".equals(errorCodes)) ||
            (suspendDurationOnFailure != null && !"".equals(suspendDurationOnFailure)) ||
            (maxSusDuration != null && !"".equals(maxSusDuration)) ||
            (susProgFactor != null && !"".equals(susProgFactor))) {

            OMElement suspendOnFailure = fac.createOMElement("suspendOnFailure", synNS);

            if (errorCodes != null && !"".equals(errorCodes)) {
                OMElement errorCodesElement = fac.createOMElement("errorCodes", synNS);
                errorCodesElement.setText(errorCodes.trim());
                suspendOnFailure.addChild(errorCodesElement);
            }
            if ((suspendDurationOnFailure != null && !"".equals(suspendDurationOnFailure))) {
                OMElement initialDuration = fac.createOMElement("initialDuration", synNS);
                initialDuration.setText(suspendDurationOnFailure.trim().startsWith("$") ? suspendDurationOnFailure.trim() : Long.valueOf(getSuspendDurationOnFailure().trim()).toString());
                suspendOnFailure.addChild(initialDuration);
            }
            if (susProgFactor != null && !"".equals(susProgFactor)) {
                OMElement progressionFactor = fac.createOMElement("progressionFactor", synNS);
                progressionFactor.setText(susProgFactor.trim().startsWith("$") ? susProgFactor : Float.valueOf(susProgFactor).toString());
                suspendOnFailure.addChild(progressionFactor);
            }
            if (maxSusDuration != null && !"".equals(maxSusDuration)) {
                OMElement maxumumDuration = fac.createOMElement("maximumDuration", synNS);
                maxumumDuration.setText(maxSusDuration.trim().startsWith("$") ? maxSusDuration.trim() : Long.valueOf(maxSusDuration.trim()).toString());
                suspendOnFailure.addChild(maxumumDuration);
            }
            httpElement.addChild(suspendOnFailure);
        }

        // retry time configuration
        if ((timedOutErrorCodes != null && !"".equals(timedOutErrorCodes)) || (retryDelay != null && !"".equals(retryDelay))
            || (retryTimeout != null && !"".equals(retryTimeout))) {

            OMElement markForSuspension = fac.createOMElement("markForSuspension", synNS);

            if (timedOutErrorCodes != null && !"".equals(timedOutErrorCodes)) {
                OMElement timedOutErrorCodesElement = fac.createOMElement("errorCodes", synNS);
                timedOutErrorCodesElement.setText(timedOutErrorCodes.trim());
                markForSuspension.addChild(timedOutErrorCodesElement);
            }
            if (retryTimeout != null && !"".equals(retryTimeout)) {
                OMElement retryTimeoutElement = fac.createOMElement("retriesBeforeSuspension", synNS);
                retryTimeoutElement.setText(retryTimeout);
                markForSuspension.addChild(retryTimeoutElement);
            }
            if (retryDelay != null && !"".equals(retryDelay)) {
                OMElement retryDelayElement = fac.createOMElement("retryDelay", synNS);
                retryDelayElement.setText(retryDelay);
                markForSuspension.addChild(retryDelayElement);
            }
            httpElement.addChild(markForSuspension);
        }

        // retry config
        if ((retryDisabledErrorCodes != null) && (!"".equals(retryDisabledErrorCodes))) {
            OMElement retryConfig = fac.createOMElement("retryConfig", synNS);
            OMElement disabledErrorCodes = fac.createOMElement("disabledErrorCodes", synNS);
            disabledErrorCodes.setText(retryDisabledErrorCodes);
            retryConfig.addChild(disabledErrorCodes);
            httpElement.addChild(retryConfig);
        }

        // time out configuration
        String timeOutConfiguration;
        if (((timeoutAction != null && !"".equals(timeoutAction)) || (timeoutActionDuration != null && !"".equals(timeoutActionDuration)))
            && !"neverTimeout".equals(timeoutAction)) {
            OMElement timeout = fac.createOMElement("timeout", synNS);

            if (timeoutActionDuration != null && !"".equals(timeoutActionDuration)) {
                OMElement duration = fac.createOMElement("duration", synNS);
                duration.setText(timeoutActionDuration.trim());
                timeout.addChild(duration);
            }
            if (timeoutAction != null && !"".equals(timeoutAction)) {
                OMElement responseAction = fac.createOMElement("responseAction", synNS);
                responseAction.setText(timeoutAction);
                timeout.addChild(responseAction);
            }
            httpElement.addChild(timeout);
        }
        endpoint.addChild(httpElement);

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

        // Description
        if (description != null && !description.equals("")) {
            OMElement descriptionElement = fac.createOMElement("description", synNS);
            descriptionElement.setText(description);
            endpoint.addChild(descriptionElement);
        }

        // add to parent element
        if (parent != null) {
            parent.addChild(endpoint);
        }
        return endpoint;
    }

    public void build(OMElement elem, boolean isAnonymous) {
        if (isAnonymous) {
            elem.addAttribute("name", "anonymous", elem.getOMFactory().createOMNamespace("", ""));
        }
        org.apache.synapse.endpoints.HTTPEndpoint httpEndpoint = (org.apache.synapse.endpoints.HTTPEndpoint) EndpointFactory.getEndpointFromElement(elem, isAnonymous, new Properties());
        buildData(httpEndpoint);        
    }

    public void build(Template template, DefinitionFactory factory) {
        OMElement endpointEl = template.getElement();
        if (endpointEl != null) {
            org.apache.synapse.endpoints.Endpoint endpoint = EndpointFactory.getEndpointFromElement(endpointEl, factory, false, new Properties());
            if (endpoint != null && endpoint instanceof org.apache.synapse.endpoints.HTTPEndpoint) {
                org.apache.synapse.endpoints.HTTPEndpoint httpEndpoint = (org.apache.synapse.endpoints.HTTPEndpoint) endpoint;
                buildData(httpEndpoint);
            }
        }
    }

    private void buildData(org.apache.synapse.endpoints.HTTPEndpoint httpEndpoint) {
        if (httpEndpoint.getName() != null) {
            setEndpointName((httpEndpoint.getName().equals("anonymous") ? "" : httpEndpoint.getName()));
        }
        setMethod(httpEndpoint.getHttpMethod());
        setUriTemplate(httpEndpoint.getUriTemplate().getTemplate());
        setDescription(httpEndpoint.getDescription());

        setLegacy(httpEndpoint.isLegacySupport());
                
        if (httpEndpoint.getDefinition().getInitialSuspendDuration() != -1) {
            setSuspendDurationOnFailure(String.valueOf(httpEndpoint.getDefinition().getInitialSuspendDuration()));
        }
        setTimeoutAction(String.valueOf(httpEndpoint.getDefinition().getTimeoutAction()));
        if (httpEndpoint.getDefinition().getTimeoutDuration() > 0) {
            setTimeoutActionDur(String.valueOf(httpEndpoint.getDefinition().getTimeoutDuration()));
        }
        
        if (httpEndpoint.getDefinition().getSuspendMaximumDuration() < Long.MAX_VALUE) {
            setMaxSusDuration(String.valueOf(httpEndpoint.getDefinition().getSuspendMaximumDuration()));
        }
        setSusProgFactor(String.valueOf(httpEndpoint.getDefinition().getSuspendProgressionFactor()));
        setErrorCodes(EndpointConfigurationHelper.errorCodeListBuilder(httpEndpoint.getDefinition().getSuspendErrorCodes()).trim());
        setRetryDisabledErrorCodes(EndpointConfigurationHelper.errorCodeListBuilder(httpEndpoint.getDefinition().
                getRetryDisabledErrorCodes()).trim());
        setTimedOutErrorCodes(EndpointConfigurationHelper.errorCodeListBuilder(httpEndpoint.getDefinition().getTimeoutErrorCodes()));
        setRetryTimeout(String.valueOf(httpEndpoint.getDefinition().getRetriesOnTimeoutBeforeSuspend()));
        setRetryDelay(String.valueOf(httpEndpoint.getDefinition().getRetryDurationOnTimeout()));
        setProperties(EndpointConfigurationHelper.buildPropertyString(httpEndpoint));
    }

}
