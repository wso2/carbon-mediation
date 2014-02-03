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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.jaxen.JaxenException;

import java.util.Iterator;
import java.util.List;

public class SalesforceClassMediator extends AbstractMediator {

    public boolean mediate(MessageContext synCtx) {

        boolean traceOn = isTraceOn(synCtx);
        boolean traceOrDebugOn = isTraceOrDebugOn(traceOn);

        // write log messages
        if (traceOrDebugOn) {
            traceOrDebug(traceOn, "Start : SurchargeStockQuote mediator");

            if (traceOn && trace.isTraceEnabled()) {
                trace.trace("Message : " + synCtx.getEnvelope());
            }
        }

        // get symbol, last elements of SOAP envelope
        SOAPBody body = synCtx.getEnvelope().getBody();

        List symbolElementList = null;
        try {
            AXIOMXPath xPathSymbol = new AXIOMXPath("//ns1:USER/ns2:FirstName");
            xPathSymbol.addNamespace("ns1", "urn:sobject.enterprise.soap.sforce.com");
            xPathSymbol.addNamespace("ns2", "urn:sobject.enterprise.soap.sforce.com");
            symbolElementList = xPathSymbol.selectNodes(body);
            printFirstNames(symbolElementList);

        } catch (JaxenException e) {
            handleException("element symbol error", e, synCtx);
        }

        return true;
    }

    private void printFirstNames(List elementList) {
        Iterator elements = elementList.iterator();
        while (elements.hasNext()) {
            OMElement firstName = (OMElement) elements.next();
            System.out.println("Name :" + firstName.getText());

        }
    }
}
