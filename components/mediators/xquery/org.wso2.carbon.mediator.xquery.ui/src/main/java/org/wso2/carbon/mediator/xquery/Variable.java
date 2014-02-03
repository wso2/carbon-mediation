/**
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 **/
package org.wso2.carbon.mediator.xquery;

import org.apache.synapse.util.xpath.SynapseXPath;

import javax.xml.namespace.QName;

public class Variable {
    public static final int BASE_VARIABLE = 1;
    public static final int CUSTOM_VARIABLE = 2;

    private int variableType = BASE_VARIABLE;

    private QName name;
    private int type;
    protected String value;
    private String regKey;
    private SynapseXPath expression;

    public Variable(QName name) {
        this.name = name;
    }

    public QName getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getRegKey() {
        return regKey;
    }

    public SynapseXPath getExpression() {
        return expression;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setRegKey(String regKey) {
        this.regKey = regKey;
    }

    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }

    public int getVariableType() {
        return variableType;
    }

    public void setVariableType(int variableType) {
        this.variableType = variableType;
    }
}
