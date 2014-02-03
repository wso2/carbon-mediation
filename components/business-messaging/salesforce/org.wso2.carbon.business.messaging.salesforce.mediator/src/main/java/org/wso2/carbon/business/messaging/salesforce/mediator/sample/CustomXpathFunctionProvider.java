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
import org.apache.synapse.util.xpath.ext.SynapseXpathFunctionContextProvider;
import org.jaxen.Function;

import javax.xml.namespace.QName;

public class CustomXpathFunctionProvider implements SynapseXpathFunctionContextProvider {

    public Function getInitializedExtFunction(MessageContext messageContext) {
        CustomGetPropertyFunction propFunction = new CustomGetPropertyFunction(messageContext);
        return propFunction;
    }

    public QName getResolvingQName() {
        return new QName("get-custom");
    }
}
