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
package org.wso2.carbon.mediator.payloadfactory.util;

import org.wso2.carbon.utils.xml.XMLPrettyPrinter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Utils {

    public static String prettyPrint(String rawXML) {
        rawXML = rawXML.replaceAll("\n|\\r|\\f|\\t", "");
        InputStream xmlIn = new ByteArrayInputStream(rawXML.getBytes());
        XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn);
        rawXML = xmlPrettyPrinter.xmlFormat();
        if (rawXML.startsWith("\n")) {
            rawXML = rawXML.substring(1);
        }
        return rawXML;
    }

}
