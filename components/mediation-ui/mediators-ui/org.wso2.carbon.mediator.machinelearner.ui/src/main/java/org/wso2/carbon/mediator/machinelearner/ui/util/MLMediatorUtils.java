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

package org.wso2.carbon.mediator.machinelearner.ui.util;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.MLModelNew;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.impl.MLModelHandler;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;

public class MLMediatorUtils {

    /**
     * Get the Features list of the model
     * @param modelName ML model name
     * @return
     * @throws DatabaseHandlerException
     * @throws MLModelHandlerException
     * @throws MLModelBuilderException
     * @throws UserStoreException
     */
    public static List<Feature> getFeaturesOfModel(String modelName) throws DatabaseHandlerException,
            MLModelHandlerException, MLModelBuilderException, UserStoreException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().
                getRealmConfiguration().getAdminUserName();
        MLModelHandler mlModelHandler = new MLModelHandler();
        MLModelNew mlModelNew = mlModelHandler.getModel(tenantId, userName, modelName);
        MLModel mlModel = mlModelHandler.retrieveModel(mlModelNew.getId());
        List<Feature> features = mlModel.getFeatures();
        return features;
    }
}
