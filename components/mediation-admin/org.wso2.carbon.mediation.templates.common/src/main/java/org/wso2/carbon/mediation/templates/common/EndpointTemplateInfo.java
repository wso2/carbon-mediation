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
package org.wso2.carbon.mediation.templates.common;

import org.apache.axiom.om.OMElement;

import java.util.ArrayList;

public class EndpointTemplateInfo {

    private String templateName;

    private String description;

    private String endpointType;

    private ArrayList<String> params = new ArrayList<String>();

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public void addParam(String paramName) {
        if (paramName != null && !"".equals(paramName.trim())) {
            params.add(paramName);
        }
    }

    public String getParamColelctionString() {
        String collectionStr = "";
        int i = 0;
        for (String param : params) {
            if (param != null && !"".equals(param.trim())) {
                collectionStr = collectionStr + param + ":";
            }
            i++;
        }
        return collectionStr;
    }

}
