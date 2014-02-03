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

package org.wso2.carbon.mediator.service.ui;

import org.apache.axiom.om.*;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.*;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.util.MediatorProperty;

import javax.xml.namespace.QName;
import java.util.*;


@SuppressWarnings({"UnusedDeclaration"})
public abstract class AbstractMediator implements Mediator {

    private int traceState = SynapseConstants.TRACING_UNSET;
    boolean statEnabled = false;
    private boolean isAuditConfigurable = false;    
    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();
    protected static final OMNamespace synNS = SynapseConstants.SYNAPSE_OMNAMESPACE;
    protected static final OMNamespace nullNS
            = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

    protected static final QName ATT_NAME    = new QName("name");
    protected static final QName ATT_VALUE   = new QName("value");
    protected static final QName ATT_XPATH   = new QName("xpath");
    protected static final QName ATT_REGEX   = new QName("regex");
    protected static final QName ATT_SEQUENCE = new QName("sequence");
    protected static final QName ATT_EXPRN   = new QName("expression");
    protected static final QName ATT_KEY     = new QName("key");
    protected static final QName ATT_SOURCE  = new QName("source");
    protected static final QName ATT_TARGET  = new QName("target");
    protected static final QName ATT_ONERROR = new QName("onError");
    protected static final QName ATT_STATS
        = new QName(XMLConfigConstants.STATISTICS_ATTRIB_NAME);
    protected static final QName PROP_Q
        = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "property");
    protected static final QName FEATURE_Q
        = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "feature");
    protected static final QName TARGET_Q
        = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "target");

    public boolean isAuditConfigurable() {
        return isAuditConfigurable;
    }   

    protected static void saveTracingState(OMElement mediatorOmElement, Mediator mediator) {
        int traceState = mediator.getTraceState();
        String traceValue = null;
        if (traceState == org.apache.synapse.SynapseConstants.TRACING_ON) {
            traceValue = XMLConfigConstants.TRACE_ENABLE;
        } else if (traceState == org.apache.synapse.SynapseConstants.TRACING_OFF) {
            traceValue = XMLConfigConstants.TRACE_DISABLE;
        }
        if (traceValue != null) {
            mediatorOmElement.addAttribute(fac.createOMAttribute(
                XMLConfigConstants.TRACE_ATTRIB_NAME, nullNS, traceValue));
        }

        if (mediator.isAuditConfigurable()) {
            if (mediator.isStatisticsEnable()) {
                mediatorOmElement.addAttribute(fac.createOMAttribute(
                        XMLConfigConstants.STATISTICS_ATTRIB_NAME, nullNS,
                        XMLConfigConstants.STATISTICS_ENABLE));
            }
        }
    }

    protected void serializeMediatorProperties(OMElement parent,
                                               Collection<MediatorProperty> props,
                                               QName childElementName) {
        for (MediatorProperty mp : props) {
            OMElement prop = fac.createOMElement(childElementName, parent);
            if (mp.getName() != null) {
                prop.addAttribute(fac.createOMAttribute("name", nullNS, mp.getName()));
            } else {
                throw new MediatorException("Mediator property name missing");
            }

            if (mp.getValue() != null) {
                prop.addAttribute(fac.createOMAttribute("value", nullNS, mp.getValue()));

            } else if (mp.getExpression() != null) {
                SynapseXPathSerializer.serializeXPath(mp.getExpression(), prop, "expression");
            } else if (mp.getPathExpression() != null) {
                SynapsePathSerializer.serializePath(mp.getPathExpression(), prop, "expression");
            } else {
                throw new MediatorException("Mediator property must have a " +
                        "literal value or be an expression");
            }
        }
    }

    public static List<MediatorProperty> getMediatorProperties(OMElement elem) {

        List<MediatorProperty> propertyList = new ArrayList<MediatorProperty>();

        Iterator itr = elem.getChildrenWithName(MediatorProperty.PROPERTY_Q);

        while (itr.hasNext()) {

            OMElement propEle = (OMElement) itr.next();
            OMAttribute attName  = propEle.getAttribute(MediatorProperty.ATT_NAME_Q);
            OMAttribute attValue = propEle.getAttribute(MediatorProperty.ATT_VALUE_Q);
            OMAttribute attExpr  = propEle.getAttribute(MediatorProperty.ATT_EXPR_Q);

            MediatorProperty prop = new MediatorProperty();

            if (attName == null || attName.getAttributeValue() == null ||
                attName.getAttributeValue().trim().length() == 0) {
                String msg = "Entry name is a required attribute for a Log property";
                throw new MediatorException(msg);
            } else {
                prop.setName(attName.getAttributeValue());
            }

            // if a value is specified, use it, else look for an expression
            if (attValue != null) {

                if (attValue.getAttributeValue() == null ||
                    attValue.getAttributeValue().trim().length() == 0) {

                    String msg = "Entry attribute value (if specified) " +
                        "is required for a Log property";
                    throw new MediatorException(msg);

                } else {
                    prop.setValue(attValue.getAttributeValue());
                }

            } else if (attExpr != null) {

                if (attExpr.getAttributeValue() == null ||
                    attExpr.getAttributeValue().trim().length() == 0) {

                    String msg = "Entry attribute expression (if specified) " +
                        "is required for a mediator property";
                    throw new MediatorException(msg);

                } else {
                    try {
                        if(attExpr.getAttributeValue().startsWith("json-eval(")) {
                            prop.setPathExpression(SynapsePathFactory.getSynapsePath(
                                    propEle, MediatorProperty.ATT_EXPR_Q));
                        } else {
                            prop.setExpression(SynapseXPathFactory.getSynapseXPath(
                                propEle, MediatorProperty.ATT_EXPR_Q));
                        }

                    } catch (JaxenException e) {
                        String msg = "Invalid XPapth expression : " + attExpr.getAttributeValue();
                        throw new MediatorException(msg);
                    }
                }

            } else {
                String msg = "Entry attribute value OR expression must " +
                    "be specified for a mediator property";
                throw new MediatorException(msg);
            }

            propertyList.add(prop);
        }

        return propertyList;
    }

    protected void serializeNamespaces(OMElement elem, AXIOMXPath xpath) {
        for (Object obj : xpath.getNamespaces().keySet()) {
            String prefix = (String) obj;
            String uri = xpath.getNamespaceContext().translateNamespacePrefixToUri(prefix);
            if (!XMLConfigConstants.SYNAPSE_NAMESPACE.equals(uri)) {
                elem.declareNamespace(uri, prefix);
            }
        }
    }

    protected void processAuditStatus(Mediator mediator, OMElement mediatorOmElement) {
        OMAttribute trace = mediatorOmElement.getAttribute(
            new QName(XMLConfigConstants.NULL_NAMESPACE, XMLConfigConstants.TRACE_ATTRIB_NAME));

        if (trace != null) {
            String traceValue = trace.getAttributeValue();
            if (traceValue != null) {
                if (traceValue.equals(XMLConfigConstants.TRACE_ENABLE)) {
                    mediator.setTraceState(org.apache.synapse.SynapseConstants.TRACING_ON);
                } else if (traceValue.equals(XMLConfigConstants.TRACE_DISABLE)) {
                    mediator.setTraceState(org.apache.synapse.SynapseConstants.TRACING_OFF);
                }
            }
        }

        OMAttribute statistics = mediatorOmElement.getAttribute(ATT_STATS);
        if (statistics != null) {
            String statisticsValue = statistics.getAttributeValue();
            if (statisticsValue != null) {
                if (mediator.isAuditConfigurable()) {
                    if (XMLConfigConstants.STATISTICS_ENABLE.equals(statisticsValue)) {
                        mediator.enableStatistics();
                    }
                }
            }
        }
    }

    protected Map<String,String> collectNameValuePairs(OMElement elem, QName childElementName) {
        Map<String,String> result = new LinkedHashMap<String,String>();
        for (Iterator it = elem.getChildrenWithName(childElementName); it.hasNext(); ) {
            OMElement child = (OMElement)it.next();
            OMAttribute attName = child.getAttribute(ATT_NAME);
            OMAttribute attValue = child.getAttribute(ATT_VALUE);
            if (attName != null && attValue != null) {
                String name = attName.getAttributeValue().trim();
                String value = attValue.getAttributeValue().trim();
                if (result.containsKey(name)) {
                    throw new MediatorException("Duplicate " + childElementName.getLocalPart()
                            + " with name " + name);
                } else {
                    result.put(name, value);
                }
            } else {
                throw new MediatorException("Both of the name and value attributes are required for a "
                        + childElementName.getLocalPart());
            }
        }
        return result;
    }

    public int getTraceState() {
        return traceState;
    }

    public void setTraceState(int traceState) {
        this.traceState = traceState;
    }

    public boolean isStatisticsEnable() {
        return statEnabled;
    }

    public void disableStatistics() {
        statEnabled = false;
    }

    public void enableStatistics() {
        statEnabled = true;
    }

    public void setAuditConfigurable(boolean auditConfigurable) {
        isAuditConfigurable = auditConfigurable;
    }
}
