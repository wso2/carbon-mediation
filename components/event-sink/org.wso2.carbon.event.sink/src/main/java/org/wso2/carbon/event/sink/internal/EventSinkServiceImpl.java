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

import org.wso2.carbon.event.sink.EventSink;
import org.wso2.carbon.event.sink.EventSinkService;

import java.util.List;

public class EventSinkServiceImpl implements EventSinkService {
	@Override
	public List<EventSink> getEventSinks() {
		return EventSinkStore.getInstance().getEventSinkList();
	}

	@Override
	public EventSink getEventSink(String eventSinkName) {
		return EventSinkStore.getInstance().getEventSink(eventSinkName);
	}
}