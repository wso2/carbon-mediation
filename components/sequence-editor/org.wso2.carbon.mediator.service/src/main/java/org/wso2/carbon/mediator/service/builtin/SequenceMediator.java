/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 **/

package org.wso2.carbon.mediator.service.builtin;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.Value;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;

import javax.xml.namespace.QName;

/**
 * This class represents a sequence mediator.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class SequenceMediator extends AbstractListMediator {

    private Value key;
    private String name;
    private String errorHandler;
    private String description;
    private boolean anonymous = false;

    public String getName() {
        return name;
    }

    public String getErrorHandler() {
        return errorHandler;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setErrorHandler(String errorHandler) {
        this.errorHandler = errorHandler;
    }

    public Value getKey() {
        return key;
    }

    public void setKey(Value key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTagLocalName() {
        return "sequence";
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public OMElement serialize(OMElement parent) {
        OMElement sequence = fac.createOMElement("sequence", synNS);
        if (!anonymous) {
            if (key != null) {
                // Use KeySerializer to serialize Key
                ValueSerializer keySerializer = new ValueSerializer();
                keySerializer.serializeValue(key, XMLConfigConstants.KEY, sequence);
            } else if (name != null) {
                sequence.addAttribute(fac.createOMAttribute(
                        "name", nullNS, name));

                if (errorHandler != null) {
                    sequence.addAttribute(fac.createOMAttribute(
                            "onError", nullNS, errorHandler));
                }
                saveTracingState(sequence, this);
                serializeChildren(sequence, getList());
            }

            if (parent != null) {
                parent.addChild(sequence);
            }
        } else {
            if (errorHandler != null) {
                sequence.addAttribute(fac.createOMAttribute(
                        "onError", nullNS, errorHandler));
            }
            saveTracingState(sequence, this);
            serializeChildren(sequence, getList());
            if (parent != null) {
                parent.addChild(sequence);
            }
        }

        if (description != null) {

            OMElement descriptionElem = sequence.getFirstChildWithName(new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "description"));

            if (descriptionElem != null) {
                descriptionElem.setText(description);
            } else {
                OMElement newDescriptionElem = fac.createOMElement("description", synNS);
                newDescriptionElem.setText(description);
                sequence.addChild(newDescriptionElem);
            }

        }

        return sequence;
    }

    public void build(OMElement elem) {
        this.key = null;
        this.name = null;
        this.errorHandler = null;

        OMAttribute n = elem.getAttribute(ATT_NAME);
        OMAttribute e = elem.getAttribute(ATT_ONERROR);

        OMElement descriptionElem = elem.getFirstChildWithName(new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "description"));
        if (descriptionElem != null && descriptionElem.getText() != null) {
            description = descriptionElem.getText();
        }

        if (!anonymous) {
            if (n != null) {
                name = n.getAttributeValue();
                if (e != null) {
                    errorHandler = e.getAttributeValue();
                }
                processAuditStatus(this, elem);
                addChildren(elem, this);

            } else {
                n = elem.getAttribute(ATT_KEY);
                if (n != null) {
                    //Use KeyFactory to create Key
                    ValueFactory keyFactory = new ValueFactory();
                    key = keyFactory.createValue(XMLConfigConstants.KEY, elem);

                    if (e != null) {
                        String msg = "A sequence mediator with a reference to another " +
                                "sequence can not have 'ErrorHandler'";
                        throw new MediatorException(msg);
                    }
                } else {
                    String msg = "A sequence mediator should be a named sequence or a reference " +
                            "to another sequence (i.e. a name attribute or key attribute is required)";
                    throw new MediatorException(msg);
                }
            }
        } else {
            if (e != null) {
                errorHandler = e.getAttributeValue();
            }
            processAuditStatus(this, elem);
            addChildren(elem, this);
        }
    }
}
