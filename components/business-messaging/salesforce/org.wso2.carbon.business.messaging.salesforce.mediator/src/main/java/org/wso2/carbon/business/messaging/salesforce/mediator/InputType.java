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
package org.wso2.carbon.business.messaging.salesforce.mediator;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.business.messaging.salesforce.mediator.handler.PropertyHandler;

import java.lang.reflect.Array;
import java.util.*;

/**
 * This class stores input parameter type information
 * handles input parameter processing
 */
public class InputType extends Type {

    /**
     * <p>
     * Holds the log4j based log for the login purposes
     * </p>
     */
    private static final Log log = LogFactory.getLog(InputType.class);

    /**
     * <p>
     * Specifies the source value of the input parameter.
     * </p>
     */
    private String sourceValue;

    /**
     * Specified whether this property represents a collection.
     */
    private boolean isCollection;
    /**
     * Specified whether this property represents a complex input type.
     */
    private boolean isComplex;

    /**
     * Holds the inputs of a collection. valid if the isCollection field is
     * true.
     */
    private List<InputType> collectionInputs = new ArrayList<InputType>();
    /**
     * <p>
     * XPath describing the element or the attribute of the message which will
     * be matched against the <code>source-xpath</code> to check the matching.
     * If there is no <code>source-xpath</code> then the presence of this
     * expression will be taken as the matching
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath sourceXPath;


    /**
     * <p>
     * If both the <code>sourceXpath</code> and <code>type</code>='string' is
     * provided then the evaluated string value of the <code>xpath</code> over
     * the message will be returned.
     * </p>
     * <p/>
     * <p>
     * If both the <code>sourceXpath</code> and <code>type</code>='xml' is
     * provided then the evaluated xml value of the <code>xpath</code> over the
     * message will be returned.
     * </p>
     * <p/>
     * <p>
     * If the <code>value</code> is provided then then that string value will be
     * returned.
     * </p>
     *
     * @param synCtx message to be evaluated.
     * @return the evaluated value from the <code>MessageContext</code>
     */

    public Object evaluate(MessageContext synCtx) {
        return evaluate(synCtx, false);
    }
/*
    public Object evaluate(MessageContext synCtx) {

        Object sourceObjectValue = null;

        // expression is required to perform the match
        if (null != sourceXPath) {
            sourceObjectValue = sourceXPath.stringValueOf(synCtx);
            if (null == sourceObjectValue) {
                log.debug(String.format("Source String : %s evaluates to null",
                                        sourceXPath.toString()));
            }
        } else if (null != sourceValue) {
            sourceObjectValue = sourceValue;
        } else if (isCollection) {
            Object newInstance = PropertyHandler.newInstance(type);
            List<Object> collection = new ArrayList<Object>();
            for (InputType collectionInput : collectionInputs) {
                collection.add(collectionInput.evaluate(synCtx));
            }

            sourceObjectValue = collection;
        }
        return sourceObjectValue;
    }
*/


    public Object evaluate(MessageContext synCtx, boolean parseCollection) {

        Object sourceObjectValue = null;

        // expression is required to perform the match
        if (isCollection) {
            CollectionHolder holder = new CollectionHolder();
            for (InputType collectionInput : collectionInputs) {
                Object result = collectionInput.evaluate(synCtx, true);
                if (result instanceof List) {
                    List selectedElementValues = (List) result;
                    for (int i = 0; i < selectedElementValues.size(); i++) {
                        holder.addInputForCollection(collectionInput.getName(),
                                                     selectedElementValues.get(i));
                    }
                } else if (result instanceof String) {
                    holder.addInputForCollection(collectionInput.getName(),
                                                 result);

                }
            }

            sourceObjectValue = holder.getArrayInstance();
        } else if (isComplex) {
            //TODO implement this
        } else if (sourceXPath != null) {
            if (!parseCollection) {
                sourceObjectValue = sourceXPath.stringValueOf(synCtx);
            } else {
                sourceObjectValue = evaluateXPathNodes(sourceXPath, synCtx);
            }
            if (sourceObjectValue == null) {
                log.debug(String.format("Source String : %s evaluates to null",
                                        sourceXPath.toString()));
            }
        } else if (sourceValue != null) {
            sourceObjectValue = sourceValue;
        }
        return sourceObjectValue;
    }

    private List evaluateXPathNodes(SynapseXPath sourceXPath, MessageContext synCtx) {
        List<String> selectedElementValues = new ArrayList<String>();
        try {
            List matchingNodes = sourceXPath.selectNodes(synCtx);
            Iterator nodes = matchingNodes.iterator();
            while (nodes.hasNext()) {
                try {
                    OMElement node = (OMElement) nodes.next();
                    selectedElementValues.add(node.getText());
                } catch (ClassCastException e) {
                    log.debug(String.format("Source String : %s selected an OMnode that is not OMElement." +
                                            " Message should define parameters inside Elements ",
                                            sourceXPath.toString()));
                }
            }
        } catch (JaxenException e) {
        }
        return selectedElementValues;  //To change body of created methods use File | Settings | File Templates.
    }


    /**
     * @return the expression
     */
    public SynapseXPath getSourceXPath() {
        return sourceXPath;
    }

    /**
     * @param xpath the expression to set
     */
    public void setSourceXPath(SynapseXPath xpath) {
        this.sourceXPath = xpath;
    }

    /**
     * @return the source value
     */
    public String getSourceValue() {
        return sourceValue;
    }

    /**
     * @param value the source value to set
     */
    public void setSourceValue(String value) {
        this.sourceValue = value;
    }

    /**
     * @return the isCollection
     */
    public boolean isCollection() {
        return isCollection;
    }

    /**
     * @param isCollection the isCollection to set
     */
    public void setCollection(boolean isCollection) {
        this.isCollection = isCollection;
    }

    /**
     * @return the collectionInputs
     */
    public List<InputType> getCollectionInputs() {
        return collectionInputs;
    }

    /**
     * @param collectionInputs the collectionInputs to set
     */
    public void setCollectionInputs(List<InputType> collectionInputs) {
        this.collectionInputs = collectionInputs;
    }

    /**
     * @return whether input parameter is a complex type
     */
    public boolean isComplex() {
        return isComplex;
    }

    /**
     * @param complex set type is complex or not
     */
    public void setComplex(boolean complex) {
        isComplex = complex;
    }

    class CollectionHolder {
        private HashMap<String, List> collectionType2InputParmsMap = new HashMap<String, List>();
        private HashMap<String, Integer> collectionType2InputSizeMap = new HashMap<String, Integer>();
        int max = 0;

        public void addInputForCollection(String cName, Object value) {
            createListForCollection(cName);
            List collection = collectionType2InputParmsMap.get(cName);
            collectionType2InputSizeMap.put(cName, collectionType2InputSizeMap.get(cName) == null ?
                                                   new Integer(0) : collectionType2InputSizeMap.get(cName) + 1);
            collection.add(value);
        }

        private int getMaxInputSize() {
            Set<String> keySet = collectionType2InputSizeMap.keySet();
            Iterator<String> keys = keySet.iterator();

            while (keys.hasNext()) {
                String key = keys.next();
                Integer temp = collectionType2InputSizeMap.get(key);
                if (temp > max) {
                    max = temp;
                }
            }
            return max;
        }

        public Object getArrayInstance() {
            Object newComponent = PropertyHandler.newInstance(type);
            Object componentArray = Array.newInstance(newComponent.getClass(), getMaxInputSize());
            for (int index = 0; index < max; index++) {
                Set<String> keySet = collectionType2InputParmsMap.keySet();
                Iterator<String> collection = keySet.iterator();
                Object newInstance = PropertyHandler.newInstance(type);
                while (collection.hasNext()) {
                    String collectionName = collection.next();
                    Object value = collectionType2InputParmsMap.get(collectionName).get(index);
                    if (value != null) {
                        PropertyHandler.setInstanceProperty(collectionName, value, newInstance);
                    }
                }
                Array.set(componentArray, index, newInstance);
            }

            return componentArray;
        }

        private void createListForCollection(String cName) {
            if (collectionType2InputParmsMap.get(cName) == null) {
                collectionType2InputParmsMap.put(cName, new ArrayList());
            }
        }
    }
}
