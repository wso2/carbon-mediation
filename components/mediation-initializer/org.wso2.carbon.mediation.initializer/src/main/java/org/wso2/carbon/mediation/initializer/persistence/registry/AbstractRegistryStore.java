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

package org.wso2.carbon.mediation.initializer.persistence.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.persistence.ServiceBusPersistenceException;

import javax.xml.stream.XMLStreamException;
import java.util.Collection;
import java.util.ArrayList;

/**
 *
 */
public abstract class AbstractRegistryStore {

    protected Log log = LogFactory.getLog(AbstractRegistryStore.class);

    protected UserRegistry registry;

    protected String configName;

    protected AbstractRegistryStore(UserRegistry registry, String configName) {
        this.registry = registry;
        this.configName = configName;
    }

    public abstract Collection<OMElement> getElements();

    public abstract OMElement getElement(String name);

    public abstract void persistElement(String name, OMElement element, String fileName);

    public abstract void deleteElement(String name);

    protected void createCollection(String path) {
        try {
            if (!registry.resourceExists(path)) {
                org.wso2.carbon.registry.core.Collection collection = registry.newCollection();
                registry.put(path, collection);
            }
        } catch (RegistryException e) {
            handleException("Unable to create the collection : " + path, e);
        }
    }

    protected void persistElement(OMElement element, String resourcePath, String fileName)
            throws RegistryException {
        try {
            registry.beginTransaction();

            if (registry.resourceExists(resourcePath)) {
                registry.delete(resourcePath);
            }
            Resource resource = registry.newResource();
            resource.setContent(element.toString());
            if (fileName != null) {
                resource.setProperty(ServiceBusConstants.DEFINITION_FILE_NAME, fileName);
            }
            resource.setMediaType("text/xml");
            registry.put(resourcePath, resource);
            registry.commitTransaction();

        } catch (Exception e) {
            registry.rollbackTransaction();
            throw new RegistryException("Unable to persist element", e);
        }
    }

    protected void handleException(String message, Exception e) {
        log.error(message, e);
        throw new ServiceBusPersistenceException(message, e);
    }

    protected void handleException(String message) {
        log.error(message);
        throw new ServiceBusPersistenceException(message);
    }

    protected Collection<OMElement> getChildElementsInPath(String path) throws RegistryException {
        Collection<OMElement> childElements = new ArrayList<OMElement>();
        if (registry.resourceExists(path)) {
            Resource resource = registry.get(path);
            if (resource instanceof org.wso2.carbon.registry.core.Collection) {
                org.wso2.carbon.registry.core.Collection collection
                        = (org.wso2.carbon.registry.core.Collection) resource;
                String[] children = collection.getChildren();
                for (String resourcePath : children) {
                    OMElement resourceElem = getResourceAsElement(resourcePath);
                    if (resourceElem != null) {
                        childElements.add(resourceElem);
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Collection at path " + path + " doesn't exists in the registry");
            }
        }

        return childElements;
    }

    protected OMElement getResourceAsElement(String resourcePath) throws RegistryException {
        OMElement resourceElem = null;
        Resource childResource = registry.get(resourcePath);
        try {
            resourceElem = new StAXOMBuilder(
                    childResource.getContentStream()).getDocumentElement();
            resourceElem.build();
            String fileNameProperty
                    = childResource.getProperty(ServiceBusConstants.DEFINITION_FILE_NAME);
            if (fileNameProperty != null) {
                resourceElem.addAttribute(ServiceBusConstants.DEFINITION_FILE_NAME,
                        fileNameProperty, resourceElem.getOMFactory().createOMNamespace("", ""));
            }
        } catch (XMLStreamException e) {
            log.warn("Resource loadded from the registry with path : "
                    + resourcePath + " is not well formed XML");
        }
        return resourceElem;
    }

    protected String getConfigurationRoot() {
        return ServiceBusConstants.SYNAPSE_CONFIG_REGISTRY_SPACE +
                RegistryConstants.PATH_SEPARATOR + configName;    
    }

    protected abstract String getConfigurationPath();

}
