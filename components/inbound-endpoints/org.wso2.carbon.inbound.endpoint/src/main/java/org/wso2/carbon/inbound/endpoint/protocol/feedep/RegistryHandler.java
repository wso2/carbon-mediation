/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.inbound.endpoint.protocol.feedep;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.inbound.endpoint.persistence.ServiceReferenceHolder;

class RegistryHandler {
    private static final Log log = LogFactory.getLog(RegistryHandler.class.getName());
    private Resource resource;
    private byte[] content;
    private Registry registry;
    private ByteArrayInputStream bis;
    private ObjectInputStream in;
    private ByteArrayOutputStream bos;
    private ObjectOutputStream oos;
    private Object obj;

    public RegistryHandler() {
        try {
            registry = ServiceReferenceHolder.getInstance().getRegistry();
        } catch (RegistryException e) {
            log.error(e.getMessage());
        }

    }

    public Object readFromRegistry(String ResorcePath) {
        try {
            if (registry.resourceExists(ResorcePath)) {
                resource = registry.get(ResorcePath);
                content = (byte[]) resource.getContent();
                try {
                    obj = toObject(content);
                } catch (ClassNotFoundException | IOException e) {
                    log.error(e.getMessage());
                }
            }
        } catch (RegistryException e) {
            log.error(e.getMessage());
        }
        return obj;
    }

    private Object toObject(byte[] array) throws IOException, ClassNotFoundException {
        bis = new ByteArrayInputStream(array);
        in = new ObjectInputStream(bis);
        return in.readObject();
    }

    private byte[] toByteArray(Object object) throws IOException, ClassNotFoundException {

        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        oos.writeObject(object);

        oos.flush();
        oos.close();
        bos.close();
        return bos.toByteArray();
    }

    public void writeToRegistry(String resourceID, Object object) {

        try {
            resource = registry.newResource();
            try {
                resource.setContent(toByteArray(object));
            } catch (IOException | ClassNotFoundException e) {
                log.error(e.getMessage());
            }
            registry.put(resourceID, resource);
        } catch (RegistryException e) {
            log.error(e.getMessage());
        }
    }

    public void deleteFromRegitry(String ResorcePath) {
        try {
            registry.delete(ResorcePath);
            log.debug(ResorcePath + " Rigistry Deleted");
        } catch (RegistryException e) {
            log.error(e.getMessage());
        }
    }
}
