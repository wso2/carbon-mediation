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

package org.wso2.carbon.mediation.configadmin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.config.xml.EntryFactory;
import org.apache.synapse.config.xml.ProxyServiceFactory;
import org.apache.synapse.config.xml.SequenceMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.core.axis2.ProxyService;

/**
 * This class validates Synapse configuration elements without actually building the
 * SynapseConfiguration or associated object model. Therefore this class can be used
 * safely to determine if a given XML configuration is correct or not.
 */
public class ConfigurationValidator {

    public ValidationError[] validate(OMElement element) {
        List<ValidationError> errors = new ArrayList<ValidationError>();
        
        List<String> configSequenceList = new ArrayList<String>();

        if (!element.getQName().equals(XMLConfigConstants.DEFINITIONS_ELT)) {

            errors.add(newValidationError(element, "Top level element is invalid"));
        }

        Iterator itr = element.getChildren();
        while (itr.hasNext()) {
            Object o = itr.next();
            if (o instanceof OMElement) {
                OMElement child = (OMElement) o;
                if (XMLConfigConstants.PROXY_ELT.equals(child.getQName())) {
                    validateProxyService(child, errors);
                } else if (XMLConfigConstants.ENDPOINT_ELT.equals(child.getQName())) {
                    validateEndpoint(child, errors);
				} else if (XMLConfigConstants.SEQUENCE_ELT.equals(child.getQName())) {
					String name = child.getAttributeValue((new QName("", "name")));
					if (configSequenceList.contains(name)) {
						errors.add(newValidationError(child, "Sequence ["+name+"] name already existing"));
					} else {
						validateSequence(child, errors);
						configSequenceList.add(name);
					}
				} else if(XMLConfigConstants.ENTRY_ELT.equals(child.getQName())){
                    validateLocalEntry(child, errors);
                }
            }
        }

        if (errors.size() > 0) {
            return errors.toArray(new ValidationError[errors.size()]);
        }
        return null;
    }

	private void validateSequence(OMElement sequenceElement, List<ValidationError> errors) {
		try {
			new SequenceMediatorFactory().createMediator(sequenceElement, new Properties());
		} catch (Exception e) {
			errors.add(newValidationError(sequenceElement, e.getMessage()));
		}
	}

    private void validateProxyService(OMElement proxyElement, List<ValidationError> errors) {
        try {
            ProxyService proxy = ProxyServiceFactory.createProxy(proxyElement, new Properties());
            URI wsdl = proxy.getWsdlURI();
            if (wsdl != null && !testURL(wsdl.toString())) {
                errors.add(newValidationError(proxyElement, "WSDL URL is not accessible"));
            }
        } catch (Exception e) {
            errors.add(newValidationError(proxyElement, e.getMessage()));
        }
    }

    private void validateEndpoint(OMElement endpointElement, List<ValidationError> errors) {
        try {
            EndpointFactory.getEndpointFromElement(endpointElement, false, new Properties());
        } catch (Exception e) {
            errors.add(newValidationError(endpointElement, e.getMessage()));
        }
    }
    
    private void validateLocalEntry(OMElement entryElement, List<ValidationError> errors){
        try {
            EntryFactory.createEntry(entryElement, new Properties());
        } catch (Exception e) {
            errors.add(newValidationError(entryElement, e.getMessage()));
        }
    }

    private boolean testURL(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);

        try {
            url.openStream();
            return true;
        } catch (IOException e) {
            String path = url.getPath();
            String synapseHome = SynapsePropertiesLoader.loadSynapseProperties().
                    getProperty(SynapseConstants.SYNAPSE_HOME);
            if (synapseHome != null) {
                if (synapseHome.endsWith("/")) {
                    synapseHome = synapseHome.substring(0, synapseHome.lastIndexOf("/"));
                }
                url = new URL(url.getProtocol() + ":" + synapseHome + "/" + path);
                try {
                    url.openStream();
                    return true;
                } catch (IOException ex) {
                    return false;
                }
            }
            return false;
        }
    }

    private ValidationError newValidationError(OMElement element, String message) {
        String name;
        if (XMLConfigConstants.ENTRY_ELT.equals(element.getQName())) {
            name = element.getAttributeValue(new QName("key"));
        } else {
            name = element.getAttributeValue(new QName("name"));
        }

        String localName = element.getLocalName();
        if (name == null) {
            name = localName;
        } else {
            name += " [" + localName.substring(0, 1).toUpperCase() + localName.substring(1) + "]";
        }

        return new ValidationError(name, message);
    }
}
