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

package org.wso2.carbon.event.sink;

import java.util.List;

/**
 * Interface for event sink OSGI declarative service
 */
public interface EventSinkService {

	/**
	 * Returns a list of all event sinks deployed in current tenant
	 *
	 * @return List of all event sinks deployed in current tenant
	 */
	public List<EventSink> getEventSinks();

	/**
	 * Returns event sink with given name that is deployed in current tenant
	 *
	 * @param eventSinkName Name of the event sink
	 * @return Event sink with given name that is deployed in current tenant. If no event sink found with given name,
	 * null is returned
	 */
	public EventSink getEventSink(String eventSinkName);
}