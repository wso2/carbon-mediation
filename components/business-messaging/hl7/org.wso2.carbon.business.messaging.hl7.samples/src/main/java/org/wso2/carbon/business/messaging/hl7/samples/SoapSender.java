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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;

public class SoapSender {

    public void send(String host, int port, String proxy) throws AxisFault {
        System.out.println("[ Executing SOAPSender : HOST:" + host + "  ;port :" + port + " ;proxy :" + proxy + " ]");
        String repo = getProperty("repoitory", "null");
        String metaDataFile = getProperty("metaFile", "meta/sample_req.xml");


        String addUrl = getProperty("addurl", "null");
        String trpUrl = getProperty("trpurl", "http://" + host + ":" + port + "/services/" + proxy);

        ConfigurationContext configCtx = null;
        Options option = new Options();
        OMElement payLoad = null;
        ServiceClient serviceClient;


        if (repo != null && !"null".equals(repo)) {
            configCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo,
                                                                                             repo + File.separator + "conf" + File.separator + "axis2.xml");
            serviceClient = new ServiceClient(configCtx, null);
        } else {
            serviceClient = new ServiceClient();
        }

        if (metaDataFile != null) {
            payLoad = loadXMLAsElement(metaDataFile);
            if (addUrl != null && !"null".equals(addUrl)) {
                serviceClient.engageModule("addressing");
                option.setTo(new EndpointReference(addUrl));
            }
            if (trpUrl != null && !"null".equals(trpUrl)) {
                option.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
            }
            serviceClient.setOptions(option);
            OMElement result = serviceClient.sendReceive(payLoad);
            System.out.println(("Received reponse from the server:\n" + result));
        } else {
            System.out.println(("The meata data descriptor file is required"));
        }
    }

    private static String getProperty(String name, String defaultVal) {
        String result = System.getProperty(name);
        if (result == null || result.length() == 0) {
            result = defaultVal;
        }
        return result;
    }

    private static OMElement loadXMLAsElement(String file) {
        OMElement rootElement = null;
        try {
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(
                    new FileInputStream(file));
/*
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(
                    ClassLoader.getSystemResourceAsStream(file));
*/
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            rootElement = builder.getDocumentElement();
        } catch (XMLStreamException e) {
            e.printStackTrace();  //TODO: add the exception handling
        } catch (Exception e) {
            e.printStackTrace();  //TODO: add the exception handling
        }
        return rootElement;
    }

    public static void main(String[] args) throws AxisFault {
        String host = System.getProperty("hl7-host");
        String port = System.getProperty("hl7-port");
        String proxy = System.getProperty("hl7-proxy");
        if (host != null && port != null && proxy != null && !"".equals(host) && !"".equals(port) && !"".equals(proxy)) {
            new SoapSender().send(host, Integer.parseInt(port), proxy);
        } else {
            new SoapSender().send("localhost", 8280, "HL7SoapProxy");
        }
    }
}

