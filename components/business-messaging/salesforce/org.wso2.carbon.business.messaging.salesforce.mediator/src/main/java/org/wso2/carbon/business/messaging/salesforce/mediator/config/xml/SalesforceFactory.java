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
package org.wso2.carbon.business.messaging.salesforce.mediator.config.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.jaxen.JaxenException;
import org.wso2.carbon.business.messaging.salesforce.mediator.*;
import org.wso2.carbon.business.messaging.salesforce.mediator.constants.SalesforceMedatorConstants;

/**
 * <p>
 * Factory for {@link SalesforceMediator} instances.Builds the
 * <code>SalesforceMediator</code> using a similar configuration as following
 * </p>
 * <p/>
 * <pre>
 *** <salesforce>
     * <configuration repository="" axis2xml=""/>?
     * <{login} type="{Login}">
         * <{username} xmlns:ns="http://wso2.services.sample" xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope" xmlns:ns3="http://org.apache.synapse/xsd" source-xpath="//ns:login/ns:username"/>
         * <{password} xmlns:ns="http://wso2.services.sample" xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope" xmlns:ns3="http://org.apache.synapse/xsd" source-xpath="//ns:login/ns:password"/>
         * <key type="{LoginResponse}"/>
         * <key type="{LoginResponse}"/>
     * </{login}>?
 * </salesforce>
 * </pre>
 * </pre>
 */
public class SalesforceFactory extends AbstractMediatorFactory {
    /**
     * create a specific mediator instance for salesforce configuration
     *
     * @param elem
     * @param props
     * @return Mediator instance
     */
    @Override
    protected Mediator createSpecificMediator(OMElement elem, Properties props) {
        SalesforceMediatorBuilder builder = createBuilder();
        Mediator mediator = new SalesforceMediator();
        builder.buildMediator(elem, mediator);
        return mediator;

    }

    /**
     * we delegate factory method to <code>SalesforceMediatorBuilder</code> instance
     *
     * @return instance of SalesforceMediatorBuilder
     */
    private SalesforceMediatorBuilder createBuilder() {
        return new SalesforceMediatorBuilder();
    }

    /*
      * (non-Javadoc)
      *
      * @see org.apache.synapse.config.xml.MediatorFactory#getTagQName()
      */

    public QName getTagQName() {
        return SalesforceMedatorConstants.QNAME_SALESFORCE;
    }

}
