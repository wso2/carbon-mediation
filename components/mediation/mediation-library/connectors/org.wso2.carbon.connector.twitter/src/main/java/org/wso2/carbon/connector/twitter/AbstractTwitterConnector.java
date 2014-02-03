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

package org.wso2.carbon.connector.twitter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonBuilder;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.connector.core.AbstractConnector;

public abstract class AbstractTwitterConnector extends AbstractConnector {

	/**
	 * Util method which converts the the json to xml string format
	 * 
	 * @param sb
	 * @return
	 * @throws JSONException
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public OMElement parseJsonToXml(String sb) throws JSONException, XMLStreamException,
	                                          IOException {
		StringWriter sw = new StringWriter(5120);
		OMElement elm =JsonBuilder.toXml(new ByteArrayInputStream(sb.getBytes()), false);
		//OMElement element = AXIOMUtil.stringToOM(sw.toString());
		return elm;
	}

    protected void preparePayload(MessageContext messageContext, OMElement element) {
	SOAPBody soapBody = messageContext.getEnvelope().getBody();
	for (Iterator itr = soapBody.getChildElements(); itr.hasNext();) {
	    OMElement child = (OMElement) itr.next();
	    child.detach();
	}
	for (Iterator itr = element.getChildElements(); itr.hasNext();) {
	    OMElement child = (OMElement) itr.next();
	    soapBody.addChild(child);
	}
    }

}
