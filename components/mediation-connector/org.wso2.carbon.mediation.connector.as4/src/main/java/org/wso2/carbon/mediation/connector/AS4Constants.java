/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediation.connector;

public class AS4Constants {

    public static final String EBMS = "ebMS";

    public static final String AS4_PMODE_LOCATION = "conf/pmodes/";

    public static final String COMPRESSION_TYPE = "CompressionType";

    public static final String MIME_TYPE = "MimeType";

    /**
     * wss-wssecurity-utility namespace
     */
    public static final  String WSU_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    /**
     * Attribute name 'Id'. This Id attribute value is being used as the PartInfo href when the SOAP body contains the payload
     */
    public static final String ATTRIBUTE_ID  = "Id";

    /**
     * Payload href prefix '#'. This is used as the PartInfo href prefix when the SOAP body contains the payload
     */
    public static final String SOAP_BODY_HREF_PREFIX = "#";

    /**
     * Payload href prefix 'cid:'. This is used as the PartInfo href prefix when the payloads are SOAP attachments
     */
    public static final String ATTACHMENT_HREF_PREFIX = "cid:";
}
