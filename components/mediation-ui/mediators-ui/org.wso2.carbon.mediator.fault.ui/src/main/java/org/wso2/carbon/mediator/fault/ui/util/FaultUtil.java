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
package org.wso2.carbon.mediator.fault.ui.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.wso2.carbon.mediator.fault.FaultMediator;
import org.wso2.carbon.sequences.ui.util.ns.XPathFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

public class FaultUtil {

    public static void populateFautlMediator(HttpServletRequest request, javax.servlet.http.HttpSession session, FaultMediator faultMediator) {

        populateSoapVersion(request, faultMediator);

        if (FaultMediator.SOAP11 == faultMediator.getSoapVersion()) {
            populateFaultCode(request, faultMediator);
            populateFaultString(request, session, faultMediator);
            populateFaultActor(request, faultMediator);
            populateFaultDetail(request, session, faultMediator);
        } else if (FaultMediator.SOAP12 == faultMediator.getSoapVersion()) {
            populateFaultCode(request, faultMediator);
            populateFaultReason(request, session, faultMediator);
            populateRole(request, faultMediator);
            populateNode(request, faultMediator);
            populateFaultDetail(request, session, faultMediator);
        }else if(FaultMediator.POX == faultMediator.getSoapVersion()){
        	populateFaultReason(request, session, faultMediator);
        	populateFaultDetail(request, session, faultMediator);
        }
    }

    private static void populateSoapVersion(HttpServletRequest request, FaultMediator faultMediator) {
        //TODO validation 
        faultMediator.setSoapVersion(Integer.parseInt(request.getParameter("soap_version")));
    }

    private static void populateFaultCode(HttpServletRequest request, FaultMediator faultMediator) {
        String faultCode;
        String namespace;
        String prefix;
        if (faultMediator.getSoapVersion() == FaultMediator.SOAP12) {
            namespace = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            faultCode = request.getParameter("fault_code2");
            prefix = "soap12Env";
        } else {
            namespace = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            faultCode = request.getParameter("fault_code1");
            prefix = "soap11Env";
        }
        if(faultMediator.getSoapVersion() != FaultMediator.POX){
        	faultMediator.setFaultCodeValue(new QName(namespace, faultCode, prefix));
        }
    }

    private static void populateFaultString(HttpServletRequest request, javax.servlet.http.HttpSession session, FaultMediator faultMediator) {
        String faultStringType = request.getParameter("fault_string");

        if ("value".equals(faultStringType)) {
            String faultString = request.getParameter("name_space");
            faultMediator.setFaultReasonValue(faultString);
            faultMediator.setFaultReasonExpr(null);
        } else if ("expression".equals(faultStringType)) {
            XPathFactory xPathFactory = XPathFactory.getInstance();
            faultMediator.setFaultReasonExpr(xPathFactory.createSynapseXPath("name_space", request, session));
            faultMediator.setFaultReasonValue(null);
        }

    }

    private static void populateFaultReason(HttpServletRequest request, HttpSession session, FaultMediator faultMediator) {
        String faultStringType = request.getParameter("fault_string");
        if ("value".equals(faultStringType)) {
            String faultString = request.getParameter("name_space");
            faultMediator.setFaultReasonValue(faultString);
            faultMediator.setFaultReasonExpr(null);
        } else if ("expression".equals(faultStringType)) {
            XPathFactory xPathFactory = XPathFactory.getInstance();
            faultMediator.setFaultReasonExpr(xPathFactory.createSynapseXPath("name_space", request, session));
            faultMediator.setFaultReasonValue(null);
        }
    }

    private static void populateFaultActor(HttpServletRequest request, FaultMediator faultMediator) {
        try {
            if (request.getParameter("fault_actor") != null) {
                URI faultActor = new URI(request.getParameter("fault_actor"));
                faultMediator.setFaultRole(faultActor);
            }
        } catch (URISyntaxException e) {
            //TODO handler this expection
        }
    }

    private static void populateFaultDetail(HttpServletRequest request, HttpSession session, FaultMediator faultMediator) {
        String faultDetailType = request.getParameter("fault_detail");
        if ("value".equals(faultDetailType)) {
            String faultDetail = request.getParameter("detail");
            faultDetail = faultDetail.trim();
            faultMediator.setFaultDetail(null);
            faultMediator.getFaultDetailElements().clear();
            faultMediator.setFaultDetailExpr(null);
            if (!faultDetail.equals("")) {
                faultDetail = "<detail>" + faultDetail.trim() + "</detail>";
                try {
                    // first try to create an OMElement
                    OMElement element = AXIOMUtil.stringToOM(faultDetail);
                    if (element.getChildElements().hasNext()){
                        Iterator it = element.getChildElements();
                        while (it.hasNext()) {
                            faultMediator.addFaultDetailElement((OMElement) it.next());
                        }
                    } else if (element.getText() != null) {
                        faultMediator.setFaultDetail(element.getText());
                    }
                } catch (Exception e) {
                    /* if failed set it as an string */
                    faultMediator.setFaultDetail(faultDetail);
                }
            } else if ("expression".equals(faultDetailType)) {
                XPathFactory xPathFactory = XPathFactory.getInstance();
                faultMediator.setFaultDetailExpr(xPathFactory.createSynapseXPath("detail", request, session));
                faultMediator.setFaultDetail(null);
            }
        }
    }

    private static void populateRole(HttpServletRequest request, FaultMediator faultMediator) {
        try {
            if (request.getParameter("fault_actor") != null) {
                URI faultActor = new URI(request.getParameter("fault_actor"));
                faultMediator.setFaultRole(faultActor);
            }
        } catch (URISyntaxException e) {
            //TODO handler this expection
        }
    }

    private static void populateNode(HttpServletRequest request, FaultMediator faultMediator) {
        try {
            if (request.getParameter("node") != null) {
                URI faultActor = new URI(request.getParameter("node"));
                faultMediator.setFaultNode(faultActor);
            }
        } catch (URISyntaxException e) {
            //TODO handler this expection
        }

    }

    public static String repalceDoubleQuotation(String in) {
        return in.replaceAll("\"", "\'");
    }

}
