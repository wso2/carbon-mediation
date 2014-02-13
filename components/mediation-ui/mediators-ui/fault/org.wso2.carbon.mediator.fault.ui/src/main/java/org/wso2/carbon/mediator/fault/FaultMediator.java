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
package org.wso2.carbon.mediator.fault;

import org.apache.axiom.soap.SOAP11Constants;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;
import org.apache.axiom.om.*;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.OMElementUtils;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class FaultMediator extends AbstractMediator {

    public static final String WSA_ACTION = "Action";
    /** Make a SOAP 1.1 fault */
    public static final int SOAP11 = 1;
    /** Make a SOAP 1.2 fault */
    public static final int SOAP12 = 2;
    /** Make a POX fault */
    public static final int POX = 3;
    /** Holds the SOAP version to be used to make the fault, if specified */
    private int soapVersion = 1;
    /** Whether to mark the created fault as a response or not */
    private boolean markAsResponse = true;
    /** Whether it is required to serialize the response attribute or not */
    private boolean serializeResponse = false;

    // -- fault elements --
    /** The fault code QName to be used */
    private QName faultCodeValue = null;
    /** An XPath expression that will give the fault code QName at runtime */
    private SynapseXPath faultCodeExpr = null;
    /** The fault reason to be used */
    private String faultReasonValue = null;
    /** An XPath expression that will give the fault reason string at runtime */
    private SynapseXPath faultReasonExpr = null;
    /** The fault node URI to be used */
    private URI faultNode = null;
    /** The fault role URI to be used - if applicable */
    private URI faultRole = null;
    /** The fault detail to be used */
    private String faultDetail = null;
    /** An XPath expression that will give the fault code QName at runtime */
    private SynapseXPath faultDetailExpr = null;
    /** array of fault detail elements */
    private List<OMElement> faultDetailElements = new ArrayList<OMElement>();

    private static final String SOAP11_STRING = "soap11";

    private static final String SOAP12_STRING = "soap12";

    private static final String POX_STRING = "pox";

    public String getTagLocalName() {
        return "makefault";
    }

    public int getSoapVersion() {
        return soapVersion;
    }

    public void setSoapVersion(int soapVersion) {
        this.soapVersion = soapVersion;
    }

    public boolean isMarkAsResponse() {
        return markAsResponse;
    }

    public void setMarkAsResponse(boolean markAsResponse) {
        this.markAsResponse = markAsResponse;
    }

    public boolean isSerializeResponse() {
        return serializeResponse;
    }

    public void setSerializeResponse(boolean serializeResponse) {
        this.serializeResponse = serializeResponse;
    }

    public QName getFaultCodeValue() {
        return faultCodeValue;
    }

    public void setFaultCodeValue(QName faultCodeValue) {

        if (soapVersion == SOAP11) {
            this.faultCodeValue = faultCodeValue;

        } else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                faultCodeValue.getNamespaceURI()) &&
                (SOAP12Constants.FAULT_CODE_DATA_ENCODING_UNKNOWN.equals(
                        faultCodeValue.getLocalPart()) ||
                        SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND.equals(
                                faultCodeValue.getLocalPart()) ||
                        SOAP12Constants.FAULT_CODE_RECEIVER.equals(
                                faultCodeValue.getLocalPart()) ||
                        SOAP12Constants.FAULT_CODE_SENDER.equals(
                                faultCodeValue.getLocalPart()) ||
                        SOAP12Constants.FAULT_CODE_VERSION_MISMATCH.equals(
                                faultCodeValue.getLocalPart())) ) {

            this.faultCodeValue = faultCodeValue;

        } else {
            throw new MediatorException("Invalid Fault code value for a SOAP 1.2 fault : " + faultCodeValue);
        }
    }

    public SynapseXPath getFaultCodeExpr() {
        return faultCodeExpr;
    }

    public void setFaultCodeExpr(SynapseXPath faultCodeExpr) {
        this.faultCodeExpr = faultCodeExpr;
    }

    public String getFaultReasonValue() {
        return faultReasonValue;
    }

    public void setFaultReasonValue(String faultReasonValue) {
        this.faultReasonValue = faultReasonValue;
    }

    public SynapseXPath getFaultReasonExpr() {
        return faultReasonExpr;
    }

    public void setFaultReasonExpr(SynapseXPath faultReasonExpr) {
        this.faultReasonExpr = faultReasonExpr;
    }

    public URI getFaultNode() {
        return faultNode;
    }

    public void setFaultNode(URI faultNode) {
        if (soapVersion == SOAP11) {
            throw new MediatorException("A fault node does not apply to a SOAP 1.1 fault");
        }
        this.faultNode = faultNode;
    }

    public URI getFaultRole() {
        return faultRole;
    }

    public void setFaultRole(URI faultRole) {
        this.faultRole = faultRole;
    }

    public String getFaultDetail() {
        return faultDetail;
    }

    public void setFaultDetail(String faultDetail) {
        this.faultDetail = faultDetail;
    }

    public SynapseXPath getFaultDetailExpr() {
        return faultDetailExpr;
    }

    public void setFaultDetailExpr(SynapseXPath faultDetailExpr) {
        this.faultDetailExpr = faultDetailExpr;
    }

    public List<OMElement> getFaultDetailElements() {
        return faultDetailElements;
    }

    public void addFaultDetailElement(OMElement element) {
        faultDetailElements.add(element);
    }

    public void addAllFaultDetailElements(List<OMElement> list) {
        faultDetailElements.addAll(list);
    }

    public OMElement removeFaultDetailElement(int pos) {
        return faultDetailElements.remove(pos);
    }

    public OMElement serialize(OMElement parent) {
        OMElement fault = fac.createOMElement("makefault", synNS);
        saveTracingState(fault, this);

        if (soapVersion == SOAP11) {
           fault.addAttribute(fac.createOMAttribute(
                "version", nullNS, SOAP11_STRING));
        } else if(soapVersion == org.apache.synapse.mediators.transform.FaultMediator.SOAP12) {
           fault.addAttribute(fac.createOMAttribute(
                "version", nullNS, SOAP12_STRING));
        } else if(soapVersion == org.apache.synapse.mediators.transform.FaultMediator.POX) {
           fault.addAttribute(fac.createOMAttribute(
                "version", nullNS, POX_STRING));
        }

        if (serializeResponse) {
            if (markAsResponse) {
                fault.addAttribute(fac.createOMAttribute("response", nullNS, "true"));
            } else {
                fault.addAttribute(fac.createOMAttribute("response", nullNS, "false"));
            }
        }

        OMElement code = soapVersion != POX ?fac.createOMElement("code", synNS, fault):null;
   
        if (faultCodeValue != null && code != null) {
            OMNamespace ns = code.declareNamespace(faultCodeValue.getNamespaceURI(),
                    faultCodeValue.getPrefix());
            code.addAttribute(fac.createOMAttribute(
                    "value", nullNS, ns.getPrefix() + ":"
                    + faultCodeValue.getLocalPart()));

        } else if (faultCodeExpr != null) {
            SynapseXPathSerializer.serializeXPath(faultCodeExpr, code, "expression");

        } else if (soapVersion != POX) {
            throw new MediatorException("Fault code is required for a fault " +
                    "mediator unless it is a pox fault");
        }

        OMElement reason = fac.createOMElement("reason", synNS, fault);
        if (faultReasonValue != null) {
            reason.addAttribute(fac.createOMAttribute(
                "value", nullNS, faultReasonValue));

        } else if (faultReasonExpr != null) {

            SynapseXPathSerializer.serializeXPath(
                faultReasonExpr, reason, "expression");

        } else if (soapVersion != POX) {
            throw new MediatorException("Fault reason is required for a fault " +
                    "mediator unless it is a pox fault");
        }


        if (faultNode != null && (soapVersion != SOAP11 && soapVersion != POX)) {
            OMElement node = fac.createOMElement("node", synNS, fault);
            node.setText(faultNode.toString());
        }

        if (faultRole != null && soapVersion != POX) {
            OMElement role = fac.createOMElement("role", synNS, fault);
            role.setText(faultRole.toString());
        }

        if (faultDetailExpr != null) {
            OMElement detail = fac.createOMElement("detail", synNS, fault);
            SynapseXPathSerializer.serializeXPath(
                    faultDetailExpr, detail, "expression");
        } else if (faultDetail != null) {
            OMElement detail = fac.createOMElement("detail", synNS, fault);
            detail.setText(faultDetail);
        } else if (faultDetailElements.size() > 0) {
            OMElement detail = fac.createOMElement("detail", synNS, fault);
            for (OMElement e : faultDetailElements) {
                detail.addChild(e);
            }
        }

        if (parent != null) {
            parent.addChild(fault);
        }
        return fault;
    }

    private static final QName ATT_VERSION_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "version");
    private static final QName ATT_RESPONSE_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "response");
    private static final QName CODE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "code");
    private static final QName REASON_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "reason");
    private static final QName NODE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "node");
    private static final QName ROLE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "role");
    private static final QName DETAIL_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "detail");

    public void build(OMElement elem) {
        OMAttribute version = elem.getAttribute(ATT_VERSION_Q);
        if (version != null) {
            if (SOAP11_STRING.equals(version.getAttributeValue())) {
                soapVersion = SOAP11;
            } else if (SOAP12_STRING.equals(version.getAttributeValue())) {
                soapVersion = SOAP12;
            } else if (POX_STRING.equals(version.getAttributeValue())) {
                soapVersion = POX;
            } else {
                throw new MediatorException("Invalid SOAP version");
            }
        }else {
            //although not complete ,we will try to implicitly derive a SOAP version
            //we will check for available namesapaces and derive from that
            extractImplicitSoapVersionFrom(elem);
        }

        OMAttribute response = elem.getAttribute(ATT_RESPONSE_Q);
        if (response != null) {
            if ("true".equals(response.getAttributeValue())) {
                markAsResponse = true;
            } else if ("false".equals(response.getAttributeValue())) {
                markAsResponse = false;
            } else {
                throw new MediatorException("Invalid value '" + response.getAttributeValue()
                        + "' passed as response. Expected 'true' or 'false'");
            }
            serializeResponse = true;
        }

        OMElement code = elem.getFirstChildWithName(CODE_Q);
        if (code != null) {
            OMAttribute value = code.getAttribute(ATT_VALUE);
            OMAttribute expression = code.getAttribute(ATT_EXPRN);

            if (value != null) {
                String strValue = value.getAttributeValue();
                String prefix = null;
                String name = null;
                if (strValue.indexOf(":") != -1) {
                    prefix = strValue.substring(0, strValue.indexOf(":"));
                    name = strValue.substring(strValue.indexOf(":")+1);
                } else {
                    throw new MediatorException("A QName is expected for fault code as prefix:name");
                }
                String namespaceURI = OMElementUtils.getNameSpaceWithPrefix(prefix, code);
                if (namespaceURI == null) {
                    throw new MediatorException("Invalid namespace prefix '" + prefix + "' in code attribute");
                }
                faultCodeValue = new QName(namespaceURI, name, prefix);
            } else if (expression != null) {
                try {
                    faultCodeExpr = SynapseXPathFactory.getSynapseXPath(code, ATT_EXPRN);
                } catch (JaxenException je) {
                    throw new MediatorException("Invalid fault code expression : " + je.getMessage());
                }
            } else {
                throw new MediatorException("A 'value' or 'expression' attribute must specify the fault code");
            }

        } else if (soapVersion != POX) {
            throw new MediatorException("The fault code is a required attribute for the " +
                    "makefault mediator unless it is a pox fault");
        }

        OMElement reason = elem.getFirstChildWithName(REASON_Q);
        if (reason != null) {
            OMAttribute value = reason.getAttribute(ATT_VALUE);
            OMAttribute expression = reason.getAttribute(ATT_EXPRN);

            if (value != null) {
                faultReasonValue = value.getAttributeValue();
            } else if (expression != null) {
                try {
                    faultReasonExpr = SynapseXPathFactory.getSynapseXPath(reason, ATT_EXPRN);
                } catch (JaxenException je) {
                    throw new MediatorException("Invalid fault reason expression : " + je.getMessage());
                }
            } else {
                throw new MediatorException("A 'value' or 'expression' attribute must specify the fault code");
            }

        } else if (soapVersion != POX) {
            throw new MediatorException("The fault reason is a required attribute for the " +
                    "makefault mediator unless it is a pox fault");
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);

        OMElement node = elem.getFirstChildWithName(NODE_Q);
        if (node != null && node.getText() != null) {
            try {
                faultNode = new URI(node.getText());
            } catch (URISyntaxException e) {
                throw new MediatorException("Invalid URI specified for fault node : " + node.getText());
            }
        }

        OMElement role = elem.getFirstChildWithName(ROLE_Q);
        if (role != null && role.getText() != null) {
            try {
                faultRole = new URI(role.getText());
            } catch (URISyntaxException e) {
                throw new MediatorException("Invalid URI specified for fault role : " + role.getText());
            }
        }

        OMElement detail = elem.getFirstChildWithName(DETAIL_Q);
        if (detail != null) {
            OMAttribute detailExpr = detail.getAttribute(ATT_EXPRN);
            if (detailExpr != null && detailExpr.getAttributeValue() != null) {
                try {
                    faultDetailExpr = SynapseXPathFactory.getSynapseXPath(detail, ATT_EXPRN);
                } catch (JaxenException e) {
                    throw new MediatorException("Unable to build the XPath for fault detail " +
                            "from the expression : " + detailExpr.getAttributeValue());
                }
            } else if (detail.getFirstOMChild() != null) {
                OMNode detailNode = detail.getFirstOMChild();
                if (detailNode instanceof OMText) {
                    faultDetail = detail.getText();
                } else if (detailNode instanceof OMElement) {
                    Iterator it = detail.getChildElements();
                    while (it.hasNext()) {
                        faultDetailElements.add((OMElement) it.next()) ;
                    }
                }
            }
        }        
    }

    public void extractImplicitSoapVersionFrom(OMElement elem) {
        boolean searchChildren = true;
        Iterator allNamespaces = elem.getAllDeclaredNamespaces();
        while (allNamespaces.hasNext()) {
            OMNamespace ns = (OMNamespace) allNamespaces.next();
            if (ns.getNamespaceURI().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                searchChildren = false;
                soapVersion = SOAP12;
                break;
            } else if (ns.getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                searchChildren = false;
                soapVersion = SOAP11;
                break;
            }

        }

        if(searchChildren){
            Iterator children = elem.getChildElements();
            while (children.hasNext()) {
                OMElement child = (OMElement) children.next();
                extractImplicitSoapVersionFrom(child);
            }

        }
    }
}
