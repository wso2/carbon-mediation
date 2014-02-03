/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.business.messaging.salesforce.mediator.ui.handler;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.business.messaging.salesforce.mediator.ui.constants.SalesforceMedatorConstants;
import org.wso2.carbon.business.messaging.salesforce.mediator.ui.constants.SalesforceResourceConstants;

/**
 * This class initalize front-end(ui) inputs schema from file system
 */
public class ConfigHandler {
    /**
     * Singleton instance
     */
    private static volatile ConfigHandler configHandler;
    /**
     * <p>root schema element for ui schema</p>
     */
    private OMElement configElem;
    private int noOps = 0;
    /**
     * <p>maps operation and child ui Display Name</p>
     */
    private final HashMap operation2DisplayNameMap = new HashMap();
    /**
     * <p>maps operation and child ui config element</p>
     */
    private final HashMap operation2ConfigElemMap = new HashMap();


    private ConfigHandler() throws XMLStreamException, FileNotFoundException {
        parseConfig();
        initMap();
    }

    /**
     * provides a single Configuration reader to the caller
     *
     * @return singleton instance
     * @throws FileNotFoundException when ui configuration descriptor not found
     * @throws XMLStreamException    error parsing xml
     */
    public static ConfigHandler getInstance() throws XMLStreamException, FileNotFoundException {
        if (null == configHandler) {
            synchronized (ConfigHandler.class) {
                if (null == configHandler) {
                    configHandler = new ConfigHandler();
                }
            }
        }
        return configHandler;
    }

    /**
     * @param operationName
     * @return operation OMelemeent for corresponding operation name
     * @throws FileNotFoundException when ui configuration descriptor not found
     * @throws XMLStreamException    error parsing xml
     * @throws JaxenException        error processing/evaluating xpath expression
     */
    public OMElement parse(String operationName) throws FileNotFoundException,
                                                        XMLStreamException, JaxenException {
        AXIOMXPath xPath = new AXIOMXPath("//service/operation[@name='"
                                          + operationName + "']");
        return (OMElement) xPath.selectSingleNode(parseConfig());
    }

    /**
     * read UI configuration file from a predefined path
     *
     * @return root OM element of config schema
     * @throws FileNotFoundException when ui configuration descriptor not found
     * @throws XMLStreamException    error parsing xml
     */
    public OMElement parseConfig() throws FileNotFoundException,
                                          XMLStreamException {
        if (configElem == null) {
            String filePath = SalesforceResourceConstants.PATH_CONFIG_SALESFORCE_INPUTS_XML;
            OMElement rootElem = new StAXOMBuilder(filePath).getDocumentElement();
            configElem = rootElem;
        }
        return configElem;
    }

    /**
     * reads each child configuration element and register operation elements + display names
     */
    private void initMap() {
        Iterator children = configElem.getChildrenWithName(SalesforceMedatorConstants.QNAME_OPERATION_UI);
        while (children.hasNext()) {
            OMElement opChild = (OMElement) children.next();
            operation2ConfigElemMap.put(opChild.getAttributeValue(SalesforceMedatorConstants.QNAME_ATTR_NAME_UI),
                                        opChild);
            operation2DisplayNameMap.put(opChild.getAttributeValue(SalesforceMedatorConstants.QNAME_ATTR_NAME_UI),
                                         opChild.getAttributeValue(SalesforceMedatorConstants.QNAME_ATTR_DISP_NAME_UI));
        }
    }

    /**
     * @return no of operations supported
     */
    public int getOperationsSize() {
        return operation2ConfigElemMap.size();
    }

    /**
     * @return names of operations supported
     */
    public Iterator getOperationNames() {
        return operation2ConfigElemMap.keySet().iterator();
    }

    /**
     * @param opName operation Name
     * @return corresponding schema OMElement of an operation
     */
    public OMElement getOperationConfigElement(String opName) {
        return (OMElement) operation2ConfigElemMap.get(opName);
    }

    /**
     * @param opName operation Name
     * @return corresponding diplay Name of an operation
     */
    public String getOperationDisplayName(String opName) {
        return (String) operation2DisplayNameMap.get(opName);
    }
}
