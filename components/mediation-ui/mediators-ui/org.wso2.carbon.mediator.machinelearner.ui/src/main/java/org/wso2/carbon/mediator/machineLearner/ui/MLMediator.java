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

package org.wso2.carbon.mediator.machineLearner.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.wso2.carbon.mediator.machineLearner.ui.MLMediatorConstants.*;


public class MLMediator extends AbstractMediator {

    private String modelName;
    private String predictionPropertyName;
    private Map<String, String> featureMappings = new HashMap<String, String>();

    @Override
    public OMElement serialize(OMElement parent) {

        OMElement mlElement = fac.createOMElement(ML_QNAME);
        saveTracingState(mlElement, this);

        if (modelName != null) {
            OMElement modelElement = fac.createOMElement(MODEL_QNAME);
            modelElement.addAttribute(fac.createOMAttribute(NAME_ATT.getLocalPart(), nullNS, modelName));
            mlElement.addChild(modelElement);
        } else {
            throw new MediatorException("Invalid ML mediator. Model name is required");
        }

        if(featureMappings.isEmpty()) {
            throw new MediatorException("Invalid ML mediator. Features required");
        }
        OMElement featuresElement = fac.createOMElement(FEATURES_QNAME);
        for(Map.Entry entry : featureMappings.entrySet()) {
            OMElement featureElement = fac.createOMElement(FEATURE_QNAME);
            featureElement.addAttribute(fac.createOMAttribute(NAME_ATT.getLocalPart(), nullNS, (String) entry.getKey()));
            featureElement.addAttribute(fac.createOMAttribute(EXPRESSION_ATT.getLocalPart(), nullNS, (String) entry.getValue()));
            featuresElement.addChild(featureElement);
        }
        mlElement.addChild(featuresElement);

        if(predictionPropertyName != null) {
            OMElement predictionElement = fac.createOMElement(PREDICTION_QNAME);
            predictionElement.addAttribute(fac.createOMAttribute(PROPERTY_ATT.getLocalPart(), nullNS, predictionPropertyName));
            mlElement.addChild(predictionElement);
        } else {
            throw new MediatorException("Invalid ML mediator. Prediction property name is required");
        }

        if(parent != null) {
            parent.addChild(mlElement);
        }
        return mlElement;
    }

    @Override
    public void build(OMElement omElement) {

        // model
        OMElement modelElement = omElement.getFirstChildWithName(MODEL_QNAME);
        if (modelElement == null) {
            throw new MediatorException("Model element is required.");
        }
        OMAttribute modelName = modelElement.getAttribute(NAME_ATT);
        if(modelName == null) {
            throw new MediatorException("Model name attribute is required.");
        }
        this.modelName = modelName.getAttributeValue();

        // features
        OMElement featuresElement = omElement.getFirstChildWithName(FEATURES_QNAME);
        if(featuresElement == null) {
            throw new MediatorException("Features element is required.");
        }

        // feature
        for (Iterator it = featuresElement.getChildrenWithName(FEATURE_QNAME); it.hasNext();) {
            OMElement featureElement = (OMElement) it.next();
            OMAttribute featureName = featureElement.getAttribute(NAME_ATT);
            if(featureName == null || featureName.getAttributeValue() == null
                    || "".equals(featureName.getAttributeValue())) {
                throw new MediatorException("Feature name is required.");
            }

            OMAttribute expression = featureElement.getAttribute(EXPRESSION_ATT);
            if (expression != null && expression.getAttributeValue() != null) {
                this.featureMappings.put(featureName.getAttributeValue(), expression.getAttributeValue());
            } else {
                throw new MediatorException("feature expression is required.");
            }

        }

        // prediction
        OMElement predictionElement = omElement.getFirstChildWithName(PREDICTION_QNAME);
        if (predictionElement == null) {
            throw new MediatorException("Prediction element is required.");
        }
        OMAttribute predictionExpression = predictionElement.getAttribute(PROPERTY_ATT);
        if(predictionExpression == null) {
            throw new MediatorException("Prediction property attribute is required.");
        }
        this.predictionPropertyName = predictionExpression.getAttributeValue();

        processAuditStatus(this, omElement);
    }

    @Override
    public String getTagLocalName() {
        return ML_TAG_LOCAL_NAME;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getPredictionPropertyName() {
        return predictionPropertyName;
    }

    public void setPredictionPropertyName(String response) {
        this.predictionPropertyName = response;
    }

    public void addFeatureMapping(String variableName, String expression) {
        this.featureMappings.put(variableName, expression);
    }

    public Map<String, String> getFeatureMappings() {
        return featureMappings;
    }
}
