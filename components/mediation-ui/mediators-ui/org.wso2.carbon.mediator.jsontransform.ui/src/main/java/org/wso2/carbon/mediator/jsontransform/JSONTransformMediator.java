/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mediator.jsontransform;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.util.MediatorProperty;

import java.util.ArrayList;
import java.util.List;


public class JSONTransformMediator extends AbstractMediator {
    /** The holder for the custom properties */
    private final List<MediatorProperty> properties = new ArrayList<>();

    public String getTagLocalName() {
        return "jsontransform";
    }

    public List<MediatorProperty> getProperties() {
        return properties;
    }

    public void addProperty(MediatorProperty p) {
        properties.add(p);
    }

    public void addAllProperties(List<MediatorProperty> list) {
        properties.addAll(list);
    }

    public OMElement serialize(OMElement parent) {
        OMElement log = fac.createOMElement("jsontransform", synNS);
        saveTracingState(log, this);

        serializeMediatorProperties(log, properties, PROP_Q);

        if (parent != null) {
            parent.addChild(log);
        }
        return log;
    }

    public void build(OMElement elem) {
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);

        addAllProperties(getMediatorProperties(elem));
    }
}
