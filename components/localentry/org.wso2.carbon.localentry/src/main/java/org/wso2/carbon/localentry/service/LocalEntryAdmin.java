/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.localentry.service;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.EntryFactory;
import org.apache.synapse.config.xml.EntrySerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.localentry.LocalEntryAdminException;
import org.wso2.carbon.localentry.dos.EntryData;
import org.wso2.carbon.localentry.util.ConfigHolder;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;
import org.wso2.carbon.mediation.dependency.mgt.services.DependencyManagementService;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * This is a POJO for Entry based administration service
 */
@SuppressWarnings({"UnusedDeclaration"})
public class LocalEntryAdmin extends AbstractServiceBusAdmin {

    private static final Log log = LogFactory.getLog(LocalEntryAdmin.class);
    public static final int LOCAL_ENTRIES_PER_PAGE = 10;

    public EntryData[] entryData() throws LocalEntryAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Map gloabalEntiesMap = synapseConfiguration.getDefinedEntries();
            ArrayList<EntryData> globalEntryList = new ArrayList<EntryData>();
            for (Object o : gloabalEntiesMap.entrySet()) {
                EntryData data = new EntryData();
                Map.Entry entry = (Map.Entry) o;
                String key = (String) entry.getKey();
                if (SynapseConstants.SERVER_IP.equals(key)
                        || SynapseConstants.SERVER_HOST.equals(key)) {
                    continue;
                }
                if (entry.getValue() instanceof Entry) {
                    Entry value = (Entry) entry.getValue();
                    data.setName(key);
                    data.setDescription(value.getDescription());
                    switch (value.getType()) {
                        case Entry.REMOTE_ENTRY:
                            data.setType("Registry Key");
                            break;
                        case Entry.INLINE_TEXT:
                            data.setType("Inline Text");
                            break;
                        case Entry.INLINE_XML:
                            data.setType("Inline XML");
                            break;
                        case Entry.URL_SRC:
                            data.setType("Source URL");
                            break;
                        default:
                            data.setType("Unknown");
                            break;
                    }

                    if (value.getValue() instanceof String) {
                        String s = (String) value.getValue();
                        data.setValue(s);
                    } else if (value.getType() == Entry.URL_SRC) {
                        String s = value.getSrc().toString();
                        data.setValue(s);
                    } else if (value.getType() == Entry.REMOTE_ENTRY) {
                        data.setValue(value.getKey());
                    } else {
                        if (value.getValue() != null) {
                            data.setValue(value.getValue().toString());
                        } else {
                            data.setValue("");
                        }
                    }

                    globalEntryList.add(data);
                }
            }

            Collections.sort(globalEntryList, new Comparator<EntryData>() {
                public int compare(EntryData o1, EntryData o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });

            return globalEntryList.toArray(new EntryData[globalEntryList.size()]);
        } finally {
            lock.unlock();
        }
    }


    public EntryData[] paginatedEntryData(int pageNumber) throws LocalEntryAdminException {
        return doPaging(pageNumber,entryData());
    }

    private EntryData[] doPaging(int pageNumber, EntryData entries[]) {
        if (entries.length == 0) {
            return entries;
        }        

        int itemsPerPageInt = LOCAL_ENTRIES_PER_PAGE;
        int numberOfPages = (int) Math.ceil((double) entries.length / itemsPerPageInt);
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }
        int startIndex = (pageNumber * itemsPerPageInt);
        int endIndex = ((pageNumber + 1) * itemsPerPageInt);
        
        List<EntryData> entriesList = Arrays.asList(entries);
        List<EntryData> paginatedEntriesList = new ArrayList<EntryData>();
        for (int i = startIndex; i < endIndex && i < entries.length; i++) {
            paginatedEntriesList.add(entriesList.get(i));
        }

        return paginatedEntriesList.toArray(new EntryData[paginatedEntriesList.size()]);
    }

    public int getEntryDataCount() throws LocalEntryAdminException {
        int entryDataCount = 0;
        if(entryData() != null) {
            entryDataCount= entryData().length;
            return entryDataCount;
        }
        return entryDataCount;
    }

    /**
     * Add a entry into the synapseConfiguration
     *
     * @param ele - Entry object to be added as an OMElement
     * @return whether the operation is successfull or not
     * @throws LocalEntryAdminException if a Entry exists with the same name or if the
     *                   element provided is not a Entry element
     */
    public boolean addEntry(String ele) throws LocalEntryAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            OMElement elem;
            try {
                elem = nonCoalescingStringToOm(ele);
            }
            catch (XMLStreamException e) {
                return false;
            }
            
            if (elem.getQName().getLocalPart().equals(XMLConfigConstants
                    .ENTRY_ELT.getLocalPart())) {

                String entryKey = elem.getAttributeValue(new QName("key"));
                assertKeyEmpty(entryKey);
                entryKey = entryKey.trim();
                log.debug("Adding local entry with key : " + entryKey);

                if (getSynapseConfiguration().getLocalRegistry().containsKey(entryKey)) {
                    handleFault(log, "An Entry with key " + entryKey +
                            " is already used within the configuration");
                } else {
                    Entry entry = EntryFactory.createEntry(elem,
                            getSynapseConfiguration().getProperties());
                    entry.setFileName(ServiceBusUtils.generateFileName(entry.getKey()));
                    getSynapseConfiguration().addEntry(entryKey, entry);
                    MediationPersistenceManager pm
                            = ServiceBusUtils.getMediationPersistenceManager(getAxisConfig());
                    pm.saveItem(entry.getKey(), ServiceBusConstants.ITEM_TYPE_ENTRY);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Local registry entry : " + entryKey + " added to the configuration");
                }
                return true;
            } else {
                handleFault(log, "Error adding local entry. Invalid definition");
            }
        } catch (SynapseException syne) {
            handleFault(log, "Unable to add local entry ", syne);
        } catch (OMException e) {
            handleFault(log, "Unable to add local entry. Invalid XML ", e);
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Saves the entry described with the OMElement representing the entry
     *
     * @param ele - OMElement representing the entry
     * @return whether the operation is successfull or not
     * @throws LocalEntryAdminException if the entry name already exists or if the Element
     *                   doesnt represent a entry element
     */
    public boolean saveEntry(String ele) throws LocalEntryAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            OMElement elem;
            try {
                elem = nonCoalescingStringToOm(ele);                
            }
            catch (XMLStreamException e) {
                return false;
            }
            
            if (elem == null) {
                handleFault(log, "Unable to save local entry. Null definition");
            }
            String key = elem.getAttributeValue(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));
            if (key != null) {
                log.debug("Saving local entry with key : " + key);
                key = key.trim();
                Entry oldEntry = getSynapseConfiguration().
                        getDefinedEntries().get(key);
                if (oldEntry == null) {
                    handleFault(log, "Unable to update local entry. Non existent");
                } else {
                    Entry entry = EntryFactory.createEntry(elem,
                            getSynapseConfiguration().getProperties());
                    getSynapseConfiguration().removeEntry(key);
                    getSynapseConfiguration().addEntry(key, entry);
                    entry.setFileName(oldEntry.getFileName());
                    MediationPersistenceManager pm
                            = ServiceBusUtils.getMediationPersistenceManager(getAxisConfig());
                    pm.saveItem(key, ServiceBusConstants.ITEM_TYPE_ENTRY);

                    if (log.isDebugEnabled()) {
                        log.debug("Added local entry : " + key + " into the configuration");
                    }
                }
            }
        } catch (SynapseException syne) {
            handleFault(log, "Unable to add local entry ", syne);
        } catch (OMException e) {
            handleFault(log, "Unable to add local entry.Invalid XML ", e);
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * Returns the OMelement representation of the entry given by sequence
     * name
     *
     * @param entryKey - name of the entry to get
     * @return OMElement representing the entryMediator of the given entry
     *         name
     * @throws LocalEntryAdminException if any error occured while getting the data from the
     *                   SynapseConfiguration
     */
    public OMElement getEntry(String entryKey) throws LocalEntryAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            assertKeyEmpty(entryKey);
            entryKey = entryKey.trim();
            if (synapseConfiguration.getEntry(entryKey) != null) {
                OMElement elem = EntrySerializer.serializeEntry(
                        synapseConfiguration.getEntryDefinition(entryKey), null);
                OMFactory fac = elem.getOMFactory();
                OMNamespace nullNS = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "ns3");
                elem.declareNamespace(nullNS);
                if (elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "key")) != null) {
                    if (elem.getAttribute(
                            new QName(XMLConfigConstants.NULL_NAMESPACE, "src")) != null) {
                        elem.addAttribute("type", Integer.toString(Entry.URL_SRC), nullNS);
                    } else if (elem.getFirstOMChild() instanceof OMText) {
                        elem.addAttribute("type", Integer.toString(
                                Entry.INLINE_TEXT), nullNS);
                    } else if (elem.getFirstOMChild() instanceof OMElement) {
                        elem.addAttribute("type", Integer.toString(Entry.INLINE_XML), nullNS);
                    }
                    return elem;
                } else {
                    handleFault(log, "Unable to fetch local entry. Key missing");
                }
            } else {
                handleFault(log, "Entry with the key " + entryKey + " does not exist");
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * Deletes the entry with the given name from SynapseConfiguration
     *
     * @param entryKey - Name of the entry to delete
     * @return whether the operation is successfull or not
     * @throws LocalEntryAdminException if the entry described by the given name doesnt
     *                   exists in the Synapse Configuration
     */
    public boolean deleteEntry(String entryKey) throws LocalEntryAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertKeyEmpty(entryKey);
            log.debug("Deleting local entry with key : " + entryKey);
            entryKey = entryKey.trim();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Entry entry = synapseConfiguration.getDefinedEntries().get(entryKey);
            if (entry != null) {
                synapseConfiguration.removeEntry(entryKey);
                MediationPersistenceManager pm
                            = ServiceBusUtils.getMediationPersistenceManager(getAxisConfig());
                pm.deleteItem(entryKey, entry.getFileName(), ServiceBusConstants.ITEM_TYPE_ENTRY);
                if (log.isDebugEnabled()) {
                    log.debug("Deleted local entry with key : " + entryKey);
                }
                return true;
            } else {
                log.warn("No entry exists by the key : " + entryKey);
                return false;
            }
        } catch (SynapseException syne) {
            handleFault(log, "Unable to delete the local entry : " + entryKey, syne);
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Returns an String array of the entry names present in the synapse configuration
     *
     * @return String array of entry names
     * @throws LocalEntryAdminException if an error occurs in getting the synapse configuration
     */
    public String[] getEntryNames() throws LocalEntryAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Map gloabalEntriesMap = synapseConfiguration.getLocalRegistry();
            List<String> propKeys = new ArrayList<String>();
            for (Object entryValue : gloabalEntriesMap.values()) {
                if (entryValue instanceof Entry) {
                    String key = ((Entry) entryValue).getKey();
                    if (SynapseConstants.SERVER_IP.equals(key)
                            || SynapseConstants.SERVER_HOST.equals(key)) {
                        continue;
                    }
                    propKeys.add(key);
                }
            }
            return propKeys.toArray(new String[propKeys.size()]);
        } finally {
            lock.unlock();
        }
    }

    public String getEntryNamesString() throws LocalEntryAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Map gloabalEntriesMap = synapseConfiguration.getLocalRegistry();
            List<String> sequenceList = new ArrayList<String>();
            List<String> endpointList = new ArrayList<String>();
            List<String> entryList = new ArrayList<String>();
            StringBuffer entrySb = new StringBuffer();
            StringBuffer endpointSb = new StringBuffer();
            StringBuffer sequenceSb = new StringBuffer();
            for (Object entryValue : gloabalEntriesMap.values()) {
                if (entryValue instanceof Endpoint) {
                    Endpoint endpoint = (Endpoint) entryValue;
                    String name = endpoint.getName();
                    if (name != null) {
                        endpointList.add(name);
                    }
                } else if (entryValue instanceof SequenceMediator) {
                    SequenceMediator sequenceMediator = (SequenceMediator) entryValue;
                    String name = sequenceMediator.getName();
                    if (name != null) {
                        sequenceList.add(name);
                    }
                } else if (entryValue instanceof Entry) {
                    Entry entry = (Entry) entryValue;
                    if (!entry.isDynamic() && !entry.isRemote()) { // only care pre-defined entries
                        String key = entry.getKey();
                        if (SynapseConstants.SERVER_IP.equals(key)
                                || SynapseConstants.SERVER_HOST.equals(key)) {
                            continue;
                        }
                        entryList.add(key);
                    }
                }
            }

            if (!sequenceList.isEmpty()) {

                Collections.sort(sequenceList, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                });
                for (String name : sequenceList) {
                    if (name != null) {
                        sequenceSb.append("[Sequence]-").append(name).append(" ");
                    }
                }
            }

            if (!entryList.isEmpty()) {

                Collections.sort(entryList, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                });
                for (String name : entryList) {
                    if (name != null) {
                        entrySb.append("[Entry]-").append(name).append(" ");
                    }
                }
            }

            if (!endpointList.isEmpty()) {

                Collections.sort(endpointList, new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                });
                for (String name : endpointList) {
                    if (name != null) {
                        endpointSb.append("[Enpoint]-").append(name).append(" ");
                    }
                }
            }
            return endpointSb.toString() + entrySb.toString() + sequenceSb.toString();
        } finally {
            lock.unlock();
        }
    }

    public ConfigurationObject[] getDependents(String entryName) {
        DependencyManagementService dependencyMgr = ConfigHolder.getInstance().
                getDependencyManager();
        if (dependencyMgr != null) {
            return dependencyMgr.getDependents(ConfigurationObject.TYPE_ENTRY, entryName);
        } else {
            return null;
        }
    }

    private void assertKeyEmpty(String entryKey) throws LocalEntryAdminException {
        if (entryKey == null || "".equals(entryKey)) {
            handleFault(log, "Entry key cannot be empty");
        }
    }

    private void handleFault(Log log, String message, Exception e) throws LocalEntryAdminException {
        message = message + " :: " + e.getMessage();
        log.error(message, e);
        throw new LocalEntryAdminException(message, e);

    }

     private void handleFault(Log log, String message) throws LocalEntryAdminException {
         log.error(message);
         throw new LocalEntryAdminException(message);
    }

    private OMElement nonCoalescingStringToOm(String xmlStr) throws XMLStreamException {
        StringReader strReader = new StringReader(xmlStr);
        XMLInputFactory xmlInFac = XMLInputFactory.newInstance();
        //Non-Coalescing parsing
        xmlInFac.setProperty("javax.xml.stream.isCoalescing", false);

        XMLStreamReader parser = xmlInFac.createXMLStreamReader(strReader);
        StAXOMBuilder builder = new StAXOMBuilder(parser);

        return builder.getDocumentElement();
    }
}

