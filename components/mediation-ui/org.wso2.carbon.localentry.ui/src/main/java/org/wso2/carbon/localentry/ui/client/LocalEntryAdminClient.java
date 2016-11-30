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

package org.wso2.carbon.localentry.ui.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminServiceStub;
import org.wso2.carbon.localentry.stub.types.EntryData;
import org.wso2.carbon.localentry.stub.types.ConfigurationObject;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;

/**
 * <code>LocalEntryAdminClient</code> class, the place holder for localentry admin client
 */
public class LocalEntryAdminClient {

    private LocalEntryAdminServiceStub stub;
    private static final Log log = LogFactory.getLog(LocalEntryAdminClient.class);
    public static final int LOCAL_ENTRIES_PER_PAGE = 10;
    public LocalEntryAdminClient(String cookie,
                              String backendServerURL,
                              ConfigurationContext configCtx)throws AxisFault{

        String serviceURL = backendServerURL + "LocalEntryAdmin";
        stub = new LocalEntryAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        
    }

    /**
     * Returns entrydata array
     * @return entry data array
     * @throws Exception
     */
    public EntryData[] getEntryData()throws Exception{
        EntryData[] data = null;
        try{
            data = stub.entryData();
        }
        catch(Exception e){
            handleFault(e);
        }
        return data;
    }


    /**
     * Returns pagnitedentrydata array
     * @return entry data array
     * @throws Exception
     */
    public EntryData[] getPaginatedEntryData(int pageNo)throws Exception{
        EntryData[] data = null;
        try{
            data = stub.paginatedEntryData(pageNo);
        }
        catch(Exception e){
            handleFault(e);
        }
        return data;
    }

    public int getEntryDataCount()throws Exception {
        int count = 0;
        try {
            count = stub.getEntryDataCount();
        }
        catch(Exception e){
            handleFault(e);
        }
        return count;
    }


    /**
     * Saves an entry
     * @param entry name of the entry
     * @throws Exception
     */


    public void saveEntry(String entry)throws Exception{
        try{
        stub.saveEntry(entry);
        }
        catch(Exception e){
            handleFault(e);
        }
    }

    /**
     * Adds an entry
     * @param entry name of the entry
     * @throws Exception
     */
    public void addEntry(String entry)throws Exception{
        try{
        stub.addEntry(entry);        
        }
        catch(Exception e){
            handleFault(e);
        }
    }

    /**
     * Deletes an entry
     * @param entry name of the entry
     * @throws Exception
     */
    public void deleteEntry(String entry)throws Exception{
        try{
        stub.deleteEntry(entry);
        }
        catch(Exception e){
            handleFault(e);
        }
    }

    public ConfigurationObject[] getDependents(String entry) throws Exception {
        try {
            ConfigurationObject[] dependents = stub.getDependents(entry);
            if (dependents != null && dependents.length > 0 && dependents[0] != null) {
                return dependents;
            }
        } catch (RemoteException e) {
            handleFault(e);
        }
        return null;
    }

    /**
     * Excepetion handler
     * @param e exception
     * @throws Exception
     */
    private void handleFault(Exception e)throws Exception{
        log.error(e.getMessage(),e);
        throw e;
    }


    public static OMElement nonCoalescingStringToOm(String xmlStr) throws XMLStreamException {
        StringReader strReader = new StringReader(xmlStr);
        XMLInputFactory xmlInFac = XMLInputFactory.newInstance();
        //Non-Coalescing parsing
        xmlInFac.setProperty("javax.xml.stream.isCoalescing", false);

        XMLStreamReader parser = xmlInFac.createXMLStreamReader(strReader);
        StAXOMBuilder builder = new StAXOMBuilder(parser);

        return builder.getDocumentElement();
    }

    /**
     * Method which will check the given xml string for XXE attacks.
     * This will throw SAXException if there is a possibility of XXE attack.
     *
     * @param xmlStr
     * @throws Exception
     */
    public static void checkForXXE(String xmlStr) throws Exception {
        DocumentBuilder documentBuilder = getSecuredDocumentBuilder(false);
        documentBuilder.parse(new ByteArrayInputStream(xmlStr.getBytes()));
    }

    /**
     * This method provides a secured document builder which will secure XXE attacks.
     *
     * @param setIgnoreComments whether to set setIgnoringComments in DocumentBuilderFactory.
     * @return DocumentBuilder
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    private static DocumentBuilder getSecuredDocumentBuilder(boolean setIgnoreComments) throws
                                                                                        ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setIgnoringComments(setIgnoreComments);
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                throw new SAXException("Possible XML External Entity (XXE) attack. Skip resolving entity");
            }
        });
        return documentBuilder;
    }

}
