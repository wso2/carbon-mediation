/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.proxyadmin;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

public class ProxyServicePolicyInfo {

    private String key;
    private String type;
    private String operationName;
    private String operationNS;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationNS() {
        return operationNS;
    }

    public void setOperationNS(String operationNS) {
        this.operationNS = operationNS;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OMElement toOM(OMFactory fac, OMNamespace ns,
                          OMNamespace nullNS) throws ProxyAdminException {
        if (key == null) {
            throw new ProxyAdminException("A policy without a key was encountered");
        }

        OMElement policyElt = fac.createOMElement("policy", ns);
        policyElt.addAttribute("key", key, nullNS);
        if (type != null) {
            policyElt.addAttribute("type", type, nullNS);
        }

        if (operationName != null) {
            policyElt.addAttribute("operationName", operationName, nullNS);
            if (operationNS != null) {
                policyElt.addAttribute("operationNamespace", operationNS, nullNS);
            }
        }

        return policyElt;
    }
}
