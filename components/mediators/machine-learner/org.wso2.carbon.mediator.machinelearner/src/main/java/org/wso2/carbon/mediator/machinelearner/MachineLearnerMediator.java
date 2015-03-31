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
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.machinelearner.util.ModelHandler;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.HashMap;
import java.util.Map;

public class MachineLearnerMediator extends AbstractMediator {

    private String resultPropertyName;
    private Map<String, SynapsePath> inputVariables;
    private String modelName;
    private ModelHandler modelHandler;

    public MachineLearnerMediator() {
        inputVariables = new HashMap<String, SynapsePath>();
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
        String prediction = getPredictionFromModel(messageContext);
        messageContext.setProperty(resultPropertyName, prediction);

        synLog.traceOrDebug("End : machineLearner mediator");
        return true;
    }

    /**
     * Extract the relevant element values from the messageContext
     * Map the actual parameter values with the model variables
     * @param messageContext
     * @return
     */
    private String getPredictionFromModel(MessageContext messageContext) {

        try {
            String prediction = ModelHandler.getInstance(modelName, inputVariables).getPrediction(messageContext);
            return prediction;
        } catch (JaxenException e) {
            handleException("Error while extracting feature values ", e, messageContext);
        } catch (MLModelHandlerException e) {
            handleException("Error while predicting value from the model ", e, messageContext);
        } catch (MLModelBuilderException e) {
            handleException("Error while building the Model ", e, messageContext);
        } catch (DatabaseHandlerException e) {
            handleException("Error while predicting value from the model", e, messageContext);
        } catch (UserStoreException e) {
            handleException("Error while building the model", e, messageContext);
        }
        return null;
    }

    public void setResultPropertyName(String propertyName) {
        this.resultPropertyName = propertyName;
    }

    public void addFeatureMapping(String variableName, SynapsePath synapsePath) {
        inputVariables.put(variableName, synapsePath);
    }

    public String getResultPropertyName() {
        return resultPropertyName;
    }

    public Map<String, SynapsePath> getInputVariables() {
        return inputVariables;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }
}
