/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.cloud.gateway.common;


/**
 * <code>CGServiceMetaDataBean </code> represent the meta data associated with a service
 */
public class CGServiceMetaDataBean {

    /**
     * is MTOM enabled?
     */
    private boolean isMTOMEnabled;
    /**
     * The service name
     */
    private String serviceName;

    /**
     * The endpoint address
     */
    private String endpoint;

    /**
     * WS-RM enabled ?
     */
    private boolean isWsRmEnabled;

    /**
     * RM policy
     */
    private String rmPolicy;

    /**
     * WS-Sec enabled
     */
    private boolean isWsSecEnabled;

    /**
     * Security policy
     */
    private String secPolicy;

    /**
     * The WSDL location of the service
     */
    private String wsdlLocation;

    /**
     * The static WSDL as an inline key
     */
    private String inLineWSDL;

    /**
     * This keeps track if any of the operation in this service has a out in operation type,
     */

    private boolean hasInOutMEP = false;

    private CGServiceDependencyBean[] serviceDependencies;
    
    public String getInLineWSDL() {
        return inLineWSDL;
    }

    public void setInLineWSDL(String inLineWSDL) {
        this.inLineWSDL = inLineWSDL;
    }

    public boolean isHasInOutMEP() {
        return hasInOutMEP;
    }

    public void setHasInOutMEP(boolean hasInOutMEP) {
        this.hasInOutMEP = hasInOutMEP;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isWsRmEnabled() {
        return isWsRmEnabled;
    }

    public void setWsRmEnabled(boolean wsRmEnabled) {
        isWsRmEnabled = wsRmEnabled;
    }

    public String getRmPolicy() {
        return rmPolicy;
    }

    public void setRmPolicy(String rmPolicy) {
        this.rmPolicy = rmPolicy;
    }

    public boolean isWsSecEnabled() {
        return isWsSecEnabled;
    }

    public void setWsSecEnabled(boolean wsSecEnabled) {
        isWsSecEnabled = wsSecEnabled;
    }

    public String getSecPolicy() {
        return secPolicy;
    }

    public void setSecPolicy(String secPolicy) {
        this.secPolicy = secPolicy;
    }

    public String getWsdlLocation() {
        return wsdlLocation;
    }

    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    public boolean isMTOMEnabled() {
        return isMTOMEnabled;
    }

    public void setMTOMEnabled(boolean MTOMEnabled) {
        isMTOMEnabled = MTOMEnabled;
    }

    public CGServiceDependencyBean[] getServiceDependencies() {
        return serviceDependencies;
    }

    public void setServiceDependencies(CGServiceDependencyBean[] serviceDependencies) {
        this.serviceDependencies = serviceDependencies;
    }
}

