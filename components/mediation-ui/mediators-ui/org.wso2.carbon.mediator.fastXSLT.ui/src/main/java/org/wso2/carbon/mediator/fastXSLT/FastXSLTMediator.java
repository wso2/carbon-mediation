/**
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mediator.fastXSLT;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.*;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.util.MediatorProperty;

import javax.xml.namespace.QName;
import java.util.*;


public class FastXSLTMediator extends AbstractMediator {
    private static final QName ATTRIBUTE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attribute");

    private Value xsltKey = null;
    private SynapseXPath source = null;
    private String targetPropertyName = null;
    private final List<MediatorProperty> properties = new ArrayList<MediatorProperty>();
    private final List<MediatorProperty> transformerFactoryFeatures =
            new ArrayList<MediatorProperty>();
    private final List<MediatorProperty> transformerFactoryAttributes
            = new ArrayList<MediatorProperty>();

    Map<String, String> resources = new HashMap<String, String>();

    public String getTagLocalName() {
        return "fastXSLT";
    }

    public OMElement serialize(OMElement parent) {
        OMElement fastXSLT = fac.createOMElement("fastXSLT", synNS);

        if (xsltKey != null) {
            // Use KeySerializer to serialize Key
            ValueSerializer keySerializer = new ValueSerializer();
            keySerializer.serializeValue(xsltKey, XMLConfigConstants.KEY, fastXSLT);

        } else {
            throw new MediatorException("Invalid FastXSLT mediator. XSLT registry key is required");
        }
        saveTracingState(fastXSLT, this);

        if (source != null) {
            SynapseXPathSerializer.serializeXPath(source, fastXSLT, "source");
        }
        if (targetPropertyName != null) {
            fastXSLT.addAttribute(fac.createOMAttribute(
                    "target", nullNS, targetPropertyName));
        }
        serializeMediatorProperties(fastXSLT, properties, PROP_Q);
        if (!transformerFactoryFeatures.isEmpty()) {
            for (MediatorProperty mp : transformerFactoryFeatures) {
                OMElement prop = fac.createOMElement("feature", synNS, fastXSLT);
                if (mp.getName() != null) {
                    prop.addAttribute(fac.createOMAttribute("name", nullNS, mp.getName()));
                } else {
                    throw new MediatorException("The Feature name is missing");
                }
                if (mp.getValue() != null) {
                    prop.addAttribute(fac.createOMAttribute("value", nullNS, mp.getValue()));
                } else {
                    throw new MediatorException("The Feature value is missing");
                }
            }
        }
        serializeMediatorProperties(fastXSLT, transformerFactoryAttributes, ATTRIBUTE_Q);

        if (resources.size() > 0) {
            OMElement resource;
            Set resourceKeys = resources.keySet();

            for (Iterator i = resourceKeys.iterator(); i.hasNext(); ) {
                String key = i.next().toString();
                String value = resources.get(key);
                resource = fac.createOMElement("resource", synNS);
                resource.addAttribute("location", key, nullNS);
                resource.addAttribute("key", value, nullNS);
                fastXSLT.addChild(resource);
            }
        }

        if (parent != null) {
            parent.addChild(fastXSLT);
        }
        return fastXSLT;
    }

    public void build(OMElement elem) {
        OMAttribute attXslt = elem.getAttribute(ATT_KEY);
        OMAttribute attSource = elem.getAttribute(ATT_SOURCE);
        OMAttribute attTarget = elem.getAttribute(ATT_TARGET);
        QName ATT_RESOURCE_QNAME = new QName("resource");

        Iterator itr = elem.getChildrenWithName(ATT_RESOURCE_QNAME);
        Map<String, String> xsltResourecMap = new HashMap<String, String>();

        while (itr.hasNext()) {
            OMElement resourceElement = (OMElement) itr.next();
            QName ATT_RESOURCE_LOCATION_QNAME = new QName("location");
            QName ATT_RESOURCE_KEY_QNAME = new QName("key");

            OMAttribute locationAttr = resourceElement.getAttribute(ATT_RESOURCE_LOCATION_QNAME);
            OMAttribute keyAttr = resourceElement.getAttribute(ATT_RESOURCE_KEY_QNAME);

            xsltResourecMap.put(locationAttr.getAttributeValue(), keyAttr.getAttributeValue());
        }
        this.resources = xsltResourecMap;

        if (attXslt != null) {
            //Use KeyFactory to create Key
            ValueFactory keyFactory = new ValueFactory();
            xsltKey = keyFactory.createValue(XMLConfigConstants.KEY, elem);
        } else {
            throw new MediatorException("The 'key' attribute is required for the FastXSLT mediator");
        }

        if (attSource != null) {
            try {
                source = SynapseXPathFactory.getSynapseXPath(elem, ATT_SOURCE);
            } catch (JaxenException e) {
                throw new MediatorException("Invalid XPath specified for the source attribute : " +
                        attSource.getAttributeValue());
            }
        }

        if (attTarget != null) {
            targetPropertyName = attTarget.getAttributeValue();
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
        // set the features
        for (Map.Entry<String, String> entry : collectNameValuePairs(elem, FEATURE_Q).entrySet()) {
            String value = entry.getValue();
            boolean isFeatureEnabled;
            if ("true".equals(value)) {
                isFeatureEnabled = true;
            } else if ("false".equals(value)) {
                isFeatureEnabled = false;
            } else {
                throw new MediatorException("The feature must have value true or false");
            }
            addFeature(entry.getKey(), isFeatureEnabled);
        }
        for (Map.Entry<String, String> entry : collectNameValuePairs(elem, ATTRIBUTE_Q).entrySet()) {
            addAttribute(entry.getKey(), entry.getValue());
        }
        properties.addAll(getMediatorProperties(elem));
    }

    public void addFeature(String featureName, boolean isFeatureEnable) {
        MediatorProperty mp = new MediatorProperty();
        mp.setName(featureName);
        if (isFeatureEnable) {
            mp.setValue("true");
        } else {
            mp.setValue("false");
        }
        transformerFactoryFeatures.add(mp);
    }

    public void addAttribute(String name, String value) {
        MediatorProperty mp = new MediatorProperty();
        mp.setName(name);
        mp.setValue(value);
        transformerFactoryAttributes.add(mp);
    }

    public List<MediatorProperty> getFeatures() {
        return transformerFactoryFeatures;
    }

    /**
     * @return Return the attributes explicitly set to the TransformerFactory through this mediator.
     */
    public List<MediatorProperty> getAttributes() {
        return transformerFactoryAttributes;
    }

    public void addAllProperties(List<MediatorProperty> list) {
        properties.addAll(list);
    }

    public List<MediatorProperty> getProperties() {
        return properties;
    }

    public String getTargetPropertyName() {
        return targetPropertyName;
    }

    public void setTargetPropertyName(String targetPropertyName) {
        this.targetPropertyName = targetPropertyName;
    }

    public SynapseXPath getSource() {
        return source;
    }

    public void setSource(SynapseXPath source) {
        this.source = source;
    }

    public Value getXsltKey() {
        return xsltKey;
    }

    public void setXsltKey(Value xsltKey) {
        this.xsltKey = xsltKey;
    }

    public void addProperty(MediatorProperty p) {
        properties.add(p);
    }

    public void setResources(Map<String, String> resources) {
        this.resources = resources;
    }

    public Map<String, String> getResources() {
        return resources;
    }
}
