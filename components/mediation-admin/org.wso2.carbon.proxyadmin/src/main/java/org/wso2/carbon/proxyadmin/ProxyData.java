/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.proxyadmin;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class ProxyData {
    private static Log log = LogFactory.getLog(ProxyData.class);
    private String name;
    private boolean running;
    private boolean enableStatistics;
    private boolean enableTracing;
    private boolean wsdlAvailable;
    // the array of transports, empty list means expose the proxy service via all possible transports
    private String[] transports;
    // the array of pinned servers
    private String[] pinnedServers;
    // the endpoint key (if the endpoint is stored in the registry)
    private String endpointKey;
    // the endpoint XML string (if the endpoint is specified as anonymous)
    private String endpointXML;
    // the in sequence key (if the in sequence is stored in the registry)
    private String inSeqKey;
    // the in sequence XML string (if the in sequence is specified as anonymous)
    private String inSeqXML;
    // the out sequence key (if the out sequence is stored in the registry)
    private String outSeqKey;
    // the out sequence XML string (if the out sequence is specified as anonymous)
    private String outSeqXML;
    // the fault sequence key (if the fault sequence is stored in the registry)
    private String faultSeqKey;
    // the fault sequence XML string (if the fault sequence is specified as anonymous)
    private String faultSeqXML;
    // the WSDL key if in registry
    private String wsdlKey;
    // endpoint which used to fetch WSDL
    private String publishWSDLEndpoint;
    // the WSDL URI if given as an URI
    private String wsdlURI;
    // the inline WSDL definition
    private String wsdlDef;
    // additional resources required by the WSDL
    private Entry [] wsdlResources;

    // true if either wsdlKey, wsdlURI or wsdlDef is present
    private boolean publishWSDL;
    // true by default
    private boolean startOnLoad = true;
    // service parameter map
    Entry [] serviceParams;

    private boolean enableSecurity;
    private ProxyServicePolicyInfo[] policies;

    private String serviceGroup;

    private String description;

    /** name of the artifact container from which the proxy deployed */
    private String artifactContainerName;

    /** Whether the artifact is edited via the management console or not */
    private boolean isEdited;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isWsdlAvailable() {
        return wsdlAvailable;
    }

    public void setWsdlAvailable(boolean wsdlAvailable) {
        this.wsdlAvailable = wsdlAvailable;
    }

    public boolean isEnableTracing() {
        return enableTracing;
    }

    public void setEnableTracing(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }

    public boolean isEnableStatistics() {
        return enableStatistics;
    }

    public void setEnableStatistics(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTransports(String [] transports) {
        this.transports = transports;
    }

    public String [] getTransports() {
        return transports;
    }

    public String [] getPinnedServers() {
        return pinnedServers;
    }

    public void setPinnedServers(String [] pinnedServers) {
        this.pinnedServers = pinnedServers;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
    }

    public String getEndpointXML() {
        return endpointXML;
    }

    public void setEndpointXML(String endpointXML) {
        this.endpointXML = endpointXML;
    }

    public String getInSeqKey() {
        return inSeqKey;
    }

    public void setInSeqKey(String inSeqKey) {
        this.inSeqKey = inSeqKey;
    }

    public String getInSeqXML() {
        return inSeqXML;
    }

    public void setInSeqXML(String inSeqXML) {
        this.inSeqXML = inSeqXML;
    }

    public String getOutSeqKey() {
        return outSeqKey;
    }

    public void setOutSeqKey(String outSeqtKey) {
        this.outSeqKey = outSeqtKey;
    }

    public String getOutSeqXML() {
        return outSeqXML;
    }

    public void setOutSeqXML(String outSeqXML) {
        this.outSeqXML = outSeqXML;
    }

    public String getFaultSeqKey() {
        return faultSeqKey;
    }

    public void setFaultSeqKey(String faultSeqKey) {
        this.faultSeqKey = faultSeqKey;
    }

    public String getFaultSeqXML() {
        return faultSeqXML;
    }

    public void setFaultSeqXML(String faultSeqXML) {
        this.faultSeqXML = faultSeqXML;
    }

    public String getWsdlKey() {
        return wsdlKey;
    }

    public void setWsdlKey(String wsdlKey) {
        this.wsdlKey = wsdlKey;
    }

    public String getWsdlURI() {
        return wsdlURI;
    }

    public void setWsdlURI(String wsdlURI) {
        this.wsdlURI = wsdlURI;
    }

    public String getPublishWSDLEndpoint() {
        return publishWSDLEndpoint;
    }

    public void setPublishWSDLEndpoint(String publishWSDLEndpoint) {
        this.publishWSDLEndpoint = publishWSDLEndpoint;
    }


    public Entry [] getWsdlResources() {
        return wsdlResources;
    }

    public void setWsdlResources(Entry [] wsdlResources) {
        this.wsdlResources = wsdlResources;
    }

    public Entry[] getServiceParams() {
        return serviceParams;
    }

    public void setServiceParams(Entry [] serviceParams) {
        this.serviceParams = serviceParams;
    }

    public String getWsdlDef() {
        return wsdlDef;
    }

    public void setWsdlDef(String wsdlDef) {
        this.wsdlDef = wsdlDef;
    }

    public boolean isStartOnLoad() {
        return startOnLoad;
    }

    public void setStartOnLoad(boolean startOnLoad) {
        this.startOnLoad = startOnLoad;
    }

    public ProxyServicePolicyInfo[] getPolicies() {
        return policies;
    }

    public void setPolicies(ProxyServicePolicyInfo[] policies) {
        this.policies = policies;
    }

    public boolean isEnableSecurity() {
        return enableSecurity;
    }

    public void setEnableSecurity(boolean enableSecurity) {
        this.enableSecurity = enableSecurity;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public OMElement retrieveOM() throws ProxyAdminException {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace syn = SynapseConstants.SYNAPSE_OMNAMESPACE;
        OMNamespace nullNS = fac.createOMNamespace("","");

        OMElement proxy = fac.createOMElement("proxy", syn);
        proxy.addAttribute("name", name, nullNS);

        // adds any transports listed in the configuration
        if (transports != null) {
            proxy.addAttribute("transports", createCSString(transports), nullNS);
        }

        if (pinnedServers != null) {
            proxy.addAttribute("pinnedServers", createCSString(pinnedServers), nullNS);
        }

        if (serviceGroup != null) {
            proxy.addAttribute("serviceGroup", serviceGroup, nullNS);
        }

        // adds the state of statistics and tracing
        if (enableStatistics) {
            proxy.addAttribute(XMLConfigConstants.STATISTICS_ATTRIB_NAME,
                    XMLConfigConstants.STATISTICS_ENABLE, nullNS);
        } else {
            proxy.addAttribute(XMLConfigConstants.STATISTICS_ATTRIB_NAME,
                    XMLConfigConstants.STATISTICS_DISABLE, nullNS);
        }

        if (enableTracing) {
            proxy.addAttribute(XMLConfigConstants.TRACE_ATTRIB_NAME, XMLConfigConstants.TRACE_ENABLE, nullNS);
        } else {
            proxy.addAttribute(XMLConfigConstants.TRACE_ATTRIB_NAME, XMLConfigConstants.TRACE_DISABLE, nullNS);
        }

        if (startOnLoad) {
            proxy.addAttribute("startOnLoad", "true", nullNS);
        } else {
            proxy.addAttribute("startOnLoad", "false", nullNS);
        }

        // creates the target section of the configuration
        OMElement element = fac.createOMElement("target", syn);
        if (inSeqKey != null) {
            element.addAttribute("inSequence", inSeqKey, nullNS);
        } else if (inSeqXML != null) {
            try {
                element.addChild(createElement(inSeqXML));
            } catch (XMLStreamException e) {
                String msg = "Unable to build the \"inSequence\" element";
                log.error(msg + e.getMessage());
                throw new ProxyAdminException(msg, e);
            }
        } 

        if (outSeqKey != null) {
            element.addAttribute("outSequence", outSeqKey, nullNS);
        } else if (outSeqXML != null) {
            try {
                element.addChild(createElement(outSeqXML));
            } catch (XMLStreamException e) {
                String msg = "Unable to build the \"outSequence\" element";
                log.error(msg + e.getMessage());
                throw new ProxyAdminException(msg, e);
            }
        }

        if (faultSeqKey != null) {
            element.addAttribute("faultSequence", faultSeqKey, nullNS);
        } else if (faultSeqXML != null) {
            try {
                element.addChild(createElement(faultSeqXML));
            } catch (XMLStreamException e) {
                String msg = "Unable to build the \"faultSequence\" element";
                log.error(msg + e.getMessage());
                throw new ProxyAdminException(msg, e);
            }
        }

        if (endpointKey != null) {
            element.addAttribute("endpoint", endpointKey, nullNS);
        } else if (endpointXML != null && !"".equals(endpointXML)) {
            try {
                element.addChild(createElement(endpointXML));
            } catch (XMLStreamException e) {
                String msg = "Unable to build the \"endpoint\" element";
                log.error(msg + e.getMessage());
                throw new ProxyAdminException(msg, e);
            }
        }
        proxy.addChild(element);

        // creates the publishWSDL section of the configuration
        element = fac.createOMElement("publishWSDL", syn);
        if (publishWSDLEndpoint != null) {
            publishWSDL = true;
            element.addAttribute("endpoint", publishWSDLEndpoint, nullNS);
        } else if (wsdlKey != null) {
            publishWSDL = true;
            element.addAttribute("key", wsdlKey, nullNS);
        } else if (wsdlURI != null) {
            publishWSDL = true;
            element.addAttribute("uri", wsdlURI, nullNS);
        } else if (wsdlDef != null) {
            publishWSDL = true;
            try {
                element.addChild(createElement(wsdlDef));
            } catch (XMLStreamException e) {
                String msg = "Unable to build the in line WSDL definition";
                log.error(msg + e.getMessage());
                throw new ProxyAdminException(msg, e);
            }
        }

        if (publishWSDL){
            // builds the additional resources (if any) required by the WSDL
            if (wsdlResources != null && wsdlResources.length != 0) {
                OMElement resource;
                Entry wsdlResource;
                for (int i = 0; i < wsdlResources.length; i++) {
                    wsdlResource = wsdlResources[i];
                    resource = fac.createOMElement("resource", syn);
                    resource.addAttribute("location", wsdlResource.getKey(), nullNS);
                    resource.addAttribute("key", wsdlResource.getValue(), nullNS);
                    element.addChild(resource);
                }
            }
            proxy.addChild(element);
        }

        // creates the additional service level parameter list of the configuration
        if (serviceParams != null && serviceParams.length != 0) {
            Entry serviceParam;
            String value;
            for (int i = 0; i < serviceParams.length; i++) {
                serviceParam = serviceParams[i];
                if(serviceParam != null){
	                element = fac.createOMElement("parameter", syn);
	                element.addAttribute("name", serviceParam.getKey(), nullNS);
	                value = serviceParam.getValue();
	                if (value.startsWith("<")) {
	                    try {
	                    	byte[] bytes = null;
	                    	try {
								 bytes = value.getBytes("UTF8");
							} catch (UnsupportedEncodingException e) {
								log.error("Unable to extract bytes in UTF-8 encoding. " + 
										"Extracting bytes in the system default encoding" + e.getMessage());
								bytes = value.getBytes();
							}
	                        element.addChild(new StAXOMBuilder(
	                                new ByteArrayInputStream(bytes)).getDocumentElement());
	                    } catch (XMLStreamException e) {
	                        String msg = "Service parameter: " + serviceParam.getKey() + " has an invalid XML as its value";
	                        log.error(msg);
	                        throw new ProxyAdminException(msg,e);
	                    }
	                } else {
	                    element.setText(value);
	                }
	                proxy.addChild(element);
                }
            }
        }

        if (enableSecurity) {
            element = fac.createOMElement("enableSec", syn);
            proxy.addChild(element);
        }

        if (policies != null) {
            for (ProxyServicePolicyInfo policy : policies) {
                proxy.addChild(policy.toOM(fac, syn, nullNS));
            }
        }

        if (description != null) {
            element = fac.createOMElement("description", syn);
            element.setText(description);
            proxy.addChild(element);
        }

        return proxy;
    }

    /**
     * Creates a comma separated string from the given list
     * @param lst the array of strings
     * @return a comma separated string
     */
    private String createCSString (String [] lst) {
        String str = "";
        for (Object item : lst) {
            str += item + ",";
        }
        return str.substring(0, str.length() - 1);
    }

    /**
     * Creates an <code>OMElement</code> from the given string
     * @param str the XML string
     * @return the <code>OMElement</code> representation of the given string
     * @throws XMLStreamException if building the <code>OMelement</code> is unsuccessful
     */
    private OMElement createElement(String str) throws XMLStreamException {
    	byte[] bytes = null;
    	try {
			bytes = str.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to extract bytes in UTF-8 encoding. " + 
					"Extracting bytes in the system default encoding" + e.getMessage());
			bytes = str.getBytes();
		}
    	
        InputStream in = new ByteArrayInputStream(bytes);
        return new StAXOMBuilder(in).getDocumentElement();
    }

    /**
     * Get the name of the artifact container from which the proxy deployed
     * @return artifactContainerName
     */
    public String getArtifactContainerName() {
        return artifactContainerName;
    }

    /**
     * Set the name of the artifact container
     * @param artifactContainerName
     */
    public void setArtifactContainerName(String artifactContainerName) {
        this.artifactContainerName = artifactContainerName;
    }

    /**
     * Whether the proxy is edited through the management console
     * @return isEdited
     */
    public boolean getIsEdited() {
        return isEdited;
    }

    /**
     * Set whether the proxy is deployed via the management console
     * @param isEdited
     */
    public void setIsEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }
}
