/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.proxyadmin.ui.client;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.core.axis2.ProxyService;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;
import org.wso2.carbon.proxyadmin.stub.types.carbon.Entry;
import org.wso2.carbon.proxyadmin.stub.types.carbon.MetaData;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyServicePolicyInfo;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

public class ProxyServiceAdminClient {
    private static final String FAILED = "failed";
    private static final Log log = LogFactory.getLog(ProxyServiceAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.proxyadmin.ui.i18n.Resources";
    private ResourceBundle bundle;
    public ProxyServiceAdminStub stub;

    public ProxyServiceAdminClient(ConfigurationContext configCtx, String backendServerURL,
                                   String cookie, Locale locale) throws AxisFault {
        bundle = ResourceBundle.getBundle(BUNDLE, locale);
        String serviceURL = backendServerURL + "ProxyServiceAdmin";
        stub = new ProxyServiceAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String disableStatistics(String proxyName60) throws AxisFault {
        try {
            return stub.disableStatistics(proxyName60);
        } catch (Exception e) {
            handleException(MessageFormat.format(
                    bundle.getString("unable.to.disable.statistics.for.proxy.service"), proxyName60), e);
        }
        return FAILED;
    }

    public String[] getAvailableEndpoints() throws AxisFault {
        try {
            return stub.getAvailableEndpoints();
        } catch (Exception e) {
            handleException(bundle.getString("unable.to.get.declared.endpoints"), e);
        }
        return null;
    }

    public String redeployProxyService(String proxyName65) throws AxisFault {
        try {
            return stub.redeployProxyService(proxyName65);
        } catch (Exception e) {
            handleException(MessageFormat.format(bundle.getString("unable.to.redeploy.proxy.service"), proxyName65), e);
        }
        return FAILED;
    }

    public void addProxy(ProxyData pd67) throws AxisFault {
        try {
            stub.addProxy(pd67);
        } catch (AxisFault af) {
        	String message = af.getMessage();
        	ProxyServiceAdminProxyAdminException proxyAdminException = new ProxyServiceAdminProxyAdminException(message);
            handleException(message, proxyAdminException);        	
        } catch (Exception e) {
        	String message = MessageFormat.format(bundle.getString("unable.to.add.proxy.service"), pd67.getName());
        	ProxyServiceAdminProxyAdminException proxyAdminException = new ProxyServiceAdminProxyAdminException(message);
            handleException(message, proxyAdminException);
        }
    }

    public void modifyProxy(ProxyData pd69) throws AxisFault {
        try {
            stub.modifyProxy(pd69);
        } catch (Exception e) {
            handleException(MessageFormat.format(bundle.getString("unable.to.modify.proxy.service"), pd69.getName()), e);
        }
    }

    public String disableTracing(String proxyName71) throws AxisFault {
        try {
            return stub.disableTracing(proxyName71);
        } catch (Exception e) {
            handleException(MessageFormat.format(
                    bundle.getString("unable.to.disable.tracing.for.proxy.service"), proxyName71), e);
        }
        return FAILED;
    }

    public MetaData getMetaData() throws AxisFault {
        try {
            return stub.getMetaData();
        } catch (Exception e) {
            handleException(bundle.getString("unable.to.get.metadata"), e);
        }
        return null;
    }


    public ProxyData getProxy(String proxyName73) throws AxisFault {
        try {
            return stub.getProxy(proxyName73);
        } catch (Exception e) {
            handleException(MessageFormat.format(
                    bundle.getString("unable.to.retrieve.data.for.proxy.service"), proxyName73), e);
        }
        return null;
    }

    public String[] getAvailableTransports() throws AxisFault {
        try {
            return stub.getAvailableTransports();
        } catch (Exception e) {
            handleException(bundle.getString("unable.to.get.available.transports"), e);
        }
        return null;
    }

    public void startProxyService(String proxyName80) throws AxisFault {
        try {
            stub.startProxyService(proxyName80);
        } catch (Exception e) {
            handleException(MessageFormat.format(bundle.getString("unable.to.start.proxy.service"), proxyName80), e);
        }
    }

    public String enableTracing(String proxyName82) throws AxisFault {
        try {
            return stub.enableTracing(proxyName82);
        } catch (Exception e) {
            handleException(MessageFormat.format(
                    bundle.getString("unable.to.enable.tracing.for.proxy.service"), proxyName82), e);
        }
        return FAILED;
    }

    public String getSourceView(ProxyData pd84) throws AxisFault {
        try {
            return stub.getSourceView(pd84);
        } catch (Exception e) {
            handleException(bundle.getString("unable.to.generate.the.source.for.the.given.design"), e);
        }
        return null;
    }

    /**
     * Populates a proxy data object based on the given XML. This mehtod will not validate any information
     * since this may be called with incomplete information
     * @param proxyXML88 proxy service configuration
     * @return the <code>ProxyData</code> object populated with given information
     * @throws AxisFault if the given XML cannot be parsed
     */
    public ProxyData getDesignView(String proxyXML88) throws AxisFault {
        ProxyData pd = new ProxyData();
        try {
           	byte[] bytes = null;
        	try {
    			bytes = proxyXML88.getBytes("UTF8");
    		} catch (UnsupportedEncodingException e) {
    			log.error("Unable to extract bytes in UTF-8 encoding. " + 
    					"Extracting bytes in the system default encoding" + e.getMessage());
    			bytes = proxyXML88.getBytes();
    		}        	
            OMElement elem = new StAXOMBuilder(
                    new ByteArrayInputStream(bytes)).getDocumentElement();

            // check whether synapse namespace is present in the configuration.
            Iterator itr = elem.getAllDeclaredNamespaces();
            OMNamespace ns;
            boolean synapseNSPresent = false;
            while (itr.hasNext()) {
                ns = (OMNamespace)itr.next();
                if (XMLConfigConstants.SYNAPSE_NAMESPACE.equals(ns.getNamespaceURI())) {
                    synapseNSPresent = true;
                    break;
                }
            }
            if (!synapseNSPresent) {
                // Oops! synpase namespace is not present
                throw new AxisFault(bundle.getString("synapse.namespace.not.present"));
            }
            OMAttribute name = elem.getAttribute(new QName("name"));
            if (name != null) {
                pd.setName(name.getAttributeValue());
            }

            OMAttribute statistics = elem.getAttribute(new QName(XMLConfigConstants.STATISTICS_ATTRIB_NAME));
            if (statistics != null) {
                String statisticsValue = statistics.getAttributeValue();
                if (statisticsValue != null) {
                    if (XMLConfigConstants.STATISTICS_ENABLE.equals(statisticsValue)) {
                        pd.setEnableStatistics(true);
                    } else if (XMLConfigConstants.STATISTICS_DISABLE.equals(statisticsValue)) {
                        pd.setEnableStatistics(false);
                    }
                }
            }

            OMAttribute trans = elem.getAttribute(new QName("transports"));
            if (trans != null) {
                String transports = trans.getAttributeValue();
                if (transports == null || "".equals(transports) ||ProxyService.ALL_TRANSPORTS.equals(transports)) {
                    // default to all transports using service name as destination
                } else {
                    String [] arr = transports.split(",");
                    if (arr != null && arr.length != 0) {
                        pd.setTransports(arr);
                    }
                }
            }

            OMAttribute pinnedServers = elem.getAttribute(new QName("pinnedServers"));
            if (pinnedServers != null) {
                String pinnedServersValue = pinnedServers.getAttributeValue();
                if (pinnedServersValue == null || "".equals(pinnedServersValue)) {
                    // default to all servers
                } else {
                    String [] arr = pinnedServersValue.split(",");
                    if (arr != null && arr.length != 0) {
                        pd.setPinnedServers(arr);
                    }
                }
            }

            OMAttribute trace = elem.getAttribute(new QName(XMLConfigConstants.TRACE_ATTRIB_NAME));
            if (trace != null) {
                String traceValue = trace.getAttributeValue();
                if (traceValue != null) {
                    if (traceValue.equals(XMLConfigConstants.TRACE_ENABLE)) {
                        pd.setEnableTracing(true);
                    } else if (traceValue.equals(XMLConfigConstants.TRACE_DISABLE)) {
                        pd.setEnableTracing(false);
                    }
                }
            }

            OMAttribute startOnLoad = elem.getAttribute(new QName("startOnLoad"));
            String val;
            if (startOnLoad != null && (val = startOnLoad.getAttributeValue()) != null && !"".equals(val)) {
                pd.setStartOnLoad(Boolean.valueOf(val));
            } else {
                pd.setStartOnLoad(true);
            }

            // read definition of the target of this proxy service. The target could be an 'endpoint'
            // or a named sequence. If none of these are specified, the messages would be mediated
            // by the Synapse main mediator
            OMElement target = elem.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "target"));
            if (target != null) {
                OMAttribute inSequence = target.getAttribute(new QName("inSequence"));
                if (inSequence != null) {
                    pd.setInSeqKey(inSequence.getAttributeValue());
                } else {
                    OMElement inSequenceElement = target.getFirstChildWithName(
                            new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "inSequence"));
                    if (inSequenceElement != null) {
                        pd.setInSeqXML(inSequenceElement.toString());
                    }
                }
                OMAttribute outSequence = target.getAttribute(new QName("outSequence"));
                if (outSequence != null) {
                    pd.setOutSeqKey(outSequence.getAttributeValue());
                } else {
                    OMElement outSequenceElement = target.getFirstChildWithName(
                            new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "outSequence"));
                    if (outSequenceElement != null) {
                        pd.setOutSeqXML(outSequenceElement.toString());
                    }
                }
                OMAttribute faultSequence = target.getAttribute(new QName("faultSequence"));
                if (faultSequence != null) {
                    pd.setFaultSeqKey(faultSequence.getAttributeValue());
                } else {
                    OMElement faultSequenceElement = target.getFirstChildWithName(
                            new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "faultSequence"));
                    if (faultSequenceElement != null) {
                        pd.setFaultSeqXML(faultSequenceElement.toString());
                    }
                }
                OMAttribute tgtEndpt = target.getAttribute(new QName("endpoint"));
                if (tgtEndpt != null) {
                    pd.setEndpointKey(tgtEndpt.getAttributeValue());
                } else {
                    OMElement endpointElement = target.getFirstChildWithName(
                            new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "endpoint"));
                    if (endpointElement != null) {
                        pd.setEndpointXML(endpointElement.toString());
                    }
                }
            }

            Iterator props = elem.getChildrenWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "parameter"));
            ArrayList <Entry> params = new ArrayList<Entry>();
            Entry entry = null;
            while (props.hasNext()) {
                Object o = props.next();
                if (o instanceof OMElement) {
                    OMElement prop = (OMElement) o;
                    OMAttribute pname = prop.getAttribute(new QName("name"));
                    OMElement propertyValue = prop.getFirstElement();
                    if (pname != null) {
                        if (propertyValue != null) {
                            entry = new Entry();
                            entry.setKey(pname.getAttributeValue());
                            entry.setValue(propertyValue.toString());
                            params.add(entry);
                        } else {
                            entry = new Entry();
                            entry.setKey(pname.getAttributeValue());
                            entry.setValue(prop.getText().trim());
                            params.add(entry);
                        }
                    }
                }
            }
            pd.setServiceParams(params.toArray(new Entry[params.size()]));


            OMElement wsdl = elem.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "publishWSDL"));
            if (wsdl != null) {
                OMAttribute wsdlkey = wsdl.getAttribute(
                        new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));
                OMAttribute wsdlEP = wsdl.getAttribute(
                        new QName(XMLConfigConstants.NULL_NAMESPACE, "endpoint"));
                if(wsdlEP != null){
                    pd.setPublishWSDLEndpoint(wsdlEP.getAttributeValue());
                } else if (wsdlkey != null) {
                    pd.setWsdlKey(wsdlkey.getAttributeValue());
                } else {
                    OMAttribute wsdlURI = wsdl.getAttribute(
                            new QName(XMLConfigConstants.NULL_NAMESPACE, "uri"));
                    if (wsdlURI != null) {
                        pd.setWsdlURI(wsdlURI.getAttributeValue());
                    } else {
                        OMElement wsdl11 = wsdl.getFirstChildWithName(
                                new QName(WSDLConstants.WSDL1_1_NAMESPACE, "definitions"));
                        String wsdlDef;
                        if (wsdl11 != null) {
                            wsdlDef = wsdl11.toString().replaceAll("\n|\\r|\\f|\\t", "");
                            wsdlDef = wsdlDef.replaceAll("> +<", "><");
                            pd.setWsdlDef(wsdlDef);
                        } else {
                            OMElement wsdl20 = wsdl.getFirstChildWithName(
                                    new QName(WSDL2Constants.WSDL_NAMESPACE, "description"));
                            if (wsdl20 != null) {
                                wsdlDef = wsdl20.toString().replaceAll("\n|\\r|\\f|\\t", "");
                                wsdlDef = wsdlDef.replaceAll("> +<", "><");
                                pd.setWsdlDef(wsdlDef);
                            }
                        }
                    }
                }

                Iterator it = wsdl.getChildrenWithName(
                        new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "resource"));
                ArrayList <Entry> resources = new ArrayList<Entry>();
                Entry resource = null;
                while (it.hasNext()) {
                    OMElement resourceElem = (OMElement)it.next();
                    OMAttribute location = resourceElem.getAttribute
                            (new QName(XMLConfigConstants.NULL_NAMESPACE, "location"));
                    if (location == null) {
                        // todo handle exception
                    }
                    OMAttribute key = resourceElem.getAttribute(
                            new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));
                    if (key == null) {
                        // todo handle exception
                    }
                    resource = new Entry();
                    resource.setKey(location.getAttributeValue());
                    resource.setValue(key.getAttributeValue());
                    resources.add(resource);
                }
                pd.setWsdlResources(resources.toArray(new Entry[resources.size()]));
            }

            OMElement enableSec = elem.getFirstChildWithName(new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                    "enableSec"));
            if (enableSec != null) {
                pd.setEnableSecurity(true);
            }

            OMElement description = elem.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "description"));
            if (description != null) {
                pd.setDescription(description.getText());
            }

            Iterator policies = elem.getChildrenWithName(new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,"policy"));
            while (policies.hasNext()){
                OMElement policyElement = (OMElement)policies.next();
                String policyKey = policyElement.getAttributeValue(new QName("key"));
                ProxyServicePolicyInfo policyInfo = new ProxyServicePolicyInfo();
                policyInfo.setKey(policyKey);
                pd.addPolicies(policyInfo);
            }

        } catch (XMLStreamException e) {
            handleException(bundle.getString("unable.to.build.the.design.view.from.the.given.xml"), e);
        }
        return pd;
    }

    public void stopProxyService(String proxyName92) throws AxisFault {
        try {
            stub.stopProxyService(proxyName92);
        } catch (Exception e) {
            handleException(MessageFormat.format(bundle.getString("unable.to.stop.proxy.service"), proxyName92), e);
        }
    }

    public String enableStatistics(String proxyName94) throws AxisFault {
        try {
            return stub.enableStatistics(proxyName94);
        } catch (Exception e) {
            handleException(MessageFormat.format(
                    bundle.getString("unable.to.enable.statistics.for.proxy"), proxyName94), e);
        }
        return FAILED;
    }

    public String[] getAvailableSequences() throws AxisFault {
        try {
            return stub.getAvailableSequences();
        } catch (Exception e) {
            handleException(bundle.getString("unable.to.get.declared.sequences"), e);
        }
        return null;
    }

    public void deleteProxyService(String proxyName99) throws AxisFault {
        try {
            stub.deleteProxyService(proxyName99);
        } catch (Exception e) {
            handleException(MessageFormat.format(bundle.getString("unable.to.delete.proxy.service"), proxyName99), e);
        }
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
