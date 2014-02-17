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
package org.wso2.carbon.mediator.command;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class CommandMediator extends AbstractMediator {
    protected static final QName ATT_ACTION = new QName("action");
    protected static final QName ATT_CTXNAME = new QName("context-name");

    protected static final String RM_ACTION = "ReadMessage";
    protected static final String UM_ACTION = "UpdateMessage";
    protected static final String RC_ACTION = "ReadContext";
    protected static final String UC_ACTION = "UpdateContext";
    protected static final String RAUM_ACTION = "ReadAndUpdateMessage";
    protected static final String RAUC_ACTION = "ReadAndUpdateContext";

    private String command = null;

    private final Map<String, Object> staticSetterProperties = new HashMap<String, Object>();

    private final Map<String, SynapseXPath> messageSetterProperties = new HashMap<String, SynapseXPath>();

    private final Map<String, String> contextSetterProperties = new HashMap<String, String>();

    private final Map<String, String> contextGetterProperties = new HashMap<String, String>();

    private final Map<String, SynapseXPath> messageGetterProperties = new HashMap<String, SynapseXPath>();

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void addStaticSetterProperty(String name, Object value) {
        this.staticSetterProperties.put(name, value);
    }

    public void addMessageSetterProperty(String name, SynapseXPath xpath) {
        this.messageSetterProperties.put(name, xpath);
    }

    public void addContextSetterProperty(String name, String ctxName) {
        this.contextSetterProperties.put(name, ctxName);
    }

    public void addContextGetterProperty(String name, String value) {
        this.contextGetterProperties.put(name, value);
    }

    public void addMessageGetterProperty(String name, SynapseXPath xpath) {
        this.messageGetterProperties.put(name, xpath);
    }

    public Map<String, Object> getStaticSetterProperties() {
        return this.staticSetterProperties;
    }

    public Map<String, SynapseXPath> getMessageSetterProperties() {
        return this.messageSetterProperties;
    }

    public Map<String, String> getContextSetterProperties() {
        return this.contextSetterProperties;
    }

    public Map<String, String> getContextGetterProperties() {
        return this.contextGetterProperties;
    }

    public Map<String, SynapseXPath> getMessageGetterProperties() {
        return this.messageGetterProperties;
    }

    public OMElement serialize(OMElement parent) {
        OMElement pojoCommand = fac.createOMElement("pojoCommand", synNS);
        saveTracingState(pojoCommand, this);

        if (command != null) {
            pojoCommand.addAttribute(fac.createOMAttribute(
                "name", nullNS, command));
        } else {
            throw new MediatorException("Invalid POJO Command mediator. The command class name is required");
        }

        for (String propName : getStaticSetterProperties().keySet()) {
            Object value = getStaticSetterProperties().get(propName);
            OMElement prop = fac.createOMElement(PROP_Q.getLocalPart(), synNS);
            prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));

            if (value instanceof String) {
                prop.addAttribute(fac.createOMAttribute("value", nullNS, (String) value));
            } else if (value instanceof OMElement) {
                prop.addChild((OMElement) value);
            } else {
                throw new MediatorException("Unable to serialize the command " +
                    "mediator property with the naem " + propName + " : Unknown type");
            }

            if (getContextGetterProperties().containsKey(propName)) {
                prop.addAttribute(fac.createOMAttribute("context-name", nullNS,
                    getContextGetterProperties().get(propName)));
            } else if (getMessageGetterProperties().containsKey(propName)) {
                SynapseXPathSerializer.serializeXPath(
                    getMessageGetterProperties().get(propName), prop, "expression");
            }
            pojoCommand.addChild(prop);
        }

        for (String propName : getMessageSetterProperties().keySet()) {
            OMElement prop = fac.createOMElement(PROP_Q.getLocalPart(), synNS);
            prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));
            SynapseXPathSerializer.serializeXPath(
                getMessageSetterProperties().get(propName), prop, "expression");

            if (getMessageGetterProperties().containsKey(propName)) {
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadAndUpdateMessage"));
            } else if (getContextGetterProperties().containsKey(propName)) {
                prop.addAttribute(fac.createOMAttribute("context-name", nullNS,
                    getContextGetterProperties().get(propName)));
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadMessage"));
            } else {
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadMessage"));
            }
            pojoCommand.addChild(prop);
        }

        for (String propName : getContextSetterProperties().keySet()) {
            OMElement prop = fac.createOMElement(PROP_Q.getLocalPart(), synNS);
            prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));
            prop.addAttribute(fac.createOMAttribute("context-name", nullNS,
                getContextSetterProperties().get(propName)));

            if (getContextGetterProperties().containsKey(propName)) {
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadAndUpdateContext"));
            } else if (getMessageGetterProperties().containsKey(propName)) {
                SynapseXPathSerializer.serializeXPath(
                    getMessageGetterProperties().get(propName), prop, "expression");
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadContext"));
            } else {
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "ReadContext"));
            }
            pojoCommand.addChild(prop);
        }

        for (String propName : getContextGetterProperties().keySet()) {
            if (!isSerialized(propName)) {
                String value = getContextGetterProperties().get(propName);
                OMElement prop = fac.createOMElement(PROP_Q.getLocalPart(), synNS);
                prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));
                prop.addAttribute(fac.createOMAttribute("context-name", nullNS, value));
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "UpdateContext"));
                pojoCommand.addChild(prop);
            }
        }

        for (String propName : getMessageGetterProperties().keySet()) {
            if (!isSerialized(propName)) {
                OMElement prop = fac.createOMElement(PROP_Q.getLocalPart(), synNS);
                prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));
                SynapseXPathSerializer.serializeXPath(
                    getMessageGetterProperties().get(propName), prop, "expression");
                prop.addAttribute(fac.createOMAttribute("action", nullNS, "UpdateMessage"));
                pojoCommand.addChild(prop);
            }
        }

        if (parent != null) {
            parent.addChild(pojoCommand);
        }
        return pojoCommand;
    }

     private boolean isSerialized(String propName) {
        return getContextSetterProperties().containsKey(propName) ||
            getStaticSetterProperties().containsKey(propName) ||
            getMessageSetterProperties().containsKey(propName);
    }

    public void build(OMElement elem) {
        OMAttribute name = elem.getAttribute(ATT_NAME);
        if (name == null) {
            String msg = "The name of the actual POJO command implementation class" +
                    " is a required attribute";
            throw new MediatorException(msg);
        }

        this.command = name.getAttributeValue();

        // setting the properties to the command. these properties will be instantiated
        // at the mediation time
        for (Iterator it = elem.getChildElements(); it.hasNext();) {
            OMElement child = (OMElement) it.next();
            if("property".equals(child.getLocalName())) {

                OMAttribute nameAttr = child.getAttribute(ATT_NAME);
                if (nameAttr != null && nameAttr.getAttributeValue() != null
                    && !"".equals(nameAttr.getAttributeValue())) {

                    handlePropertyAction(nameAttr.getAttributeValue(), child);
                } else {
                    throw new MediatorException("A POJO command mediator " +
                        "property must specify the name attribute");
                }
            }
        }
    }

    private void handlePropertyAction(String name, OMElement propElem) {

        OMAttribute valueAttr   = propElem.getAttribute(ATT_VALUE);
        OMAttribute exprAttr    = propElem.getAttribute(ATT_EXPRN);
        OMAttribute ctxNameAttr = propElem.getAttribute(ATT_CTXNAME);
        OMAttribute actionAttr  = propElem.getAttribute(ATT_ACTION);

        SynapseXPath xpath = null;
        try {
            if (exprAttr != null) {
                xpath = SynapseXPathFactory.getSynapseXPath(propElem, ATT_EXPRN);
            }
        } catch (JaxenException e) {
            throw new MediatorException("Error in building the expression as an SynapseXPath" + e);
        }

        // if there is a value attribute there is no action (action is implied as read value)
        if (valueAttr != null) {
            String value = valueAttr.getAttributeValue();
            // all other three attributes can not co-exists
            if (exprAttr != null && ctxNameAttr != null) {
                throw new MediatorException("Command properties can not contain all three 'value', " +
                    "'expression' and 'context-name' attributes. Only one or " +
                    "combination of two can be there.");
            } else {
                addStaticSetterProperty(name, value);
                if (exprAttr != null) {
                    // action ==> ReadValueAndUpdateMesssage
                    addMessageGetterProperty(name, xpath);
                } else if (ctxNameAttr != null) {
                    // action ==> ReadValueAndUpdateContext
                    addContextGetterProperty(name, ctxNameAttr.getAttributeValue());
                } // else the action ==> ReadValue
            }
        } else if (propElem.getFirstElement() != null) {
            // all other two attributes can not co-exists
            if (exprAttr != null && ctxNameAttr != null) {
                throw new MediatorException("Command properties can not contain all the " +
                    "'expression' and 'context-name' attributes with a child. Only one " +
                    "attribute of those can co-exists with a child");
            } else {
                addStaticSetterProperty(name, propElem.getFirstElement());
                if (exprAttr != null) {
                    // action ==> ReadValueAndUpdateMesssage
                    addMessageGetterProperty(name, xpath);
                } else if (ctxNameAttr != null) {
                    // action ==> ReadValueAndUpdateContext
                    addContextGetterProperty(name, ctxNameAttr.getAttributeValue());
                } // else the action ==> ReadValue
            }
        } else {
            // if both context-name and expression is there
            if (exprAttr != null && ctxNameAttr != null) {
                if (actionAttr != null && actionAttr.getAttributeValue() != null) {
                    String action = actionAttr.getAttributeValue();
                    if (RM_ACTION.equals(action) || UC_ACTION.equals(action)) {
                        // action ==> ReadMessageAndUpdateContext
                        addMessageSetterProperty(name, xpath);
                        addContextGetterProperty(name, ctxNameAttr.getAttributeValue());
                    } else if (RC_ACTION.equals(action) || UM_ACTION.equals(action)) {
                        // action ==> ReadContextAndUpdateMessage
                        addContextSetterProperty(name, ctxNameAttr.getAttributeValue());
                        addMessageGetterProperty(name, xpath);
                    } else {
                        throw new MediatorException("Invalid action for " +
                            "the command property with the name " + name);
                    }
                } else {
                    throw new MediatorException("Action attribute " +
                        "is required for the command property with name " + name);
                }
            } else {
                // only one of expression or context-name is present
                if (actionAttr != null && actionAttr.getAttributeValue() != null) {
                    String action = actionAttr.getAttributeValue();
                    if (exprAttr != null) {
                        if (RM_ACTION.equals(action)) {
                            // action ==> ReadMessage
                            addMessageSetterProperty(name, xpath);
                        } else if (UM_ACTION.equals(action)) {
                            // action ==> UpdateMessage
                            addMessageGetterProperty(name, xpath);
                        } else if (RAUM_ACTION.equals(action)) {
                            // action ==> ReadAndUpdateMessage
                            addMessageSetterProperty(name, xpath);
                            addMessageGetterProperty(name, xpath);
                        } else {
                            throw new MediatorException("Invalid action for " +
                                "the command property with the name " + name);
                        }
                    } else if (ctxNameAttr != null) {
                        String ctxName = ctxNameAttr.getAttributeValue();
                        if (RC_ACTION.equals(action)) {
                            // action ==> ReadContext
                            addContextSetterProperty(name, ctxName);
                        } else if (UC_ACTION.equals(action)) {
                            // action ==> UpdateContext
                            addContextGetterProperty(name, ctxName);
                        } else if (RAUC_ACTION.equals(action)) {
                            // action ==> ReadAndUpdateContext
                            addContextSetterProperty(name, ctxName);
                            addContextGetterProperty(name, ctxName);
                        } else {
                            throw new MediatorException("Invalid action for " +
                                "the command property with the name " + name);
                        }
                    } else {
                        throw new MediatorException("Unrecognized command property with the name " + name);
                    }
                } else {
                    // action ==> ReadAndUpdateMessage/Context
                    if (exprAttr != null) {
                        addMessageSetterProperty(name, xpath);
                        addMessageGetterProperty(name, xpath);
                    } else if (ctxNameAttr != null) {
                        String ctxName = ctxNameAttr.getAttributeValue();
                        addContextSetterProperty(name, ctxName);
                        addContextGetterProperty(name, ctxName);
                    } else {
                        throw new MediatorException("Unrecognized command property with the name " + name);
                    }
                }
            }
        }
    }

    public String getTagLocalName() {
        return "pojoCommand";  
    }
}
