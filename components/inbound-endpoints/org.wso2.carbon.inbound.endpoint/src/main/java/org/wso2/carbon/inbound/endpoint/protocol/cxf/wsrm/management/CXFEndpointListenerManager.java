/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.management;

import org.apache.cxf.Bus;
import org.apache.log4j.Logger;
import org.wso2.carbon.inbound.endpoint.protocol.http.management.EndpointListenerManager;

import java.util.concurrent.ConcurrentHashMap;

public class CXFEndpointListenerManager {

	private ConcurrentHashMap<Integer, Bus> cxfEndpointMap;
	private static CXFEndpointListenerManager instance;
	private static final Logger logger = Logger.getLogger(CXFEndpointListenerManager.class);

	private CXFEndpointListenerManager() {
		cxfEndpointMap = new ConcurrentHashMap<Integer, Bus>();
	}

	public static synchronized CXFEndpointListenerManager getInstance() {
		if (instance == null) {
			instance = new CXFEndpointListenerManager();
		}
		return instance;
	}

	public void addCXFEndpoint(int port, Bus bus) {
		cxfEndpointMap.put(port, bus);
	}

	public boolean authorizeCXFInboundEndpoint(int port, String name) {
		Bus b = cxfEndpointMap.get(port);
		b.shutdown(true);
		cxfEndpointMap.remove(port);
		return true;
//		return EndpointListenerManager.getInstance().startCXFEndpoint(port, name);
	}

	public void unregisterCXFInboundEndpoint(int port) {
		cxfEndpointMap.remove(port);
		EndpointListenerManager.getInstance().closeCXFEndpoint(port);
	}
}
