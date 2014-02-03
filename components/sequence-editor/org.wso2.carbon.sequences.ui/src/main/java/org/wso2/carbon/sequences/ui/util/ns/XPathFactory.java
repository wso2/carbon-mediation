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
 */

package org.wso2.carbon.sequences.ui.util.ns;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.sequences.ui.util.SequenceEditorHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;

/**
 *
 */
@SuppressWarnings({"UnusedDeclaration"})
public class XPathFactory {

    private static final Log log = LogFactory.getLog(XPathFactory.class);

    private final static XPathFactory ourInstance = new XPathFactory();

    public static XPathFactory getInstance() {
        return ourInstance;
    }

    private XPathFactory() {
    }

    public AXIOMXPath createAXIOMXPath(String id, String source, HttpSession httpSession) {
        try {
            if (!assertIDNotEmpty(id) || !assertSourceNotEmpty(source)) {
                return null;
            }
            AXIOMXPath xPath = new AXIOMXPath(source.trim());
            addNameSpaces(xPath, id, httpSession);
            return xPath;
        } catch (JaxenException e) {
            String msg = "Error creating a XPath from text : " + source;
            throw new RuntimeException(msg, e);
        }
    }

    public SynapseXPath createSynapseXPath(String id, String source, HttpSession httpSession) {
        try {
            if (!assertIDNotEmpty(id) || !assertSourceNotEmpty(source)) {
                return null;
            }
            SynapseXPath xPath = new SynapseXPath(source.trim());
            addNameSpaces(xPath, id, httpSession);
            return xPath;
        } catch (JaxenException e) {
            String msg = "Error creating a XPath from text : " + source;
            throw new RuntimeException(msg, e);
        }
    }

    public AXIOMXPath createAXIOMXPath(String id, HttpServletRequest request, HttpSession httpSession) {
        return createAXIOMXPath(id, request.getParameter(id), httpSession);
    }

    public SynapseXPath createSynapseXPath(String id, HttpServletRequest request, HttpSession httpSession) {
        return createSynapseXPath(id, request.getParameter(id), httpSession);
    }

    private AXIOMXPath addNameSpaces(AXIOMXPath xPath, String id, HttpSession httpSession) {

        NameSpacesInformationRepository repository = (NameSpacesInformationRepository) httpSession.getAttribute(
                NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
        if (repository == null) {
            return xPath;
        }

        NameSpacesInformation information = repository.getNameSpacesInformation(
                SequenceEditorHelper.getEditingMediatorPosition(httpSession), id);
        if (information == null) {
            return xPath;
        }

        if (log.isDebugEnabled()) {
            log.debug("Getting NameSpaces :" + information + " for id :" + id);
        }

        Iterator<String> iterator = information.getPrefixes();
        while (iterator.hasNext()) {
            String prefix = iterator.next();
            String nsURI = information.getNameSpaceURI(prefix);
            try {
                xPath.addNamespace(prefix, nsURI);
            } catch (JaxenException je) {
                String msg = "Error adding declared name space with prefix : "
                        + prefix + "and uri : " + nsURI
                        + " to the xPath : " + xPath;
                throw new RuntimeException(msg, je);
            }
        }
        information.removeAllNameSpaces();
        return xPath;
    }

    private static boolean assertIDNotEmpty(String id) {
        if (id == null || "".equals(id)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided id is empty or null ,returning a null as XPath");
            }
            return false;
        }
        return true;
    }

    private static boolean assertSourceNotEmpty(String source) {
        if (source == null || "".equals(source)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided source is empty or null ,returning a null as XPath");
            }
            return false;
        }
        return true;
    }
}
