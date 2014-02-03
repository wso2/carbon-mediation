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

import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.parser.PipeParser;

import java.lang.management.ManagementFactory;

public class HL7Server {

    private SimpleServer server;

    public HL7Server(int port) {
        LowerLayerProtocol llp = LowerLayerProtocol.makeLLP(); // The transport protocol
        PipeParser parser = new PipeParser();
        server = new SimpleServer(port, llp, parser);
    }

    public void start() {
        /*
        * The server may have any number of "application" objects registered to handle messages. We
        * are going to create an application to listen to ADT^A01 messages.
        */
        Application handler = new SampleApp();
        server.registerApplication("*", "*", handler);

        /*
        * Another option would be to specify a single application to handle all messages, like
        * this:
        *
        * server.registerApplication("*", "*", handler);
        */

        // Start the server listening for messages
        server.start();

    }

    public void stop() {
        server.stop();
    }

    public static void main(String[] args) {
        String port = System.getProperty("hl7-port");
        if (port != null && !"".equals(port)) {
            System.out.println("Server started on Port : " +port + "  Process details : " +
                               ManagementFactory.getRuntimeMXBean().getName());
            new HL7Server(Integer.parseInt(port)).start();
        } else {
            System.out.println("Server started on Port : " +9988 + "  Process details : " +
                               ManagementFactory.getRuntimeMXBean().getName());
            new HL7Server(9988).start();
        }


    }
}
