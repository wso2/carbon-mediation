/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.application.mgt.synapse;

/**
 * Metadata object for holding all synapse related artifacts
 */
public class SynapseApplicationMetadata {

    private String appName;
    private String[] sequences;
    private EndpointMetadata[] endpoints;
    private String[] localEntries;
    private String[] proxyServices;
    private String[] events;
    private String[] mediators;
    private TaskMetadata[] tasks;


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String[] getSequences() {
        return sequences;
    }

    public void setSequences(String[] sequences) {
        this.sequences = sequences;
    }

    public EndpointMetadata[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(EndpointMetadata[] endpoints) {
        this.endpoints = endpoints;
    }

    public String[] getLocalEntries() {
        return localEntries;
    }

    public void setLocalEntries(String[] localEntries) {
        this.localEntries = localEntries;
    }

    public String[] getProxyServices() {
        return proxyServices;
    }

    public void setProxyServices(String[] proxyServices) {
        this.proxyServices = proxyServices;
    }

    public String[] getEvents() {
        return events;
    }

    public void setEvents(String[] events) {
        this.events = events;
    }

    public String[] getMediators() {
        return mediators;
    }

    public void setMediators(String[] mediators) {
        this.mediators = mediators;
    }

    public TaskMetadata[] getTasks() {
        return tasks;
    }

    public void setTasks(TaskMetadata[] tasks) {
        this.tasks = tasks;
    }
}
