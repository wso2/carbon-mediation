/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.mediator.xquery.internal;

import net.sf.saxon.javax.xml.xquery.XQItemType;
import org.wso2.carbon.mediator.xquery.Variable;

import javax.xml.namespace.QName;


public class XQueryMediatorClientHelper {

    public static String getType(Variable variable) {
        String type = null;
        if (variable.getVariableType() == Variable.BASE_VARIABLE) {
            QName name = variable.getName();
            Object value = variable.getValue();
            if (name != null && value != null) {

                int varibelType = variable.getType();
                if (XQItemType.XQBASETYPE_INT == varibelType) {
                    type = "INT";
                } else if (XQItemType.XQBASETYPE_INTEGER == varibelType) {
                    type = "INTEGER";
                } else if (XQItemType.XQBASETYPE_BOOLEAN == varibelType) {
                    type = "BOOLEAN";
                } else if (XQItemType.XQBASETYPE_BYTE == varibelType) {
                    type = "BYTE";
                } else if (XQItemType.XQBASETYPE_DOUBLE == varibelType) {
                    type = "DOUBLE";
                } else if (XQItemType.XQBASETYPE_SHORT == varibelType) {
                    type = "SHORT";
                } else if (XQItemType.XQBASETYPE_LONG == varibelType) {
                    type = "LONG";
                } else if (XQItemType.XQBASETYPE_FLOAT == varibelType) {
                    type = "FLOAT";
                } else if (XQItemType.XQBASETYPE_STRING == varibelType) {
                    type = "STRING";
                } else if (XQItemType.XQITEMKIND_DOCUMENT == varibelType) {
                    type = "DOCUMENT";
                } else if (XQItemType.XQITEMKIND_DOCUMENT_ELEMENT == varibelType) {
                    type = "DOCUMENT_ELEMENT";
                } else if (XQItemType.XQITEMKIND_ELEMENT == varibelType) {
                    type = "ELEMENT";
                }
            }
        } else if (variable.getVariableType() == Variable.CUSTOM_VARIABLE) {            
            QName name = variable.getName();
            if (name != null) {

                int varibelType = variable.getType();
                if (XQItemType.XQITEMKIND_DOCUMENT == varibelType) {
                    type = "DOCUMENT";
                } else if (XQItemType.XQITEMKIND_DOCUMENT_ELEMENT == varibelType) {
                    type = "DOCUMENT_ELEMENT";
                } else if (XQItemType.XQITEMKIND_ELEMENT == varibelType) {
                    type = "ELEMENT";
                } else if (XQItemType.XQBASETYPE_INT == varibelType) {
                    type = "INT";
                } else if (XQItemType.XQBASETYPE_INTEGER == varibelType) {
                    type = "INTEGER";
                } else if (XQItemType.XQBASETYPE_BOOLEAN == varibelType) {
                    type = "BOOLEAN";
                } else if (XQItemType.XQBASETYPE_BYTE == varibelType) {
                    type = "BYTE";
                } else if (XQItemType.XQBASETYPE_DOUBLE == varibelType) {
                    type = "DOUBLE";
                } else if (XQItemType.XQBASETYPE_SHORT == varibelType) {
                    type = "SHORT";
                } else if (XQItemType.XQBASETYPE_LONG == varibelType) {
                    type = "LONG";
                } else if (XQItemType.XQBASETYPE_FLOAT == varibelType) {
                    type = "FLOAT";
                } else if (XQItemType.XQBASETYPE_STRING == varibelType) {
                    type = "STRING";

                }
            }
        }
        return type;
    }

    public static int getType(String type) {

        int typeValue = -1;
        if ("INT".equals(type.trim())) {
            typeValue = XQItemType.XQBASETYPE_INT;
        } else if ("INTEGER".equals(type.trim())) {
            typeValue = XQItemType.XQBASETYPE_INTEGER;
        } else if ("BOOLEAN".equals(type.trim())) {
            typeValue = XQItemType.XQBASETYPE_BOOLEAN;
        } else if ("BYTE".equals(type.trim())) {
            typeValue = XQItemType.XQBASETYPE_BYTE;
        } else if ("DOUBLE".equals(type.trim())) {
            typeValue = XQItemType.XQBASETYPE_DOUBLE;
        } else if ("SHORT".equals(type.trim())) {
            typeValue = XQItemType.XQBASETYPE_SHORT;
        } else if ("LONG".equals(type.trim())) {
            typeValue = XQItemType.XQBASETYPE_LONG;
        } else if ("FLOAT".equals(type.trim())) {
            typeValue = XQItemType.XQBASETYPE_FLOAT;
        } else if ("STRING".equals(type.trim())) {
            typeValue = XQItemType.XQBASETYPE_STRING;
        } else if ("DOCUMENT".equals(type.trim())) {
            typeValue = XQItemType.XQITEMKIND_DOCUMENT;
        } else if ("DOCUMENT_ELEMENT".equals(type.trim())) {
            typeValue = XQItemType.XQITEMKIND_DOCUMENT_ELEMENT;
        } else if ("ELEMENT".equals(type.trim())) {
            typeValue = XQItemType.XQITEMKIND_ELEMENT;
        } else if ("Select-A-Value".equals(type.trim())) {
            typeValue = -1;
        } else {
            throw new RuntimeException("Unknow type : " + type);
        }
        return typeValue;
    }
}
