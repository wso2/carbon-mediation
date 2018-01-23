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
package org.wso2.carbon.mediator.validate;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.*;
import org.apache.synapse.mediators.Value;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractListMediator;
import org.wso2.carbon.mediator.service.util.MediatorProperty;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class ValidateMediator extends AbstractListMediator {
    private static final QName ON_FAIL_Q  = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "on-fail");
    private static final QName SCHEMA_Q   = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "schema");
    private static final QName ATT_CACHE_SCHEMA = new QName("cache-schema");

    private List<Value> schemaKeys = new ArrayList<Value>();
    private SynapsePath source = null;
    private final List<MediatorProperty> explicityFeatures = new ArrayList<MediatorProperty>();
    private boolean cacheSchema = true;
    
	Map<String, String> resources = new HashMap<String, String>();
	
    public String getTagLocalName() {
        return "validate";
    }

    public OMElement serialize(OMElement parent) {
        OMElement validate = fac.createOMElement("validate", synNS);
        saveTracingState(validate, this);

        if (source != null) {
            SynapsePathSerializer.serializePath(source, validate, "source");
        }

        for (Value key : schemaKeys) {
            OMElement schema = fac.createOMElement("schema", synNS, validate);
            // Use keySerializer to serialize Key
            ValueSerializer keySerializer = new ValueSerializer();
            keySerializer.serializeValue(key, XMLConfigConstants.KEY, schema);
        }

        if (!explicityFeatures.isEmpty()) {
            for (MediatorProperty mp : explicityFeatures) {
                OMElement feature = fac.createOMElement("feature", synNS, validate);
                if (mp.getName() != null) {
                    feature.addAttribute(fac.createOMAttribute("name", nullNS, mp.getName()));
                } else {
                    throw new MediatorException("The Feature name is missing");
                }
                if (mp.getValue() != null) {
                    feature.addAttribute(fac.createOMAttribute("value", nullNS, mp.getValue()));
                } else {
                    throw new MediatorException("The Feature value is missing");
                }
            }
        }
		
		if (resources.size() > 0) {
			OMElement resource;
			Set resourceKeys = resources.keySet();

			for (Iterator i = resourceKeys.iterator(); i.hasNext();) {
				String key = i.next().toString();
				String value = resources.get(key);
				resource = fac.createOMElement("resource", synNS);
				resource.addAttribute("location", key, nullNS);
				resource.addAttribute("key", value, nullNS);
				validate.addChild(resource);
			}
		}
		
        OMElement onFail = fac.createOMElement("on-fail", synNS, validate);
        if (getList().isEmpty()) {
            throw new MediatorException("No 'Fault' mediator found within the on-fail section of the 'Validate' mediator.");
        }
        serializeChildren(onFail, getList());

        if (!isCacheSchema()) {
            OMAttribute cacheSchemaAtt = fac.createOMAttribute("cache-schema", nullNS, String.valueOf(isCacheSchema()));
            validate.addAttribute(cacheSchemaAtt);
        }

        if (parent != null) {
            parent.addChild(validate);
        }
        return validate;
    }

    public void build(OMElement elem) {
        List<Value> schemaKeys = new ArrayList<Value>();
        Iterator schemas = elem.getChildrenWithName(SCHEMA_Q);

		QName ATT_RESOURCE_QNAME = new QName("resource");
        
        Iterator itr = elem.getChildrenWithName(ATT_RESOURCE_QNAME);
		Map<String, String> validateResourecMap = new HashMap<String, String>();
		
		while (itr.hasNext()) {
			OMElement resourceElement = (OMElement) itr.next();
			QName ATT_RESOURCE_LOCATION_QNAME = new QName("location");
			QName ATT_RESOURCE_KEY_QNAME = new QName("key");

			OMAttribute locationAttr = resourceElement.getAttribute(ATT_RESOURCE_LOCATION_QNAME);
			OMAttribute keyAttr = resourceElement.getAttribute(ATT_RESOURCE_KEY_QNAME);
			
			validateResourecMap.put(locationAttr.getAttributeValue(), keyAttr.getAttributeValue());
		}       
		this.resources = validateResourecMap;
		
        while (schemas.hasNext()) {
            Object o = schemas.next();
            if (o instanceof OMElement) {
                OMElement omElem = (OMElement) o;
                OMAttribute keyAtt = omElem.getAttribute(ATT_KEY);
                if (keyAtt != null) {
                    //Use KeyFactory to create Key
                    ValueFactory keyFactory = new ValueFactory();
                    Value key = keyFactory.createValue(XMLConfigConstants.KEY, omElem);
                    schemaKeys.add(key);

                } else {
                    throw new MediatorException("A 'schema' definition must contain a local property 'key'");
                }
            } else {
                throw new MediatorException("Invalid 'schema' declaration for validate mediator");
            }
        }

        if (schemaKeys.size() == 0) {
            throw new MediatorException("No schemas specified for the validate mediator");
        } else {
            this.schemaKeys = schemaKeys;
        }

        // process source XPath attribute if present
        OMAttribute attSource = elem.getAttribute(ATT_SOURCE);

        if (attSource != null) {
            try {
                source = SynapsePathFactory.getSynapsePath(elem, ATT_SOURCE);
            } catch (JaxenException e) {
                throw new MediatorException("Invalid XPath expression specified for attribute 'source'");
            }
        }

        // process schema cacheability.
        OMAttribute attSchemaCache = elem.getAttribute(ATT_CACHE_SCHEMA);
        if (attSchemaCache != null) {
            this.setCacheSchema(Boolean.parseBoolean(attSchemaCache.getAttributeValue()));
        }

        // process on-fail
        OMElement onFail = null;
        Iterator iterator = elem.getChildrenWithName(ON_FAIL_Q);
        if (iterator.hasNext()) {
            onFail = (OMElement) iterator.next();
        }

        if (onFail != null && onFail.getChildElements().hasNext()) {
            addChildren(onFail, this);
        } else {
            throw new MediatorException("A non-empty <on-fail> child element is required for " +
                    "the <validate> mediator");
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
    }

    public Object getFeature(String key) {
        for (MediatorProperty prop : explicityFeatures) {
            if (key.equals(prop.getName())) {
                return prop.getValue();
            }
        }
        return null;
    }

   public void addFeature(String featureName, boolean isFeatureEnable) {
        MediatorProperty mp = new MediatorProperty();
        mp.setName(featureName);
        if (isFeatureEnable) {
            mp.setValue("true");
        } else {
            mp.setValue("false");
        }
        explicityFeatures.add(mp);
    }

    public void setSchemaKeys(List<Value> schemaKeys) {
        this.schemaKeys = schemaKeys;
    }

    public void setSource(SynapsePath source) {
       this.source = source;
    }

    public SynapsePath getSource() {
        return source;
    }

    public List<Value> getSchemaKeys() {
        return schemaKeys;
    }

    public List<MediatorProperty> getFeatures() {
        return explicityFeatures;
    }
	
	public void setResources(Map<String,String> resources) {
	    this.resources = resources;
    }

	public Map<String,String> getResources() {
	    return resources;
    }

    public boolean isCacheSchema() {
        return cacheSchema;
    }

    public void setCacheSchema(boolean cacheSchema) {
        this.cacheSchema = cacheSchema;
    }
}
