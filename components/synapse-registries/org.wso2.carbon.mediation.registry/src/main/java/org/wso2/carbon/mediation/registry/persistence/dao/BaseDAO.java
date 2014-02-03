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

package org.wso2.carbon.mediation.registry.persistence.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.registry.ESBRegistryConstants;
import org.wso2.carbon.mediation.registry.persistence.dataobject.BaseDO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;


/**
 * The base DataAccessObject
 */
public abstract class BaseDAO {

    protected Log log = LogFactory.getLog(BaseDAO.class);

    private Registry registry;

    public BaseDAO(Registry registry) {
        this.registry = registry;
    }

    public void create(String path, BaseDO baseDO) {
        Resource resource = createResource(path);
        populateResource(resource, baseDO);
    }

    public void update(String path, BaseDO baseDO) {
        Resource resource = getResource(path);
        if (resource != null) {
            populateResource(resource, baseDO);
            setResource(resource);
        }
    }

    public void delete(String path) {
        deleteResource(path);
    }

    public BaseDO get(String path) {
        Resource resource = getResource(path);
        if (resource != null) {
            return populateDataObject(resource);
        }
        return null;
    }

    public abstract void populateResource(Resource resource, BaseDO baseDO);

    public abstract BaseDO populateDataObject(Resource resource);

    private String getCorrectPath(String path) {
        if (path == null) {
            return path;
        }
        String tempPath = path;
        if (path.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            tempPath = path.substring(0, path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
        }
        if (!tempPath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            tempPath = RegistryConstants.PATH_SEPARATOR + tempPath;
        }
        return ESBRegistryConstants.ROOT_PATH + tempPath;
    }

    private Resource createResource(String path) {
        try {
            String resolvedPath = getCorrectPath(path);
            if (!registry.resourceExists(resolvedPath)) {
                ResourceImpl resource = new ResourceImpl();
                resource.setPath(resolvedPath);
                return resource;
            }
        } catch (RegistryException e) {
            handleException("Unable to create a Resource in path : " + path, e);
        }
        return null;
    }

    private void setResource(Resource resource) {
        try {
            registry.put(resource.getPath(), resource);
        } catch (RegistryException e) {
            handleException("Error when setting a resource in the path : " + resource.getPath(), e);
        }
    }

    private Resource getResource(String path) {
        try {
            String resolvedPath = getCorrectPath(path);
             if (registry.resourceExists(resolvedPath)) {
                return registry.get(resolvedPath);
             }
        } catch (RegistryException e) {
            handleException("Error when setting a resource in the path : " + path, e);
        }
        return null;
    }

    private void deleteResource(String path) {
        try {
            String resolvedPath = getCorrectPath(path);
            if (registry.resourceExists(resolvedPath)) {
                registry.delete(resolvedPath);
            }
        } catch (RegistryException e) {
            handleException("Error when deleting a resource at path :" + path, e);
        }
    }

    protected void handleException(String msg, Throwable throwable) {
        log.error(msg, throwable);
        throw new RuntimeException(msg, throwable);
    }

    protected void handleException(String msg) {
        log.error(msg);
        throw new RuntimeException(msg);
    }
}

