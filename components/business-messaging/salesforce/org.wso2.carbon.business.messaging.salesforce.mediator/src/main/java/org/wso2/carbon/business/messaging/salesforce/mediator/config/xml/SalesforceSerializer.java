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

import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.business.messaging.salesforce.mediator.*;

/**
 * <p>
 * Factory for {@link SalesforceMediator} instances.Builds the
 * <code>SalesforceMediator</code> using the following configuration
 * </p>
 * <p/>
 * <pre>
 ** <salesforce>
     * <configuration repository="" axis2xml=""/>?
     * <{login} type="{Login}">
         * <{username} xmlns:ns="http://wso2.services.sample" xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope" xmlns:ns3="http://org.apache.synapse/xsd" source-xpath="//ns:login/ns:username"/>
         * <{password} xmlns:ns="http://wso2.services.sample" xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope" xmlns:ns3="http://org.apache.synapse/xsd" source-xpath="//ns:login/ns:password"/>
         * <key type="{LoginResponse}"/>
         * <key type="{LoginResponse}"/>
     * </{login}>?
 * </salesforce>
 * </pre>
 */
public class SalesforceSerializer extends AbstractMediatorSerializer {


    @Override
    protected OMElement serializeSpecificMediator(Mediator mediator) {
        SalesforceMediatorSerializer serializer = createSerializer();
        OMElement salesforceElem = serializer.serializeMediator(null, mediator);
        return salesforceElem;
    }

    /**
     * we delegate serialization to <code>SalesforceMediatorSerializer</code> instance
     *
     * @return instance of SalesforceMediatorBuilder
     */
    private SalesforceMediatorSerializer createSerializer() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace synNS = SynapseConstants.SYNAPSE_OMNAMESPACE;
        OMNamespace nullNS
                = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");
        return new SalesforceMediatorSerializer(fac, synNS, nullNS);
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * org.apache.synapse.config.xml.MediatorSerializer#getMediatorClassName()
      */

    public String getMediatorClassName() {
        return SalesforceMediator.class.getName();
    }

}
