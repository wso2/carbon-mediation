/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.sink.internal;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.sink.EventSink;

import java.util.*;

/**
 * Singleton class that stores event sinks deployed in the server. Event sinks are stored in tenant aware manner
 */
public class EventSinkStore {
	private static EventSinkStore instance = new EventSinkStore();

	//tenant id|sink name -> sink
	private Map<String, EventSink> eventSinkMap = Collections.synchronizedMap(new HashMap<String, EventSink>());

	/**
	 * returns singleton instance of EventSinkStore
	 *
	 * @return Singleton instance
	 */
	public static EventSinkStore getInstance() {
		return instance;
	}

	private EventSinkStore() {
	}

	/**
	 * Adds new event sink to current tenant of store
	 *
	 * @param eventSink Event sink to be added to the store.
	 */
	public void addEventSink(EventSink eventSink) {
		String key = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId() + "|" + eventSink.getName();
		eventSinkMap.put(key, eventSink);
	}

	/**
	 * Removes event sink specified from current tenant of store.
	 *
	 * @param eventSinkName the name of the event sink to be removed from store
	 */
	public void removeEventSink(String eventSinkName) {
		String key = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId() + "|" + eventSinkName;
		eventSinkMap.remove(key);
	}

	/**
	 * Finds event sink with given name that is registered in current tenant.
	 *
	 * @param name Name of the event sink
	 * @return Event sink registered in current tenant with given name. If no event sink found with name, returns null
	 */
	public EventSink getEventSink(String name) {
		String key = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId() + "|" + name;
		return eventSinkMap.get(key);
	}

	/**
	 * Returns list of all event sinks registered in current tenant
	 *
	 * @return list of all event sinks registered in current tenant
	 */
	public List<EventSink> getEventSinkList() {
		String tenantKey = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId() + "|";
		List<EventSink> list = new ArrayList<EventSink>();
		synchronized (eventSinkMap) {
			for (Map.Entry<String, EventSink> entry : eventSinkMap.entrySet()) {
				if (entry.getKey().startsWith(tenantKey)) {
					list.add(entry.getValue());
				}
			}
		}
		return list;
	}
}