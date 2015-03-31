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

package org.wso2.carbon.mediator.machinelearner.util;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.SynapsePath;
import org.jaxen.JaxenException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.MLModelNew;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.impl.MLModelHandler;
import org.wso2.carbon.ml.core.impl.Predictor;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelHandler {

    private static ModelHandler instance;

    private String modelName;
    private Map<SynapsePath, Integer> featureIndexMap;
    private MLModel mlModel;
    private MLModelConfigurationContext context;
    private long modelId;

    private ModelHandler(String modelName, Map<String, SynapsePath> featureMappings)
            throws MLModelHandlerException, DatabaseHandlerException, MLModelBuilderException, UserStoreException {
        initializeModel(modelName, featureMappings);
    }

    /**
     * Get the ModelHandler instance
     * @param modelName         name of the ML-model
     * @param featureMappings   Map containing pairs <feature-name, synapse-path>
     * @return
     * @throws DatabaseHandlerException
     * @throws MLModelHandlerException
     * @throws MLModelBuilderException
     */
    public static ModelHandler getInstance(String modelName, Map<String, SynapsePath> featureMappings)
            throws DatabaseHandlerException, MLModelHandlerException, MLModelBuilderException, UserStoreException {
        if(instance == null) {
            instance = new ModelHandler(modelName, featureMappings);
        }
        return instance;
    }

    /**
     * Set the model name
     * @param modelName model name
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Retrieve the ML model and map the feature indices with the xpath expressions
     * @param inputVariables Map containing the key- value pairs <feature-name, xpath-expression-to-extract-feature-value>
     */
    private void initializeModel(String modelName, Map<String, SynapsePath> inputVariables)
            throws DatabaseHandlerException, MLModelHandlerException, MLModelBuilderException, UserStoreException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().
                getRealmConfiguration().getAdminUserName();
        MLModelHandler mlModelHandler = new MLModelHandler();
        MLModelNew mlModelNew = mlModelHandler.getModel(tenantId, userName, modelName);
        modelId = mlModelNew.getId();
        mlModel = mlModelHandler.retrieveModel(modelId);

        featureIndexMap = new HashMap<SynapsePath, Integer>();
        List<Feature> features = mlModel.getFeatures();
        for(Feature feature : features) {
            if(inputVariables.get(feature.getName()) != null) {
                featureIndexMap.put(inputVariables.get(feature.getName()), feature.getIndex());
            }
        }
        createModelConfigurationContext();
    }

    /**
     * Create the spark context and model configuration context
     * @throws DatabaseHandlerException
     */
    private void createModelConfigurationContext() throws DatabaseHandlerException, UserStoreException {

        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        DatabaseService databaseService = valueHolder.getDatabaseService();

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().
                getRealmConfiguration().getAdminUserName();

        Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
        MLModelNew model = databaseService.getModel(tenantId, userName, modelId);
        String dataType = databaseService.getDataTypeOfModel(modelId);
        String columnSeparator = MLUtils.ColumnSeparatorFactory.getColumnSeparator(dataType);
        SparkConf sparkConf = MLCoreServiceValueHolder.getInstance().getSparkConf();
        Workflow facts = databaseService.getWorkflow(model.getAnalysisId());

        context = new MLModelConfigurationContext();
        context.setModelId(modelId);
        context.setColumnSeparator(columnSeparator);
        context.setFacts(facts);

        sparkConf.setAppName(String.valueOf(modelId));
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        context.setSparkContext(sparkContext);
    }

    /**
     * Get the predicted value for the given input features using the ML-Model
     * @param messageContext    the incoming message context
     * @return                  the predicted value as String
     */
    public String getPrediction(MessageContext messageContext) throws MLModelBuilderException, JaxenException,
            MLModelHandlerException {

        String data[] = new String[featureIndexMap.size()];
        for(Map.Entry<SynapsePath, Integer> entry : featureIndexMap.entrySet()) {

            SynapsePath synapsePath = entry.getKey();
            // Extract the feature value from the message
            String variableValue = synapsePath.stringValueOf(messageContext);
            if(variableValue != null) {
                // Get the mapping feature index of the ML-model
                int featureIndex = entry.getValue();
                data[featureIndex] = variableValue;
            }
            // TODO handle null/empty values
        }
        return predict(data);
    }

    /**
     * Predict the value using the feature values
     * @param data  feature values array
     * @return      predicted value as String
     * @throws      MLModelBuilderException
     */
    private String predict(String[] data) throws MLModelBuilderException {

        context.setDataToBePredicted(data);
        Predictor predictor = new Predictor(modelId, mlModel, context);
        List<?> predictions = predictor.predict();
        return predictions.get(0).toString();
    }
}
