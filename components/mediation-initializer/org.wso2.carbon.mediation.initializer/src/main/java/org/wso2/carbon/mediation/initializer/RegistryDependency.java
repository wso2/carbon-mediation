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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

/**
 * This class is used to find wsdl/schema dependencies using registry.core()
 * and doing string manipulation to
 * map the relative location to absolute registry location
 * 
 */
public class RegistryDependency {
	
	private String registryKey;
	private List<String> dependencyResourcePaths = new ArrayList<String>();
	private static final Log log = LogFactory.getLog(RegistryDependency.class);
	/**
	 * 
	 * @param registryKey
	 */
	public RegistryDependency(String registryKey) {
		this.registryKey = registryKey;

	}

	/**
	 * Get the associations related to a particular WSDL/schema key
	 */
	public Map<String, String> getDependencies() {
		List<Association> childDependencies = null;

		Map<String, String> dependencyMap = new HashMap<String, String>();
		UserRegistry registry = getRegistry();
		registryKey = constructResourcePath();
		if (registryKey != null) {

			childDependencies = getChildDependencies(registryKey, registry);
		}
		if (childDependencies == null || childDependencies.size() == 0) {
			return dependencyMap;
		}

		Iterator<Association> parentDependenciesItr = childDependencies.iterator();

		while (parentDependenciesItr.hasNext()) {
			Map<String, String> childDependencyMap = new HashMap<String, String>();
			Association childAssociation = (Association) parentDependenciesItr.next();

			childDependencyMap = createAssociationTree(childAssociation, registry);
			dependencyMap.putAll(childDependencyMap);
		}

		return dependencyMap;
	}

	/**
	 * Construct all associations related to all child associations of imported
	 * resources
	 * 
	 * @param tmpAsso
	 * @param associationTreeBean
	 * @param registry
	 * @return
	 */
	private Map<String, String> createAssociationTree(Association childAssociation,
	                                                  UserRegistry registry) {

		Map<String, String> childAssociationMap = new HashMap<String, String>();
		boolean loopEnd = false;

		String childAssociationResourcePath = childAssociation.getDestinationPath();
		String childAssociationResourceKey = constructResourceKey(childAssociationResourcePath);
		childAssociationMap.put(childAssociationResourcePath, childAssociationResourceKey);
		dependencyResourcePaths.add(childAssociationResourcePath);

		try {
			if (registry.resourceExists(childAssociationResourcePath)) {
				List<Association> childAssociations =  getChildDependencies(childAssociationResourcePath, registry);
				if (childAssociations == null) {
					loopEnd = true;
				}
				if (!loopEnd) {
					if (!childAssociations.isEmpty()) {
						Iterator<Association> descendantChildAssociations =  childAssociations.
																						iterator();
						while (descendantChildAssociations.hasNext()) {
							Association descendantChildAssociation = (Association) descendantChildAssociations.next();

							if (!childAssociationResourcePath.equals(descendantChildAssociation.
							                                         getDestinationPath())) {

								if (!dependencyResourcePaths.contains(descendantChildAssociation.
								                                      getDestinationPath())) {

									String childResourcePath = descendantChildAssociation.
																			getDestinationPath();
									String childResourceKey = constructResourceKey(childResourcePath);
									// store the Descendant Child Association to the map
									childAssociationMap.put(childResourcePath, childResourceKey);

									break;
								}
							}
						}
					}
				}
				// Do recursion for child Association of Association
				if (!loopEnd && !childAssociations.isEmpty()) {

					Iterator<Association> descendantChildAssociations =  childAssociations.iterator();
					while (descendantChildAssociations.hasNext()) {
						Association descendantChildAssociation = (Association) descendantChildAssociations.next();

						if (!childAssociationResourcePath.equals(descendantChildAssociation.
						                                         getDestinationPath())) {
							if (!dependencyResourcePaths.contains(descendantChildAssociation.
							                                      getDestinationPath())) {
								createAssociationTree(descendantChildAssociation, registry);
							}
						}
					}

				}
			}
		} catch (RegistryException e) {
			String msg = "Could not locate the resource/dependencies for the" +
			             childAssociationResourcePath;
			log.error(msg, e);
		}
		return childAssociationMap;

	}


	/**
	 * 
	 * @param registryActualPath
	 * @return
	 */
	private List<Association> getChildDependencies(String registryActualPath, UserRegistry registry) {
		Association[] dependencies = null;
	
		List<Association> dependencyList = new ArrayList<Association>();
		List<Association> temproryList = new ArrayList<Association>();

		try {
			dependencies =
			               registry.getAssociations(registryActualPath, ServiceBusConstants.DEPENDS);
			temproryList.addAll(Arrays.asList(dependencies));

		} catch (RegistryException e) {
			String msg = "Could not locate the dependencies for the" + registryKey;
			log.error(msg, e);

		}

		Iterator<Association> itr = temproryList.iterator();
		while (itr.hasNext()) {
			Association childAssociation = (Association) itr.next();
			dependencyList.add(childAssociation);
		}

		return dependencyList;

	}
	
	/**
	 * construct the resource key based on resource location
	 * 
	 * @param resourcePath
	 * @return
	 */
	private String constructResourceKey(String resourcePath) {
		String resourceKey = null;
		String prefix;

		if (resourcePath.startsWith("/_system/config")) {
			prefix = "/_system/config";
			resourceKey = resourcePath.replace(prefix, "conf:");
		}
		if (resourcePath.startsWith("/_system/governance")) {
			prefix = "/_system/governance";
			resourceKey = resourcePath.replace(prefix, "gov:");
		}

		return resourceKey;

	}

	private UserRegistry getRegistry() {
		RegistryService registrySvc = ServiceBusInitializer.getRegistryService();
		if (registrySvc == null) {
			log.warn("Unable to access the registry service");
			return null;
		}

		try {
			return registrySvc.getRegistry();
		} catch (RegistryException e) {
			log.error("Error while obtaining a system registry instance", e);
			return null;
		}
	}

	/**
	 * construct the wsdl registry key based on wsdl location
	 * 
	 * @param resourcePath
	 * @return
	 */
	private String constructResourcePath() {
		String wsdlResourcePath = null;
		String prefix;
		if (registryKey != null && !"".equals(registryKey)) {
			if (registryKey.startsWith("conf:")) {
				prefix = "conf:";
				wsdlResourcePath = registryKey.replace(prefix, "/_system/config");
			}
			if (registryKey.startsWith("gov:")) {
				prefix = "gov:";
				wsdlResourcePath = registryKey.replace(prefix, "/_system/governance");
			}
		}
		return wsdlResourcePath;

	}
/**
 * Constructing the relative location to the actual registry path
 * (eg:removing versions and ../ (../../../a.xsd;version 1000)
 * @param relativeLocation
 * @return
 */
	public String constructRegistryPathToRelativePath(String relativeLocation) {
		
		//get rid of versions
		if (relativeLocation.contains(";")) {
			String relativeLocation_new =
			                              relativeLocation.substring(0,
			                                                         relativeLocation.indexOf(";"));
			relativeLocation = relativeLocation_new;
		}
		//get rid of ../
		if (relativeLocation.contains("../")) {
			
			String registryPath_suffix =
				relativeLocation.substring(relativeLocation.lastIndexOf("../")+3,
				                           relativeLocation.length());
			relativeLocation  = registryPath_suffix;
		}
	//schema relative path starts as ./xsd/schemaA.xsd
		if (relativeLocation.contains("./")) {
			
			String registryPath_suffix =
				relativeLocation.substring(relativeLocation.lastIndexOf("./")+2,
				                           relativeLocation.length());
			relativeLocation  = registryPath_suffix;
		}

		return relativeLocation;
	}
}
