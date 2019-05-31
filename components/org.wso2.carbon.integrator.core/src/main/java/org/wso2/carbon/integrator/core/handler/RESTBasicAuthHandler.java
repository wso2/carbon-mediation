/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.integrator.core.handler;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.Handler;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * This class can be added as a handler to enforce Basic Auth
 */
public class RESTBasicAuthHandler implements Handler {

    private static final Log log = LogFactory.getLog(RESTBasicAuthHandler.class);
        
    private boolean isInitialized;
	private String[] allowRoles;
    
    private Map<String, Object> properties;
	
	public RESTBasicAuthHandler() {
		this.isInitialized = false;
		this.allowRoles = null;
        this.properties = new HashMap<String, Object>();
	}
	
    @Override
    public boolean handleRequest(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext
                = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object headers = axis2MessageContext.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        if (!isInitialized) {
        	this.initialize(messageContext);
        	isInitialized = true;
        }

        String authHeader = null;
        if (headers != null && headers instanceof Map) {
            Map headersMap = (Map) headers;
            if (headersMap.get(HTTPConstants.HEADER_AUTHORIZATION) != null) {
                authHeader = (String) headersMap.get(HTTPConstants.HEADER_AUTHORIZATION);
            }
        }
        
        if (authHeader == null) {
            // No authorization header found in request, return 401
            headersMap.clear();
            axis2MessageContext.setProperty(BasicAuthConstants.HTTP_STATUS_CODE, BasicAuthConstants.SC_UNAUTHORIZED);
            headersMap.put(BasicAuthConstants.WWW_AUTHENTICATE, BasicAuthConstants.WWW_AUTH_METHOD);
            axis2MessageContext.setProperty(BasicAuthConstants.NO_ENTITY_BODY, true);
            messageContext.setProperty(BasicAuthConstants.RESPONSE, BasicAuthConstants.TRUE);
            messageContext.setTo(null);
            Axis2Sender.sendBack(messageContext);
            return false;
        } else {
            // The format of the Authorization header is :
            //   Authorization: Basic <base64 value of user:password>
            if (authHeader.startsWith("Basic ") {
                String credentials = authHeader.substring(6).trim();
                if (processSecurity(credentials, messageContext.getProperty("REST_API_CONTEXT").toString())) {
                    return true;
                }
            }

            // The authentication header is not Basic or the authentication / authorization phase failed, return HTTP 403
            headersMap.clear();
            axis2MessageContext.setProperty(BasicAuthConstants.HTTP_STATUS_CODE, BasicAuthConstants.SC_FORBIDDEN);
            axis2MessageContext.setProperty(BasicAuthConstants.NO_ENTITY_BODY, true);
            messageContext.setProperty(BasicAuthConstants.RESPONSE, BasicAuthConstants.TRUE);
            messageContext.setTo(null);
            Axis2Sender.sendBack(messageContext);
            return false;
        }
    }
    
    private void initialize(MessageContext context) {
    	Parameter defaultAllowRoles = context.getConfiguration().getAxisConfiguration().getParameter("defaultAllowRoles");
    	if (defaultAllowRoles != null) {
    		String[] splittedAllowRoles = ((String) defaultAllowRoles.getValue()).split(",");
    		if (splittedAllowRoles != null) {
    			if (allowRoles != null) {
    				allowRoles = (String[]) ArrayUtils.addAll(allowRoles, splittedAllowRoles);
    			} else {
    				allowRoles = splittedAllowRoles;
    			}
    		}
    	}
    	
        if (allowRoles != null) {
	    	UserRealm userRealm = (UserRealm) PrivilegedCarbonContext.getThreadLocalCarbonContext()
	                .getUserRealm();
	    	
	    	String resourceName = "API" + context.getProperty("REST_API_CONTEXT");
	    	try {
				AuthorizationManager manager = userRealm.getAuthorizationManager();
	            for (String role : allowRoles) {
	                manager.authorizeRole(role, resourceName,
	                                      UserCoreConstants.INVOKE_SERVICE_PERMISSION);
	            }
			} catch (Exception e) {
	            String msg = "Cannot apply security parameters for API " + resourceName;
	            log.error(msg, e);
			}
        }
	}

	@Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    @Override
    public void addProperty(String s, Object o) {
    	this.properties.put(s, o);
    }
    
    public void setAllowRoles(String allowRolesParameter) {
    	if (allowRolesParameter != null) {
            if (log.isDebugEnabled()) {
                log.debug("Authorizing roles " + allowRolesParameter);
            }
            this.allowRoles = allowRolesParameter.split(",");
        }
    }

    @Override
    public Map getProperties() {
    	return this.properties;
    }

    /**
     * This method authenticates credentials
     *
     * @param credentials The Basic Auth credentials of the request
     * @return true if the credentials are authenticated successfully
     */
    public boolean processSecurity(String credentials, String serviceName) {
        try {
	    	String username = null;
	    	String password = null;

	    	// Get username and password from the Authorization header 
	        String decodedCredentials = new String(new Base64().decode(credentials.getBytes()));
	        if (decodedCredentials != null) {
	        	String[] splittedCredentials = decodedCredentials.split(":");
	        	if (splittedCredentials.length == 2) {
	                username = decodedCredentials.split(":")[0];
	                password = decodedCredentials.split(":")[1];        		
	        	}
			}
	        if (username == null || password == null) {
				throw new UserStoreException("Unable to get Username and Password values from Authorization header");
			}

            UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
            UserStoreManager userStoreManager = realm.getUserStoreManager();
            // Authenticate user
            if (userStoreManager.authenticate(username, password)) {
            	String resourceName = "API" + serviceName;
            	// Authorize user
	            if (realm.getAuthorizationManager()
	            		.isUserAuthorized(tenantAwareUserName, resourceName,
	                            UserCoreConstants.INVOKE_SERVICE_PERMISSION)) {
	                return true;
	            } else if (log.isDebugEnabled()) {
                    log.debug("Authorization failure for user : " + tenantAwareUserName);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error in authenticating user", e);
        }
        return false;
    }
}
