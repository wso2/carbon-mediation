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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.xml.IEntrySerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import java.net.URL;

public class StratosEntrySerialiser implements IEntrySerializer {
    private static Log log = LogFactory.getLog(StratosEntrySerialiser.class);

    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();
    protected static final OMNamespace synNS = SynapseConstants.SYNAPSE_OMNAMESPACE;
    protected static final OMNamespace nullNS
            = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");
    public OMElement serializeEntry(Entry entry, OMElement parent) {
        OMElement entryElement = fac.createOMElement("localEntry", synNS);

        if (entry.getDescription() != null) {
            OMElement descriptionElem = fac.createOMElement(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "description"));
            descriptionElem.setText(entry.getDescription());
            entryElement.addChild(descriptionElem);
        }

        entryElement.addAttribute(fac.createOMAttribute(
                "key", nullNS, entry.getKey().trim()));
        int type = entry.getType();
        if (type == Entry.URL_SRC) {
            URL srcUrl = entry.getSrc();
            if (srcUrl != null && !"file".equals(srcUrl.getProtocol())) {
                entryElement.addAttribute(fac.createOMAttribute(
                        "src", nullNS, srcUrl.toString().trim()));
            }else if(srcUrl != null){
                handleException("Invalid LocalEntry. Read blocked for local file system with path : " + srcUrl.getPath());
            }
        } else if (type == Entry.INLINE_XML) {
            Object value = entry.getValue();
            if (value != null && value instanceof OMElement) {
                entryElement.addChild((OMElement) value);
            }
        } else if (type == Entry.INLINE_TEXT) {
            Object value = entry.getValue();
            if (value != null && value instanceof String) {
                OMTextImpl textData = (OMTextImpl) fac.createOMText(((String) value).trim());
                textData.setType(XMLStreamConstants.CDATA);
                entryElement.addChild(textData);
            }
        } else if (type == Entry.REMOTE_ENTRY) {
            // nothing to serialize
            return null;
        } else {
            handleException("Entry type undefined");
        }

        if (parent != null) {
            parent.addChild(entryElement);
        }
        return entryElement;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}
