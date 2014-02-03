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
package org.wso2.carbon.message.processor.ui.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.synapse.config.xml.MessageProcessorFactory;
import org.apache.synapse.config.xml.MessageStoreFactory;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * class <code>MessageProcessorData</code>  contain the Message Store configuration data
 */
public class MessageProcessorData {

    private String name;

    private String clazz;

    private String targetEndpoint;

    private String messageStore;

    private Map<String, String> params = new HashMap<String, String>();

    public MessageProcessorData(String xml) throws XMLStreamException {
        populate(xml);
    }

    public String getName() {
        return name;
    }

    public String getMessageStore() {
        return messageStore;
    }

    public Map<String, String> getParams() {
        return params;
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

    public void setMessageStore(String store){
        this.messageStore=store;
    }

    private void populate(String xml) throws XMLStreamException {
        InputStream in = new ByteArrayInputStream(xml.getBytes());
        OMElement elem = new StAXOMBuilder(in).getDocumentElement();

        OMAttribute attElem = elem.getAttribute(MessageProcessorFactory.CLASS_Q);

        if (attElem != null) {
            this.clazz = attElem.getAttributeValue();
        }

        attElem = elem.getAttribute(MessageProcessorFactory.NAME_Q);

        if (attElem != null) {
            this.name = attElem.getAttributeValue();
        }

        attElem = elem.getAttribute(MessageProcessorFactory.TARGET_ENDPOINT_Q);

        if (attElem != null) {
            this.targetEndpoint = attElem.getAttributeValue();
        }

        attElem = elem.getAttribute(MessageProcessorFactory.MESSAGE_STORE_Q);

        if (attElem != null) {
            this.messageStore = attElem.getAttributeValue();
        }

        Iterator<OMElement> it = elem.getChildrenWithName(MessageStoreFactory.PARAMETER_Q);

        while (it.hasNext()) {
            OMElement paramElem = it.next();
            OMAttribute nameAtt = paramElem.getAttribute(MessageStoreFactory.NAME_Q);

            assert nameAtt != null;
            String name = nameAtt.getAttributeValue();
            String value = paramElem.getText();

            params.put(name, value);
        }

    }

    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    public void setTargetEndpoint(String targetSequence) {
        this.targetEndpoint = targetSequence;
    }
}
