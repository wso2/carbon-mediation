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

package org.wso2.carbon.endpoint.ui.endpoints.defaultendpoint;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.endpoints.DefinitionFactory;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.endpoints.Template;
import org.wso2.carbon.endpoint.ui.endpoints.Endpoint;
import org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper;

import java.util.Properties;


public class DefaultEndpoint extends Endpoint {

    private String epName;
    private String suspendDurationOnFailure;
    private String maxSusDuration;
    private String susProgFactor;
    private String errorCodes;
    private String format;
    private boolean swa = false;
    private boolean mtom = false;
    private boolean soap11 = false;
    private boolean soap12 = false;
    private boolean get = false;
    private boolean rest = false;
    private boolean pox = false;
    private String timedOutErrorCodes;
    private String retryDisabledErrorCodes;
    private String retryTimeout;
    private String retryDelay;
    private String timeoutAct;
    private String timeoutActionDuration;
    private boolean wsadd;
    private boolean sepList;
    private boolean wssec;
    private boolean wsrm;
    private String secPolKey;
    private String rmPolKey;
    private String description = "";
    private String properties;

    public String getTagLocalName() {
        return "default";
    }

    public String getEpName() {
        return epName;
    }

    public void setEpName(String name) {
        this.epName = name;
    }

    public String getSuspendDurationOnFailure() {
        return suspendDurationOnFailure;
    }

    public void setSuspendDurationOnFailure(String suspendDurationOnFailure) {
        this.suspendDurationOnFailure = suspendDurationOnFailure;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isSwa() {
        return swa;
    }

    public void setSwa(boolean swa) {
        this.swa = swa;
    }

    public boolean isMtom() {
        return mtom;
    }

    public void setMtom(boolean mtom) {
        this.mtom = mtom;
    }

    public boolean isSoap11() {
        return soap11;
    }

    public void setSoap11(boolean soap11) {
        this.soap11 = soap11;
    }

    public boolean isSoap12() {
        return soap12;
    }

    public void setSoap12(boolean soap12) {
        this.soap12 = soap12;
    }

    public boolean isRest() {
        return rest;
    }

    public void setRest(boolean rest) {
        this.rest = rest;
    }

    public boolean isGet() {
        return get;
    }

    public void setGet(boolean get) {
        this.get = get;
    }

    public boolean isPox() {
        return pox;
    }

    public void setPox(boolean pox) {
        this.pox = pox;
    }

    public String getTimeoutAct() {
        return timeoutAct;
    }

    public void setTimeoutAct(String timeoutAct) {
        if (timeoutAct.equals("101")) {
            this.timeoutAct = "discard";
        } else if (timeoutAct.equals("102")) {
            this.timeoutAct = "fault";
        } else if (timeoutAct.equals("100")) {
            this.timeoutAct = null;
        } else {
            this.timeoutAct = timeoutAct;
        }
    }

    public String getTimeoutActionDur() {
        return timeoutActionDuration;
    }

    public void setTimeoutActionDur(String timeoutActionDur) {
        this.timeoutActionDuration = timeoutActionDur;
    }

    public boolean isWsadd() {
        return wsadd;
    }

    public void setWsadd(boolean wsadd) {
        this.wsadd = wsadd;
    }

    public boolean isSepList() {
        return sepList;
    }

    public void setSepList(boolean sepList) {
        this.sepList = sepList;
    }

    public boolean isWssec() {
        return wssec;
    }

    public void setWssec(boolean wssec) {
        this.wssec = wssec;
    }

    public boolean isWsrm() {
        return wsrm;
    }

    public void setWsrm(boolean wsrm) {
        this.wsrm = wsrm;
    }

    public String getSecPolKey() {
        return secPolKey;
    }

    public void setSecPolKey(String secPolKey) {
        this.secPolKey = secPolKey;
    }

    public String getRmPolKey() {
        return rmPolKey;
    }

    public void setRmPolKey(String rmPolKey) {
        this.rmPolKey = rmPolKey;
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

    public OMElement serialize(OMElement parent) {

        // top element
        OMElement endpoint = fac.createOMElement("endpoint", synNS);
        if (epName != null && !"".equals(epName)) {
            endpoint.addAttribute(fac.createOMAttribute(
                    "name", nullNS, getEpName()));
        }

        // default endpoint element
        OMElement defaultElement = fac.createOMElement("default", synNS);
        // format
        if (isSoap11()) {
            defaultElement.addAttribute(fac.createOMAttribute("format", nullNS, "soap11"));

        } else if (isSoap12()) {
            defaultElement.addAttribute(fac.createOMAttribute("format", nullNS, "soap12"));

        } else if (isPox()) {
            defaultElement.addAttribute(fac.createOMAttribute("format", nullNS, "pox"));

        } else if (isRest()) {
            defaultElement.addAttribute(fac.createOMAttribute("format", nullNS, "rest"));

        } else if (isGet()) {
            defaultElement.addAttribute(fac.createOMAttribute("format", nullNS, "get"));
        }

        // optimize
        if (isSwa()) {
            defaultElement.addAttribute(fac.createOMAttribute("optimize", nullNS, "swa"));
        } else if (isMtom()) {
            defaultElement.addAttribute(fac.createOMAttribute("optimize", nullNS, "mtom"));
        }

        //Suspend configuration
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
            defaultElement.addChild(suspendOnFailure);
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
            defaultElement.addChild(markForSuspension);
        }

        if ((retryDisabledErrorCodes != null) && (!"".equals(retryDisabledErrorCodes))) {
            OMElement retryConfig = fac.createOMElement("retryConfig", synNS);
            OMElement disabledErrorCodes = fac.createOMElement("disabledErrorCodes", synNS);
            disabledErrorCodes.setText(retryDisabledErrorCodes);
            retryConfig.addChild(disabledErrorCodes);
            defaultElement.addChild(retryConfig);
        }

        //time out configuration
        String timeOutConfiguration;
        if (((timeoutAct != null && !"".equals(timeoutAct)) || (timeoutActionDuration != null && !"".equals(timeoutActionDuration)))
            && !"neverTimeout".equals(timeoutAct)) {

            OMElement timeout = fac.createOMElement("timeout", synNS);

            if (timeoutActionDuration != null && !"".equals(timeoutActionDuration)) {
                OMElement duration = fac.createOMElement("duration", synNS);
                duration.setText(timeoutActionDuration.trim());
                timeout.addChild(duration);
            }
            if (timeoutAct != null && !"".equals(timeoutAct)) {
                OMElement responseAction = fac.createOMElement("responseAction", synNS);
                responseAction.setText(timeoutAct);
                timeout.addChild(responseAction);
            }
            defaultElement.addChild(timeout);
        }

        // QoS configuration
        if (wsadd) {
            OMElement enableAddressing = fac.createOMElement("enableAddressing", synNS);
            if (sepList) {
                enableAddressing.addAttribute(fac.createOMAttribute(
                        "separateListener", nullNS, "true"));
            }
            defaultElement.addChild(enableAddressing);
        }
        if (wssec) {
            OMElement enableSecurity = fac.createOMElement("enableSec", synNS);
            if (secPolKey != null && !"".equals(secPolKey)) {
                enableSecurity.addAttribute(fac.createOMAttribute(
                        "policy", nullNS, secPolKey));
            }
            defaultElement.addChild(enableSecurity);
        }

        if (wsrm) {
            OMElement enableRM = fac.createOMElement("enableRM", synNS);
            if (rmPolKey != null && !"".equals(rmPolKey)) {
                enableRM.addAttribute(fac.createOMAttribute(
                        "policy", nullNS, rmPolKey));
            }
            defaultElement.addChild(enableRM);
        }
        endpoint.addChild(defaultElement);

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
        org.apache.synapse.endpoints.DefaultEndpoint defaultEndpoint = (org.apache.synapse.endpoints.DefaultEndpoint) EndpointFactory.getEndpointFromElement(elem, isAnonymous, new Properties());
        buildData(defaultEndpoint);
    }

    public void build(Template template, DefinitionFactory factory) {
        OMElement endpointEl = template.getElement();
        if (endpointEl != null) {
            org.apache.synapse.endpoints.Endpoint endpoint = EndpointFactory.getEndpointFromElement(endpointEl, factory, false, new Properties());
            if (endpoint != null && endpoint instanceof org.apache.synapse.endpoints.DefaultEndpoint) {
                org.apache.synapse.endpoints.DefaultEndpoint defaultEndpoint = (org.apache.synapse.endpoints.DefaultEndpoint) endpoint;
                buildData(defaultEndpoint);
            }
        }
    }

    private void buildData(org.apache.synapse.endpoints.DefaultEndpoint defaultEndpoint) {
        if (defaultEndpoint.getName() != null) {
            setEpName((defaultEndpoint.getName().equals("anonymous") ? "" : defaultEndpoint.getName()));
        }
        setDescription(defaultEndpoint.getDescription());
        setSoap11(defaultEndpoint.getDefinition().isForceSOAP11());
        setSoap12(defaultEndpoint.getDefinition().isForceSOAP12());
        setRest(defaultEndpoint.getDefinition().isForceREST());
        setGet(defaultEndpoint.getDefinition().isForceGET());
        setPox(defaultEndpoint.getDefinition().isForcePOX());
        setSwa(defaultEndpoint.getDefinition().isUseSwa());
        setMtom(defaultEndpoint.getDefinition().isUseMTOM());
        if (defaultEndpoint.getDefinition().getInitialSuspendDuration() != -1) {
            setSuspendDurationOnFailure(String.valueOf(defaultEndpoint.getDefinition().getInitialSuspendDuration()));
        }
        setTimeoutAct(String.valueOf(defaultEndpoint.getDefinition().getTimeoutAction()));
        if (defaultEndpoint.getDefinition().getTimeoutDuration() > 0) {
            setTimeoutActionDur(String.valueOf(defaultEndpoint.getDefinition().getTimeoutDuration()));
        }
        setWsadd(defaultEndpoint.getDefinition().isAddressingOn());
        setSepList(defaultEndpoint.getDefinition().isUseSeparateListener());
        setWssec(defaultEndpoint.getDefinition().isSecurityOn());
        setWsrm(defaultEndpoint.getDefinition().isReliableMessagingOn());
        setRmPolKey(defaultEndpoint.getDefinition().getWsRMPolicyKey());
        setSecPolKey(defaultEndpoint.getDefinition().getWsSecPolicyKey());
        if (defaultEndpoint.getDefinition().getSuspendMaximumDuration() < Long.MAX_VALUE) {
            setMaxSusDuration(String.valueOf(defaultEndpoint.getDefinition().getSuspendMaximumDuration()));
        }
        setSusProgFactor(String.valueOf(defaultEndpoint.getDefinition().getSuspendProgressionFactor()));
        setErrorCodes(EndpointConfigurationHelper.errorCodeListBuilder(defaultEndpoint.getDefinition().getSuspendErrorCodes()).trim());
        setRetryDisabledErrorCodes(EndpointConfigurationHelper.errorCodeListBuilder(defaultEndpoint.getDefinition().
                getRetryDisabledErrorCodes()).trim());
        setTimedOutErrorCodes(EndpointConfigurationHelper.errorCodeListBuilder(defaultEndpoint.getDefinition().getTimeoutErrorCodes()));
        setRetryTimeout(String.valueOf(defaultEndpoint.getDefinition().getRetriesOnTimeoutBeforeSuspend()));
        setRetryDelay(String.valueOf(defaultEndpoint.getDefinition().getRetryDurationOnTimeout()));
        setProperties(EndpointConfigurationHelper.buildPropertyString(defaultEndpoint));
    }

}
