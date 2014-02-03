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
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.service.util.MediatorProperty;
import org.wso2.carbon.sequences.ui.util.SequenceEditorHelper;

import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
@SuppressWarnings({"UnusedDeclaration"})
public class NameSpacesRegistrar {

    private static final Log log = LogFactory.getLog(NameSpacesRegistrar.class);

    private final static NameSpacesRegistrar ourInstance = new NameSpacesRegistrar();

    public static NameSpacesRegistrar getInstance() {
        return ourInstance;
    }

    private NameSpacesRegistrar() {
    }

    @Deprecated
    public void addNameSpace(String prefix, String uri, String id, HttpSession httpSession) {

        if (!assertIDNotEmpty(id)) {
            return;
        }

        NameSpacesInformationRepository repository
                = getNameSpacesInformationRepository(httpSession);
        NameSpacesInformation information = getNameSpacesInformation(
                SequenceEditorHelper.getEditingMediatorPosition(httpSession), id, repository);

        addNameSpace(prefix, uri, information);
        logOnSuccess(information, id);
    }

    public void registerNameSpaces(AXIOMXPath xPath, String id, HttpSession httpSession) {

        if (id == null || "".equals(id)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided id is empty or null," +
                        " to register NameSpace there must be a id ");
            }
            return;
        }

        if (xPath == null) {
            if (log.isDebugEnabled()) {
                log.debug("Provided XPath for id ' " + id + " ' is null ");
            }
            return;
        }

        NameSpacesInformationRepository repository
                = getNameSpacesInformationRepository(httpSession);
        NameSpacesInformation information = getNameSpacesInformation(
                SequenceEditorHelper.getEditingMediatorPosition(httpSession), id, repository);

        for (Object prefixObject : xPath.getNamespaces().keySet()) {
            if (prefixObject != null) {
                String prefix = (String) prefixObject;
                String uri = xPath.getNamespaceContext().translateNamespacePrefixToUri(prefix);
                addNameSpace(prefix, uri, information);
            }
        }
        logOnSuccess(information, id);
    }

    public void registerNameSpaces(List<MediatorProperty> properties,
                                   String baseId, HttpSession httpSession) {

        if (properties == null) {
            if (log.isDebugEnabled()) {
                log.debug("Provided MediatorProperty list is null, " +
                        "returning without registering NameSpaces");
            }
            return;
        }

        if (baseId == null || "".equals(baseId)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided  baseId is empty or null, " +
                        "to register NameSpace there must be a id ");
            }
            return;
        }

        registerNameSpaces(properties.iterator(), baseId, httpSession);
    }

    public void registerNameSpaces(Iterator<MediatorProperty> properties,
                                   String baseId, HttpSession httpSession) {

        if (properties == null) {
            if (log.isDebugEnabled()) {
                log.debug("Provided MediatorProperty list iterator is null, " +
                        "returning without registering NameSpaces");
            }
            return;
        }

        if (baseId == null || "".equals(baseId)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided  baseId is empty or null, " +
                        "to register NameSpace there must be a id ");
            }
            return;
        }

        int i = 0;
        while (properties.hasNext()) {
            MediatorProperty property = properties.next();
            if (property != null) {
                AXIOMXPath xPath = property.getExpression();
                if (xPath != null) {
                    registerNameSpaces(xPath, baseId + String.valueOf(i), httpSession);
                }
            }
            i++;
        }
    }

    public void registerNameSpaces(QName qName, String id, HttpSession httpSession) {

        if (qName == null) {
            if (log.isDebugEnabled()) {
                log.debug("Provide QName is null. returning without registering NameSpaces");
            }
            return;
        }

        NameSpacesInformationRepository repository
                = getNameSpacesInformationRepository(httpSession);
        NameSpacesInformation information = getNameSpacesInformation(
                SequenceEditorHelper.getEditingMediatorPosition(httpSession), id, repository);

        addNameSpace(qName.getPrefix(), qName.getNamespaceURI(), information);
        logOnSuccess(information, id);
    }

    public void unRegisterNameSpaces(HttpSession httpSession) {
        NameSpacesInformationRepository repository
                = (NameSpacesInformationRepository) httpSession.getAttribute(
                NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
        if (repository != null) {
            repository.removeAllNameSpacesInformation(
                    SequenceEditorHelper.getEditingMediatorPosition(httpSession));
        }
    }

    private NameSpacesInformationRepository getNameSpacesInformationRepository(
            HttpSession httpSession) {

        NameSpacesInformationRepository repository
                = (NameSpacesInformationRepository) httpSession.getAttribute(
                NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);

        if (repository == null) {
            repository = new NameSpacesInformationRepository();
            httpSession.setAttribute(
                    NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY, repository);
        }
        return repository;

    }

    private NameSpacesInformation getNameSpacesInformation(
            String ownerID, String id, NameSpacesInformationRepository repository) {

        NameSpacesInformation information = repository.getNameSpacesInformation(ownerID, id);
        if (information != null) {
            information.removeAllNameSpaces();
        }

        if (information == null) {
            information = new NameSpacesInformation();
            repository.addNameSpacesInformation(ownerID, id, information);
        }
        return information;
    }

    private void addNameSpace(String prefix, String uri, NameSpacesInformation information) {

        if (uri == null || "".equals(uri) || prefix == null || "".equals(prefix)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided uri or prefix is empty or null , " +
                        "to register NameSpace there must be a valid uri and a prefix ");
            }
            return;
        }

        if (!XMLConfigConstants.SYNAPSE_NAMESPACE.equals(uri)) {
            information.addNameSpace(prefix, uri);
        }

    }

    private boolean assertIDNotEmpty(String id) {

        if (id == null || "".equals(id)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided id is empty or null, " +
                        "to register NameSpace there must be a id ");
            }
            return false;
        }
        return true;
    }

    private void logOnSuccess(NameSpacesInformation information, String id) {
        if (log.isDebugEnabled()) {
            log.debug("Registered NameSpaces :" + information + " with id :" + id);
        }
    }
}
