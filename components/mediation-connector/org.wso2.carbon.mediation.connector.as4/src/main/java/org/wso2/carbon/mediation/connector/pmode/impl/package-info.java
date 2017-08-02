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

@XmlSchema(
        namespace = "http://wso2.org/schemas/2016/10/pmode",
        elementFormDefault = XmlNsForm.QUALIFIED,
        xmlns = {
                @XmlNs(prefix = "", namespaceURI = "http://wso2.org/schemas/2016/10/pmode"),
                @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"),
                @XmlNs(prefix = "pMode", namespaceURI = "http://wso2.org/schemas/2016/10/pmode ../../repository/xsd/pmode.xsd")
        }
)
package org.wso2.carbon.mediation.connector.pmode.impl;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
