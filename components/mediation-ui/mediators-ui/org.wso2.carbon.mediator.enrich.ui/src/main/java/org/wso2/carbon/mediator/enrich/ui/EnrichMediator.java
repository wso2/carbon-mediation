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
package org.wso2.carbon.mediator.enrich.ui;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.SynapsePathFactory;
import org.apache.synapse.config.xml.SynapsePathSerializer;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Iterator;


public class EnrichMediator extends AbstractMediator {

    private static Log log = LogFactory.getLog(EnrichMediator.class);

    private final String SOURCE = "source";
    private final String TARGET = "target";
    private final String CLONE = "clone";
    private final String ACTION = "action";
    private final String TYPE = "type";
    private final String XPATH = "xpath";
    private final String PROPERTY = "property";
    private final String DEFAULT_SOURCE_TYPE = "custom";
    private final String DEFAULT_TARGET_TYPE = "custom";
    private final String DEFAULT_TARGET_ACTION_TYPE = "replace";
    private String sourceClone = "true";
    private String sourceType = DEFAULT_SOURCE_TYPE;
    private SynapsePath sourceExpression = null;
    private String sourceProperty = "";
    private final String INLINE = "inline";
    private final String INLINE_REG_KEY = "key";
    private String targetAction = "replace";
    private String targetType = DEFAULT_TARGET_TYPE;
    private String sourceInlineElement = "";
    private String inlineSourceRegKey = "";
    private SynapsePath targetExpression = null;
    private String targetProperty = "";


    public String getSourceType() {
        return sourceType;
    }


    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }


    public String getSourceClone() {
        return sourceClone;
    }


    public void setSourceClone(String sourceClone) {
        this.sourceClone = sourceClone;
    }

    public SynapsePath getSourceExpression() {
        return sourceExpression;
    }

    public void setSourceExpression(SynapsePath sourceExpression) {
        this.sourceExpression = sourceExpression;
    }

    public String getSourceProperty() {
        return sourceProperty;
    }

    public void setSourceProperty(String sourceProperty) {
        this.sourceProperty = sourceProperty;
    }

    public String getSourceInlineElement() {
        return sourceInlineElement;
    }

    public void setSourceInlineElement(String sourceInlineElement) {
        this.sourceInlineElement = sourceInlineElement;
    }

    public String getTargetAction() {
        return targetAction;
    }

    public void setTargetAction(String targetAction) {
        this.targetAction = targetAction;
    }

    public String getTargetType() {
        return targetType;
    }


    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }


    public SynapsePath getTargetExpression() {
        return targetExpression;
    }

    public void setTargetExpression(SynapsePath targetExpression) {
        this.targetExpression = targetExpression;
    }

    public String getTargetProperty() {
        return targetProperty;
    }

    public void setTargetProperty(String targetProperty) {
        this.targetProperty = targetProperty;
    }


    public String getTagLocalName() {
        return "enrich";
    }

    public String getInlineSourceRegKey() {
        return inlineSourceRegKey;
    }

    public void setInlineSourceRegKey(String inlineSourceRegKey) {
        this.inlineSourceRegKey = inlineSourceRegKey;
    }

    public OMElement serialize(OMElement parent) {
        OMElement enrichElem = fac.createOMElement("enrich", synNS);
        OMElement sourceElem = fac.createOMElement(SOURCE, synNS);

        saveTracingState(enrichElem, this);

        sourceElem.addAttribute(CLONE, sourceClone, nullNS);
        sourceElem.addAttribute(TYPE, sourceType, nullNS);

        if (null != sourceExpression) {
            SynapsePathSerializer.serializePath(sourceExpression, sourceElem, XPATH);
        }
        if (sourceProperty != null && !sourceProperty.equals("")) {
            sourceElem.addAttribute(PROPERTY, sourceProperty, nullNS);
        }

        if (sourceType.equals(INLINE)) {
            /*XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader("sfsdf");
            //create the builder
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            //get the root element of the XML
            OMElement documentElement = builder.getDocumentElement();*/

            if (sourceInlineElement != null && !sourceInlineElement.equals("")) {
                OMElement inlineXMLElement;
                JsonParser parser = new JsonParser();
                // check for inline JSON values
                try {
                    parser.parse(sourceInlineElement);
                    String formattedString = "<source xmlns=\"" + SynapseConstants.SYNAPSE_NAMESPACE
                            + "\" type=\"inline\">" + sourceInlineElement + "</source>";
                    sourceElem = AXIOMUtil.stringToOM(formattedString);
                } catch (JsonSyntaxException ex) {
                    // not a JSON
                    try {
                        inlineXMLElement = AXIOMUtil.stringToOM(sourceInlineElement);
                        sourceElem.addChild(inlineXMLElement);
                    } catch (XMLStreamException e) {
                        log.error("Inline source : " + sourceInlineElement + " is not a valid XML or JSON", e);
                    }
                } catch (XMLStreamException e) {
                    throw new AssertionError("Cannot reach the catch block since we are wrapping the JSON inside " +
                            "valid XML tags",e);
                }
            } else if (inlineSourceRegKey != null && !inlineSourceRegKey.equals("")) {
                sourceElem.addAttribute(INLINE_REG_KEY, inlineSourceRegKey, null);
            }
        }

        OMElement targetElem = fac.createOMElement(TARGET, synNS);

        targetElem.addAttribute(ACTION, targetAction, nullNS);
        targetElem.addAttribute(TYPE, targetType, nullNS);

        if (null != targetExpression) {
            SynapsePathSerializer.serializePath(targetExpression, targetElem, XPATH);
        }
        if (targetProperty != null && !targetProperty.equals("")) {
            targetElem.addAttribute(PROPERTY, targetProperty, nullNS);
        }

        enrichElem.addChild(sourceElem);
        enrichElem.addChild(targetElem);

        if (null != parent) {
            parent.addChild(enrichElem);
        }

        return enrichElem;
    }


    public void build(OMElement elem) {

        for (Iterator it = elem.getChildElements(); it.hasNext();) {
            OMElement childElem = (OMElement) it.next();
            if (childElem.getLocalName().equals(SOURCE)) {

                OMAttribute cloneAttr = childElem.getAttribute(new QName(CLONE));
                if (cloneAttr != null && cloneAttr.getAttributeValue() != null) {
                    sourceClone = cloneAttr.getAttributeValue();
                }

                OMAttribute sourceTypeAttr = childElem.getAttribute(new QName(TYPE));
                if (sourceTypeAttr != null && sourceTypeAttr.getAttributeValue() != null) {
                    sourceType = sourceTypeAttr.getAttributeValue();
                } else {
                    sourceType = DEFAULT_SOURCE_TYPE;
                }

                OMAttribute sourceXpathAttr = childElem.getAttribute(new QName(XPATH));
                if (sourceXpathAttr != null
                        && sourceXpathAttr.getAttributeValue() != null
                        && !sourceXpathAttr.getAttributeValue().equals("")) {
                    try {
                        sourceExpression = SynapsePathFactory.getSynapsePath(childElem, new QName(XPATH));
                    } catch (JaxenException e) {
                        String msg = "Invalid XPath expression for 'source' attribute 'xpath' : " + sourceXpathAttr.getAttributeValue();
                        throw new MediatorException(msg);
                    }
                }

                OMAttribute sourcePropertyAttr = childElem.getAttribute(new QName(PROPERTY));
                if (sourcePropertyAttr != null && sourcePropertyAttr.getAttributeValue() != null) {
                    sourceProperty = sourcePropertyAttr.getAttributeValue();
                }

                OMElement inlineXMLElement = childElem.getFirstElement();
                if (inlineXMLElement != null) {
                    sourceInlineElement = inlineXMLElement.toString();
                } else if (childElem.getAttributeValue(new QName(null, INLINE_REG_KEY)) != null) {
                    inlineSourceRegKey = childElem.getAttributeValue(new QName(null, INLINE_REG_KEY));
                } else {
                    // handle JSON input scenario
                    sourceInlineElement = childElem.getText();
                }
            }

            if (childElem.getLocalName().equals(TARGET)) {
                OMAttribute actionAttr = childElem.getAttribute(new QName(ACTION));
                if (actionAttr != null && actionAttr.getAttributeValue() != null) {
                    targetAction = actionAttr.getAttributeValue();
                } else {
                    targetAction = DEFAULT_TARGET_ACTION_TYPE;
                }

                OMAttribute targetTypeAttr = childElem.getAttribute(new QName(TYPE));
                if (targetTypeAttr != null && targetTypeAttr.getAttributeValue() != null) {
                    targetType = targetTypeAttr.getAttributeValue();
                } else {
                    targetType = DEFAULT_TARGET_TYPE;
                }

                OMAttribute targetXpathAttr = childElem.getAttribute(new QName(XPATH));
                if (targetXpathAttr != null
                        && targetXpathAttr.getAttributeValue() != null
                        && !targetXpathAttr.getAttributeValue().equals("")) {
                    try {
                        targetExpression = SynapsePathFactory.getSynapsePath(childElem, new QName(XPATH));
                    } catch (JaxenException e) {
                        String msg = "Invalid XPath expression for 'target' attribute 'xpath' : " + targetXpathAttr.getAttributeValue();
                        throw new MediatorException(msg);
                    }

                }

                OMAttribute targetPropertyAttr = childElem.getAttribute(new QName(PROPERTY));
                if (targetPropertyAttr != null && targetPropertyAttr.getAttributeValue() != null) {
                    targetProperty = targetPropertyAttr.getAttributeValue();
                }

            }
        }

    }
}
