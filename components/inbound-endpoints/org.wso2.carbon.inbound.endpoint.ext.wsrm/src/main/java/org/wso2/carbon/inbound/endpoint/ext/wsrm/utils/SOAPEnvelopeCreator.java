/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.inbound.endpoint.ext.wsrm.utils;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAPEnvelope;

import java.io.IOException;
import java.io.InputStream;

public class SOAPEnvelopeCreator {

    /**
     * Creates a SOAPEnvelope from an InputStream
     *
     * @param inputStream the inputStream
     * @return A SOAP Envelope from the InputStream
     * @throws java.io.IOException
     */
    public static SOAPEnvelope getSOAPEnvelopeFromStream(InputStream inputStream) throws IOException {

        OMXMLParserWrapper soapModelBuilder = OMXMLBuilderFactory.createSOAPModelBuilder(inputStream, "UTF-8");
        inputStream.close();
        OMDocument document = soapModelBuilder.getDocument();
        document.build();
        return (SOAPEnvelope) document.getOMDocumentElement();
    }
}

