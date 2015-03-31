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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.mediator.machinelearner.MachineLearnerMediator;

import java.util.Map;

import static org.wso2.carbon.mediator.machinelearner.MachineLearnerMediatorConstants.*;

public class MachineLearnerMediatorSerializer extends AbstractMediatorSerializer {

    /**
     *  Build the ML mediator configuration from MLMediator instance
     *
     *  ML mediator configuration template
     *  <machineLearner>
     *       <model name="string"/>
     *       <features>
     *           <feature name="string" expression="xpath"/>+
     *       </features>
     *       <prediction expression="xpath"/>
     *   </machineLearner>
     *
     * @param mediator MLMediator instance
     * @return
     */
    @Override
    protected OMElement serializeSpecificMediator(Mediator mediator) {

        assert mediator instanceof MachineLearnerMediator : "machineLearner mediator is expected";

        MachineLearnerMediator machineLearnerMediator = (MachineLearnerMediator) mediator;
        OMElement machineLearner = fac.createOMElement(ML_QNAME);

        // <model>
        OMElement modelConfiguration = fac.createOMElement(MODEL_QNAME);
        modelConfiguration.addAttribute(fac.createOMAttribute(NAME_ATT.getLocalPart(),
                nullNS, machineLearnerMediator.getModelName()));
        machineLearner.addChild(modelConfiguration);

        // <features>
        OMElement features = fac.createOMElement(FEATURES_QNAME);
        // <feature>+
        for(Map.Entry<String, SynapsePath> entry : machineLearnerMediator.getInputVariables().entrySet()) {
            String featureName = entry.getKey();
            SynapsePath expression = entry.getValue();
            OMElement feature = fac.createOMElement(FEATURE_QNAME);
            feature.addAttribute(fac.createOMAttribute(NAME_ATT.getLocalPart(), nullNS, featureName));
            SynapseXPathSerializer.serializeXPath((SynapseXPath) expression, feature, EXPRESSION_ATT.getLocalPart());
            features.addChild(feature);
        }
        machineLearner.addChild(features);

        // <prediction>
        OMElement prediction = fac.createOMElement(PREDICTION_QNAME);
        prediction.addAttribute(fac.createOMAttribute(PROPERTY_ATT.getLocalPart(),
                nullNS, machineLearnerMediator.getResultPropertyName()));
        machineLearner.addChild(prediction);
        return machineLearner;
    }

    @Override
    public String getMediatorClassName() {
        return MachineLearnerMediator.class.getName();
    }
}
