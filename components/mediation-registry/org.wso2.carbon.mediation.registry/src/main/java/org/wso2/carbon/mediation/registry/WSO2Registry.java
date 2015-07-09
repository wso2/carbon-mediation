/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.registry;

import org.apache.synapse.registry.AbstractRegistry;
import org.apache.synapse.registry.RegistryEntry;
import org.apache.synapse.SynapseException;
import org.apache.synapse.util.SynapseBinaryDataSource;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.context.CarbonContext;

import javax.xml.stream.*;
import javax.activation.DataHandler;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class WSO2Registry extends AbstractRegistry {

    private static final Log log = LogFactory.getLog(WSO2Registry.class);

    public static final String CONFIG_REGISTRY_PREFIX = "conf:";
    public static final String GOVERNANCE_REGISTRY_PREFIX = "gov:";
    public static final String LOCAL_REGISTRY_PREFIX = "local:";

    private static final String EXPIRY_TIME = "expiryTime";
    private static final String CACHABLE_DURATION = "cachableDuration";
    private static final String EXTENSIONS = "extensions";
    private static final String ROOT = "root";

    private static final int MAX_KEYS = 200;

    private Registry configRegistry;
    private Registry localRegistry;
    private Registry governanceRegistry;

    private String rootPath;

    private List<RegistryExtension> extensions = new ArrayList<RegistryExtension>();

    private String domain;
    private int tenantId;

    public WSO2Registry() {
        RegistryService registryService = RegistryServiceHolder.getInstance().getRegistryService();
        try {
            configRegistry = registryService.getConfigSystemRegistry(
                    CarbonContext.getThreadLocalCarbonContext().getTenantId());
            governanceRegistry = registryService.getGovernanceSystemRegistry(
                    CarbonContext.getThreadLocalCarbonContext().getTenantId());
            localRegistry = registryService.getLocalRepository(
                    CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (RegistryException e) {
            handleException("Error while initializing the mediation registry adapter", e);
        }
    }

    @Override
    public void init(Properties properties) {
        super.init(properties);

        //Saving tenant information as instance variables to use later when registry lookup calls comes via non-carbon
        //thread pools
        CarbonContext cc = CarbonContext.getThreadLocalCarbonContext();
        domain = cc.getTenantDomain();
        tenantId = cc.getTenantId();

        String root = properties.getProperty(ROOT);
        if (root == null || "".equals(root)) {
            rootPath = RegistryConstants.ROOT_PATH;
        } else {
            if (!root.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                root = RegistryConstants.PATH_SEPARATOR + root;
            }

            if (!root.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                root += RegistryConstants.PATH_SEPARATOR;
            }
            rootPath = root;
        }

        String extensionsProperty = properties.getProperty(EXTENSIONS);
        if (extensionsProperty != null && !"".equals(extensionsProperty)) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing mediation registry extensions");
            }

            String[] classNames = extensionsProperty.split(",");
            for (String className : classNames) {
                registerExtension(className);
            }
        }
    }

    public OMNode lookup(String key) {

        setTenantInfo();

        if (key == null) {
            handleException("Resource cannot be found.");
        }

        if ("".equals(key)) {
            handleException("Resource cannot be empty");
        }

        if (log.isDebugEnabled()) {
            log.debug("==> Repository fetch of resource with key : " + key);
        }

        try {
            Resource resource = getResource(key);

            if (resource instanceof Collection) {
                return null;
            }

            if (resource == null) { // try via available extensions
                if (extensions.size() > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("Attempting to fetch the resource from the engaged extensions");
                    }

                    for (RegistryExtension extension : extensions) {
                        OMNode result = extension.lookup(key);
                        if (result != null) {
                            return result;
                        }
                    }
                }
                return null;
            }

            ByteArrayInputStream inputStream = null;
            Object content = resource.getContent();
            if (content instanceof String) {
                inputStream = new ByteArrayInputStream(content.toString().getBytes());
            } else if (content instanceof byte[]) {
                inputStream = new ByteArrayInputStream((byte[]) content);
            }

            OMNode result = null;
            try {
                XMLStreamReader parser = XMLInputFactory.newInstance().
                        createXMLStreamReader(inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                result = builder.getDocumentElement();

            } catch (OMException ignored) {
                result = readNonXML(resource);

            } catch (XMLStreamException ignored) {
                result = readNonXML(resource);

            } catch (Exception e) {
                // a more general exception(e.g. a Runtime exception if the XML doc has an
                // external DTD deceleration and if not connected to internet) which in case
                // just log for debugging
                log.error("Error while reading the resource '" + key + "'", e);
            } finally {
                try {
                    resource.discard();
                    if (result != null && result.getParent() != null) {
                        result.detach();
                        OMDocumentImpl parent = new OMDocumentImpl(OMAbstractFactory.getOMFactory());
                        parent.addChild(result);
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.error("Error while closing the input stream", e);
                }
            }
            return result;

        } catch (RegistryException e) {
            handleException("Error while fetching the resource " + key, e);
        }

        return null;
    }

    public OMNode lookupFormat(String key) {

        setTenantInfo();

        if (key == null) {
            handleException("Resource cannot be found.");
        }

        if ("".equals(key)) {
            handleException("Resource cannot be empty");
        }

        if (log.isDebugEnabled()) {
            log.debug("==> Repository fetch of resource with key : " + key);
        }

        try {
            Resource resource = getResource(key);

            if (resource instanceof Collection) {
                return null;
            }

            if (resource == null) { // try via available extensions
                if (extensions.size() > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("Attempting to fetch the resource from the engaged extensions");
                    }

                    for (RegistryExtension extension : extensions) {
                        OMNode result = extension.lookup(key);
                        if (result != null) {
                            return result;
                        }
                    }
                }
                return null;
            }

            ByteArrayInputStream inputStream = null;
            Object content = resource.getContent();
            if (content instanceof String) {
                inputStream = new ByteArrayInputStream(content.toString().getBytes());
            } else if (content instanceof byte[]) {
                inputStream = new ByteArrayInputStream((byte[]) content);
            }

            OMNode result = null;
            try {
                XMLStreamReader parser = XMLInputFactory.newInstance().
                        createXMLStreamReader(inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                result = builder.getDocumentElement();

            } catch (OMException ignored) {
                result = readNonXML(resource);

            } catch (XMLStreamException ignored) {
                result = readNonXML(resource);

            } catch (Exception e) {
                // a more general exception(e.g. a Runtime exception if the XML doc has an
                // external DTD deceleration and if not connected to internet) which in case
                // just log for debugging
                log.error("Error while reading the resource '" + key + "'", e);
            } finally {
                try {
                    resource.discard();

                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.error("Error while closing the input stream", e);
                }
            }
            return result;

        } catch (RegistryException e) {
            handleException("Error while fetching the resource " + key, e);
        }

        return null;
    }

    public void delete(String key) {

        setTenantInfo();

        try {
            Registry registry = getRegistry(key);
            String resolvedPath = resolvePath(key);
            if (registry.resourceExists(resolvedPath)) {
                registry.delete(resolvedPath);
            }
        } catch (RegistryException e) {
            handleException("Error while deleting the resource at path :" + key, e);
        }
    }

    public void newResource(String key, boolean isDirectory) {

        setTenantInfo();

        Registry registry = getRegistry(key);
        String resolvedKey = resolvePath(key);
        try {
            Resource resource;
            if (isDirectory) {
                resource = registry.newCollection();
            } else {
                resource = registry.newResource();
            }
            registry.put(resolvedKey, resource);
        } catch (RegistryException e) {
            handleException("Error while saving a resource at " + key, e);
        }
    }

    /**
     * Updates the content of a resource in the given path with given content
     *
     * @param path  The resource path
     * @param value The resource content to be set
     */
    public void updateResource(String path, Object value) {

        setTenantInfo();

        Resource resource = getResource(path);
        if (resource != null) {
            Registry registry = getRegistry(path);
            if (value instanceof OMNode) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    XMLStreamWriter xmlStreamWriter =
                            XMLOutputFactory.newInstance().createXMLStreamWriter(baos);
                    ((OMNode) value).serialize(xmlStreamWriter);
                    resource.setContent(baos.toByteArray());
                } catch (XMLStreamException e) {
                    handleException("Error when serializing OMNode " + value, e);
                } catch (RegistryException e) {
                    handleException("Error when setting content " + value, e);
                }

            } else {
                try {
                    resource.setContent(value);
                } catch (RegistryException e) {
                    handleException("Error when setting content " + value, e);
                }
            }

            try {
                registry.put(resource.getPath(), resource);
                resource.discard();
            } catch (RegistryException e) {
                handleException("Error when setting a resource in the path : " + path, e);
            }
        }
    }

    public void updateRegistryEntry(RegistryEntry entry) {

        setTenantInfo();

        String key = entry.getKey();
        Resource resource = getResource(key);

        if (resource != null) {
            Registry registry = getRegistry(key);
            resource.setProperty(CACHABLE_DURATION,
                    String.valueOf(entry.getCachableDuration()));
            resource.setProperty(EXPIRY_TIME,
                    String.valueOf(System.currentTimeMillis() + entry.getCachableDuration()));
            try {
                registry.put(resource.getPath(), resource);
                resource.discard();
            } catch (RegistryException e) {
                handleException("Error when setting a resource in the path : " + key, e);
            }
        }
    }

    public RegistryEntry getRegistryEntry(String entryKey) {

        setTenantInfo();

        MediationRegistryEntryImpl entry = new MediationRegistryEntryImpl();
        Resource resource = getResource(entryKey);

        if (resource != null) {

            entry.setKey(entryKey);
            entry.setName(resource.getPath());

            if (resource instanceof Collection) {
                entry.setType(ESBRegistryConstants.FOLDER);
            } else {
                entry.setType(ESBRegistryConstants.FILE);
            }

            entry.setDescription("Resource at : " + resource.getPath());
            entry.setLastModified(resource.getLastModified().getTime());
            entry.setVersion(resource.getLastModified().getTime());

            // Get root's cachable duration value
            long cacheTime = getCachableDuration();
            String cachableDuration = resource.getProperty(CACHABLE_DURATION);
            if (cachableDuration != null) {
                // If the resource defines its own cachableDuration property,
                // override the value from root
                try {
                    cacheTime = Long.parseLong(cachableDuration);
                } catch (NumberFormatException e) {
                    handleException("Couldn't pass the cachableDuration as a long", e);
                }
            }
            entry.setCachableDuration(cacheTime);
        } else {
            // If the resource was found by an extension we need to set the
            // following properties for cachine to work
            entry.setKey(entryKey);
            entry.setCachableDuration(getCachableDuration());
        }

        return entry;
    }

    public RegistryEntry[] getChildren(RegistryEntry entry) {

        setTenantInfo();

        if (entry == null) {
            // give the children of the root
            // null or key = "" stands for root

            MediationRegistryEntryImpl registryEntry = new MediationRegistryEntryImpl();
            registryEntry.setKey(rootPath);
            entry = registryEntry;
        } else {
            if ("".equals(entry.getKey())) {
                ((MediationRegistryEntryImpl) entry).setKey(rootPath);
            }
        }

        String parentPath;
        if (!entry.getKey().endsWith(RegistryConstants.PATH_SEPARATOR)) {
            parentPath = entry.getKey() + RegistryConstants.PATH_SEPARATOR;
        } else {
            parentPath = entry.getKey();
        }

        Resource resource = getResource(entry.getKey());
        if (resource == null) {
            return new RegistryEntry[]{new MediationRegistryEntryImpl()};
        }

        if (resource instanceof Collection) {

            CollectionImpl collection = (CollectionImpl) resource;
            String[] children = new String[0];
            try {
                children = collection.getChildren();
            } catch (RegistryException e) {
                handleException("Error when retrieving children");
            }
            List<RegistryEntry> entryList = new ArrayList<RegistryEntry>();

            for (String child : children) {

                String key;
                if (child.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    key = child.substring(0, child.length() - 1);
                } else {
                    key = child;
                }

                Resource childResource = getResource(key);
                if (childResource == null) {
                    continue;
                }

                MediationRegistryEntryImpl registryEntryEmbedded = new MediationRegistryEntryImpl();
                registryEntryEmbedded.setKey(parentPath + RegistryUtils.getResourceName(key));

                if (childResource instanceof Collection) {
                    registryEntryEmbedded.setType(ESBRegistryConstants.FOLDER);
                } else {
                    registryEntryEmbedded.setType(ESBRegistryConstants.FILE);
                }

                entryList.add(registryEntryEmbedded);
            }

            return entryList.toArray(new RegistryEntry[entryList.size()]);

        } else {
            ((MediationRegistryEntryImpl) entry).setType(ESBRegistryConstants.FILE);
            return new RegistryEntry[]{entry};
        }

    }

    public RegistryEntry[] getDescendants(RegistryEntry entry) {

        setTenantInfo();

        ArrayList<RegistryEntry> list = new ArrayList<RegistryEntry>();
        RegistryEntry[] entries = getChildren(entry);

        if (entries != null) {
            for (RegistryEntry currentEntry : entries) {
                if (list.size() > MAX_KEYS) {
                    break;
                }
                fillDescendants(currentEntry, list);
            }
        }

        RegistryEntry[] descendants = new RegistryEntry[list.size()];
        for (int i = 0; i < list.size(); i++) {
            descendants[i] = list.get(i);
        }

        return descendants;
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

    public Resource getResource(String path) {

        setTenantInfo();

        Registry registry = getRegistry(path);
        String key = resolvePath(path);

        try {
            if (registry.resourceExists(key)) {
                return registry.get(key);
            }
        } catch (RegistryException e) {
            handleException("Error while fetching the resource " + path, e);
        }
        return null;
    }

    /**
     * Returns all resource properties for a given registry resource
     *
     * @param entryKey - Registry path of the resource
     * @return Map of resource properties
     */

    public Properties getResourceProperties(String entryKey) {

        setTenantInfo();

        Resource resource = getResource(entryKey);
        if (resource != null) {
            Properties properties = new Properties();
            Properties resourceProperties = resource.getProperties();
            if (resourceProperties != null) {
                for (Object key : resourceProperties.keySet()) {
                    Object value = resourceProperties.get(key);
                    if (value instanceof List) {
                        if (((List) value).size() > 0) {
                            properties.put(key, ((List) value).get(0));
                        }
                    } else {
                        properties.put(key, value);
                    }
                }
            }

            return properties;
        }
        return null;
    }

    private Registry getRegistry(String path) {
        if (path == null || "".equals(path) || path.startsWith(GOVERNANCE_REGISTRY_PREFIX)) {
            return governanceRegistry;
        } else if (path.startsWith(CONFIG_REGISTRY_PREFIX)) {
            return configRegistry;
        } else if (path.startsWith(LOCAL_REGISTRY_PREFIX)) {
            return localRegistry;
        } else {
            return governanceRegistry;
        }
    }

    private String resolvePath(String path) {
        if (path == null || "".equals(path)) {
            path = RegistryConstants.ROOT_PATH;
        }

        boolean governanceReg = false;

        if (path.startsWith(GOVERNANCE_REGISTRY_PREFIX)) {
            path = path.substring(GOVERNANCE_REGISTRY_PREFIX.length());
            governanceReg = true;
        } else if (path.startsWith(CONFIG_REGISTRY_PREFIX)) {
            path = path.substring(CONFIG_REGISTRY_PREFIX.length());
        } else if (path.startsWith(LOCAL_REGISTRY_PREFIX)) {
            path = path.substring(LOCAL_REGISTRY_PREFIX.length());
        } else {
            governanceReg = true;
        }

        if (governanceReg && rootPath != null) {
            if (path.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                path = path.substring(1);
            }
            path = rootPath + path;
        }
        return path;
    }

    /**
     * Helper method to handle non-XMl resources
     *
     * @param resource Registry resource
     * @return The content as an OMNode
     * @throws RegistryException if an error occurs while accessing the resource content
     */
    private OMNode readNonXML(Resource resource) throws RegistryException {

        if (log.isDebugEnabled()) {
            log.debug("The resource at the specified path does not contain " +
                    "well-formed XML - Processing as text");
        }

        if (resource != null) {

            if (resource.getMediaType() != null) {
                if (resource.getMediaType().equals("text/plain")) {
                    // for non-xml text content
                    return OMAbstractFactory.getOMFactory().createOMText(
                            new String((byte[]) resource.getContent()));
                }
            } else {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(
                        (byte[]) resource.getContent());
                try {
                    OMFactory omFactory = OMAbstractFactory.getOMFactory();
                    return omFactory.createOMText(
                            new DataHandler(new SynapseBinaryDataSource(inputStream,
                                    resource.getMediaType())), true);
                } catch (IOException e) {
                    handleException("Error while getting a stream from resource content ", e);
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("Error while closing the input stream", e);
                    }
                }
            }
        }
        return null;
    }

    private void registerExtension(String className) {
        try {
            Class clazz = this.getClass().getClassLoader().loadClass(className.trim());
            RegistryExtension ext = (RegistryExtension) clazz.newInstance();
            ext.init(properties);
            extensions.add(ext);
        } catch (Exception e) {
            handleException("Error while instantiating the registry extension " +
                    "class : " + className, e);
        }
    }

    private long getCachableDuration() {
        String cachableDuration = (String) properties.get("cachableDuration");
        return cachableDuration == null ? 0 : Long.parseLong(cachableDuration);
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    /**
     *  Carbon Kernel mandates to set Threadlocal before calling anything in kernel
     */
    private void setTenantInfo() {
        // Preserve user name
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        PrivilegedCarbonContext.destroyCurrentContext();
        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        cc.setTenantDomain(domain);
        cc.setTenantId(tenantId);
        if (username != null) {         // Set back the user name
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
        }
    }
}
