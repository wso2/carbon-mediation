package org.wso2.business.messaging.salesforce.mediator.samples.test;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.business.messaging.salesforce.mediator.samples.test.factory.PayloadFactory;

import javax.xml.stream.XMLStreamException;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Oct 13, 2010
 * Time: 7:09:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class SalesforceProxyClient {

    public static void main(String[] args) {

        System.out.println("Start");
        String password = "Welcomeusw1233MmrAxtmUed7JGOwbUsfhXg1v";
        String username = "udayangaw@wso2.com";
        String query = "1265369211";
        try {

            PayloadFactory factory = PayloadFactory.getInstance(PayloadFactory.SAMPLE1_REQ);
            executeClient(factory, username, password);
//              executeMockClient();
            /*PayloadFactory factory = PayloadFactory.getInstance(PayloadFactory.LOGIN_REQ);
            executeClient(factory, username, password);

            PayloadFactory factory2 = PayloadFactory.getInstance(PayloadFactory.QUERY_REQ);
            executeClient(factory2,query);

            PayloadFactory factory = PayloadFactory.getInstance(PayloadFactory.LOGOUT_REQ);
            executeClient(factory);
*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("End");
        System.exit(0);
    }

    private static void printResult(OMElement result) throws Exception {
        System.out.println("**** SalesForce API Call ****");
        System.out.println("response SOAP : " + result);
        System.out.println("********* Thank You *********");
    }

    public static void executeClient(PayloadFactory ploadFactory, Object... params) throws Exception {


        String soapVer = "soap11";
		String trpUrl =  "http://localhost:8280/services/sforce";
//		String trpUrl =  "http://localhost:8280/services/salesforce";
//        String trpUrl = "http://localhost:8280/services/stub";

        String repo = "/home/usw/axis_demo/traning/binary/axis2-SNAPSHOT/repository/";

        ConfigurationContext configContext = null;

        Options options = new Options();
        OMElement payload = null;
        ServiceClient serviceClient;
        
        if (repo != null && !"null".equals(repo)) {
            configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(repo, repo
                                                                    + "../conf/axis2.xml");
            serviceClient = new ServiceClient(configContext, null);
        } else {
            serviceClient = new ServiceClient();
        }
        
        payload = ploadFactory.createPayload(params);
        System.out.println("Payload :" + payload);
        if (trpUrl != null && !"null".equals(trpUrl)) {
            options.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
        }

        if ("soap12".equals(soapVer)) {
            options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        }

        serviceClient.setOptions(options);

        OMElement element = serviceClient.sendReceive(payload);
        //serviceClient.fireAndForget(payload);
        printResult(element);

        Thread.sleep(3600);

        serviceClient.cleanup();
    }

}
