/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transports.sap;

/**
 * SAP adapter constants
 */
public class SAPConstants {

    public static final String SAP_IDOC_PROTOCOL_NAME = "idoc";
    public static final String SAP_IDOC_PROTOCOL_PREFIX = SAP_IDOC_PROTOCOL_NAME + ":/";

    public static final String SAP_BAPI_PROTOCOL_NAME = "bapi";
    public static final String SAP_BAPI_PROTOCOL_PREFIX = SAP_BAPI_PROTOCOL_NAME + ":/";

    public static final String SAP_ENABLED = "enabled";

    /* Transport listener parameters */
    public static final String SERVER_NAME_PARAM = "transport.sap.serverName";
    public static final String ENABLE_ERROR_LISTENER_PARAM = "transport.sap.enableErrorListener";
    public static final String ENABLE_TID_HANDLER_PARAM = "transport.sap.enableTIDHandler"; 
    public static final String CONNECTIONS_PARAM = "transport.sap.connections";
    public static final String CUSTOM_ERROR_LISTENER_PARAM = "transport.sap.customErrorListener";
    public static final String CUSTOM_EXCEPTION_LISTENER_PARAM = "transport.sap.customExceptionListener";
    public static final String CUSTOM_TID_HANDLER_PARAM = "transport.sap.customTIDHandler";

    /* Transport sender parameters */
    public static final String CUSTOM_IDOC_XML_MAPPERS = "transport.sap.customXMLMappers";
    public static final String SAP_IDOC_VERSION = "version";

    public static final int SAP_SERVER_DEFAULT_CONNECTIONS = 1;
    public static final String SAP_IDOC_VERSION_2 = "2";
    public static final String SAP_IDOC_VERSION_3 = "3";

    public static final String SAP_CONTENT_TYPE = "application/xml";

    public static final String XML_MAPPER_ELT = "mapper";
    public static final String XML_MAPPER_KEY_ATTR = "key";

    /* Client options */
    public static final String CLIENT_XML_MAPPER_KEY = "transport.sap.xmlMapper";
    public static final String CLIENT_XML_PARSER_OPTIONS = "transport.sap.xmlParserOptions";
}
