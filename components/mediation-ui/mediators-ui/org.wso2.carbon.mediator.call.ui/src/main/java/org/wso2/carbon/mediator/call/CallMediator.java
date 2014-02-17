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

    private Endpoint endpoint = null;

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
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

    }
}
