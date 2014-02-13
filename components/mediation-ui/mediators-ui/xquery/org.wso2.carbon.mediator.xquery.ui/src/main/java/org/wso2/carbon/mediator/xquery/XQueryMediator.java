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

import net.sf.saxon.javax.xml.xquery.XQItemType;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.*;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.util.MediatorProperty;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.config.xml.ValueFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XQueryMediator extends AbstractListMediator {
    public static final QName ATT_NAME_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "name");
    public static final QName ATT_VALUE_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "value");
    public static final QName ATT_EXPR_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "expression");
    public static final QName ATT_KEY_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "key");
    public static final QName ATT_TYPE_Q = new QName(XMLConfigConstants.NULL_NAMESPACE, "type");

    private Value queryKey = null;
    private String querySource = null;
    private SynapseXPath target = null;
    private final List<MediatorProperty> dataSourceProperties = new ArrayList<MediatorProperty>();
    private final List<Variable> variables = new ArrayList<Variable>();

    public String getTagLocalName() {
        return "xquery";
    }

    public OMElement serialize(OMElement parent) {
        OMElement xquery = fac.createOMElement("xquery", synNS);

        if (queryKey != null) {
            // Use KeySerializer to serialize Key
            ValueSerializer keySerializer =  new ValueSerializer();
            keySerializer.serializeValue(queryKey, XMLConfigConstants.KEY, xquery);
        }

        saveTracingState(xquery, this);

        if (target != null) {
            SynapseXPathSerializer.serializeXPath(target, xquery, "target");
        }
        
        if (dataSourceProperties != null && !dataSourceProperties.isEmpty()) {
            OMElement dataSource = fac.createOMElement("dataSource", synNS);
            serializeMediatorProperties(dataSource, dataSourceProperties, PROP_Q);
            xquery.addChild(dataSource);
        }

        if (variables != null && !variables.isEmpty()) {
            for (Variable variable : variables) {
                if (variable.getVariableType() == Variable.BASE_VARIABLE) {
                    QName name = variable.getName();
                    Object value = variable.getValue();
                    if (name != null && value != null) {
                        OMElement baseElement = fac.createOMElement("variable", synNS);
                        baseElement.addAttribute(fac.createOMAttribute(
                                "name", nullNS, name.getLocalPart()));
                        baseElement.addAttribute(fac.createOMAttribute(
                                "value", nullNS, (String) value));
                        String type = null;
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
                        } else {
                            throw new MediatorException("Unknown Type " + varibelType);
                        }
                        if (type != null) {
                            baseElement.addAttribute(fac.createOMAttribute(
                                    "type", nullNS, type));

                        }
                        xquery.addChild(baseElement);
                    }
                } else if (variable.getVariableType() == Variable.CUSTOM_VARIABLE) {
                    QName name = variable.getName();
                    if (name != null) {
                        OMElement customElement = fac.createOMElement("variable", synNS);
                        customElement.addAttribute(fac.createOMAttribute(
                                "name", nullNS, name.getLocalPart()));
                        String regkey = variable.getRegKey();
                        if (regkey != null) {
                            customElement.addAttribute(fac.createOMAttribute(
                                    "key", nullNS, regkey));
                        }
                        SynapseXPath expression = variable.getExpression();
                        if (expression != null) {
                            SynapseXPathSerializer.serializeXPath(expression,
                                    customElement, "expression");
                        }
                        String type = null;
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
                        } else {
                            throw new MediatorException("Unknown Type " + varibelType);
                        }
                        if (type != null) {
                            customElement.addAttribute(fac.createOMAttribute(
                                    "type", nullNS, type));

                        }
                        xquery.addChild(customElement);
                    }
                }
            }
        }
        if (parent != null) {
            parent.addChild(xquery);
        }
        return xquery;
    }

    public void build(OMElement elem) {
        OMAttribute xqueryKey = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                "key"));
        OMAttribute attrTarget = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                "target"));
        if (xqueryKey != null) {
            //Use KeyFactory to create Key
            ValueFactory keyFactory = new ValueFactory();
            queryKey = keyFactory.createValue(XMLConfigConstants.KEY, elem);
        } else {
            throw new MediatorException("The 'key' attribute is required for the XQuery mediator");
        }
        if (attrTarget != null) {
            String targetValue = attrTarget.getAttributeValue();
            if (targetValue != null && !"".equals(targetValue)) {
                try {
                    this.querySource = targetValue;
                    this.target = SynapseXPathFactory.getSynapseXPath(elem, ATT_TARGET);
                } catch (JaxenException e) {
                    throw new MediatorException("Invalid XPath specified for the target attribute : " +
                            targetValue);
                }
            }
        }
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
        OMElement dataSource = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "dataSource"));
        if (dataSource != null) {
            dataSourceProperties.addAll(getMediatorProperties(dataSource));
        }

        Iterator it = elem.getChildrenWithName(new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
                "variable"));
        while (it.hasNext()) {
            OMElement variableOM = (OMElement) it.next();
            String name = variableOM.getAttributeValue(ATT_NAME_Q);
            if (name != null && !"".equals(name)) {
                String type = variableOM.getAttributeValue(ATT_TYPE_Q);
                if (type != null && !"".equals(type)) {
                    String value = variableOM.getAttributeValue(ATT_VALUE_Q);
                    Variable variable;
                    if (value != null && !"".equals(value)) {
                        variable = new Variable(new QName(name.trim()));
                        variable.setValue(value.trim());
                        variable.setType(Variable.BASE_VARIABLE);
                    } else {
                        String key = variableOM.getAttributeValue(ATT_KEY_Q);
                        String expr = variableOM.getAttributeValue(ATT_EXPR_Q);
                        variable = new Variable(new QName(name.trim()));
                        variable.setVariableType(Variable.CUSTOM_VARIABLE);
                        if (key != null) {
                            variable.setRegKey(key.trim());
                        }
                        if (expr != null && !"".equals(expr)) {
                            try {
                                variable.setExpression(SynapseXPathFactory.getSynapseXPath(
                                        variableOM, MediatorProperty.ATT_EXPR_Q));
                            } catch (JaxenException e) {
                                throw new MediatorException("Invalid XPath specified for" +
                                        " the expression attribute : " + expr);
                            }
                        }
                    }
                    if ("INT".equals(type.trim())) {
                        variable.setType(XQItemType.XQBASETYPE_INT);
                    } else if ("INTEGER".equals(type.trim())) {
                        variable.setType(XQItemType.XQBASETYPE_INTEGER);
                    } else if ("BOOLEAN".equals(type.trim())) {
                        variable.setType(XQItemType.XQBASETYPE_BOOLEAN);
                    } else if ("BYTE".equals(type.trim())) {
                        variable.setType(XQItemType.XQBASETYPE_BYTE);
                    } else if ("DOUBLE".equals(type.trim())) {
                        variable.setType(XQItemType.XQBASETYPE_DOUBLE);
                    } else if ("SHORT".equals(type.trim())) {
                        variable.setType(XQItemType.XQBASETYPE_SHORT);
                    } else if ("LONG".equals(type.trim())) {
                        variable.setType(XQItemType.XQBASETYPE_LONG);
                    } else if ("FLOAT".equals(type.trim())) {
                        variable.setType(XQItemType.XQBASETYPE_FLOAT);
                    } else if ("STRING".equals(type.trim())) {
                        variable.setType(XQItemType.XQBASETYPE_STRING);
                    } else if ("DOCUMENT".equals(type.trim())) {
                        variable.setType(XQItemType.XQITEMKIND_DOCUMENT);
                    } else if ("DOCUMENT_ELEMENT".equals(type.trim())) {
                        variable.setType(XQItemType.XQITEMKIND_DOCUMENT_ELEMENT);
                    } else if ("ELEMENT".equals(type.trim())) {
                        variable.setType(XQItemType.XQITEMKIND_ELEMENT);
                    } else {
                        throw new MediatorException("Unsupported Type");
                    }
                    variables.add(variable);
                }
            }
        }
    }

    public Value getQueryKey() {
        return queryKey;
    }

    public void setQueryKey(Value queryKey) {
        this.queryKey = queryKey;
    }

    public String getQuerySource() {
        return querySource;
    }

    public void setQuerySource(String querySource) {
        this.querySource = querySource;
    }

    public void addAllVariables(List<Variable> list) {
        this.variables.addAll(list);
    }

    public void addVariable(Variable variable) {
        this.variables.add(variable);
    }

    public List<MediatorProperty> getDataSourceProperties() {
        return dataSourceProperties;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public SynapseXPath getTarget() {
        return target;
    }

    public void setTarget(SynapseXPath source) {
        this.target = source;
    }

    public void addAllDataSourceProperties(List<MediatorProperty> list) {
        this.dataSourceProperties.addAll(list);
    }
}
