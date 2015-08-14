/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.rest.api;


public class APIData {

	private String name;

	private String host;

	private int port = -1;

	private String context;

	private String fileName;

	private ResourceData[] resources;

    private boolean isDeployedFromCApp = false;

    private boolean isEdited = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public ResourceData[] getResources() {
		return resources;
	}

	public void setResources(ResourceData[] resources) {
		this.resources = resources;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
    }

    /**
     * Check whether the api is deployed from CApp
     * @return true if api deployed from CApp, else false
     */
    public boolean getDeployedFromCApp() {
        return isDeployedFromCApp;
    }

    /**
     * Set whether the api is deployed from CApp
     * @param isDeployedFromCApp true if api deployed from CApp, else false
     */
    public void setDeployedFromCApp(boolean isDeployedFromCApp) {
        this.isDeployedFromCApp = isDeployedFromCApp;
    }

    /**
     * Check whether the api deployed from CApp is edited through management console
     * @return true if the api is edited, else false
     */
    public boolean getEdited() {
        return isEdited;
    }

    /**
     * Set whether the api deployed from CApp is edited through management console
     * @param isEdited true if the api is edited, else false
     */
    public void setEdited(boolean isEdited) {
        this.isEdited = isEdited;
	}

}
