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
package org.wso2.carbon.mediation.initializer.multitenancy;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.XMLToObjectMapper;
import org.apache.synapse.config.xml.IEntryFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class StratosEntryFactory implements IEntryFactory,XMLToObjectMapper {

    private static Log log = LogFactory.getLog(StratosEntryFactory.class);

    private static final QName DESCRIPTION_Q
            = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "description");

    public Entry createEntry(OMElement elem) {
        OMAttribute key = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));
        Entry entry = new Entry(key.getAttributeValue());

        OMElement descriptionElem = elem.getFirstChildWithName(DESCRIPTION_Q);
        if (descriptionElem != null) {
            entry.setDescription(descriptionElem.getText());
            descriptionElem.detach();
        }

        String src  = elem.getAttributeValue(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "src"));

        // if a src attribute is present, this is a URL source resource,
        // it would now be loaded from the URL source, as all static properties
        // are initialized at startup
        if (src != null) {
            try {
                URL url = new URL(src.trim());
                if (!"file".equals(url.getProtocol())) {
                    entry.setSrc(url);
                    entry.setType(Entry.URL_SRC);
                    entry.setValue(SynapseConfigUtils.getObject(entry.getSrc(), new Properties()));
                }else{
                    handleException("Cannot create LocalEntry. Read blocked for local file system with path : " +
                                    url.getPath());
                }
            } catch (MalformedURLException e) {
                handleException("The entry with key : " + key + " refers to an invalid URL");
            }

        } else {
            OMNode nodeValue = elem.getFirstOMChild();
            OMElement elemValue = elem.getFirstElement();

            if (elemValue != null) {
                entry.setType(Entry.INLINE_XML);
                entry.setValue(elemValue);
            } else if (nodeValue != null && nodeValue instanceof OMText) {
                entry.setType(Entry.INLINE_TEXT);
                entry.setValue(elem.getText());
            }
        }

        return entry;

    }

    public Object getObjectFromOMNode(OMNode om, Properties properties) {
        if (om instanceof OMElement) {
            return createEntry((OMElement) om);
        } else {
            handleException("Invalid XML configuration for an Entry. OMElement expected");
        }
        return null;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

}
