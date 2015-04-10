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

package org.wso2.carbon.mediator.predict;

import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;
import javax.xml.namespace.QName;

public class PredictMediatorConstants {

    public static final QName PREDICT_QNAME = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "predict");

    public static final QName MODEL_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "model");
    public static final QName FEATURES_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "features");
    public static final QName FEATURE_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "feature");
    public static final QName PREDICTION_OUTPUT_QNAME = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "predictionOutput");

    public static final QName NAME_ATT = new QName("name");
    public static final QName EXPRESSION_ATT = new QName("expression");
    public static final QName PROPERTY_ATT = new QName("property");
}
