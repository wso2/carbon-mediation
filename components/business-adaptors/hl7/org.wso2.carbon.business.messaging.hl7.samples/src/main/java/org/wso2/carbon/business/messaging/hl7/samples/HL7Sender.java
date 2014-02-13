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
package org.wso2.carbon.business.messaging.hl7.samples;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;

import java.io.IOException;

public class HL7Sender {

    public void send(String host, int port) throws HL7Exception {
        System.out.println("[ Executing HL7Sender : HOST:" + host + "  ;port :" + port + " ]");
        // The connection hub connects to listening servers
        ConnectionHub connectionHub = ConnectionHub.getInstance();
        // A connection object represents a socket attached to an HL7 server
        Connection connection = connectionHub
                .attach(host, port, new PipeParser(), MinLowerLayerProtocol.class);

        // The initiator is used to transmit unsolicited messages
        Initiator initiator = connection.getInitiator();
        HL7Message sampleMessage = new HL7Message();

        //send
        Message response = null;
        try {
            response = initiator.sendAndReceive(sampleMessage.getHL7Message());
            PipeParser parser = new PipeParser();
            String responseString = parser.encode(response);
            System.out.println("Received response:\n" + responseString);
        } catch (LLPException e) {
            System.out.println("Error : " + e);
        } catch (IOException e) {
            System.out.println("Error : " + e);
        }

        // Close the connection and server
        connectionHub.discard(connection);
    }

    public static void main(String[] args) throws HL7Exception {
        String host = System.getProperty("hl7-host");
        String port = System.getProperty("hl7-port");
        if (host != null && port != null && !"".equals(host) && !"".equals(port)) {
            new HL7Sender().send(host, Integer.parseInt(port));
        } else {
            new HL7Sender().send("localhost", 9293);
        }
//        new HL7Sender().send("localhost",9988);
    }


}
