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
package org.wso2.carbon.message.store.ui.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.synapse.config.xml.MessageStoreFactory;
import org.apache.synapse.util.xpath.SynapseJsonPath;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * class <code>MessageStoreData</code>  contain the Message Store configuration data
 */
public class MessageStoreData {

    private String name;

    private String clazz;

    private String sequence;

    private Map<String, String> params = new HashMap<String, String>();

    private PathInfo pathInfo;

    private String artifactContainerName;

    private boolean isEdited;

    public static final String REGISTRY_KEY_PREFIX = "$registry:";

    public MessageStoreData(){}

    public MessageStoreData(String xml) throws XMLStreamException, JaxenException {
        populate(xml);
    }

    public String getName() {
        return name;
    }

    public String getSequence() {
        return sequence;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public PathInfo getPathInfo() {
        return pathInfo;
    }

    public String getClazz() {
        return clazz;
    }

    public void setName(String name){
        this.name=name;
    }

    public void setClazz(String provider){
        this.clazz=provider;
    }

    /**
     * Returns the Xpath expression based on the specified property.
     *
     * @param element the element which contains the expression and namespace.
     * @param expression the expression extracted from the element.
     * @return the Synapse Xpath expression value.
     * @throws JaxenException when an error occurs while creating the xPath
     */
    private SynapseXPath getXPathExpression(OMElement element,String expression) throws JaxenException {
        return new SynapseXPath(element,expression);
    }

    private void populate(String xml) throws XMLStreamException, JaxenException {
        InputStream in = new ByteArrayInputStream(xml.getBytes());
        OMElement elem = new StAXOMBuilder(in).getDocumentElement();

        OMAttribute attElem = elem.getAttribute(MessageStoreFactory.CLASS_Q);

        if (attElem != null) {
            this.clazz = attElem.getAttributeValue();
        }

        attElem = elem.getAttribute(MessageStoreFactory.NAME_Q);

        if (attElem != null) {
            this.name = attElem.getAttributeValue();
        }

        attElem = elem.getAttribute(MessageStoreFactory.SEQUENCE_Q);

        if (attElem != null) {
            this.sequence = attElem.getAttributeValue();
        }

        Iterator<OMElement> it = elem.getChildrenWithName(MessageStoreFactory.PARAMETER_Q);

        while (it.hasNext()) {
            OMElement paramElem = it.next();
            OMAttribute nameAtt = paramElem.getAttribute(MessageStoreFactory.NAME_Q);
            OMAttribute expressionAttribute = paramElem.getAttribute(MessageStoreFactory.EXPRESSION_Q);
            OMAttribute keyAtt = paramElem.getAttribute(MessageStoreFactory.KEY_Q);
            assert nameAtt != null;
            String name = nameAtt.getAttributeValue();
            String value;
            if (expressionAttribute != null) {
                value = expressionAttribute.getAttributeValue();
                this.pathInfo = new PathInfo();
                if (!value.startsWith("json")) {
                    SynapseXPath xPathExpression = getXPathExpression(paramElem, value);
                    this.pathInfo.setxPath(xPathExpression);
                } else {
                    SynapseJsonPath jsonPath = new SynapseJsonPath(value);
                    this.pathInfo.setJsonPath(jsonPath);
                }
            } else if (keyAtt != null) {
                value = REGISTRY_KEY_PREFIX + keyAtt.getAttributeValue();
            } else {
                value = paramElem.getText();
            }
            params.put(name, value);
        }

    }

    public String getString() {
        StringBuffer storeString = new StringBuffer();
        storeString.append("name: " + name + "\n");
        storeString.append("clazz: " + clazz + "\n");
        storeString.append("sequence: " + sequence + "\n");
        storeString.append("params: " + params + "\n");
        return storeString.toString();
    }

    /**
     * Get the name of the artifact container from which the proxy deployed
     * @return artifactContainerName
     */
    public String getArtifactContainerName() {
        return artifactContainerName;
    }

    /**
     * Set the name of the artifact container
     * @param artifactContainerName
     */
    public void setArtifactContainerName(String artifactContainerName) {
        this.artifactContainerName = artifactContainerName;
    }

    /**
     * Whether the proxy is edited through the management console
     * @return isEdited
     */
    public boolean getIsEdited() {
        return isEdited;
    }

    /**
     * Set whether the proxy is deployed via the management console
     * @param isEdited
     */
    public void setIsEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }
}
