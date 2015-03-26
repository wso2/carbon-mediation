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

package org.wso2.carbon.mediator.machinelearner;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.machinelearner.util.ModelHandler;
import org.wso2.carbon.mediator.machinelearner.util.ResponseBuilder;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

import java.util.HashMap;
import java.util.Map;

public class MachineLearnerMediator extends AbstractMediator {

    private SynapseXPath responseVariableXpath;
    private Map<String, SynapseXPath> inputVariables;
    private String modelName;
    private ModelHandler modelHandler;
    private ResponseBuilder responseBuilder;

    public MachineLearnerMediator() {
        inputVariables = new HashMap<String, SynapseXPath>();
        modelHandler = new ModelHandler();
        responseBuilder = new ResponseBuilder();
    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog synLog = getLog(messageContext);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : machineLearner mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }
        getPredctionFromModel(messageContext, synLog);

        synLog.traceOrDebug("End : machineLearner mediator");
        return true;
    }

    /**
     * Extract the relevant element values from the messageContext
     * Map the actual parameter values with the model variables
     * @param messageContext
     * @return
     */
    private void getPredctionFromModel(MessageContext messageContext, SynapseLog synLog) {

        try {
            String prediction = modelHandler.getPrediction(messageContext);
            responseBuilder.buildResponseElement(prediction, messageContext, responseVariableXpath, synLog);

        } catch (JaxenException e) {
            handleException("Invalid XPath specified for the response-variable attribute : "
                    + responseVariableXpath.getExpression(), e, messageContext);
        } catch (MLModelHandlerException e) {
            handleException("Error while predicting value from the Model ", e, messageContext);
        } catch (MLModelBuilderException e) {
            handleException("Error while building the Model ", e, messageContext);
        }
    }

    public void setResponseVariable(SynapseXPath xpath) {
        this.responseVariableXpath = xpath;
    }

    public void addInputVariable(String variableName, SynapseXPath xpath) {
        inputVariables.put(variableName, xpath);
    }

    public SynapseXPath getResponseVariableXpath() {
        return responseVariableXpath;
    }

    public Map<String, SynapseXPath> getInputVariables() {
        return inputVariables;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public void initializeModel() throws DatabaseHandlerException, MLModelHandlerException, MLModelBuilderException {
        modelHandler.initializeModel(modelName, inputVariables);
    }
}
