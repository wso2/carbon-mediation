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

package org.wso2.carbon.proxyadmin.ui.client;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;

public class ProxyAdminClientUtils {

    /**
     * Sorts the given array alphabetically. This method does not sort in-place. Therefore
     * the original array is always unaffected. Returned array is a sorted copy.
     *
     * @param names An array of Strings
     * @return a sorted array
     */
    public static String[] sortNames(String[] names) {
        if (names == null || names.length == 0 || names[0] == null) {
            return names;
        }

        String[] copy = new String[names.length];
        System.arraycopy(names, 0, copy, 0, names.length);
        Arrays.sort(copy);
        return copy;
    }

    /**
     * This method provides a secured document builder which will secure XXE attacks.
     *
     * @param setIgnoreComments whether to set setIgnoringComments in DocumentBuilderFactory.
     * @return DocumentBuilder
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public static DocumentBuilder getSecuredDocumentBuilder(boolean setIgnoreComments) throws
            ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setIgnoringComments(setIgnoreComments);
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setXIncludeAware(false);
        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(0);
        documentBuilderFactory.setAttribute(Constants.XERCES_PROPERTY_PREFIX +
                    Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                throw new SAXException("Possible XML External Entity (XXE) attack. Skipping entity resolving");
            }
        });
        return documentBuilder;
    }
}
