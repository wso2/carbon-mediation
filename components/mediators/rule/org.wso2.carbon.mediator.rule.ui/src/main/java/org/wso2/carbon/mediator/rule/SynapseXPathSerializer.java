/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.mediator.rule;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.BaseXPath;

/**
 * Serialize SynapseXPath
 */
public class SynapseXPathSerializer  {

    /**
     * Serializes <code>SynapseXPath </code> on given OMElement
     *
     * @param xpath         XPath to be serialized
     * @param element       Target OMElement
     * @param attributeName Target attribute name
     */
    public void serializeXPath(BaseXPath xpath, OMElement element, String attributeName) {
        if (xpath instanceof SynapseXPath) {
            org.apache.synapse.config.xml.SynapseXPathSerializer.serializeXPath(
                    (SynapseXPath) xpath, element,
                    attributeName);
        }
    }
}
