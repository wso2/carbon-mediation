/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.connector.salesforce;

import java.util.Iterator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

public class SetupDescribeSobjects extends AbstractConnector {

	public void connect(MessageContext synCtx) {

		SynapseLog synLog = getLog(synCtx);

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("Start : Salesforce describe SObjects mediator");

			if (synLog.isTraceTraceEnabled()) {
				synLog.traceTrace("Message : " + synCtx.getEnvelope());
			}
		}

		SOAPEnvelope envelope = synCtx.getEnvelope();
		OMFactory fac = OMAbstractFactory.getOMFactory();
		SOAPBody body = envelope.getBody();
		Iterator<OMElement> bodyChildElements = body.getChildrenWithLocalName("describeSObjects");
		if (bodyChildElements.hasNext()) {
			try {
				OMElement bodyElement = bodyChildElements.next();
				String strSobject = (String) ConnectorUtils.lookupTemplateParamater(synCtx,
						SalesforceUtil.SALESFORCE_SOBJECTS);
				OMElement sObjects = AXIOMUtil.stringToOM(strSobject);
				Iterator<OMElement> sObject = sObjects.getChildElements();
				OMNamespace omNsurn = fac.createOMNamespace("urn:partner.soap.sforce.com", "urn");
				// Loops sObject
				while (sObject.hasNext()) {
					OMElement currentElement = sObject.next();
					OMElement newElement = fac.createOMElement("sObjectType", omNsurn);
					// Add the fields
					newElement.addChild(fac.createOMText(currentElement.getText()));
					bodyElement.addChild(newElement);
				}
			} catch (Exception e) {
				synLog.error("Saleforce adaptor - error injecting sObjects to payload : " + e);
			}
		}

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("End : Salesforce describe SObjects mediator");

			if (synLog.isTraceTraceEnabled()) {
				synLog.traceTrace("Message : " + synCtx.getEnvelope());
			}
		}
	}
}
