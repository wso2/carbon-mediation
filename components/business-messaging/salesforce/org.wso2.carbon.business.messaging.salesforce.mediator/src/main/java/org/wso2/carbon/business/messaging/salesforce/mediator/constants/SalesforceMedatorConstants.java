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
package org.wso2.carbon.business.messaging.salesforce.mediator.constants;

import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;

public class SalesforceMedatorConstants {

    /**
     *
     * UI CONFIGURATIO CONSTANTS
     *
     */

    /**
     * Holds the reference for the key type attribute.
     */
    public static final QName QNAME_ATTR_KEY_UI = new QName("key");
    /**
     * Holds the reference for the operation element.
     */
    public static final QName QNAME_OPERATION_UI = new QName("operation");
    /**
     * Holds the reference for the type attribute.
     */
    public static QName QNAME_ATTR_TYPE_UI = new QName("type");
    /**
     * Holds the reference for the name attribute.
     */
    public static QName QNAME_ATTR_NAME_UI = new QName("name");
    /**
     * Holds the reference for the name attribute.
     */
    public static QName QNAME_ATTR_DISP_NAME_UI = new QName("display-name");
    /**
     * Holds the reference for the name attribute.
     */
    public static QName QNAME_ATTR_REQUIRED_UI = new QName("required");
    /**
     * Holds the reference for the inputs element.
     */
    public static QName QNAME_ELEM_PROPERTY_UI = new QName("property");
    /**
     * Holds the reference for the input element.
     */
    public static QName QNAME_ELEM_INPUT_UI = new QName("input");
    /**
     * Holds the reference for the output element.
     */
    public static QName QNAME_ELEM_OUTPUT_UI = new QName("output");
    /**
     * Holds the reference for the collection attribute.
     */
    public static final QName QNAME_ATT_COLLECTION_UI = new QName("collection");
    /**
     * Holds the reference for the complex type attribute.
     */
    public static final QName QNAME_ATT_COMPLEX_UI = new QName("complex");
    /**
     * Holds the reference for the keys attribute.
     */
    public static final QName QNAME_ATTR_KEYS_UI = new QName("keys");


    /**
     *
     * MEDIATOR CONFIGURATION CONSTANTS
     *
     */

    /**
     * Holds the reference for the top level salesforce config element.
     */
    public static final QName QNAME_SALESFORCE = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "salesforce");
    /**
     * Holds the reference for the top level axis2.xml location config attribute.
     */
    public static final QName QNAME_ATT_AXIS2XML = new QName("axis2xml");
    /**
     * Holds the reference for the top level axis2 repo location config attribute.
     */
    public static final QName QNAME_ATT_REPOSITORY = new QName("repository");
    /**
     * Holds the reference for salesforce config element.
     */
    public static final QName QNAME_CONFIG = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "configuration");
    /**
     * Holds the reference for the top level salesforce input wrapper element.
     */
    public static final QName QNAME_INPUT_WRAPPER = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "input-wrapper");
    /**
     * Holds the reference for salesforce namespace attribute for wrapper elements.
     */
    public static final QName QNAME_ATT_NAMESPACE = new QName("namespace");
    /**
     * Holds the reference for  salesforce namespace prefix attribute.
     */
    public static final QName QNAME_ATT_NAMESPACE_PREFIX = new QName("ns-prefix");
    /**
     * Holds the reference for salesforce name attribute.
     */
    public static final QName QNAME_ATT_NAME = new QName("name");
    /**
     * Holds the reference for salesforce type attribute for operations , inputs(optional) , outputs.
     */
    public static final QName QNAME_ATT_TYPE = new QName("type");
    /**
     * Holds the reference for salesforce xpath attribute for inputs/outputs.
     */
    public static final QName QNAME_ATT_SOURCE_XPATH = new QName("source-xpath");
    /**
     * Holds the reference for salesforce xpath attribute for inputs/outputs.
     */
    public static final QName QNAME_ATT_TARGET_XPATH = new QName("target-xpath");
    /**
     * Holds the reference for salesforce source value attribute for inputs/outputs.
     */
    public static final QName QNAME_ATT_SOURCE_VALUE = new QName("source-value");
    /**
     * Holds the reference for salesforce target property key attribute for inputs/outputs.
     */
    public static final QName QNAME_ATT_TARGET_KEY = new QName("target-key");
    /**
     * Holds the reference for salesforce input type collection attribute.
     */
    public static final QName QNAME_ATT_COLLECTION = new QName("collection");
    /**
     * Holds the reference for salesforce complex type attribute for inputs.
     */
    public static final QName QNAME_ATT_COMPLEX = new QName("complex");
    /**
     * Holds the reference for salesforce attribute for output name.
     */
    public static final QName QNAME_ELEM_KEY = new QName(XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "key");

    /**
     * MEDIATOR Serializer CONSTANTS
     */
    public static final String TAG_SALESFORCE = "salesforce";

    public static final String TAG_CONFIGURATION = "configuration";

    public static final String ATTR_REPOSITORY = "repository";

    public static final String ATTR_AXIS2XML = "axis2xml";

    public static final String ATTR_TYPE = "type";

    public static final String TAG_INPUT_WRAPPER = "input-wrapper";

    public static final String ATTR_NAME = "name";

    public static final String ATTR_NAMESPACE = "namespace";

    public static final String ATTR_NS_PREFIX = "ns-prefix";

    public static final String ATTR_COLLECTION = "collection";

    public static final String ATTR_COMPLEX = "complex";

    public static final String ATTR_SOURCE_XPATH = "source-xpath";

    public static final String ATTR_SOURCE_VALUE = "source-value";

    public static final String ATTR_TARGET_KEY = "target-key";

    public static final String ATTR_TARGET_XPATH = "target-xpath";
}
