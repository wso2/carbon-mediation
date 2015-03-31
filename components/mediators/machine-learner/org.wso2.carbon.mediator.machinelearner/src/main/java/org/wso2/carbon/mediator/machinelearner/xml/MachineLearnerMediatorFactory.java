/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediator.machinelearner.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.SynapsePathFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.machinelearner.MachineLearnerMediator;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

import static org.wso2.carbon.mediator.machinelearner.MachineLearnerMediatorConstants.*;

public class MachineLearnerMediatorFactory extends AbstractMediatorFactory {

    private static final Log log = LogFactory.getLog(MachineLearnerMediatorFactory.class);

    @Override
    protected Mediator createSpecificMediator(OMElement omElement, Properties properties) {

        MachineLearnerMediator machineLearnerMediator = new MachineLearnerMediator();
        configureMediator(omElement, machineLearnerMediator);
        return machineLearnerMediator;
    }

    /**
     * Configure the ML mediator
     * @param omElement  mediator xml configuration <machineLearner></machineLeaner>
     * @param machineLearnerMediator MLMediator to be configured
     */
    private void configureMediator(OMElement omElement, MachineLearnerMediator machineLearnerMediator) {

        // Configure model
        OMElement modelElement = omElement.getFirstChildWithName(MODEL_QNAME);
        if(modelElement != null) {
            String modelName = getAttributeValue(modelElement, NAME_ATT);
            machineLearnerMediator.setModelName(modelName);
        } else {
            handleException("Model element not defined.");

        }

        // Configure features
        OMElement featuresElement = omElement.getFirstChildWithName(FEATURES_QNAME);
        if(featuresElement != null) {
            Iterator featuresItr = featuresElement.getChildrenWithName(FEATURE_QNAME);
            while (featuresItr.hasNext()) {
                OMElement featureElement = (OMElement) featuresItr.next();
                String expression = getAttributeValue(featureElement, EXPRESSION_ATT);
                String featureName = getAttributeValue(featureElement, NAME_ATT);

                // TODO json path support
                if (expression != null || featureName != null) {
                    SynapsePath synapsePath = null;
                    if (expression != null) {
                        try {
                            synapsePath = SynapsePathFactory.getSynapsePath(featureElement, EXPRESSION_ATT);
                        } catch (JaxenException e) {
                            handleException("Invalid Synapse Path specified for feature expression attribute : " +
                                    expression, e);
                        }
                    }
                    machineLearnerMediator.addFeatureMapping(featureName, synapsePath);
                }
            }
        } else {
            handleException("Features element not defined.");
        }

        // Configure prediction
        OMElement predictionElement = omElement.getFirstChildWithName(PREDICTION_QNAME);
        if(predictionElement != null) {
            String predictionPropertyName = getAttributeValue(predictionElement, PROPERTY_ATT);
            if (predictionPropertyName != null) {
                machineLearnerMediator.setResultPropertyName(predictionPropertyName);
            } else {
                handleException("Prediction property element not defined.");
            }
        } else {
            handleException("Prediction element not defined.");
        }
    }

    /**
     * Get the attribute value from the OMAttribute
     * @param element OMElement containing the OMAttribute
     * @param qName Qname of the Attribute
     * @return
     */
    private String getAttributeValue(OMElement element, QName qName) {
        OMAttribute a = element.getAttribute(qName);
        if (a != null) {
            return a.getAttributeValue();
        }
        return null;
    }

    @Override
    public QName getTagQName() {
        return ML_QNAME;
    }
}
