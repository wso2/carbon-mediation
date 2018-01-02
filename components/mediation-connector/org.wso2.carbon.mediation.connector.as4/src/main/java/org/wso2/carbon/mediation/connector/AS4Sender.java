/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.connector;

import org.apache.axis2.AxisFault;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.mediation.connector.exceptions.AS4ErrorMapper;
import org.wso2.carbon.mediation.connector.exceptions.AS4Exception;
import org.wso2.carbon.mediation.connector.message.AS4Message;
import org.wso2.carbon.mediation.connector.message.util.AS4Utils;
import org.wso2.carbon.mediation.connector.pmode.PModeRepository;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * AS4 Message sender class
 */
public class AS4Sender extends AbstractConnector {

    boolean reportErrorToProducer;

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {

        if (log.isDebugEnabled()) {
            log.debug("AS4 connector sending message");
        }
        String pmode = (String) getParameter(messageContext, "pmode");
        try {
            buildAS4Message(messageContext, pmode);
        } catch (AS4Exception e) {
            if(reportErrorToProducer) {
                AS4ErrorHandler.generateErrorMessage(messageContext, e);
                messageContext.setProperty("AS4_Error", true);
            }
        } catch (Exception e) {
	        throw new ConnectException(e);
        }
    }

    /**
     * Build the AS4 SOAP message
     * @param messageContext {@link MessageContext} object
     * @param pmode PMode agreement
     * @throws AxisFault
     * @throws AS4Exception
     */
    private void buildAS4Message(MessageContext messageContext, String pmode) throws AxisFault, AS4Exception {

        PModeRepository pmodeRepository = PModeRepository.getInstance();
        AS4Message as4Message = new AS4Message(pmode, pmodeRepository);
        reportErrorToProducer = AS4Utils.reportErrorToProducer(as4Message.getPmode());
        try {
            as4Message.generateAS4Message(messageContext);
        } catch (JAXBException e) {
            log.error(e);
            throw new AS4Exception("Processing error : JAXBException", AS4ErrorMapper.ErrorCode.EBMS0004, null);
        } catch (XMLStreamException e) {
            log.error(e);
            throw new AS4Exception("Processing error : XMLStreamException", AS4ErrorMapper.ErrorCode.EBMS0004, null);
        } catch (IOException e) {
            log.error(e);
            throw new AS4Exception("Processing error : IOException", AS4ErrorMapper.ErrorCode.EBMS0004, null);
        }
    }
}