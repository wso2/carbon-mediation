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
package org.wso2.carbon.mediator.property;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.JavaUtils;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.xml.*;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;

import javax.xml.namespace.QName;


public class PropertyMediator extends AbstractMediator {
    private String name = null;
    private String value = null;
    private SynapsePath expression = null;

    public static final int ACTION_SET = 0;
    public static final int ACTION_REMOVE = 1;
    /* Defaults to ACTION_SET */
    private int action = ACTION_SET;

    private static final QName ATT_SCOPE = new QName("scope");
    private static final QName ATT_ACTION = new QName("action");
    private static final QName ATT_PATTERN = new QName("pattern");
    private static final QName ATT_GROUP = new QName("group");
    private static final QName ATT_TYPE = new QName("type");

    private String scope = null;

    private String type = null;

    private int group = 0;

    private String pattern = null;

    private OMElement valueElement = null;

    public String getScope() {
        return scope;
    }

    public int getAction() {
        return action;
    }

    public SynapsePath getExpression() {
        return expression;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setExpression(SynapsePath expression) {
        this.expression = expression;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getType() {
        return type;
    }

    public int getGroup() {
        return group;
    }

    public String getPattern() {
        return pattern;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public OMElement getValueElement() {
        return valueElement;
    }

    public void setValueElement(OMElement valueElement) {
        this.valueElement = valueElement;
    }

    public String getTagLocalName() {
        return "property"; 
    }

    public OMElement serialize(OMElement parent) {
        OMElement property = fac.createOMElement("property", synNS);
        saveTracingState(property, this);

        if (name != null) {
            property.addAttribute(fac.createOMAttribute(
                    "name", nullNS, name));
        } else {
            throw new MediatorException("Property name not specified");
        }

        if (action == ACTION_SET) {
            if (value != null) {
                property.addAttribute(fac.createOMAttribute(
                        "value", nullNS, value));
            } else if (expression != null) {
                SynapsePathSerializer.serializePath(expression, property, "expression");
            } else if (valueElement != null) {
                property.addChild(valueElement);
            } else {
                throw new MediatorException("Property value, expression or inline " +
                        "element must be set");
            }

            if (pattern != null) {
                property.addAttribute(fac.createOMAttribute("pattern", nullNS, pattern));
                if (group != 0) {
                    property.addAttribute(fac.createOMAttribute("group", nullNS,
                            Integer.toString(group)));
                }
            }
        } else if (action == ACTION_REMOVE) {
            property.addAttribute(fac.createOMAttribute(
                    "action", nullNS, "remove"));
        }

        if (scope != null) {
            // if we have already built a mediator with scope, scope should be valid, now save it
            property.addAttribute(fac.createOMAttribute("scope", nullNS, scope));
        }

        if (type != null) {
            property.addAttribute(fac.createOMAttribute("type", nullNS, type));
        }

        if (parent != null) {
            parent.addChild(property);
        }
        return property;
    }

    public void build(OMElement elem) {
        OMAttribute nameAttr = elem.getAttribute(ATT_NAME);
        OMAttribute valueAttr = elem.getAttribute(ATT_VALUE);
        OMAttribute expressionAttr = elem.getAttribute(ATT_EXPRN);
        OMAttribute scopeAttr = elem.getAttribute(ATT_SCOPE);
        OMAttribute actionAttr = elem.getAttribute(ATT_ACTION);
        OMAttribute typeAttr = elem.getAttribute(ATT_TYPE);
        OMAttribute patternAttr = elem.getAttribute(ATT_PATTERN);
        OMAttribute groupAttr = elem.getAttribute(ATT_GROUP);

        OMElement valueElement = elem.getFirstElement();

        if (nameAttr == null) {
            String msg = "'name' attribute is required for the configuration of a property mediator";
            throw new MediatorException(msg);
        } else if ((valueAttr == null && valueElement == null && expressionAttr == null) &&
                !(actionAttr != null && "remove".equals(actionAttr.getAttributeValue()))) {
            String msg = "A child element or 'value' attribute or 'expression' attribute is " +
                    "required for a property mediator when action is SET";
            throw new MediatorException(msg);
        }

        this.name = nameAttr.getAttributeValue();

        // The action attribute is optional, if provided and equals to 'remove' the
        // property mediator will act as a property remove mediator
        if (actionAttr != null && "remove".equals(actionAttr.getAttributeValue())) {
            this.action = ACTION_REMOVE;
        }

        if (valueAttr != null) {
            this.value = valueAttr.getAttributeValue();
        } else if (valueElement != null) {
            this.valueElement = valueElement;
        } else if (expressionAttr != null) {
            try {
                this.expression = SynapsePathFactory.getSynapsePath(elem, ATT_EXPRN);
            } catch (JaxenException e) {
                String msg = "Invalid XPath expression for attribute 'expression' : " + expressionAttr.getAttributeValue();
                throw new MediatorException(msg);
            }
        }

        if (scopeAttr != null) {
            String valueStr = scopeAttr.getAttributeValue();
            if (!XMLConfigConstants.SCOPE_AXIS2.equals(valueStr)
                    && !XMLConfigConstants.SCOPE_TRANSPORT.equals(valueStr)
                && !XMLConfigConstants.SCOPE_OPERATION.equals(valueStr)
                && !XMLConfigConstants.SCOPE_DEFAULT.equals(valueStr)
                && !XMLConfigConstants.SCOPE_CLIENT.equals(valueStr)) {
                String msg = "Only '" + XMLConfigConstants.SCOPE_AXIS2
                        + "' or '" + XMLConfigConstants.SCOPE_TRANSPORT
                        + "' or '" + XMLConfigConstants.SCOPE_CLIENT
                        + "' or '" + XMLConfigConstants.SCOPE_OPERATION
                        + "' or '" + XMLConfigConstants.SCOPE_DEFAULT
                        + "' values are allowed for attribute scope for a property mediator"
                        + ", Unsupported scope " + valueStr;
                throw new MediatorException(msg);
            }
            this.scope = valueStr;
        }

        if (typeAttr != null) {
            type = typeAttr.getAttributeValue();            
        }

        if (patternAttr != null) {
            pattern = patternAttr.getAttributeValue();
            if (groupAttr != null) {
                group = Integer.parseInt(groupAttr.getAttributeValue());
            }
        }
        
        if(value != null  &&  type != null){
        	convertValue(value, type); // checking property type validation
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
    }
    
    private Object convertValue(String value, String type) {
        if (type == null) {
            // If no type is set we simply return the string value
            return value;
        }

        try {
            XMLConfigConstants.DATA_TYPES dataType = XMLConfigConstants.DATA_TYPES.valueOf(type);
            switch (dataType) {
                case BOOLEAN    : return JavaUtils.isTrueExplicitly(value);
                case DOUBLE     : return Double.parseDouble(value);
                case FLOAT      : return Float.parseFloat(value);
                case INTEGER    : return Integer.parseInt(value);
                case LONG       : return Long.parseLong(value);
                case OM         : return SynapseConfigUtils.stringToOM(value);
                case SHORT      : return Short.parseShort(value);
                default         : return value;
            }
        } catch (IllegalArgumentException e) {
            String msg = "For value [" +value+ " ] unknown type : [" + type + "] for the property mediator or the " +
                    "property value cannot be converted into the specified type.";
           throw new SynapseException(msg, e);
        }
    }
}
