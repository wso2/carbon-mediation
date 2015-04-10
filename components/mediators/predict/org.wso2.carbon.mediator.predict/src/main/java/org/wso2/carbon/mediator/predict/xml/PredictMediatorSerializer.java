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

package org.wso2.carbon.mediator.predict.xml;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.SynapsePathSerializer;
import org.wso2.carbon.mediator.predict.PredictMediator;

import java.util.Map;

import static org.wso2.carbon.mediator.predict.PredictMediatorConstants.*;

public class PredictMediatorSerializer extends AbstractMediatorSerializer {

    /**
     *  Build the ML mediator configuration from MLMediator instance
     *
     *  ML mediator configuration template
     *  <predict>
     *       <model name="string"/>
     *       <features>
     *           <feature name="string" expression="xpath|json-path"/>+
     *       </features>
     *       <predictionOutput property="string"/>
     *   </predict>
     *
     * @param mediator MLMediator instance
     * @return
     */
    @Override
    protected OMElement serializeSpecificMediator(Mediator mediator) {

        assert mediator instanceof PredictMediator : "predict mediator is expected";

        PredictMediator predictMediator = (PredictMediator) mediator;
        OMElement predictElement = fac.createOMElement(PREDICT_QNAME.getLocalPart(), synNS);

        // <model>
        OMElement modelConfiguration = fac.createOMElement(MODEL_QNAME.getLocalPart(), synNS);
        modelConfiguration.addAttribute(fac.createOMAttribute(NAME_ATT.getLocalPart(),
                nullNS, predictMediator.getModelName()));
        predictElement.addChild(modelConfiguration);

        // <features>
        OMElement features = fac.createOMElement(FEATURES_QNAME.getLocalPart(), synNS);
        // <feature>+
        for(Map.Entry<String, SynapsePath> entry : predictMediator.getFeatureMappings().entrySet()) {
            String featureName = entry.getKey();
            SynapsePath expression = entry.getValue();
            OMElement feature = fac.createOMElement(FEATURE_QNAME.getLocalPart(), synNS);
            feature.addAttribute(fac.createOMAttribute(NAME_ATT.getLocalPart(), nullNS, featureName));
            SynapsePathSerializer.serializePath(expression, feature, EXPRESSION_ATT.getLocalPart());
            features.addChild(feature);
        }
        predictElement.addChild(features);

        // <predictionOutput>
        OMElement prediction = fac.createOMElement(PREDICTION_OUTPUT_QNAME.getLocalPart(), synNS);
        prediction.addAttribute(fac.createOMAttribute(PROPERTY_ATT.getLocalPart(),
                nullNS, predictMediator.getResultPropertyName()));
        predictElement.addChild(prediction);
        return predictElement;
    }

    @Override
    public String getMediatorClassName() {
        return PredictMediator.class.getName();
    }
}
