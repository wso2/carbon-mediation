/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediator.machinelearner.util;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

public class ResponseBuilder {

    public void buildResponseElement(String response, MessageContext messageContext, SynapseXPath responseVariableXpath, SynapseLog synLog) throws JaxenException {

        Object targetElement = responseVariableXpath.selectSingleNode(messageContext);
        if(targetElement == null) {
            //TODO insert or replace element
//            insertElement(sourceNodeList, targetElem, synLog);
        } else {
            replaceElement(response, targetElement, synLog);
        }

        System.out.println("Test : " + messageContext);
    }

    /**
     * Replace the value of the target element with the response value
     * @param response
     * @param targetElement
     * @param synLog
     */
    private void replaceElement(String response, Object targetElement, SynapseLog synLog) {

        System.out.println("class ============= " + targetElement.getClass());;
        if(targetElement instanceof SOAPBody) {
            ((SOAPBody) targetElement).setText(response);
        }
        else if (targetElement instanceof OMElement) {
            OMElement targetElem = (OMElement) targetElement;
            targetElem.setText(response);
        } else if (targetElement instanceof OMText) {
            OMText targetText = (OMText) targetElement;
            if (targetText.getParent() != null) {
                Object parent = targetText.getParent();
                if (parent instanceof OMElement) {
                    ((OMElement)parent).setText(response);
                }
            }
        } else if (targetElement instanceof OMAttribute) {
            OMAttribute attribute = (OMAttribute) targetElement;
            attribute.setAttributeValue(response);
        } else {
            synLog.error("Invalid Response Element");
            throw new SynapseException("Invalid Response Element");
        }
    }
}
