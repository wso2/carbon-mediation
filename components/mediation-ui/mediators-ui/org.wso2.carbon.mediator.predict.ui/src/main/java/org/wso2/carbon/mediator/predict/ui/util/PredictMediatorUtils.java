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

package org.wso2.carbon.mediator.predict.ui.util;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.core.exceptions.MLAnalysisHandlerException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

public class PredictMediatorUtils {

    /**
     * Retrieve the ML-Model from the Registry
     * @param modelName
     * @return
     * @throws RegistryException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static MLModel retrieveModelFromRegistry(String modelName) throws RegistryException, IOException, ClassNotFoundException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Registry registry = carbonContext.getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        Resource resource = registry.get(MLCoreServiceValueHolder.getInstance().getModelStorage().getStorageDirectory() + "/" + modelName);
        byte[] readArray = (byte[]) resource.getContent();
        ByteArrayInputStream bis = new ByteArrayInputStream(readArray);
        ObjectInputStream objectInputStream = new ObjectInputStream(bis);
        MLModel model = (MLModel) objectInputStream.readObject();
        return model;
    }

    /**
     * Get the Features list of the model
     * @param modelName ML model name
     * @return
     */
    public static List<Feature> getFeaturesOfModel(String modelName) throws IOException,
            RegistryException, ClassNotFoundException {

        MLModel mlModel = retrieveModelFromRegistry(modelName);
        List<Feature> features = mlModel.getFeatures();
        return features;
    }

    /**
     * Get the response variable of the model
     * @param modelName model name
     * @return
     * @throws UserStoreException
     * @throws MLModelHandlerException
     * @throws MLAnalysisHandlerException
     */
    public static String getResponseVariable(String modelName) throws IOException,
            RegistryException, ClassNotFoundException {
        MLModel mlModel = retrieveModelFromRegistry(modelName);
        return mlModel.getResponseVariable();
    }
}
