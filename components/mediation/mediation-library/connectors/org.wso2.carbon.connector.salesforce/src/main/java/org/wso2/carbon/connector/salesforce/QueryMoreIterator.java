/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.connector.salesforce;

import java.util.Iterator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.core.AbstractConnector;

/**
 * The QueryMoreIterator inject Payload that can be used with WSO2-ESB Iterator
 * mediator to use Salesforce query more
 */

public class QueryMoreIterator extends AbstractConnector {

	public void connect(MessageContext synCtx) {

		SynapseLog synLog = getLog(synCtx);

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("Start : Salesforce QueryMoreIterator mediator");

			if (synLog.isTraceTraceEnabled()) {
				synLog.traceTrace("Message : " + synCtx.getEnvelope());
			}
		}

		SOAPEnvelope envelope = synCtx.getEnvelope();

		SOAPBody body = envelope.getBody();

		Iterator<OMElement> bodyChildElements = body.getChildElements();

		if (bodyChildElements.hasNext()) {
			try {
				String strQueryLocator = (String) synCtx
						.getProperty("salesforce.query.queryLocator");
				String strRecordSize = (String) synCtx.getProperty("salesforce.query.recordSize");

				String[] arrQueryLocator = strQueryLocator.split("-");

				if (arrQueryLocator.length >= 2) {
					int iBatchSize = Integer.valueOf(arrQueryLocator[1]);
					int iRecordSize = Integer.valueOf(strRecordSize);
					int fPageSize = iRecordSize / iBatchSize;
					if ((fPageSize * iBatchSize) < iRecordSize) {
						fPageSize++;
					}
					if (fPageSize >= 2) {
						OMElement bodyElement = bodyChildElements.next();

						OMFactory fac = OMAbstractFactory.getOMFactory();
						OMNamespace omNs = fac.createOMNamespace(
								"http://wso2.org/salesforce/adaptor", "sfdc");
						OMElement value = fac.createOMElement("iterators", omNs);
						for (int i = 2; i <= fPageSize; i++) {
							OMElement subValue = fac.createOMElement("iterator", omNs);
							subValue.addChild(fac.createOMText(value, "Page" + i));
							value.addChild(subValue);
						}
						bodyElement.addChild(value);
					}
				}
			} catch (NumberFormatException nfe) {
				synLog.auditWarn("Saleforce adaptor - invalid value returned : " + nfe);
			} catch (Exception e) {
				synLog.error("Saleforce adaptor - error generating the iterator payload : " + e);
			}

		}

		if (synLog.isTraceOrDebugEnabled()) {
			synLog.traceOrDebug("End : Salesforce QueryMoreIterator mediator");

			if (synLog.isTraceTraceEnabled()) {
				synLog.traceTrace("Message : " + synCtx.getEnvelope());
			}
		}
	}

}
