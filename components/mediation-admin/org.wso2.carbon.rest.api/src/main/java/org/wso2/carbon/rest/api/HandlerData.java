/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.rest.api;

/**
 * This class manages the attributes of a handler
 */
public class HandlerData {
	
	private String handler;
	private String[] properties;

    public String getHandler() {

        return handler;
    }

    public void setHandler(String handler) {

        this.handler = handler;
    }

    public String[] getProperties() {

        return properties;
    }

    public void setProperties(String[] properties) {

        this.properties = properties;
    }
}
