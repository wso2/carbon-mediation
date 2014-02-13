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
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * Creates SynapseXPaths
 */
public class SynapseXPathFactory  {

    private static Log log = LogFactory.getLog(SynapseXPathFactory.class);

    /**
     * Creates a <code>SynapseXPath</code>
     *
     * @param element       OMElement Instance to be used to locate XPath expression and namespaces
     * @param attributeName Attribute name to get XPath expression
     * @return a <code>SynapseXPath</code>  instance
     */
    public BaseXPath createXPath(OMElement element, QName attributeName) {
        try {
            return org.apache.synapse.config.xml.SynapseXPathFactory.getSynapseXPath(element,
                    attributeName);
        } catch (JaxenException e) {
//            throw new LoggedRuntimeException("Error creating XPath from omelement : " + element
//                    + " with attribute : " + attributeName, e, log);
            return null;
        }
    }

    public BaseXPath createXPath(String xpath, Collection<OMNamespace> omNameSpaces) {
        if (xpath == null || "".equals(xpath)) {
//            throw new LoggedRuntimeException("XPath expression is null or empty", log);
        }
        try {
            SynapseXPath synapseXPath = new SynapseXPath(xpath);
            for (OMNamespace omNamespace : omNameSpaces) {
                if (omNamespace != null) {
                    synapseXPath.addNamespace(omNamespace.getPrefix(), omNamespace.getNamespaceURI());
                }
            }
            return synapseXPath;
        } catch (JaxenException e) {
//            throw new LoggedRuntimeException("Invalid XPapth expression : " +
//                    xpath, e, log);
            return null;
        }
    }
}
