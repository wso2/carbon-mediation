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

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfigUtils;
import org.wso2.carbon.mediator.machinelearner.MachineLearnerMediator;

import java.util.Properties;

public class MachineLearnerMediatorTest extends TestCase{

    private String xml =
            "<machineLearner xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
            "        <model name=\"lrboston-model\"/>\n" +
            "        <features>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"fixed acidity\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:fixed-acidity\"/>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"volatile acidity\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:volatile-acidity\"/>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"citric acid\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:citric-acid\"/>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"residual sugar\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:residual-sugar\"/>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"chlorides\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:chlorides\"/>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"free sulfur dioxide\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:free-sulfur-dioxide\"/>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"total sulfur dioxide\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:total-sulfur-dioxide\"/>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"density\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:density\"/>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"pH\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:pH\"/>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"sulphates\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:sulphates\"/>\n" +
            "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
            "                     name=\"alcohol\"\n" +
            "                     expression=\"$body/ns:getWineQuality/ns:features/ns:alcohol\"/>\n" +
            "        </features>\n" +
            "        <prediction property=\"result\"/>\n" +
            "    </machineLearner>";


    public void testMachineLearnerMediatorFactory() {

        OMElement mediatorElement = SynapseConfigUtils.stringToOM(xml);
        MachineLearnerMediatorFactory factory = new MachineLearnerMediatorFactory();
        MachineLearnerMediator mediator =
                (MachineLearnerMediator) factory.createSpecificMediator(mediatorElement, new Properties());

        assertEquals(mediator.getModelName(), "lrboston-model");

        assertEquals(mediator.getFeatureMappings().get("fixed acidity").getExpression(), "$body/ns:getWineQuality/ns:features/ns:fixed-acidity");
        assertEquals(mediator.getFeatureMappings().get("fixed acidity").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("volatile acidity").getExpression(), "$body/ns:getWineQuality/ns:features/ns:volatile-acidity");
        assertEquals(mediator.getFeatureMappings().get("volatile acidity").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("citric acid").getExpression(), "$body/ns:getWineQuality/ns:features/ns:citric-acid");
        assertEquals(mediator.getFeatureMappings().get("citric acid").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("residual sugar").getExpression(), "$body/ns:getWineQuality/ns:features/ns:residual-sugar");
        assertEquals(mediator.getFeatureMappings().get("residual sugar").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("chlorides").getExpression(), "$body/ns:getWineQuality/ns:features/ns:chlorides");
        assertEquals(mediator.getFeatureMappings().get("chlorides").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("free sulfur dioxide").getExpression(), "$body/ns:getWineQuality/ns:features/ns:free-sulfur-dioxide");
        assertEquals(mediator.getFeatureMappings().get("free sulfur dioxide").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("total sulfur dioxide").getExpression(), "$body/ns:getWineQuality/ns:features/ns:total-sulfur-dioxide");
        assertEquals(mediator.getFeatureMappings().get("total sulfur dioxide").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("density").getExpression(), "$body/ns:getWineQuality/ns:features/ns:density");
        assertEquals(mediator.getFeatureMappings().get("density").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("pH").getExpression(), "$body/ns:getWineQuality/ns:features/ns:pH");
        assertEquals(mediator.getFeatureMappings().get("pH").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("sulphates").getExpression(), "$body/ns:getWineQuality/ns:features/ns:sulphates");
        assertEquals(mediator.getFeatureMappings().get("sulphates").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("alcohol").getExpression(), "$body/ns:getWineQuality/ns:features/ns:alcohol");
        assertEquals(mediator.getFeatureMappings().get("alcohol").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getResultPropertyName(), "result");
    }
}
