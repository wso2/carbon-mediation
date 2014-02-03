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

package org.wso2.carbon.endpoint.ui.endpoints;

import org.apache.axiom.om.OMElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Basic structure for the <code>ListEndpoint</code> which contains a list of child endpoints
 */
public abstract class ListEndpoint extends Endpoint{

    protected List<Endpoint> endpoints = new ArrayList<Endpoint>();

    /**
     * Get the list of child endpoints containing in this endpoint
     *
     * @return all the child endpoints
     */
    public List<Endpoint> getList() {
        return endpoints;
    }

    /**
     * Get a child endpoint
     *
     * @param pos position in the child endpoint list
     * @return child endpoint of the given position in the list
     */
    public Endpoint getChild(int pos) {
        return endpoints.get(pos);
    }

    /**
     * Remove a child endpoint
     *
     * @param pos position in the child endpoint list
     */
    public Endpoint removeChild(int pos) {
        return endpoints.remove(pos);
    }

    /**
     * Remove a child endpoint
     *
     * @param endpoint Endpoint to be removed
     * @return whether child endpoint removal is success
     */
    public boolean removeChild(Endpoint endpoint) {
        return endpoints.remove(endpoint);
    }

    /**
     * Add a child endpoint to the list
     *
     * @param endpoint endpoint to be added
     */
    public void addChild(Endpoint endpoint) {
        endpoints.add(endpoint);
    }

    /**
     * Serialize all child endpoints
     *
     * @param parent parent endpoint
     * @param list child endpoint list
     */
    protected void serializeChildren(OMElement parent, List<Endpoint> list) {
        for (Endpoint child : list) {
            child.serialize(parent);
        }
    }

    /**
     * Add children to the <code>ListEndpoint</code> from a configuration.
     *
     * @param element configuration element
     * @param listEndpoint Endpoint to which childEndpoints to be added
     */
    protected void addChildren(OMElement element, ListEndpoint listEndpoint) {
        Iterator it = element.getChildElements();
        while (it.hasNext()) {
            OMElement child = (OMElement) it.next();
            EndpointService endpointService = EndpointStore.getInstance().getEndpointService(child);
            if (endpointService != null) {
                Endpoint endpoint = endpointService.getEndpoint();
                if (endpoint != null) {
                    endpoint.build(child,false);
                    listEndpoint.addChild(endpoint);
                }
            }
        }
    }

    /*
     * Check whether retry option is available with the Endpoint type
     */
    public abstract boolean isRetryAvailable();

}
