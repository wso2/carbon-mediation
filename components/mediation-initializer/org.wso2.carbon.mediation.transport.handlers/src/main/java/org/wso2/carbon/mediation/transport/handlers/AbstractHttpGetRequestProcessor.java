/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mediation.transport.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.ServerConstants;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.xml.namespace.QName;

public abstract class AbstractHttpGetRequestProcessor {

    private static final Log LOG = LogFactory.getLog(AbstractHttpGetRequestProcessor.class);

    public static final QName ITEM_QN = new QName(ServerConstants.CARBON_SERVER_XML_NAMESPACE, "Item");
    public static final QName CLASS_QN = new QName(ServerConstants.CARBON_SERVER_XML_NAMESPACE, "Class");

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String TEXT_HTML = "text/html";
    public static final String FAVICON_ICO = "/favicon.ico";
    public static final String LOCALHOST = "localhost";
    public static final String HOST_NAME = "HostName";
    public static final String WSO2_LOADBALANCER_SYS_PROPERTY = "wso2.loadbalancer";

    private String faviconUrl;
    protected Map<String, org.wso2.carbon.core.transports.HttpGetRequestProcessor> getRequestProcessors =
            new LinkedHashMap<String, org.wso2.carbon.core.transports.HttpGetRequestProcessor>();
    protected ConfigurationContext cfgCtx;

    public void init(ConfigurationContext configurationContext) throws AxisFault {

        this.cfgCtx = configurationContext;
        DataHolder.getInstance().setAxisConfigurationContext(configurationContext);

        if (cfgCtx.getProperty("GETRequestProcessorMap") != null) {
            getRequestProcessors = (Map<String, org.wso2.carbon.core.transports.HttpGetRequestProcessor>)
                    cfgCtx.getProperty("GETRequestProcessorMap");
        } else {
            populateGetRequestProcessors();
        }
    }

    protected void populateGetRequestProcessors() throws AxisFault {
        try {
            OMElement docEle = XMLUtils.toOM(ServerConfiguration.getInstance().getDocumentElement());
            if (docEle != null) {
                SimpleNamespaceContext nsCtx = new SimpleNamespaceContext();
                nsCtx.addNamespace("wsas", ServerConstants.CARBON_SERVER_XML_NAMESPACE);
                XPath xp = new AXIOMXPath("//wsas:HttpGetRequestProcessors/wsas:Processor");
                xp.setNamespaceContext(nsCtx);
                List nodeList = xp.selectNodes(docEle);
                for (Object aNodeList : nodeList) {
                    OMElement processorEle = (OMElement) aNodeList;
                    OMElement itemEle = processorEle.getFirstChildWithName(ITEM_QN);
                    if (itemEle == null) {
                        throw new ServletException("Required element, 'Item' not found!");
                    }
                    OMElement classEle = processorEle.getFirstChildWithName(CLASS_QN);
                    org.wso2.carbon.core.transports.HttpGetRequestProcessor processor;
                    if (classEle == null) {
                        throw new ServletException("Required element, 'Class' not found!");
                    } else {
                        processor =
                                (org.wso2.carbon.core.transports.HttpGetRequestProcessor)
                                        Class.forName(classEle.getText().trim()).newInstance();
                    }
                    getRequestProcessors.put(itemEle.getText().trim(), processor);
                }
            }
        } catch (Exception e) {
            handleException("Error populating GetRequestProcessors", e);
        }
    }

    /**
     * Returns the HTML text for the list of services deployed.
     * This can be delegated to another Class as well
     * where it will handle more options of GET messages.
     *
     * @param prefix to be used for the Service names
     * @return the HTML to be displayed as a String
     */
    protected String getServicesHTML(String prefix) {

        Map services = cfgCtx.getAxisConfiguration().getServices();
        Hashtable erroneousServices = cfgCtx.getAxisConfiguration().getFaultyServices();
        boolean servicesFound = false;

        StringBuffer resultBuf = new StringBuffer();
        resultBuf.append("<html><head><title>Axis2: Services</title></head>" + "<body>");

        if ((services != null) && !services.isEmpty()) {

            servicesFound = true;
            resultBuf.append("<h2>" + "Deployed services" + "</h2>");

            for (Object service : services.values()) {

                AxisService axisService = (AxisService) service;
                Parameter isHiddenService = axisService.getParameter(
                        NhttpConstants.HIDDEN_SERVICE_PARAM_NAME);
                Parameter isAdminService = axisService.getParameter("adminService");
                boolean isClientSide = axisService.isClientSide();

                boolean isSkippedService = (isHiddenService != null &&
                        JavaUtils.isTrueExplicitly(isHiddenService.getValue())) || (isAdminService != null &&
                        JavaUtils.isTrueExplicitly(isAdminService.getValue())) || isClientSide;
                if (axisService.getName().startsWith("__") || isSkippedService) {
                    continue;    // skip private services
                }

                Iterator iterator = axisService.getOperations();
                resultBuf.append("<h3><a href=\"").append(prefix).append(axisService.getName()).append(
                        "?wsdl\">").append(axisService.getName()).append("</a></h3>");

                if (iterator.hasNext()) {
                    resultBuf.append("Available operations <ul>");

                    for (; iterator.hasNext();) {
                        AxisOperation axisOperation = (AxisOperation) iterator.next();
                        resultBuf.append("<li>").append(
                                axisOperation.getName().getLocalPart()).append("</li>");
                    }
                    resultBuf.append("</ul>");
                } else {
                    resultBuf.append("No operations specified for this service");
                }
            }
        }

        if ((erroneousServices != null) && !erroneousServices.isEmpty()) {
            servicesFound = true;
            resultBuf.append("<hr><h2><font color=\"blue\">Faulty Services</font></h2>");
            Enumeration faultyservices = erroneousServices.keys();

            while (faultyservices.hasMoreElements()) {
                String faultyserviceName = (String) faultyservices.nextElement();
                resultBuf.append("<h3><font color=\"blue\">").append(
                        faultyserviceName).append("</font></h3>");
            }
        }

        if (!servicesFound) {
            resultBuf.append("<h2>There are no services deployed</h2>");
        }

        resultBuf.append("</body></html>");
        return resultBuf.toString();
    }

    /**
     * Returns the service name.
     *
     * @param uri HttpRequest uri
     * @return service name as a String
     */
    protected String getServiceName(String uri) {

        String servicePath = getServicePath();

        String serviceName = null;
        if (uri.startsWith(servicePath)) {
            serviceName = uri.substring(servicePath.length());
            if (serviceName.startsWith("/")) {
                serviceName = serviceName.substring(1);
            }
            if (serviceName.contains("?")) {
                serviceName = serviceName.substring(0, serviceName.indexOf("?"));
            }
        } else {
            // this may be a custom URI

            Map serviceURIMap = (Map) cfgCtx.getProperty(NhttpConstants.EPR_TO_SERVICE_NAME_MAP);
            if (serviceURIMap != null) {
                Set keySet = serviceURIMap.keySet();
                for (Object key : keySet) {
                    if (uri.toLowerCase().contains(((String) key).toLowerCase())) {
                        return (String) serviceURIMap.get(key);
                    }
                }
            }
        }

        if (serviceName != null) {
            int opnStart = serviceName.indexOf("/");
            if (opnStart != -1) {
                serviceName = serviceName.substring(0, opnStart);
            }
        }
        return serviceName;
    }

    protected String getServicePath() {
        String servicePath = cfgCtx.getServiceContextPath();
        if (!servicePath.startsWith("/")) {
            servicePath = "/" + servicePath;
        }
        return servicePath;
    }

    /**
     * Returns the favicon URL.
     *
     * @return favicon URL
     */
    protected String getFaviconUrl() {
        if (faviconUrl == null) {
            String hostName = LOCALHOST;
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            if (serverConfig.getFirstProperty(HOST_NAME) != null) {
                hostName = serverConfig.getFirstProperty(HOST_NAME);
            }
            faviconUrl = "https://" +
                    hostName + ":" + System.getProperty("carbon.https.port") + "/carbon/admin/images/favicon.ico";
        }
        return faviconUrl;
    }

    public void handleException(String msg, Exception e) throws AxisFault {
        LOG.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
