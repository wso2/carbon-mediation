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
package org.wso2.carbon.rest.api.ui.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;

import javax.xml.stream.XMLStreamException;
import java.util.*;

public class TestClient {

    private static String resourceTemplate = "<resource xmlns=\"http://ws.apache.org/ns/synapse\" uri-template=\"[1]\" methods=\"[2]\" >\n" +
                                             "\t\t    <inSequence>\n" +
                                             "\t\t    \t<send>\n" +
                                             "\t\t\t\t<endpoint name=\"Delecious\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                                             "\t\t\t\t\t<address uri=\"[3]\"  />\n" +
                                             "\t\t\t\t</endpoint>\n" +
                                             "\t\t\t</send>\t\n" +
                                             "\t\t    </inSequence>\n" +
                                             "\t\t    <outSequence>\n" +
                                             "\t\t    \t<send />\n" +
                                             "\t\t    </outSequence>\n" +
                                             "\t\t</resource>\t";

    private static String apiTemplate = "<api xmlns=\"http://ws.apache.org/ns/synapse\"  name=\"[1]\" context=\"[2]\">\t"
                                        +
                                        "</api>";

    private static String handlersTemplate = "<handlers xmlns=\"http://ws.apache.org/ns/synapse\"> </handlers>";
    private static String handlerTemplate = "<handler xmlns=\"http://ws.apache.org/ns/synapse\" class=\"[1]\" />";

    private Map apiMappings;
    private List<Map> resourceMappings;
    private List<Map> handlerMappings;

    private RestApiAdminStub stub;


    public static final String KEY_FOR_API_NAME = "key_for_api_name";
    public static final String KEY_FOR_API_CONTEXT = "key_for_api_context";

    public static final String KEY_FOR_RESOURCE_URI_TEMPLATE = "key_for_resource_uri_template";
    public static final String KEY_FOR_RESOURCE_METHODS = "key_for_resource_methods";
    public static final String KEY_FOR_RESOURCE_URI = "key_for_resource_uri";

    public static final String KEY_FOR_HANDLER = "key_for_handler_class";

    public TestClient(Map apiMappings, List<Map> resourceMappings, List<Map> handlerMappings, String cookie) throws AxisFault {
        this.apiMappings = apiMappings;
        this.resourceMappings = resourceMappings;
        this.handlerMappings = handlerMappings;
        initStub(cookie);
    }

    private void initStub(String cookie) throws AxisFault {
        String serviceURL = AuthAdminServiceClient.SERVICE_URL + "RestApiAdmin";
        stub = new RestApiAdminStub(null, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void addApi() throws AxisFault {
        try {
            String apiConfig = getConfigStringForTemplate();
            System.out.println(apiConfig);
            stub.addApiFromString(apiConfig);
        } catch (Exception e) {
//			handleException(bundle.getString("could.not.add.api"), e);
        }
    }

    private String getConfigStringForTemplate() {
        String configAPI = constructAPIConfig();
        OMElement configAPIOM = createOMElementFrom(configAPI);
        assert configAPIOM != null;

        List<String> configResources = constructResourceConfig();
        for (String configResource : configResources) {
            OMElement configResourceOM = createOMElementFrom(configResource);
            if (configResourceOM != null) {
                configAPIOM.addChild(configResourceOM);
            }
        }
        List<String> handlerConfigs = constructHandlerConfig();

        OMElement hadlersConfigOM = createOMElementFrom(handlersTemplate);
        for (String handlerConfig : handlerConfigs) {
            OMElement configSingleHandlerOM = createOMElementFrom(handlerConfig);
            if (configSingleHandlerOM != null) {
                hadlersConfigOM.addChild(configSingleHandlerOM);
            }

        }

        configAPIOM.addChild(hadlersConfigOM);
        return configAPIOM.toString();
    }

    private String constructAPIConfig() {
        StringBuffer apiTempl = new StringBuffer(apiTemplate);
        if (apiMappings.get(KEY_FOR_API_NAME) != null && apiMappings.get(KEY_FOR_API_CONTEXT) != null) {
            String apiConf = apiTempl.toString().replaceAll("\\[1\\]", (String) apiMappings.get(KEY_FOR_API_NAME)).
                    replaceAll("\\[2\\]", (String) apiMappings.get(KEY_FOR_API_CONTEXT));
            return apiConf;
        }
        return null;
    }

    private List<String> constructHandlerConfig() {
        Iterator<Map> handlerMaps = handlerMappings.iterator();
        List<String> handlerListStr = new ArrayList<String>();

        while (handlerMaps.hasNext()) {
            Map singleHandler = handlerMaps.next();
            StringBuffer handlerTempl = new StringBuffer(handlerTemplate);
            if (singleHandler != null && singleHandler.get(KEY_FOR_HANDLER) != null) {
                String replacedStr = handlerTempl.toString().replaceAll("\\[1\\]", (String) singleHandler.get(KEY_FOR_HANDLER));
                handlerListStr.add(replacedStr);
            }
        }
        return handlerListStr;
    }

    private List<String> constructResourceConfig() {
        Iterator<Map> resourceMaps = resourceMappings.iterator();
        List<String> resListStr = new ArrayList<String>();

        while (resourceMaps.hasNext()) {
            Map singleResMap = resourceMaps.next();

            StringBuffer resTempl = new StringBuffer(resourceTemplate);

            if (singleResMap != null && singleResMap.get(KEY_FOR_RESOURCE_METHODS) != null &&
                singleResMap.get(KEY_FOR_RESOURCE_URI_TEMPLATE) != null &&
                singleResMap.get(KEY_FOR_RESOURCE_URI) != null) {
                String replacedStr = resTempl.toString().replaceAll("\\[1\\]", (String) singleResMap.get(KEY_FOR_RESOURCE_URI_TEMPLATE)).
                        replaceAll("\\[2\\]", (String) singleResMap.get(KEY_FOR_RESOURCE_METHODS)).
                        replaceAll("\\[3\\]", (String) singleResMap.get(KEY_FOR_RESOURCE_URI));
                resListStr.add(replacedStr);
            }
        }
        return resListStr;
    }

    public static OMElement createOMElementFrom(String omString) {
        try {
            return AXIOMUtil.stringToOM(omString);
        } catch (XMLStreamException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        Map testAPIMappings = new HashMap();
        testAPIMappings.put(KEY_FOR_API_NAME, "DelciousAPI3");
        testAPIMappings.put(KEY_FOR_API_CONTEXT, "/v3");

        Map testResourceMappings_1 = new HashMap();
        testResourceMappings_1.put(KEY_FOR_RESOURCE_URI_TEMPLATE, "/tags/get");
        testResourceMappings_1.put(KEY_FOR_RESOURCE_METHODS, "GET");
        testResourceMappings_1.put(KEY_FOR_RESOURCE_URI, "https://api.del.icio.us");

        Map testResourceMappings_2 = new HashMap();
        testResourceMappings_2.put(KEY_FOR_RESOURCE_URI_TEMPLATE, "/posts/get");
        testResourceMappings_2.put(KEY_FOR_RESOURCE_METHODS, "GET");
        testResourceMappings_2.put(KEY_FOR_RESOURCE_URI, "https://api.del.icio.us");

        Map testResourceMappings_3 = new HashMap();
        testResourceMappings_3.put(KEY_FOR_RESOURCE_URI_TEMPLATE, "/posts/delete?url={posturl}");
        testResourceMappings_3.put(KEY_FOR_RESOURCE_METHODS, "DELETE");
        testResourceMappings_3.put(KEY_FOR_RESOURCE_URI, "https://api.del.icio.us");

        Map testResourceMappings_4 = new HashMap();
        testResourceMappings_4.put(KEY_FOR_RESOURCE_URI_TEMPLATE, "/posts/add?url={posturl};description={desc}");
        testResourceMappings_4.put(KEY_FOR_RESOURCE_METHODS, "POST");
        testResourceMappings_4.put(KEY_FOR_RESOURCE_URI, "https://api.del.icio.us");

        List<Map> resourceMappings = new ArrayList<Map>();
        resourceMappings.add(testResourceMappings_1);
        resourceMappings.add(testResourceMappings_2);
        resourceMappings.add(testResourceMappings_3);
        resourceMappings.add(testResourceMappings_4);

        Map testHandlerMappings_1 = new HashMap();
//        testHandlerMappings_1.put(KEY_FOR_HANDLER, "org.wso2.throttle.Handler");

        List<Map> handlerMappings = new ArrayList<Map>();
        handlerMappings.add(testHandlerMappings_1);

        String adminCookie = null;
        adminCookie = new AuthAdminServiceClient().login(AuthAdminServiceClient.HOST_NAME,
                                                         AuthAdminServiceClient.USER_NAME,
                                                         AuthAdminServiceClient.PASSWORD);

        /* AuthAdminServiceClient.setSystemProperties(AuthAdminServiceClient.CLIENT_TRUST_STORE_PATH,
AuthAdminServiceClient.KEY_STORE_TYPE,
AuthAdminServiceClient.KEY_STORE_PASSWORD);
boolean loggedin = new AuthWrapper().login(AuthAdminServiceClient.HOST_NAME,
        AuthAdminServiceClient.USER_NAME,
        AuthAdminServiceClient.PASSWORD);*/
        if (adminCookie != null) {
            System.out.println("logged in to the back-end server successfully....");
        } else {
            throw new RuntimeException("could not login to the back-end server.... /n  aborting...");
        }

        TestClient restAPIClient = new TestClient(testAPIMappings, resourceMappings, handlerMappings,
                                                  adminCookie);
        restAPIClient.addApi();


    }

}
