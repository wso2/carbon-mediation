package org.wso2.carbon.mediator.machineLearner.ui.util;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.MLModelNew;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.impl.MLModelHandler;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

import java.util.List;

public class MLMediatorUtils {

    public static List<Feature> getFeaturesOfModel(String modelName) throws DatabaseHandlerException, MLModelHandlerException, MLModelBuilderException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = "admin"; //carbonContext.getUsername();
        MLModelHandler mlModelHandler = new MLModelHandler();
        MLModelNew mlModelNew = mlModelHandler.getModel(tenantId, userName, modelName);
        MLModel mlModel = mlModelHandler.retrieveModel(mlModelNew.getId());
        List<Feature> features = mlModel.getFeatures();
        return features;
    }

    public static List<String> getFeaturesOfModelFromDB(String modelName) throws DatabaseHandlerException {
        long modelId = 1;
        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        DatabaseService databaseService = valueHolder.getDatabaseService();
        return databaseService.getFeatureNames(modelId);
    }
}
