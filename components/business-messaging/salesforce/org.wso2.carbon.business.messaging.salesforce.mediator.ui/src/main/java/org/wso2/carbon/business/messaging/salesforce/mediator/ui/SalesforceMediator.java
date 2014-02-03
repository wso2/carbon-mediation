/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.business.messaging.salesforce.mediator.ui;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.business.messaging.salesforce.mediator.ui.config.SalesforceUIHandler;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

/**
 * <p>
 * Mediates the requests which are extracted from the
 * <code>MessageContext</code> using the specified particular source inputs and
 * maps the response back to the <code>MessageContext</code> using the outputs
 * specified.
 * </p>
 *
 * @see OperationType
 * @see org.apache.synapse.Mediator
 * @see org.apache.synapse.mediators.AbstractMediator
 */
public class SalesforceMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(SalesforceMediator.class);
    /**
     * Holds the location of the client repository configuration.
     */
    private String clientRepository;

    /**
     * Holds the location of the axis2 configuration.
     */
    private String axis2xml;

    /**
     * The <code>Operation</code> to be invoked.
     *
     * @see org.wso2.carbon.business.messaging.salesforce.mediator.ui.OperationType
     */
    private OperationType operation;

    /**
     * stores reference to an ui handler object
     */
    private final SalesforceUIHandler handler = new SalesforceUIHandler();

    /**
     * stores reference to salesforce configuration Serializer
     * serialize this mediator into a configuration
     */
    private SalesforceMediatorSerializer serializer;

    /**
     * stores reference to salesforce configuration builder
     * builds this mediator using a configuration provided
     */
    private SalesforceMediatorBuilder builder;

    /**
     * This will serialize this mediator configuration into an serialized XML format which would be
     * used to send it over wire to backend ESB
     *
     * @param parent top level configuration/sequence element
     * @return serialized sales force mediator config
     */
    public OMElement serialize(OMElement parent) {
        if (serializer == null) {
            serializer = new SalesforceMediatorSerializer(fac, synNS, nullNS);
        }
        OMElement salesforce = serializer.serializeMediator(parent, this);

        if (parent != null) {
            parent.addChild(salesforce);
        }
        if (log.isDebugEnabled()) {
            log.debug("The Serialized output " + salesforce);
        }
        return salesforce;
    }

    /**
     * build/populate this mediator from the provided XML configuration
     *
     * @param elem salesforce configuration element
     */
    public void build(OMElement elem) {
        if (builder == null) {
            builder = new SalesforceMediatorBuilder();
        }
        if (log.isDebugEnabled()) {
            log.debug("Building.....");
        }
        builder.buildMediator(elem, this);
        if (log.isDebugEnabled()) {
            log.debug("The Build output " + elem);
        }
    }

    /**
     * Getter method for <code>clientRepository</code>
     *
     * @return the client repository location.
     */
    public String getClientRepository() {
        return clientRepository;
    }

    /**
     * Setter method for <code>clientRepository</code>
     *
     * @param clientRepository the client repository location.
     */
    public void setClientRepository(String clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Getter method for <code>axis2xml</code>
     *
     * @return the axis2 xml configuration location.
     */
    public String getAxis2xml() {
        return axis2xml;
    }

    /**
     * Setter method for <code>axis2xml</code>
     *
     * @param axis2xml the axis2 xml configuration location.
     */
    public void setAxis2xml(String axis2xml) {
        this.axis2xml = axis2xml;
    }

    /**
     * Getter method for <code>operation</code>
     *
     * @return the operation the operation to invoke.
     */
    public OperationType getOperation() {
        return operation;
    }

    /**
     * Setter method for <code>operation</code>
     *
     * @param operation the operation to invoke
     */
    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public String getTagLocalName() {
        return "salesforce";
    }

    /**
     * @return handler object
     */
    public SalesforceUIHandler getHandler() {
        return handler;
    }

}
