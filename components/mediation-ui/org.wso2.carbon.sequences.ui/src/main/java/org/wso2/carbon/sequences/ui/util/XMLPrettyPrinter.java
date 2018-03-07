package org.wso2.carbon.sequences.ui.util;
/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

/*
 * This is a temporary class added to support XML Pretty Print changes for XML Comment Nodes.
 * Original changes should go to org.wso2.carbon.utils.XMLPrettyPrinter class (in carbon kernel utils).
 * This class should be removed after carbon kernel release 4.0.1+ with required changes.
*/
public class XMLPrettyPrinter {

    private static final int ENTITY_EXPANSION_LIMIT = 0;
    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private static Log log = LogFactory.getLog(XMLPrettyPrinter.class);

    static {
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);

        try {
            documentBuilderFactory
                    .setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, e);
        }

        try {
            documentBuilderFactory
                    .setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, e);
        }

        try {
            documentBuilderFactory
                    .setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.LOAD_EXTERNAL_DTD_FEATURE, e);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        documentBuilderFactory
                .setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);
    }

    private InputStream in;
    private boolean xmlFormat;
    private boolean numericEnc;
    private boolean done = false;
    private String encoding = "UTF-8";

    public XMLPrettyPrinter(InputStream in, boolean format, boolean numeric, String encoding) {
        this.in = in;
        xmlFormat = format;
        numericEnc = numeric;
        if (encoding != null) {
            this.encoding = encoding;
        }
    }

    public XMLPrettyPrinter(InputStream in) {
        this(in, true, false, null);
    }

    public XMLPrettyPrinter(InputStream in, String encoding) {
        this(in, true, false, encoding);
    }

    /**
     * XML Pretty Print method with XML Comments support.
     *
     * @return XML formatted String
     */
    public String xmlFormatWithComments() {
        String xmlOutput = null;
        Document doc;
        LSSerializer lsSerializer;

        try {
            doc = getDocumentBuilderFactory().newDocumentBuilder().parse(in);

            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            lsSerializer = domImplementation.createLSSerializer();
            lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);

            LSOutput lsOutput = domImplementation.createLSOutput();
            lsOutput.setEncoding(encoding);
            Writer stringWriter = new StringWriter();
            lsOutput.setCharacterStream(stringWriter);
            lsSerializer.write(doc, lsOutput);

            xmlOutput = stringWriter.toString();

        } catch (IOException e) {
            log.error("XML Pretty Printer Error reading data from given InputStream to XML Document ", e);
        } catch (SAXException e) {
            log.error("XML Pretty Printer Error parsing the given InputStream to XML Document", e);
        } catch (Exception e) {
            log.error("XML Pretty Printer failed. ", e);
        }
        return xmlOutput;
    }

    private DocumentBuilderFactory getDocumentBuilderFactory() {
        return documentBuilderFactory;
    }

}
