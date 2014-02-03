/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mediation.initializer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.jaxp.SchemaResourceLSInput;
import org.apache.synapse.util.resolver.ResourceMap;
import org.apache.synapse.util.resolver.UserDefinedXmlSchemaURIResolver;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

/**
 * Adapting the schemas' Relativepath to the registry actual path
 **/

public class RegistryXmlSchemaURIResolver implements UserDefinedXmlSchemaURIResolver {
	private String wsdlKey;
	private ResourceMap resourceMap;
	private SynapseConfiguration synCfg;
	private List<Value> schemaRegKeys = new ArrayList<Value>();
	
	private static final Log log = LogFactory.getLog(RegistryXmlSchemaURIResolver.class);

	public void init(ResourceMap resourceMap, SynapseConfiguration synCfg, String wsdlKey) {
		this.resourceMap = resourceMap;
		this.synCfg = synCfg;
		this.wsdlKey = wsdlKey;

	}
	
	public void init(ResourceMap resourceMap, SynapseConfiguration synCfg, List<Value> schemaRegKeys) {
		this.resourceMap = resourceMap;
		this.synCfg = synCfg;
		this.schemaRegKeys = schemaRegKeys;

	}
	/**
	 * Resolve a schema import.
	 * This method will first attempt to resolve the location using the
	 * configured {@link ResourceMap} object. If this fails (because no
	 * {@link ResourceMap} is
	 * configured or because
	 * {@link ResourceMap#resolve(SynapseConfiguration, String)} returns null,
	 * it will resolve the location using
	 * {@link SynapseConfigUtils#resolveRelativeURI(String, String)}.
	 */
	public InputSource resolveEntity(String targetNamespace, String schemaLocation, String baseUri) {

		InputSource result = null;

		if (resourceMap != null) {
			result = resourceMap.resolve(synCfg, schemaLocation);
		}

		if (result == null && wsdlKey != null) {
			if (log.isDebugEnabled()) {
				log.info("Starting to resolve schema " + schemaLocation +
				         " using the dependencies got from wsdlKey " + wsdlKey);
			}
			RegistryDependency regWSDLDep = new RegistryDependency(wsdlKey);
			Map<String, String> dependencyMap = regWSDLDep.getDependencies();
			if (dependencyMap != null) {
				Set<String> keys = dependencyMap.keySet();
				for (Iterator<String> i = keys.iterator(); i.hasNext();) {
					String key = (String) i.next();
					String value = dependencyMap.get(key);
					String constructedPath =
					                         regWSDLDep.constructRegistryPathToRelativePath(schemaLocation);

					if (value.endsWith(constructedPath)) {
						if (resourceMap == null) {
							resourceMap = new ResourceMap();
						}
						resourceMap.addResource(schemaLocation, value);
						break;
					}
				}
				result = resourceMap.resolve(synCfg, schemaLocation);
			}
		} else if (result == null) {
			result =
			         new InputSource(SynapseConfigUtils.resolveRelativeURI(baseUri, schemaLocation));
		}
		return result;

	}

	/**
	 * Used in validate mediator to validate the schemas
	 * 
	 * @param type
	 *            The type of the resource being resolved. For XML [XML 1.0]
	 *            resources (i.e. entities), applications must use the value
	 *            "http://www.w3.org/TR/REC-xml". For XML Schema [XML Schema
	 *            Part 1] , applications must use the value
	 *            "http://www.w3.org/2001/XMLSchema". Other types of resources
	 *            are outside the scope of this specification and therefore
	 *            should recommend an absolute URI in order to use this method.
	 * @param namespaceURI
	 *            The namespace of the resource being resolved, e.g. the
	 *            target namespace of the XML Schema [XML Schema Part 1] when
	 *            resolving XML Schema resources.
	 * @param publicId
	 *            The public identifier of the external entity being
	 *            referenced, or null if no public identifier was supplied or if
	 *            the resource is not an entity.
	 * @param systemId
	 *            The system identifier, a URI reference [IETF RFC 2396], of
	 *            the external resource being referenced, or null if no system
	 *            identifier was supplied.
	 * @param baseURI
	 *            The absolute base URI of the resource being parsed, or null
	 *            if there is no base URI.
	 * 
	 * @return A LSInput,
	 *         object describing the new input source, or null to
	 *         request that the parser open a regular URI connection to the
	 *         resource.
	 * 
	 */
	public LSInput resolveResource(String type, String namespaceURI, String publicId,
	                               String systemId, String baseURI) {
	
		InputSource inputSource = null;
		if (log.isDebugEnabled()) {
			log.debug("Resolving Schema resource " + systemId);
		}

		// check with the registry schema resolver
		if (resourceMap == null) {
			for (int i = 0; i < schemaRegKeys.size(); i++) {
				RegistryDependency regWSDLDep =
				                                new RegistryDependency(schemaRegKeys.get(i)
				                                                                    .getKeyValue());
				Map<String, String> dependencyMap = regWSDLDep.getDependencies();
				if (dependencyMap.size()>0) {
					Set<String> keys = dependencyMap.keySet();
					for (Iterator<String> itr = keys.iterator(); itr.hasNext();) {
						String key = (String) itr.next();
						String value = dependencyMap.get(key);
						String constructedPath =
						                         regWSDLDep.constructRegistryPathToRelativePath(systemId);

						if (value.endsWith(constructedPath)) {
							if (resourceMap == null) {
								resourceMap = new ResourceMap();
							}
							resourceMap.addResource(systemId, value);
							break;
						}
					}
					inputSource = resourceMap.resolve(synCfg, systemId);
				} else {
					// It comes here since there is no any other 'include'
					// schemas.
					// We need to resolve this schema(base schema without any
					// includes) also.
					resourceMap.addResource(systemId, schemaRegKeys.get(i).getKeyValue());
				}
			}

		} else {
			inputSource = resourceMap.resolve(synCfg, systemId);
		}
		if (inputSource == null) {
			log.warn("Unable to resolve schema resource " + systemId);
			return null;
		}
		SchemaResourceLSInput schemaResourceLSInput = new SchemaResourceLSInput();
		schemaResourceLSInput.setByteStream(inputSource.getByteStream());
		return schemaResourceLSInput;
	}
}
