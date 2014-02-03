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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.util.resolver.ResourceMap;
import org.apache.synapse.util.resolver.UserDefinedWSDLLocator;
import org.xml.sax.InputSource;

/**
 * If imports present in wsdl , instead of providing
 * ResourceMap(relativeloaction, registrylocation of the resources) from the
 * client side this class will resolve the imported rsources against the wsdl
 * key
 */
public class RegistryWSDLLocator implements UserDefinedWSDLLocator {
	private InputSource baseInputSource;
	private String baseURI;
	private ResourceMap resourceMap;
	private String wsdlKey;
	private String latestImportURI;
	private SynapseConfiguration synCfg;
	private static final Log log = LogFactory.getLog(RegistryWSDLLocator.class);

	public void init(InputSource baseInputSource, String baseURI, ResourceMap resourceMap,
	                 SynapseConfiguration synCfg, String wsdlKey) {
		this.baseInputSource = baseInputSource;
		this.baseURI = baseURI;
		this.resourceMap = resourceMap;
		this.synCfg = synCfg;
		this.wsdlKey = wsdlKey;

	}

	/**
	 * Resolve a schema or WSDL import.
	 * This method will first attempt to resolve the location using the
	 * configured {@link ResourceMap} object. If this fails (because no
	 * {@link ResourceMap} is
	 * configured or because
	 * {@link ResourceMap#resolve(SynapseConfiguration, String)} returns null,
	 * it will resolve the location using
	 * {@link SynapseConfigUtils#resolveRelativeURI(String, String)}.
	 */
	public InputSource getImportInputSource(String parentLocation, String relativeLocation) {

		InputSource result = null;
		String key = null;

		if (resourceMap != null) {
			result = resourceMap.resolve(synCfg, relativeLocation);
		}
		// get the associations related to the parent loaction(using wsdlkey)

		if (wsdlKey != null && result == null) {
			if (log.isDebugEnabled()) {
				log.info("Starting to resolve imported resources " + relativeLocation +
				         " using the dependencies got from wsdlKey " + wsdlKey);
			}
			RegistryDependency regWSDLDep = new RegistryDependency(wsdlKey);
			Map<String, String> dependencyMap = regWSDLDep.getDependencies();
			if (dependencyMap != null) {

				Set<String> keys = dependencyMap.keySet();
				for (Iterator<String> i = keys.iterator(); i.hasNext();) {
					key = (String) i.next();
					String value = dependencyMap.get(key);
				
					String constructedPath =
					                         regWSDLDep.constructRegistryPathToRelativePath(relativeLocation);

					if (value.endsWith(constructedPath)) {
						if (resourceMap == null) {
							resourceMap = new ResourceMap();
						}
						resourceMap.addResource(relativeLocation, value);
						latestImportURI = relativeLocation;
						break;
					}
				}
				result = resourceMap.resolve(synCfg, relativeLocation);
			}
		} else if (result == null) {
			String location =
			                  SynapseConfigUtils.resolveRelativeURI(parentLocation,
			                                                        relativeLocation);
			result = new InputSource(location);
			latestImportURI = location;
		} else {
			latestImportURI = relativeLocation;
		}

		return result;
	}

	public String getLatestImportURI() {
		return latestImportURI;
	}

	public InputSource getBaseInputSource() {
		return baseInputSource;
	}

	public String getBaseURI() {
		return baseURI;
	}

	public void close() {
	}

}
