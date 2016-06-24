/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*Defines configuration properties for DataMapperMediator*/
package org.wso2.carbon.mediator.datamapper.config.xml;

/**
 * Defines the properties and attributes of DataMapperMediator
 */
public class DataMapperMediatorConstants {
    public static final String DATAMAPPER = "datamapper";
    public static final String CONFIG = "config";
    public static final String INPUT_SCHEMA = "inputSchema";
    public static final String OUTPUT_SCHEMA = "outputSchema";
    public static final String INPUT_TYPE = "inputType";
    public static final String OUTPUT_TYPE = "outputType";

    /* Names of the different contexts used in the ESB */
    public static final String DEFAULT_CONTEXT = "DEFAULT";
    public static final String SYNAPSE_CONTEXT = "SYNAPSE";
    public static final String AXIS2_CONTEXT = "AXIS2";
    public static final String AXIS2_CLIENT_CONTEXT = "AXIS2-CLIENT";
    public static final String TRANSPORT_CONTEXT = "TRANSPORT";
    public static final String OPERATIONS_CONTEXT = "OPERATIONS";
    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";
    public static final String FUNCTION_CONTEXT = "FUNCTION";
    public static final String EMPTY_STRING = "";
}
