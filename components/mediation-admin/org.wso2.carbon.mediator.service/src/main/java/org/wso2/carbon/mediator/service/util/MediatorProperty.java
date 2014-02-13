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
 **/

package org.wso2.carbon.mediator.service.util;

import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.synapse.MessageContext;

import javax.xml.namespace.QName;

/**
 * This class acts as a helper for creating and serializing properties of the mediators
 */
@SuppressWarnings({"UnusedDeclaration"})
public class MediatorProperty {

    public static final QName PROPERTY_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "property");
    public static final QName ATT_NAME_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "name");
    public static final QName ATT_VALUE_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "value");
    public static final QName ATT_EXPR_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "expression");

    private String name;
    private String value;
    private SynapseXPath expression;
    private SynapsePath pathExpression;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @deprecated
     * @see #getPathExpression()
     */
    public SynapseXPath getExpression() {
        return expression;
    }

    /**
     * @deprecated
     * @see #setPathExpression(SynapsePath)
     * @param expression
     */
    public void setExpression(SynapseXPath expression) {
        this.expression = expression;
    }

    public void setPathExpression(SynapsePath expression) {
        this.pathExpression = expression;
    }

    public SynapsePath getPathExpression() {
        return this.pathExpression;
    }

    public String getEvaluatedExpression(MessageContext synCtx) {
        return expression.stringValueOf(synCtx);
    }
}
