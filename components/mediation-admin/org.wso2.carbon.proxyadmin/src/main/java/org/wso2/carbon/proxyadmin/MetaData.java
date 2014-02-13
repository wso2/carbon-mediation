/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.proxyadmin;

public class MetaData {
    private boolean transportsAvailable;
    private boolean endpointsAvailable;
    private boolean sequencesAvailable;

    private String [] transports;
    private String [] endpoints;
    private String [] sequences;

    public boolean isTransportsAvailable() {
        return transportsAvailable;
    }

    public void setTransportsAvailable(boolean transportsAvailable) {
        this.transportsAvailable = transportsAvailable;
    }

    public boolean isEndpointsAvailable() {
        return endpointsAvailable;
    }

    public void setEndpointsAvailable(boolean endpointsAvailable) {
        this.endpointsAvailable = endpointsAvailable;
    }

    public boolean isSequencesAvailable() {
        return sequencesAvailable;
    }

    public void setSequencesAvailable(boolean sequencesAvailable) {
        this.sequencesAvailable = sequencesAvailable;
    }

    public String[] getTransports() {
        return transports;
    }

    public void setTransports(String[] transports) {
        this.transports = transports;
    }

    public String[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String[] endpoints) {
        this.endpoints = endpoints;
    }

    public String[] getSequences() {
        return sequences;
    }

    public void setSequences(String[] sequences) {
        this.sequences = sequences;
    }
}
