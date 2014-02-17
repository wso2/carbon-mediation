/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediator.oauth;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;

import javax.xml.namespace.QName;

public class OAuthMediator extends AbstractMediator {
    private String remoteServiceUrl;
    private String username;
    private String password;
    private static final QName PROP_NAME_SERVICE_EPR = new QName("remoteServiceUrl");
    private static final QName PROP_NAME_USERNAME = new QName("username");
    private static final QName PROP_NAME_PASSWORD = new QName("password");

    public String getRemoteServiceUrl() {
        return remoteServiceUrl;
    }

    public void setRemoteServiceUrl(String remoteServiceUrl) {
        this.remoteServiceUrl = remoteServiceUrl;
    }

    /**
     * {@inheritDoc}
     */
    public OMElement serialize(OMElement parent) {
        OMElement oauthService = fac.createOMElement("oauthService", synNS);

        if (remoteServiceUrl != null) {
        	oauthService.addAttribute(fac.createOMAttribute("remoteServiceUrl", nullNS,
                    remoteServiceUrl));
        } else {
            throw new MediatorException(
                    "Invalid OAuth mediator. OAuth service epr is required");
        }
        if(username != null){
        	oauthService.addAttribute(fac.createOMAttribute("username", nullNS, username));
        } else {
        	throw new MediatorException("Invalid OAuth mediator. Username is required");
        }
        if(password != null){
        	oauthService.addAttribute(fac.createOMAttribute("password", nullNS, password));
        } else {
        	throw new MediatorException("Invalid OAuth mediator. Password is required");
        }
        saveTracingState(oauthService, this);

        if (parent != null) {
            parent.addChild(oauthService);
        }
        return oauthService;
    }

    /**
     * {@inheritDoc}
     */
    public void build(OMElement elem) {
        OMAttribute attRemoteServiceUri = elem.getAttribute(PROP_NAME_SERVICE_EPR);
        OMAttribute attUsername = elem.getAttribute(PROP_NAME_USERNAME);
        OMAttribute attPassword = elem.getAttribute(PROP_NAME_PASSWORD);

        if (attRemoteServiceUri != null) {
            remoteServiceUrl = attRemoteServiceUri.getAttributeValue();
        } else {
            throw new MediatorException(
                    "The 'remoteServiceUrl' attribute is required for the OAuth mediator");
        }
        if(attUsername != null) {
        	username = attUsername.getAttributeValue();
        } else {
        	throw new MediatorException("The 'username' attribute is required for the OAuth mediator ");
        }
        if(attPassword != null){
        	password = attPassword.getAttributeValue();
        } else {
        	throw new MediatorException("The 'password' attribute is required for the OAuth mediator");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getTagLocalName() {
        return "oauthService";
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
}
