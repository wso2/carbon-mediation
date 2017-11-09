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

package org.wso2.carbon.mediation.connector.message.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Jaxb bean class for <PartProperties></PartProperties> element.
 */
@XmlRootElement(name = "PartProperties")
@XmlAccessorType(XmlAccessType.FIELD)
public class PartProperties {
    @XmlElement(name = "Property")
    private List<Property> partProperties;

    public List<Property> getPartProperties() {
        return partProperties;
    }

    public void setPartProperties(List<Property> partProperties) {
        this.partProperties = partProperties;
    }

    public void addPartProperty(Property property) {
        if (partProperties == null) {
            partProperties = new ArrayList<Property>();
        }
        partProperties.add(property);
    }

    public Property getProperty(String name) {
        for (Property property : partProperties) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }
}
