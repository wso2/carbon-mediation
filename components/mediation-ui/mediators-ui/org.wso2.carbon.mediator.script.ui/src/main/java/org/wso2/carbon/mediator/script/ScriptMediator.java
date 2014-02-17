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
package org.wso2.carbon.mediator.script;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.synapse.config.xml.ValueFactory;
import org.apache.synapse.config.xml.ValueSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.Value;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class ScriptMediator extends AbstractMediator {
    private static final QName INCLUDE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "include");
    /**
     * The registry entry key for a script loaded from the registry
     */
    private Value key;
    /**
     * The language of the script code
     */
    private String language;
    /**
     * The map of included scripts; key = registry entry key, value = script source
     */
    private Map<String, Object> includes = new TreeMap<String, Object>();
    /**
     * The optional name of the function to be invoked, defaults to mediate
     */
    private String function = "";
    /**
     * The source code of the script
     */
    private String scriptSourceCode;

    public String getLanguage() {
        return language;
    }

    public Value getKey() {
        return key;
    }

    public String getFunction() {
        return function;
    }

    public String getScriptSrc() {
        return scriptSourceCode;
    }
    
    public String getTagLocalName() {
        return "script";
    }

    public String getScriptSourceCode() {
        return scriptSourceCode;
    }

    public Map<String, Object> getIncludes() {
        return includes;
    }

    public void setIncludes(Map<String, Object> includes) {
        this.includes = includes;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public void setScriptSourceCode(String scriptSourceCode) {
        this.scriptSourceCode = scriptSourceCode;
    }

    public void setKey(Value key) {
        this.key = key;
    }    

    public OMElement serialize(OMElement parent) {
        OMElement script = fac.createOMElement("script", synNS);

        if (key != null) {
            script.addAttribute(fac.createOMAttribute("language", nullNS, language));

            // Use KeySerializer to serialize Key
            ValueSerializer keySerializer = new ValueSerializer();
            keySerializer.serializeValue(key, XMLConfigConstants.KEY, script);

            if (!function.equals("mediate")) {
                script.addAttribute(fac.createOMAttribute("function", nullNS, function));
            }
        } else {
            script.addAttribute(fac.createOMAttribute("language", nullNS, language));
            OMTextImpl textData = (OMTextImpl) fac.createOMText(
                    scriptSourceCode.trim());
            textData.setType(XMLStreamConstants.CDATA);
            script.addChild(textData);
        }

        Map<String, Object> includeMap = includes;
        for (String includeKey : includeMap.keySet()) {
            if (includeKey != null && includeKey.length() != 0) {
                OMElement includeKeyElement = fac.createOMElement("include", synNS);
                includeKeyElement.addAttribute(fac.createOMAttribute("key", nullNS, includeKey));
                script.addChild(includeKeyElement);
            }
        }

        saveTracingState(script, this);
        if (parent != null) {
            parent.addChild(script);
        }
        return script;
    }

    public void build(OMElement elem) {
        includes.clear();
        OMAttribute keyAtt = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                "key"));
        OMAttribute langAtt = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                "language"));
        OMAttribute funcAtt = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                "function"));

        if (langAtt == null) {
            throw new MediatorException("The 'language' attribute is required for" +
                    " a script mediator");            
        }
        if (keyAtt == null && funcAtt != null) {
            throw new MediatorException("Cannot use 'function' attribute without 'key' " +
                    "attribute for a script mediator");
        }

        getIncludeKeysMap(elem);

        if (keyAtt != null) {
            String functionName = (funcAtt == null ? null : funcAtt.getAttributeValue());
            this.language = langAtt.getAttributeValue();
            this.function = functionName;

            //Use KeyFactory to create Key
            ValueFactory keyFactory = new ValueFactory();
            key = keyFactory.createValue(XMLConfigConstants.KEY, elem);
            
        } else {
            this.language = langAtt.getAttributeValue();
            this.scriptSourceCode = elem.getText();
        }

        processAuditStatus(this, elem);
    }

    private void getIncludeKeysMap(OMElement elem) {
        // get <include /> scripts
        // map key = registry entry key, value = script source
        // at this time map values are null, later loaded
        // from void ScriptMediator.prepareExternalScript(MessageContext synCtx)

        // TreeMap used to keep given scripts order if needed
        Iterator iter = elem.getChildrenWithName(INCLUDE_Q);
        while (iter.hasNext()) {
            OMElement includeElem = (OMElement) iter.next();
            OMAttribute key = includeElem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                    "key"));

            if (key == null) {
                throw new MediatorException("Cannot use 'include' element without 'key'" +
                        " attribute for a script mediator");
            }

            String keyText = key.getAttributeValue();
            includes.put(keyText, null);
        }
    }
}
