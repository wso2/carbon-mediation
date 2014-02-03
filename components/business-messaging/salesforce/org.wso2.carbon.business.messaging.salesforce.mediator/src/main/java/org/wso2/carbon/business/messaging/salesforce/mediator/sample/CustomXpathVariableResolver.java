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
package org.wso2.carbon.business.messaging.salesforce.mediator.sample;

import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.ext.SynapseXpathVariableResolver;

import javax.xml.namespace.QName;


public class CustomXpathVariableResolver implements SynapseXpathVariableResolver {
    public static final String SALESFORCE = "SALESFORCE";

    public CustomXpathVariableResolver() {

    }

    public Object resolve(MessageContext messageContext) {

        return messageContext.getProperty(SALESFORCE);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public QName getResolvingQName() {
        QName resolvingQName = new QName(SALESFORCE);
        return resolvingQName;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
