/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediation.library.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.SynapseImportFactory;
import org.apache.synapse.config.xml.SynapseImportSerializer;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.libraries.model.Library;
import org.apache.synapse.libraries.util.LibDeployerUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;

@SuppressWarnings({ "UnusedDeclaration" })
public class MediationLibraryAdminService extends AbstractServiceBusAdmin {

	private static Log log = LogFactory.getLog(MediationLibraryAdminService.class);

	public static final int MSGS_PER_PAGE = 10;

	/**
	 * Get an XML configuration element for a message processor from the FE and
	 * creates and add the MessageStore to the synapse configuration.
	 * 
	 * @param xml
	 *            string that contain the message processor configuration.
	 * @throws AxisFault
	 *             if some thing goes wrong when creating a MessageProcessor
	 *             with the given xml.
	 */
	private void addImport(String xml) throws AxisFault {
		try {
			OMElement imprtElem = createElement(xml);
			SynapseImport synapseImport = SynapseImportFactory.createImport(imprtElem, null);
			if (synapseImport != null && synapseImport.getName() != null) {
				SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
				String fileName = ServiceBusUtils.generateFileName(synapseImport.getName());
				synapseImport.setFileName(fileName);
				synapseConfiguration.addSynapseImport(synapseImport.getName(), synapseImport);
				String synImportQualfiedName = LibDeployerUtils.getQualifiedName(synapseImport);
				Library synLib =
				                 getSynapseConfiguration().getSynapseLibraries()
				                                          .get(synImportQualfiedName);
				if (synLib != null) {
					LibDeployerUtils.loadLibArtifacts(synapseImport, synLib);
				}
				MediationPersistenceManager mp = getMediationPersistenceManager();
				mp.saveItem(synapseImport.getName(), ServiceBusConstants.ITEM_TYPE_IMPORT);

			} else {
				String message = "Unable to create a Synapse Import for :  " + xml;
				handleException(log, message, null);
			}

		} catch (XMLStreamException e) {
			String message = "Unable to create a Synapse Import for :  " + xml;
			handleException(log, message, e);
		}

	}

	public void addImport(String libName, String packageName) throws AxisFault {
		SynapseImport synImport = new SynapseImport();
		synImport.setLibName(libName);
		synImport.setLibPackage(packageName);
		OMElement impEl = SynapseImportSerializer.serializeImport(synImport);
		if (impEl != null) {
			try {
				addImport(impEl.toString());
			} catch (AxisFault axisFault) {
				handleException(log, "Could not add Synapse Import", axisFault);
			}
		} else {
			handleException(log,
			                "Could not add Synapse Import. Invalid import params for libName : " +
			                        libName + " packageName : " + packageName, null);
		}
	}

	/**
	 * Method loads the Library artifact information
	 * 
	 * @param libName
	 * @param packageName
	 * @return
	 * @throws AxisFault
	 */
	public LibraryInfo getLibraryInfo(String libName, String packageName) throws AxisFault {
		SynapseImport synImport = new SynapseImport();
		synImport.setLibName(libName);
		synImport.setLibPackage(packageName);
		OMElement impEl = SynapseImportSerializer.serializeImport(synImport);
		if (impEl != null) {
			try {
				OMElement imprtElem = createElement(impEl.toString());
				SynapseImport synapseImport = SynapseImportFactory.createImport(imprtElem, null);
				if (synapseImport != null && synapseImport.getName() != null) {
					// SynapseConfiguration synapseConfiguration =
					// getSynapseConfiguration();
					String fileName = ServiceBusUtils.generateFileName(synapseImport.getName());
					synapseImport.setFileName(fileName);

					String synImportQualfiedName = LibDeployerUtils.getQualifiedName(synapseImport);
					Library synLib =
					                 getSynapseConfiguration().getSynapseLibraries()
					                                          .get(synImportQualfiedName);
					if (synLib != null) {
						LibraryInfo info = new LibraryInfo();
						info.setLibName(libName);
						info.setPackageName(packageName);

						List<LibraryArtifiactInfo> artifactsList =
						                                           new ArrayList<LibraryArtifiactInfo>();

						for (Map.Entry<String, String> entry : synLib.getLibArtifactDetails()
						                                             .entrySet()) {
							if (entry.getValue() != null && entry.getKey() != null) {
								LibraryArtifiactInfo artifactInfo = new LibraryArtifiactInfo();
								artifactInfo.setName(entry.getKey());
								artifactInfo.setDescription(synLib.getArtifactDescription(entry.getKey()));
								artifactsList.add(artifactInfo);
							}
						}
						LibraryArtifiactInfo[] artifacts =
						                                   new LibraryArtifiactInfo[artifactsList.size()];
						for (int i = 0; i < artifacts.length; i++) {
							artifacts[i] = artifactsList.get(i);
						}
						info.setArtifacts(artifacts);
						return info;
					}

				}

			} catch (XMLStreamException e) {
				String message = "Unable to create a Synapse Import for :  ";
				handleException(log, message, e);
			}
		}
		return null;
	}

	/**
	 * Get the Synapse configuration for a Message processor
	 * 
	 * @param qualifiedName
	 *            name of the message processor
	 * @return XML String that contain the configuration
	 * @throws AxisFault
	 */
	public String getImport(String qualifiedName) throws AxisFault {
		SynapseConfiguration configuration = getSynapseConfiguration();

		assert configuration != null;
		SynapseImport synapseImport = configuration.getSynapseImports().get(qualifiedName);
		String xml = null;
		if (synapseImport != null) {
			xml = SynapseImportSerializer.serializeImport(synapseImport).toString();
		} else {
			handleException(log, "Library Import " + synapseImport + " does not exist", null);
		}

		return xml;

	}

	/**
	 * Delete the SynapseImport instance with given importQualifiedName in the
	 * synapse configuration
	 * 
	 * @param importQualifiedName
	 *            of the MessageProcessor to be deleted
	 * @throws AxisFault
	 *             if Message processor does not exist
	 */
	public void deleteImport(String importQualifiedName) throws AxisFault {
		SynapseConfiguration configuration = getSynapseConfiguration();

		assert configuration != null;
		if (configuration.getSynapseImports().containsKey(importQualifiedName)) {
			SynapseImport synapseImport = configuration.removeSynapseImport(importQualifiedName);
			String fileName = synapseImport.getFileName();
			// get corresponding library for un-loading this import
			Library synLib =
			                 getSynapseConfiguration().getSynapseLibraries()
			                                          .get(importQualifiedName);
			if (synLib != null) {
				// this is a important step -> we need to unload what ever the
				// components loaded thru this import
				synLib.unLoadLibrary();
			}

			MediationPersistenceManager pm = getMediationPersistenceManager();
			pm.deleteItem(synapseImport.getName(), fileName, ServiceBusConstants.ITEM_TYPE_IMPORT);

		}

	}

	public void deleteLibrary(String libQualifiedName) throws Exception {
		// If libQualifiedName is null throw an exception
		if (libQualifiedName == null) {
			handleException(log, "Library name can't be null", null);
			return;
		}

		// CarbonApplication instance to delete
		Library currentMediationLib = null;

		SynapseConfiguration synConfigForTenant = getSynapseConfiguration();
		if (synConfigForTenant != null) {
			Collection<Library> appList = synConfigForTenant.getSynapseLibraries().values();
			for (Library mediationLib : appList) {
				if (libQualifiedName.equals(mediationLib.getQName().toString())) {
					currentMediationLib = mediationLib;
				}
			}

			// If requested application not found, throw an exception
			if (currentMediationLib == null) {
				handleException(log,
				                "No Mediation Library found of the name : " + libQualifiedName,
				                null);
				return;
			}

			// deleting relevent configuration
			deleteImport(libQualifiedName);

			// Remove the app artifact file from repository, cApp hot undeployer
			// will do the rest
			String libFilePath = currentMediationLib.getFileName();
			File file = new File(libFilePath);
			if (file.exists() && !file.delete()) {
				log.error("Artifact file couldn't be deleted for Mediation Library : " +
				          currentMediationLib.getQName().toString());
			}
		}
	}

	/**
	 * Get all the Current Message processor names defined in the configuration
	 * 
	 * @return array of Strings that contains MessageStore names
	 * @throws AxisFault
	 */
	public String[] getAllImports() throws AxisFault {
		SynapseConfiguration configuration = getSynapseConfiguration();

		assert configuration != null;
		Collection<String> names = configuration.getSynapseImports().keySet();
		return names.toArray(new String[names.size()]);
	}

	/**
	 * Get all the Current Message processor names defined in the configuration
	 * 
	 * @return array of Strings that contains MessageStore names
	 * @throws AxisFault
	 */
	public String[] getAllLibraries() throws AxisFault {
		SynapseConfiguration configuration = getSynapseConfiguration();

		assert configuration != null;
		Collection<String> names = configuration.getSynapseLibraries().keySet();
		return names.toArray(new String[names.size()]);
	}

	/**
	 * Get all the Current Message processor names defined in the configuration
	 * 
	 * @return array of Strings that contains MessageStore names
	 * @throws AxisFault
	 */
	public LibraryInfo[] getAllLibraryInfo() throws AxisFault {
		SynapseConfiguration configuration = getSynapseConfiguration();

		assert configuration != null;
		ArrayList<LibraryInfo> librarySet = new ArrayList<LibraryInfo>();

		Collection<Library> libraries = configuration.getSynapseLibraries().values();
		for (Library library : libraries) {
			LibraryInfo libInfo = new LibraryInfo();
			libInfo.setLibName(library.getQName().getLocalPart());
			libInfo.setPackageName(library.getPackage());
			libInfo.setDescription(library.getDescription());
			libInfo.setQName(library.getQName().toString());
			libInfo.setStatus(library.getLibStatus());
			librarySet.add(libInfo);
		}

		return librarySet.toArray(new LibraryInfo[libraries.size()]);
	}

	private void handleException(Log log, String message, Exception e) throws AxisFault {

		if (e == null) {

			AxisFault exception = new AxisFault(message);
			log.error(message, exception);
			throw exception;

		} else {
			message = message + " :: " + e.getMessage();
			log.error(message, e);
			throw new AxisFault(message, e);
		}
	}

	/**
	 * Creates an <code>OMElement</code> from the given string
	 * 
	 * @param str
	 *            the XML string
	 * @return the <code>OMElement</code> representation of the given string
	 * @throws javax.xml.stream.XMLStreamException
	 *             if building the <code>OmElement</code> is unsuccessful
	 */
	private OMElement createElement(String str) throws XMLStreamException {
		InputStream in = new ByteArrayInputStream(str.getBytes());
		return new StAXOMBuilder(in).getDocumentElement();
	}

	private static String getTenantIdString(AxisConfiguration axisConfig) {
		return String.valueOf(getTenantId(axisConfig));
	}

	private static int getTenantId(AxisConfiguration axisConfig) {
		PrivilegedCarbonContext carbonContext =
		                                        PrivilegedCarbonContext.getThreadLocalCarbonContext();
		return carbonContext.getTenantId();
	}

	/**
	 * Used to download a carbon application archive.
	 * 
	 * @param fileName
	 *            the name of the application archive (.car) to be downloaded
	 * @return datahandler corresponding to the .car file to be downloaded
	 * @throws Exception
	 *             for invalid scenarios
	 */
	public DataHandler downloadLibraryArchive(String fileName) throws Exception {
		// CarbonApplication instance to delete
		Library currentMediationLib = null;
		// Iterate all applications for this tenant and find the application to
		// delete
		SynapseConfiguration synConfigForTenant = getSynapseConfiguration();
		Collection<Library> appList = synConfigForTenant.getSynapseLibraries().values();
		for (Library mediationLib : appList) {
			if (fileName.equals(mediationLib.getQName().getLocalPart().toString())) {
				currentMediationLib = mediationLib;
			}
		}

		FileDataSource datasource = new FileDataSource(new File(currentMediationLib.getFileName()));
		DataHandler handler = new DataHandler(datasource);

		return handler;
	}

	/**
	 * Performing the action of enabling/disabling the given meidation library
	 * 
	 * @param libName
	 * @param packageName
	 * @param status
	 * @throws AxisFault
	 */
	public void updateStatus(String libQName, String libName, String packageName, String status)
	                                                                                            throws AxisFault {
		try {
			SynapseConfiguration configuration = getSynapseConfiguration();
			SynapseImport synapseImport = configuration.getSynapseImports().get(libQName);
			if (synapseImport == null && libName != null && packageName != null) {
				addImport(libName, packageName);
				synapseImport = configuration.getSynapseImports().get(libQName);
			}
			Library synLib = getSynapseConfiguration().getSynapseLibraries().get(libQName);
			if (libQName != null && synLib != null) {
				if ("enabled".equals(status)) {
					synapseImport.setStatus(true);
					synLib.setLibStatus(true);
					synLib.loadLibrary();
				} else {
					synapseImport.setStatus(false);
					synLib.setLibStatus(false);
					synLib.unLoadLibrary();
				}

				// update synapse configuration.
				MediationPersistenceManager mp = getMediationPersistenceManager();
				mp.saveItem(synapseImport.getName(), ServiceBusConstants.ITEM_TYPE_IMPORT);
			}

		} catch (Exception e) {
			String message = "Unable to update status for :  " + libQName;
			handleException(log, message, e);
		}
	}

}
