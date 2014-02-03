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
import org.wso2.carbon.endpoint.ui.endpoints.address.AddressEndpointService;
import org.wso2.carbon.endpoint.ui.endpoints.defaultendpoint.DefaultEndpointService;
import org.wso2.carbon.endpoint.ui.endpoints.failover.FailoverEndpointService;
import org.wso2.carbon.endpoint.ui.endpoints.http.HttpEndpointService;
import org.wso2.carbon.endpoint.ui.endpoints.loadbalance.LoadBalanceEndpointService;
import org.wso2.carbon.endpoint.ui.endpoints.recipientlist.RecipientlistEndpointService;
import org.wso2.carbon.endpoint.ui.endpoints.template.TemplateEndpointService;
import org.wso2.carbon.endpoint.ui.endpoints.wsdl.WsdlEndpointService;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Keeps track of the endpoints that are registered for the Endpoint editor UI
 */
public class EndpointStore {

    // Singleton instance
    private static EndpointStore instance = null;
    // EndpointService instances registered for the given endpoint name
    private Map<String, EndpointService> store = new TreeMap<String, EndpointService>();

    private EndpointStore() {
    }

    /**
     * Singleton access to the EndpointStore
     *
     * @return the singleton instance
     */
    public synchronized static EndpointStore getInstance() {
        if (instance == null) {
            instance = new EndpointStore();
            instance.init();
        }
        return instance;
    }

    /**
     * Registers a {@link org.wso2.carbon.endpoint.ui.endpoints.EndpointService} with the store
     *
     * @param endpointTagLocalName tag name of the endpoint being registered
     * @param endpointService      implementation of the endpoint service for
     *                             the endpoint with name <code>endpointTagLocalName</code>
     */
    public void registerEndpoint(String endpointTagLocalName, EndpointService endpointService) {
        store.put(endpointTagLocalName, endpointService);
    }

    /**
     * Retrieves the {@link org.wso2.carbon.endpoint.ui.endpoints.EndpointService} with
     * the given <code>element</code>
     *
     * @param element configuration of the endpoint to retrieve the endpoint service
     * @return the endpoint service mapped to the given configuration
     */
    public EndpointService getEndpointService(OMElement element) {
        if (element.getFirstElement() != null && store.get(element.getFirstElement().getLocalName()) != null) {
            return store.get(element.getFirstElement().getLocalName());
        } else {
            Iterator it = element.getChildElements();
            while (it.hasNext()) {
                OMElement child = (OMElement) it.next();
                if (store.get(child.getLocalName()) != null) {
                    return store.get(child.getLocalName());
                }
            }
        }
        // For template endpoint
        if (element.getAttribute(new QName("template")) != null) {
            return store.get("template");
        }
        return null;
    }

    /**
     * Retrieves the {@link org.wso2.carbon.endpoint.ui.endpoints.EndpointService} with
     * the given <code>tagLocalName</code>
     *
     * @param tagLocalName tag name of the endpoint to retrieve the endpoint service
     * @return the endpoint service mapped to the given configuration
     */
    public EndpointService getEndpointService(String tagLocalName) {
        return store.get(tagLocalName);
    }

    /**
     * Retrieves all the {@link org.wso2.carbon.endpoint.ui.endpoints.EndpointService}
     *
     * @return all endpoint services registered in the store
     */
    public Collection<EndpointService> getRegisteredEndpoints() {
        return store.values();
    }

    /**
     * Initialize the Store. Add all the endpoint services to store at here
     */
    private void init() {
        registerEndpoint("address", new AddressEndpointService());
        registerEndpoint("loadbalance", new LoadBalanceEndpointService());
        registerEndpoint("wsdl", new WsdlEndpointService());
        registerEndpoint("default", new DefaultEndpointService());
        registerEndpoint("failover", new FailoverEndpointService());
        registerEndpoint("recipientlist", new RecipientlistEndpointService());
        registerEndpoint("template", new TemplateEndpointService());
        registerEndpoint("http", new HttpEndpointService());
    }

    /**
     * Retrieves the menu item of endpoint types, which is being used by the UI
     *
     * @return the mapped data model for the endpoint menu items
     */
    public ArrayList<String[]> getMenuItems() {
        ArrayList<String[]> menuItems = new ArrayList<String[]>();
        for (EndpointService epService : store.values()) {
            if (epService.canAddAsChild()) {
                menuItems.add(new String[]{epService.getDisplayName(), epService.getType()});
            }
        }
        return menuItems;
    }

}
