/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.registry;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.registry.AbstractRegistry;
import org.apache.synapse.registry.RegistryEntry;
import org.wso2.carbon.mediation.registry.persistence.PersistenceManager;
import org.wso2.carbon.mediation.registry.persistence.dataobject.RegistryEntryDO;
import org.wso2.carbon.registry.core.RegistryConstants;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Registry implementation for ESB. This assumes that registry resources can be accessed using FILE
 * system or http. Registry meta data are accessed using web services.
 */
public class ESBRegistry extends AbstractRegistry {

    public static final int FILE = 100;
    public static final int HTTP = 101;
    public static final int HTTPS = 102;
    public static final String URL_SEPARATOR = "/";
    public static final char URL_SEPARATOR_CHAR = '/';

    private static final int DELETE_RETRY_SLEEP_TIME = 10;
    private static final long DEFAULT_CACHABLE_DURATION = 0;
    private static final Log log = LogFactory.getLog(ESBRegistry.class);

    private static final int MAX_KEYS = 200;

    /**
     * File system path corresponding to the FILE url path. This is a system depending path
     * used for accessing resources as files.
     */
    private String localRegistry = null;

    /**
     * URL of the WS endpoind to get data about registry entries.
     */
    private String metaDataService = null;

    /**
     * Specifies whether the registry is in the local host or a remote registry.
     * Local host means the same computer as ESB is running.
     */
    private int registryType = ESBRegistryConstants.LOCAL_HOST_REGISTRY;

    /**
     * Contains the protocol for the registry. Allowd values are FILE, HTTP and HTTPS.
     */
    private int registryProtocol = FILE;

    public ESBRegistry() {
        this.localRegistry = RegistryHelper.getHome();
    }

    public void init(Properties properties) {

        super.init(properties);
        for (Object o : properties.keySet()) {
            if (o != null) {
                String name = (String) o;
                String value = (String) properties.get(name);
                addConfigProperty(name, value);
            }
        }

    }

    public OMNode lookup(String key) {

        if (log.isDebugEnabled()) {
            log.info("==> Repository fetch of resource with key : " + key);
        }
        URLConnection urlc;
        URL url = null;
        try {
            url = new URL(getRoot() + key);
        } catch (MalformedURLException e) {
            handleException("Invalid path '" + getRoot() + key + "' for URL", e);
        }

        if ("file".equals(url.getProtocol())) {
            try {
                url.openStream();
            } catch (IOException ignored) {
                if (!localRegistry.endsWith(URL_SEPARATOR)) {
                    localRegistry = localRegistry + URL_SEPARATOR;
                }
                try {
                    url = new URL(url.getProtocol() + ":" + localRegistry + key);
                } catch (MalformedURLException e) {
                    handleException("Invalid path '" + url.getProtocol() + ":" +
                            localRegistry + key + "' for URL", e);
                }
                try {
                    url.openStream();
                } catch (IOException e) {
                    return null;
                }
            }
        }

        try {
            urlc = url.openConnection();
            urlc.connect();
        } catch (IOException e) {
            return null;
        }

        InputStream input = null;
        try {
            input = urlc.getInputStream();
        } catch (IOException e) {
            handleException("Error when getting a stream from the URL", e);
        }

        if (input == null) {
            return null;
        }

        BufferedInputStream inputStream = new BufferedInputStream(input);
        OMNode result = null;
        try {
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            result = builder.getDocumentElement();

        } catch (OMException ignored) {

            if (log.isDebugEnabled()) {
                log.debug("The resource at the provided URL isn't " +
                        "well-formed XML,So,takes it as a text");
            }

            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Error in closing the input stream. ", e);
            }

            result = SynapseConfigUtils.readNonXML(url);

        } catch (XMLStreamException ignored) {

            if (log.isDebugEnabled()) {
                log.debug("The resource at the provided URL isn't " +
                        "well-formed XML,So,takes it as a text");
            }

            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Error in closing the input stream. ", e);
            }
            result = SynapseConfigUtils.readNonXML(url);

        } finally {
            try {
                result.detach();
                OMDocumentImpl parent = new OMDocumentImpl(OMAbstractFactory.getOMFactory());
                parent.addChild(result);
                inputStream.close();
            } catch (IOException e) {
                log.error("Error in closing the input stream.", e);
            }

        }
        return result;
    }

    public RegistryEntry getRegistryEntry(String key) {

        // get information from the actual resource
        MediationRegistryEntryImpl entryEmbedded = new MediationRegistryEntryImpl();

        try {
            URL url = new URL(getRoot() + key);
            if ("file".equals(url.getProtocol())) {
                try {
                    url.openStream();
                } catch (IOException ignored) {
                    if (!localRegistry.endsWith(URL_SEPARATOR)) {
                        localRegistry = localRegistry + URL_SEPARATOR;
                    }
                    url = new URL(url.getProtocol() + ":" + localRegistry + key);
                    try {
                        url.openStream();
                    } catch (IOException e) {
                        return null;
                    }
                }
            }
            URLConnection urlc = url.openConnection();

            entryEmbedded.setKey(key);
            entryEmbedded.setName(url.getFile());
            entryEmbedded.setType(ESBRegistryConstants.FILE);

            entryEmbedded.setDescription("Resource at : " + url.toString());
            entryEmbedded.setLastModified(urlc.getLastModified());
            entryEmbedded.setVersion(urlc.getLastModified());
            if (urlc.getExpiration() > 0) {
                entryEmbedded.setCachableDuration(
                        urlc.getExpiration() - System.currentTimeMillis());
            } else {
                entryEmbedded.setCachableDuration(getCachableDuration());
            }

        } catch (MalformedURLException e) {
            handleException("Invalid URL reference " + getRoot() + key, e);
        } catch (IOException e) {
            handleException("IO Error reading from URL " + getRoot() + key, e);
        }

        // get information from the database
        PersistenceManager persistenceManager = PersistenceManager.getInstance();
        RegistryEntryDO registryEntryDO = persistenceManager.getRegistryEntry(key);

        if (registryEntryDO != null) {

            if (registryEntryDO.getExpiryTime() != null) {
                entryEmbedded.setCachableDuration(registryEntryDO.getExpiryTime());
            } else {
                entryEmbedded.setCachableDuration(0);
            }
        }

        return entryEmbedded;
    }

    public OMNode lookupFormat(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Updates the metadata of the given registry entry
     *
     * @param entry RegistryEntry containing the new metadata
     */
    public void updateRegistryEntry(RegistryEntry entry) {

        RegistryEntryDO registryEntryDO = new RegistryEntryDO();
        registryEntryDO.setRegistryKey(entry.getKey());
        registryEntryDO.setExpiryTime(entry.getCachableDuration());

        PersistenceManager persistenceManager = PersistenceManager.getInstance();
        persistenceManager.saveOrUpdateRegistryEntry(registryEntryDO);
    }

    /**
     * Updates the registry resource pointed by the given key.
     *
     * @param key   Key of the resource to be updated
     * @param value New value of the resource
     */
    public void updateResource(String key, Object value) {

        if (registryType == ESBRegistryConstants.LOCAL_HOST_REGISTRY) {
            File file = new File(localRegistry + RegistryHelper.getSystemDependentPath(key));
            if (file.exists()) {
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(file));
                    writer.write(value.toString());
                    writer.flush();
                } catch (IOException e) {
                    handleException("Couldn't write to registry entry: " + key, e);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException ignored) {
                        }
                    }
                }

            }
        } else {
            handleException("Remote registry is not supported.");
        }
    }

    /**
     * Adds a new resource to the registry.
     *
     * @param parentName   Key of the parent of the new resource
     * @param resourceName Name of the new resource
     * @param isLeaf       Specifies whether the new resource is a leaf or not. In a FILE system based
     *                     registry, leaf is a FILE and non-leaf is a FOLDER.
     * @throws Exception if an error occurs while creating the resources
     */
    private void addResource(String parentName,
                             String resourceName, boolean isLeaf) throws Exception {

        if (registryType == ESBRegistryConstants.LOCAL_HOST_REGISTRY) {

            if (isLeaf) {
                createFile(RegistryHelper.getSystemDependentPath(parentName), resourceName);
            } else {
                createFolder(RegistryHelper.getSystemDependentPath(parentName), resourceName);
            }
        }
    }

    /**
     * Removes the registry resource identified by the given key. If the key points to a directory,
     * all its subdirectories and files in those directoris will be deleted. All the database
     * entries for deleted registry resources will be removed.
     *
     * @param key resource key
     */
    private void removeResource(String key) {

        if (registryType == ESBRegistryConstants.LOCAL_HOST_REGISTRY) {
            File resource = new File(localRegistry + RegistryHelper.getSystemDependentPath(key));
            if (resource.exists()) {

                if (resource.isFile()) {
                    deleteFile(resource);
                } else if (resource.isDirectory()) {
                    deleteDirectory(resource);
                }

            } else {
                throw new SynapseException("Parent folder: " + key + " does not exists.");
            }
        }
    }

    /**
     * Returns the registry key in URL style (with "/" as the separator) for a given FILE.
     *
     * @param file File to get the key
     * @return Registry key
     */
    private String getRegsitryKey(File file) {

        String path = file.getAbsolutePath();
        String rootPath = new File(localRegistry).getAbsolutePath();
        return getURLPath(path.substring(rootPath.length() + 1)); // we have remove the preceeding "/"
    }

    private void deleteFile(File file) {

        boolean success = file.delete();
        if (!success) {
            // try with this work around to overcome a known bug in windows
            // work around:
            // run garbage collector and sleep for some time and delete
            // if still didn't delete,
            // rename FILE to a temp FILE in "temp" dir and mark it to delete on exist

            System.gc();
            try {
                Thread.sleep(DELETE_RETRY_SLEEP_TIME);
            } catch (InterruptedException e) {
                // ignore the exception
            }

            success = file.delete();
            if (!success) {
                int suffix = 1;
                File renamedFile;

                File tempDir = new File("temp");
                if (!tempDir.exists()) {
                    tempDir.mkdir();
                }

                do {
                    String changedName = "d" + suffix + file.getName();
                    renamedFile = new File(tempDir, changedName);
                    suffix++;
                } while (renamedFile.exists());

                if (file.renameTo(renamedFile)) {
                    renamedFile.deleteOnExit();
                } else {
                    handleException("Cannot delete the resource: " + file.getName());
                }
            }
        }

        PersistenceManager persistenceManager = PersistenceManager.getInstance();
        persistenceManager.deleteRegistryEntry(getRegsitryKey(file));
    }

    private void deleteDirectory(File dir) {

        File[] children = dir.listFiles();
        for (File aChildren : children) {
            if (aChildren != null) {
                if (aChildren.isFile()) {
                    deleteFile(aChildren);
                } else if (aChildren.isDirectory()) {
                    deleteDirectory(aChildren);
                }
            }
        }

        boolean success = dir.delete();
        if (success) {
            PersistenceManager persistenceManager = PersistenceManager.getInstance();
            persistenceManager.deleteRegistryEntry(getRegsitryKey(dir));
        } else {
            handleException("Cannot delete the resource: " + dir.getName());
        }
    }

    private void createFolder(String parentName, String newFolderName) throws Exception {

        /*
        search for parent. if found, create the new FOLDER in it. this depends on whether we are
        using a remote FILE system or not.
        add entry 'parentName/newFolderName' as key to the database.
        */

        if (registryType == ESBRegistryConstants.LOCAL_HOST_REGISTRY) {
            File parent = new File(localRegistry + parentName);
            if (parent.exists()) {

                File newEntry = new File(parent, newFolderName);

                boolean success = newEntry.mkdir();
                if (!success) {
                    handleException("Couldn't create folder: " + newFolderName);
                }

                // update meta data to the database
                // note that we are using the registry key part (path without the local registry
                // root as the key in db
                // if the new FOLDER has a parent FOLDER, use its values for defaults for all
                // possible properties

                RegistryEntryDO registryEntryDO = new RegistryEntryDO();
                registryEntryDO.setRegistryKey(parentName + URL_SEPARATOR + newFolderName);

                PersistenceManager persistenceManager = PersistenceManager.getInstance();
                RegistryEntryDO parentEntryDO =
                        persistenceManager.getRegistryEntry(getURLPath(parentName));
                if (parentEntryDO != null) {
                    registryEntryDO.setExpiryTime(parentEntryDO.getExpiryTime());
                } else {
                    registryEntryDO.setExpiryTime(getCachableDuration());
                }

                persistenceManager.addRegistryEntry(registryEntryDO);

            } else {
                handleException("Parent folder: " + parentName + " does not exists.");
            }
        }
    }

    private void createFile(String parentName, String newFileName) throws Exception {

        /*
        search for parent. if found, create the new FOLDER in it. this depends on whether we are
        using a remote FILE system or not.
        */

        if (registryType == ESBRegistryConstants.LOCAL_HOST_REGISTRY) {
            File parent = new File(localRegistry + parentName);
            if (parent.exists()) {

                File newFile = new File(parent, newFileName);
                boolean success = newFile.createNewFile();
                if (!success) {
                    handleException("Couldn't create resource: " + newFileName);
                }

                // update meta data to the database
                // note that we are using the registry key part (path without the local registry
                // root as the key in db
                // update the db only if we have some thing else to write other than the key

                RegistryEntryDO registryEntryDO = new RegistryEntryDO();
                registryEntryDO.setRegistryKey(parentName + URL_SEPARATOR + newFileName);

                PersistenceManager persistenceManager = PersistenceManager.getInstance();
                RegistryEntryDO parentEntryDO =
                        persistenceManager.getRegistryEntry(getURLPath(parentName));
                if (parentEntryDO != null) {
                    registryEntryDO.setExpiryTime(parentEntryDO.getExpiryTime());
                } else {
                    registryEntryDO.setExpiryTime(getCachableDuration());
                }

                persistenceManager.addRegistryEntry(registryEntryDO);

            } else {
                handleException("Parent folder: " + parentName + " does not exists.");
            }
        }
    }

    /**
     * Configure the ESB registry using registry parameters.
     * <p/>
     * root: FILE:directory -   registry is on local host
     * directory is used to access metadata
     * <p/>
     * root: http/https:location -  has to specify one of the following settings
     * localRegistry - location of the local registry
     * metadataService - url of the service to access metadata
     * If none of above parameters are given "registry" FOLDER is taken as the local registry.
     *
     * @param name name of the config
     * @param value value of the config
     */
    private void addConfigProperty(String name, String value) {

        if (localRegistry == null) {
            // registry root should always end with "/"
            if (ESBRegistryConstants.LOCAL_REGISTRY_ROOT.endsWith(URL_SEPARATOR)) {
                localRegistry = ESBRegistryConstants.LOCAL_REGISTRY_ROOT;
            } else {
                localRegistry = ESBRegistryConstants.LOCAL_REGISTRY_ROOT + URL_SEPARATOR;
            }
        }
        if (name != null && value != null) {
            if (name.equals("root")) {

                // root should always end with '/'
                // therefore, property keys do not have to begin with '/', which could be misleading
                try {
                    URL url = new URL(value);
                    if ("file".equals(url.getProtocol())) {
                        try {
                            url.openStream();
                        } catch (IOException ignored) {
                            if (!localRegistry.endsWith(URL_SEPARATOR)) {
                                localRegistry = localRegistry + URL_SEPARATOR;
                            }
                            url = new URL(url.getProtocol() + ":" + localRegistry + url.getPath());
                            try {
                                url.openStream();
                            } catch (IOException e) {
                                log.error("Unable to open a connection to url : " + url, e);
                            }
                        }
                    }
                    if (url.getProtocol().equals("file")) {
                        registryProtocol = FILE;

                        registryType = ESBRegistryConstants.LOCAL_HOST_REGISTRY;

                        if (url.getPath().endsWith(URL_SEPARATOR)) {
                            localRegistry = RegistryHelper.getSystemDependentPath(url.getPath());
                        } else {
                            localRegistry = RegistryHelper.getSystemDependentPath(url.getPath()) + File.separator;
                        }

                    } else if (url.getProtocol().equals("http")) {
                        registryProtocol = HTTP;
                    } else if (url.getProtocol().equals("https")) {
                        registryProtocol = HTTPS;
                    }


                    if (!value.endsWith(URL_SEPARATOR)) {
                        value = value + URL_SEPARATOR;
                    }


                } catch (MalformedURLException e) {
                    // don't set the root if this is not a valid URL
                    handleException("Registry root should be a valid URL.", e);
                }
            }

            if (name.equals("localRegistry")) {
                registryType = ESBRegistryConstants.LOCAL_HOST_REGISTRY;

                // registry root always ends with "/"
                if (!value.endsWith(File.separator)) {
                    value = value + File.separator;
                }
                localRegistry = value;
            }

            if (name.equals("matadataService")) {
                registryType = ESBRegistryConstants.REMOTE_HOST_REGISTRY;
                metaDataService = value;
            }

            // check if host name verification is disabled
            if (name.equalsIgnoreCase("disableHostNameVerification")) {
                if (value.equalsIgnoreCase("true")) {
                    HostnameVerifier hv = new HostnameVerifier() {
                        public boolean verify(String urlHostName, SSLSession session) {
                            return true;
                        }
                    };

                    HttpsURLConnection.setDefaultHostnameVerifier(hv);
                }
            }

        } else {
            log.debug("Name and Value must need");
        }
    }

    /**
     * Returns the root of the registry. This is always a URL.
     *
     * @return Registry root.
     */
    public String getRoot() {
        String root = (String) properties.get("root");
        if (root == null) {
            return "";
        } else {
            return root;
        }
    }

    public long getCachableDuration() {
        String cachableDuration = (String) properties.get("cachableDuration");
        return cachableDuration == null ? DEFAULT_CACHABLE_DURATION : Long.parseLong(cachableDuration);
    }

    /**
     * Gives the children of the given entry. If the registry is in the same host get the children
     * (subfolders and files) using the FILE system. If the registry is in a remote host, get
     * children using a WS call. Give null or registry entry with "" as the key to list the children
     * of the root.
     *
     * @param entry registry entry to get the children
     * @return children of the given entry
     */
    public RegistryEntry[] getChildren(RegistryEntry entry) {

        String registryRoot = localRegistry;

        if (entry == null) {
            // give the children of the root
            // null or key = "" stands for root

            MediationRegistryEntryImpl registryEntry = new MediationRegistryEntryImpl();
            registryEntry.setKey("");
            entry = registryEntry;
        }

        if (registryType == ESBRegistryConstants.LOCAL_HOST_REGISTRY) {

            // registry is in the local FILE system. access it directly.

            String entryPath = RegistryHelper.getSystemDependentPath(entry.getKey());
            File file = new File(registryRoot + entryPath);
            if (!file.isDirectory()) {
                return null;
            }

            try {

                String[] children = file.list();
                RegistryEntry[] entries = new RegistryEntry[children.length];
                for (int i = 0; i < children.length; i++) {

                    MediationRegistryEntryImpl registryEntry = new MediationRegistryEntryImpl();
                    if (entry.getKey().equals("")) {
                        // user asking for the children of the root
                        registryEntry.setKey(children[i]);
                    } else {
                        if (entryPath.endsWith(URL_SEPARATOR)) {
                            registryEntry.setKey(getURLPath(entry.getKey() + children[i]));
                        } else {
                            registryEntry.setKey(getURLPath(entry.getKey() + URL_SEPARATOR + children[i]));
                        }
                    }

                    // set if the registry entry is a FILE or a FOLDER
                    File entryFile = new File
                            (registryRoot + RegistryHelper.getSystemDependentPath(registryEntry.getKey()));
                    if (entryFile.isDirectory()) {
                        registryEntry.setType(ESBRegistryConstants.FOLDER);
                    }

                    entries[i] = registryEntry;
                }

                return entries;

            } catch (Exception e) {
                handleException("Error in reading the URL.");
            }


        } else if (registryType == ESBRegistryConstants.REMOTE_HOST_REGISTRY) {
            // implement for remote registries.
        }

        return null;
    }

    public RegistryEntry[] getDescendants(RegistryEntry entry) {

        ArrayList<RegistryEntry> list = new ArrayList<RegistryEntry>();
        RegistryEntry[] entries = getChildren(entry);
        if (entries != null) {
            for (RegistryEntry entry1 : entries) {

                if (list.size() > MAX_KEYS) {
                    break;
                }

                fillDescendants(entry1, list);
            }
        }

        RegistryEntry[] descendants = new RegistryEntry[list.size()];
        for (int i = 0; i < list.size(); i++) {
            descendants[i] = list.get(i);
        }

        return descendants;
    }

    public void delete(String path) {
        removeResource(path);
    }

    public void newResource(String path, boolean isDirectory) {
        String parent = getParentPath(path);
        String fileName = getResourceName(path);
        try {
            addResource(parent, fileName, !isDirectory);
        } catch (Exception e) {
            handleException("Error when adding a new resource", e);
        }
    }


    private void fillDescendants(RegistryEntry parent, ArrayList<RegistryEntry> list) {

        RegistryEntry[] entries = getChildren(parent);
        if (entries != null) {
            for (RegistryEntry entry : entries) {

                if (list.size() > MAX_KEYS) {
                    break;
                }

                fillDescendants(entry, list);
            }
        } else {
            list.add(parent);
        }
    }

    private String getURLPath(String filePath) {
        return filePath.replace(File.separatorChar, URL_SEPARATOR_CHAR);
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private String getParentPath(String resourcePath) {

        String parentPath;
        if (resourcePath.equals(RegistryConstants.ROOT_PATH)) {
            parentPath = null;
        } else {
            if (resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) == 0) {
                parentPath = RegistryConstants.ROOT_PATH;
            } else {
                parentPath = resourcePath.substring(
                        0, resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
            }
        }
        return parentPath;
    }


    private String getResourceName(String path) {
        if (path != null) {
            String correctedPath = path;
            if (path.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                correctedPath = path.substring(0,
                        path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
            }
            return correctedPath.substring(
                    correctedPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1,
                    correctedPath.length());
        }
        return "";

    }

}
