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
package org.wso2.carbon.mediator.cache.util;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.saaj.MessageFactoryImpl;
import org.apache.axis2.saaj.util.IDGenerator;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This class provides utility method to building soap messages.
 */
public class SOAPMessageHelper {

	/**
	 *  Builds the SOAPEnvelope from provided bytes and returns the OM representation of SOAPEnvelope
	 *
	 * @param data byte array of the SOAPEnvelope
	 * @param isSoap11 whether the SOAPEnvelope format is SOAP11 or not
	 * @return OM representation of SOAPEnvelope
	 * @throws SOAPException When SOAPEnvelope cannot be created from bytes due to SOAP exception
	 * @throws IOException When SOAPEnvelope cannot be created from bytes due to IO exception
	 */
	public static SOAPEnvelope buildSOAPEnvelopeFromBytes(byte[] data, boolean isSoap11)
			throws SOAPException, IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		try {
			MessageFactory mf = new MessageFactoryImpl();
			if (!isSoap11) {
				MimeHeaders mimeHeaders = new MimeHeaders();
				mimeHeaders.addHeader("Content-ID", IDGenerator.generateID());
				mimeHeaders.addHeader("content-type",
				                      HTTPConstants.MEDIA_TYPE_APPLICATION_SOAP_XML);
				SOAPMessage smsg = mf.createMessage(mimeHeaders, byteArrayInputStream);
				return SAAJUtil.toOMSOAPEnvelope(smsg.getSOAPPart().getDocumentElement());
			} else {
				if (data != null) {
					SOAPMessage smsg = mf.createMessage(new MimeHeaders(), byteArrayInputStream);
					return SAAJUtil.toOMSOAPEnvelope(smsg.getSOAPPart().getDocumentElement());
				} else {
					return null;
				}
			}
		} finally {
			byteArrayInputStream.close();
		}
	}
}
