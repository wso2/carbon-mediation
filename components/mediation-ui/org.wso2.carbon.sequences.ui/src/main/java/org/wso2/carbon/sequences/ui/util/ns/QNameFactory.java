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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.sequences.ui.util.SequenceEditorHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.Iterator;

@SuppressWarnings({"UnusedDeclaration"})
public class QNameFactory {

    private static final Log log = LogFactory.getLog(QNameFactory.class);

    private final static QNameFactory ourInstance = new QNameFactory();

    public static QNameFactory getInstance() {
        return ourInstance;
    }

    private QNameFactory() {
    }

    public QName createQName(String id, String localName, HttpSession httpSession) {

        if (!assertIDNotEmpty(id) || !assertLocalNameNotEmpty(localName)) {
            return null;
        }

        NameSpacesInformationRepository repository = (NameSpacesInformationRepository) httpSession.getAttribute(
                NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
        if (repository == null) {
            return new QName(localName);
        }

        NameSpacesInformation information = repository.getNameSpacesInformation(
                SequenceEditorHelper.getEditingMediatorPosition(httpSession), id);
        if (information == null) {
            return new QName(localName);
        }
        if (log.isDebugEnabled()) {
            log.debug("Getting NameSpaces :" + information + " for id :" + id);
        }

        Iterator<String> iterator = information.getPrefixes();
        if (iterator.hasNext()) {
            String prefix = iterator.next();
            String uri = information.getNameSpaceURI(prefix);
            if (uri == null) {
                uri = XMLConstants.NULL_NS_URI;
            }
            if (prefix == null) {
                prefix = XMLConstants.DEFAULT_NS_PREFIX;
            }
            return new QName(uri, localName, prefix);
        }
        information.removeAllNameSpaces();
        return new QName(localName);
    }

    public QName createQName(String id, HttpServletRequest request, HttpSession httpSession) {
        return createQName(id, request.getParameter(id), httpSession);
    }

    private static boolean assertIDNotEmpty(String id) {
        if (id == null || "".equals(id)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided id is empty or null ,returning a null as QName");
            }
            return false;
        }
        return true;
    }

    private static boolean assertLocalNameNotEmpty(String source) {
        if (source == null || "".equals(source)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided Localname is empty or null ,returning a null as QName");
            }
            return false;
        }
        return true;
    }
}
