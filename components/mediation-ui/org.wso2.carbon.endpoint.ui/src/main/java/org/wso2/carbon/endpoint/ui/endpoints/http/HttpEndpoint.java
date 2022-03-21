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
import org.apache.commons.lang.StringUtils;
import org.apache.synapse.config.xml.endpoints.DefinitionFactory;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.endpoints.BasicAuthConfiguredHTTPEndpoint;
import org.apache.synapse.endpoints.HTTPEndpoint;
import org.apache.synapse.endpoints.OAuthConfiguredHTTPEndpoint;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.endpoints.auth.basicauth.BasicAuthHandler;
import org.apache.synapse.endpoints.auth.oauth.AuthorizationCodeHandler;
import org.apache.synapse.endpoints.auth.oauth.ClientCredentialsHandler;
import org.apache.synapse.endpoints.auth.AuthConstants;
import org.apache.synapse.endpoints.auth.oauth.PasswordCredentialsHandler;
import org.wso2.carbon.endpoint.ui.endpoints.Endpoint;
import org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

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

    // oauth related configs
    private String clientId;
    private String clientSecret;
    private String authMode;
    private String refreshToken;
    private String tokenURL;
    private String username;
    private String password;
    private Map<String, String> requestParametersMap;

    // basic auth related configs
    private String basicAuthUsername;
    private String basicAuthPassword;

    private String description = "";
    private String properties;

    private boolean legacySupport = false;

    public static String legacyPrefix = HTTPEndpoint.legacyPrefix;

    public String getUriTemplate() {
    	return uriTemplate; 
    }

    public Map<String, String> getRequestParametersMap() {
        return requestParametersMap;
    }

    public void setRequestParametersMap(Map<String, String> requestParametersMap) {
        this.requestParametersMap = requestParametersMap;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenURL() {
        return tokenURL;
    }

    public void setTokenURL(String tokenURL) {
        this.tokenURL = tokenURL;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public String getRequestParametersAsString() {
        if (requestParametersMap != null && requestParametersMap.size() > 0) {
            return requestParametersMap.keySet().stream()
                    .map(key -> key + "=" + requestParametersMap.get(key))
                    .collect(Collectors.joining(",", "{", "}"));
        }
        return null;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getBasicAuthUsername() {
        return basicAuthUsername;
    }

    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    public void setBasicAuthUsername(String basicAuthUsername) {
        this.basicAuthUsername = basicAuthUsername;
    }

    public void setBasicAuthPassword(String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
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

        if (!StringUtils.isEmpty(getClientId()) && !"null".equals(getClientId())) {
            // oauth configuration
            OMElement authentication = fac.createOMElement(AuthConstants.AUTHENTICATION, synNS);
            OMElement oauth = fac.createOMElement(AuthConstants.OAUTH, synNS);
            authentication.addChild(oauth);
            if (isPasswordGrant()) {
                OMElement passwordCredentials = fac.createOMElement(AuthConstants.PASSWORD_CREDENTIALS, synNS);
                serializeOAuthCommonParameters(passwordCredentials, getClientId(), getClientSecret(), getTokenURL(),
                        getAuthMode());
                OMElement username = fac.createOMElement(AuthConstants.OAUTH_USERNAME, synNS);
                username.setText(getUsername());
                passwordCredentials.addChild(username);
                OMElement password = fac.createOMElement(AuthConstants.OAUTH_PASSWORD, synNS);
                password.setText(getPassword());
                passwordCredentials.addChild(password);
                serializeOAuthRequestParameters(passwordCredentials, getRequestParametersMap());
                oauth.addChild(passwordCredentials);
            } else if (isAuthorizationCodeGrant()) {
                OMElement authCode = fac.createOMElement(AuthConstants.AUTHORIZATION_CODE, synNS);
                serializeOAuthCommonParameters(authCode, getClientId(), getClientSecret(), getTokenURL(), getAuthMode());
                OMElement refreshToken = fac.createOMElement(AuthConstants.OAUTH_REFRESH_TOKEN, synNS);
                refreshToken.setText(getRefreshToken());
                authCode.addChild(refreshToken);
                serializeOAuthRequestParameters(authCode, getRequestParametersMap());
                oauth.addChild(authCode);
            } else {
                OMElement clientCredentials = fac.createOMElement(AuthConstants.CLIENT_CREDENTIALS, synNS);
                serializeOAuthCommonParameters(clientCredentials, getClientId(), getClientSecret(), getTokenURL(),
                        getAuthMode());
                serializeOAuthRequestParameters(clientCredentials, getRequestParametersMap());
                oauth.addChild(clientCredentials);
            }
            httpElement.addChild(authentication);
        } else if (isBasicAuthentication()) {
            // basic auth configuration
            OMElement authentication = fac.createOMElement(AuthConstants.AUTHENTICATION, synNS);
            OMElement basicAuth = fac.createOMElement(AuthConstants.BASIC_AUTH, synNS);
            authentication.addChild(basicAuth);
            OMElement username = fac.createOMElement(AuthConstants.BASIC_AUTH_USERNAME, synNS);
            username.setText(getBasicAuthUsername());
            basicAuth.addChild(username);
            OMElement password = fac.createOMElement(AuthConstants.BASIC_AUTH_PASSWORD, synNS);
            password.setText(getBasicAuthPassword());
            basicAuth.addChild(password);
            httpElement.addChild(authentication);
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

        if (httpEndpoint instanceof OAuthConfiguredHTTPEndpoint) {
            if (((OAuthConfiguredHTTPEndpoint) httpEndpoint).getOauthHandler() instanceof AuthorizationCodeHandler) {
                AuthorizationCodeHandler handler =
                        (AuthorizationCodeHandler) ((OAuthConfiguredHTTPEndpoint) httpEndpoint).getOauthHandler();
                setClientId(handler.getClientId());
                setClientSecret(handler.getClientSecret());
                setRefreshToken(handler.getRefreshToken());
                setTokenURL(handler.getTokenUrl());
                setAuthMode(handler.getAuthMode());
                setRequestParametersMap(handler.getRequestParametersMap());
            } else if (((OAuthConfiguredHTTPEndpoint) httpEndpoint)
                    .getOauthHandler() instanceof ClientCredentialsHandler) {
                ClientCredentialsHandler handler =
                        (ClientCredentialsHandler) ((OAuthConfiguredHTTPEndpoint) httpEndpoint).getOauthHandler();
                setClientId(handler.getClientId());
                setClientSecret(handler.getClientSecret());
                setTokenURL(handler.getTokenUrl());
                setAuthMode(handler.getAuthMode());
                setRequestParametersMap(handler.getRequestParametersMap());
            } else if (((OAuthConfiguredHTTPEndpoint) httpEndpoint)
                    .getOauthHandler() instanceof PasswordCredentialsHandler) {
                PasswordCredentialsHandler handler =
                        (PasswordCredentialsHandler) ((OAuthConfiguredHTTPEndpoint) httpEndpoint).getOauthHandler();
                setClientId(handler.getClientId());
                setClientSecret(handler.getClientSecret());
                setPassword(handler.getPassword());
                setUsername(handler.getUsername());
                setTokenURL(handler.getTokenUrl());
                setAuthMode(handler.getAuthMode());
                setRequestParametersMap(handler.getRequestParametersMap());
            }
        } else if (httpEndpoint instanceof BasicAuthConfiguredHTTPEndpoint) {
            BasicAuthHandler handler = ((BasicAuthConfiguredHTTPEndpoint) httpEndpoint).getBasicAuthHandler();
            setBasicAuthUsername(handler.getUsername());
            setBasicAuthPassword(handler.getPassword());
        }
    }

    /**
     * Method to serialize additional request parameters used in OAuth configs
     *
     * @param oauthCredentials     OAuth element the request parameters needs to be added
     * @param requestParametersMap Map containing the request parameters as key value pairs
     */
    private static void serializeOAuthRequestParameters(OMElement oauthCredentials,
                                                        Map<String, String> requestParametersMap) {

        if (requestParametersMap != null && requestParametersMap.size() > 0) {
            OMElement requestParameters = fac.createOMElement(AuthConstants.REQUEST_PARAMETERS, synNS);
            for (Map.Entry<String, String> entry : requestParametersMap.entrySet()) {
                OMElement parameter = fac.createOMElement(AuthConstants.REQUEST_PARAMETER, synNS);
                parameter.addAttribute("name", entry.getKey(), nullNS);
                parameter.setText(entry.getValue());
                requestParameters.addChild(parameter);
            }
            oauthCredentials.addChild(requestParameters);
        }
    }

    /**
     * Method to serialize common parameters used in OAuth configs
     *
     * @param oauthCredentials OAuth element the parameters needs to be added
     * @param clientId         clientId used in OAuth config
     * @param clientSecret     clientSecret used in OAuth config
     * @param tokenURL         tokenURL used in OAuth config
     */
    private static void serializeOAuthCommonParameters(OMElement oauthCredentials, String clientId,
                                                       String clientSecret, String tokenURL, String authMode) {

        OMElement clientIdElement = fac.createOMElement(AuthConstants.OAUTH_CLIENT_ID, synNS);
        clientIdElement.setText(clientId);
        oauthCredentials.addChild(clientIdElement);
        OMElement clientSecretElement = fac.createOMElement(AuthConstants.OAUTH_CLIENT_SECRET, synNS);
        clientSecretElement.setText(clientSecret);
        oauthCredentials.addChild(clientSecretElement);
        OMElement tokenUrlElement = fac.createOMElement(AuthConstants.TOKEN_API_URL, synNS);
        tokenUrlElement.setText(tokenURL);
        oauthCredentials.addChild(tokenUrlElement);
        if (!StringUtils.isEmpty(authMode)) {
            OMElement authModeElement = fac.createOMElement(AuthConstants.OAUTH_AUTHENTICATION_MODE, synNS);
            authModeElement.setText(authMode);
            oauthCredentials.addChild(authModeElement);
        }
    }

    /**
     * Method to check whether Password Credentials Grant type is configured
     *
     * @return true if Password Credentials Grant is configured
     */
    private boolean isPasswordGrant() {

        return StringUtils.isNotBlank(username) && !"null".equals(username);
    }

    /**
     * Method to check whether Authorization Code Grant type is configured
     *
     * @return true if Authorization Code Grant is configured
     */
    private boolean isAuthorizationCodeGrant() {

        return StringUtils.isNotBlank(refreshToken) && !"null".equals(refreshToken);
    }

    private boolean isBasicAuthentication() {
        return !StringUtils.isEmpty(getBasicAuthUsername()) && !"null".equals(getBasicAuthUsername())
                && !StringUtils.isEmpty(getBasicAuthPassword()) && !"null".equals(getBasicAuthPassword());
    }
}
