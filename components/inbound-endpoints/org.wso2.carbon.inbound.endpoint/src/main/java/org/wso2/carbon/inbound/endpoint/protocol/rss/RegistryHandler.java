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

package org.wso2.carbon.inbound.endpoint.protocol.rss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

public class RegistryHandler {
	private static final Log log = LogFactory.getLog(RegistryHandler.class.getName());
	Resource resource;
	byte[] content;
	CarbonContext cCtx;
	Registry registry;
	ByteArrayInputStream bis;
	ObjectInputStream in;
	ByteArrayOutputStream bos;
	ObjectOutputStream oos;
	Object obj;

	public RegistryHandler() {
		cCtx = CarbonContext.getThreadLocalCarbonContext();
		registry = cCtx.getRegistry(RegistryType.LOCAL_REPOSITORY);
	}

	public Object readFromRegistry(String ResorcePath) {
		try {
			if (registry.resourceExists(ResorcePath)) {
				resource = registry.get(ResorcePath);
				content = (byte[]) resource.getContent();
				try {
					obj = toObject(content);
				} catch (ClassNotFoundException e) {
					log.error(e.getMessage());
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		} catch (RegistryException e) {
			e.printStackTrace();
		}
		return obj;
	}

	public Object toObject(byte[] array) throws IOException, ClassNotFoundException {
		bis = new ByteArrayInputStream(array);
		in = new ObjectInputStream(bis);
		return in.readObject();
	}

	public byte[] toByteArray(Object object) throws IOException, ClassNotFoundException {

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
			} catch (IOException e) {
				log.error(e.getMessage());
			} catch (ClassNotFoundException e) {
				log.error(e.getMessage());
			}
			registry.put(resourceID, resource);
		} catch (RegistryException e) {
			log.error(e.getMessage());
		}
	}

	public void DeleteFromRegitry(String ResorcePath) {
		try {
			registry.delete(ResorcePath);
			log.debug(ResorcePath + " Rigistry Deleted");
		} catch (RegistryException e) {
			log.error(e.getMessage());
		}
	}
}
