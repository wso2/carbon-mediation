/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transports.sap.idoc;

import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.IDocRepository;
import com.sap.conn.idoc.IDocXMLProcessor;
import com.sap.conn.idoc.jco.JCoIDoc;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transports.sap.SAPConstants;
import org.wso2.carbon.transports.sap.idoc.util.IDoCAdapterUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;

/**
 * <p>
 * DefaultIDocXMLMapper basically implements messagecontext to  IDOCs parsing.
 * </p>
 */
public class DefaultIDocXMLMapper implements IDocXMLMapper {

    private Log log = LogFactory.getLog(getClass());

    private IDocXMLProcessor xmlProcessor = JCoIDoc.getIDocFactory().getIDocXMLProcessor();

    public IDocDocumentList getDocumentList(IDocRepository repo,
                                            MessageContext msgContext) throws AxisFault {

        ByteArrayInputStream bais = null;
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            SOAPBody body = msgContext.getEnvelope().getBody();
            OMElement idocElement = body.getFirstElement();
            String stampArcKey = IDoCAdapterUtils.getProperty("stamp_s_arc_key");
            if (stampArcKey == null || !stampArcKey.equals("false")) {
                IDoCAdapterUtils.stampArcKey(idocElement, msgContext.getMessageID());
            }
            idocElement.serialize(baos);
            baos.flush();

            bais = new ByteArrayInputStream(baos.toByteArray());
            Object prop  = msgContext.getOptions().getProperty(
                    SAPConstants.CLIENT_XML_PARSER_OPTIONS);
            if (prop != null) {
                return xmlProcessor.parse(repo, bais, Integer.parseInt(prop.toString()));
            } else {
                return xmlProcessor.parse(repo, bais);
            }
        } catch (Exception e) {
            throw new AxisFault("Error while retrieving IDocs from the message context", e);
        } finally {
            closeStream(bais);
            closeStream(baos);
        }
    }

    private void closeStream(Closeable stream) {
        try {
            stream.close();
        } catch (IOException e) {
            log.error("Error while closing the stream", e);
        }
    }
}
