/*
*  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mediator.call;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.endpoints.Endpoint;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;
import java.util.Properties;


public class CallMediator extends AbstractMediator {

    private static final QName ENDPOINT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "endpoint");

    private static final QName BLOCKING_Q = new QName("blocking");

    private static final QName ATT_AXIS2XML = new QName("axis2xml");

    private static final QName ATT_REPOSITORY = new QName("repository");

    private static final QName  ATT_INIT_AXIS2_CLIENT_OPTIONS = new QName("initAxis2ClientOptions");

    private Endpoint endpoint = null;

    private boolean blocking = false;

    private boolean initAxis2ClientOptions = true;

    private String clientRepository = null;

    private String axis2xml = null;

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public boolean getBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public boolean getInitAxis2ClientOptions() {
        return initAxis2ClientOptions;
    }

    public void setInitAxis2ClientOptions(boolean initAxis2ClientOptions) {
        this.initAxis2ClientOptions = initAxis2ClientOptions;
    }

    public String getClientRepository() {
        return clientRepository;
    }

    public void setClientRepository(String clientRepository) {
        this.clientRepository = clientRepository;
    }

    public String getAxis2xml() {
        return axis2xml;
    }

    public void setAxis2xml(String axis2xml) {
        this.axis2xml = axis2xml;
    }

    public String getTagLocalName() {
        return "call";
    }

    public OMElement serialize(OMElement parent) {
        OMElement call = fac.createOMElement("call", synNS);
        saveTracingState(call, this);

        Endpoint activeEndpoint = getEndpoint();
        if (activeEndpoint != null) {
            call.addChild(EndpointSerializer.getElementFromEndpoint(activeEndpoint));
        }
        if (blocking) {
            call.addAttribute(fac.createOMAttribute("blocking", nullNS, "true"));
            if(!initAxis2ClientOptions) {
                call.addAttribute(fac.createOMAttribute("initAxis2ClientOptions",nullNS,"false"));
            }
            if (clientRepository != null){
                call.addAttribute(fac.createOMAttribute("repository", nullNS, clientRepository));
            }
            if (axis2xml != null) {
                call.addAttribute(fac.createOMAttribute("axis2xml", nullNS, axis2xml));
            }
        }

        if (parent != null) {
            parent.addChild(call);
        }

        return call;
    }

    public void build(OMElement elem) {
        endpoint = null;

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);

        OMElement epElement = elem.getFirstChildWithName(ENDPOINT_Q);
        if (epElement != null) {
            // create the endpoint and set it in the send mediator
            Endpoint endpoint = EndpointFactory.getEndpointFromElement(epElement, true,
                    new Properties());
            if (endpoint != null) {
                setEndpoint(endpoint);
            }
        }
        OMAttribute blocking = elem.getAttribute(BLOCKING_Q);
        if (blocking != null) {
            setBlocking(Boolean.parseBoolean(blocking.getAttributeValue()));
            OMAttribute attInitAxis2ClientOptions = elem.getAttribute(ATT_INIT_AXIS2_CLIENT_OPTIONS);
            OMAttribute attAxis2xml = elem.getAttribute(ATT_AXIS2XML);
            OMAttribute attRepo = elem.getAttribute(ATT_REPOSITORY);
            if (attInitAxis2ClientOptions != null) {
                setInitAxis2ClientOptions(Boolean.parseBoolean(attInitAxis2ClientOptions.getAttributeValue()));
            }else{
                setInitAxis2ClientOptions(true);
            }
            if (attAxis2xml != null && attAxis2xml.getAttributeValue() != null) {
                setAxis2xml(attAxis2xml.getAttributeValue());
            }
            if (attRepo != null && attRepo.getAttributeValue() != null) {
               setClientRepository(attRepo.getAttributeValue());
            }

        } else {
            setBlocking(false);
        }

    }
}
